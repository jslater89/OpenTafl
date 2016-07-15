package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.command.CommandEngine;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;
import com.manywords.softworks.tafl.command.player.NetworkServerPlayer;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.network.packet.ingame.ClockUpdatePacket;
import com.manywords.softworks.tafl.network.packet.ingame.GameEndedPacket;
import com.manywords.softworks.tafl.network.packet.ingame.HistoryPacket;
import com.manywords.softworks.tafl.network.packet.ingame.VictoryPacket;
import com.manywords.softworks.tafl.network.packet.pregame.StartGamePacket;
import com.manywords.softworks.tafl.network.packet.utility.ErrorPacket;
import com.manywords.softworks.tafl.network.server.task.SendPacketTask;
import com.manywords.softworks.tafl.network.server.task.StartGameTask;
import com.manywords.softworks.tafl.network.server.task.interval.IntervalTask;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.UiCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ServerGame wraps a Game object and its associated things, and synchronizes access to them.
 */
public class ServerGame {
    public static ServerGame getDummyGame(NetworkServer server, String attackerName, String defenderName, Game game) {
        ServerGame g = new ServerGame();
        g.mGame = game;
        g.mAttackerClient = DummyServerClient.get(server, attackerName);
        g.mDefenderClient = DummyServerClient.get(server, defenderName);

        return g;
    }

    public final UUID uuid;

    private NetworkServer mServer;

    private Game mGame;
    private CommandEngine mCommandEngine;
    private ServerUiCallback mUiCallback = new ServerUiCallback();
    private TimeSpec mClockSetting;
    private ClockUpdateTask mClockUpdateTask = new ClockUpdateTask();
    private List<MoveRecord> mPregameHistory = null;
    private boolean mPregameHistoryLoaded = false;

    private ServerClient mAttackerClient;
    private NetworkServerPlayer mAttackerPlayer;
    private ServerClient mDefenderClient;
    private NetworkServerPlayer mDefenderPlayer;
    private final List<ServerClient> mSpectators = new ArrayList<>();

    private boolean mHasStarted;
    private boolean mChatCombined = true;
    private boolean mReplayAllowed = true;

    private String mBase64HashedPassword = "";

    public ServerGame() {
        uuid = UUID.randomUUID();
    }

    public ServerGame(NetworkServer server, UUID uuid) {
        this.uuid = uuid;
        mServer = server;
    }

    public void setChatCombined(boolean combined) {
        mChatCombined = combined;
    }

    public void setReplayAllowed(boolean allowed) {
        mReplayAllowed = allowed;
    }

    public boolean isChatCombined() {
        return mChatCombined;
    }

    public boolean isReplayAllowed() {
        return mReplayAllowed;
    }

    public synchronized void setClock(TimeSpec clockSetting) {
        mClockSetting = clockSetting;

        // The game clock will be updated by our interval task.
        if(mGame != null) {
            mGame.setClock(new GameClock(mGame, clockSetting));
            mGame.getClock().setServerMode(true);
        }
    }

    public synchronized void setRules(Rules r) {
        mGame = new Game(r, mUiCallback);

        if(mClockSetting != null) {
            mGame.setClock(new GameClock(mGame, mClockSetting));
            mGame.getClock().setServerMode(true);
        }

        mAttackerPlayer = new NetworkServerPlayer(mServer);
        mDefenderPlayer = new NetworkServerPlayer(mServer);
        mCommandEngine = new CommandEngine(mGame, mUiCallback, mAttackerPlayer, mDefenderPlayer);

        if(mPregameHistory != null && !mPregameHistoryLoaded) {
            loadGame(mPregameHistory);
        }
    }

    public synchronized void loadGame(List<MoveRecord> moves) {
        mPregameHistory = moves;
        if(mGame == null) {
            mPregameHistoryLoaded = false;
        }
        else {
            boolean good = true;
            for(MoveRecord m : moves) {
                int result = mGame.getCurrentState().makeMove(m);

                if(result != GameState.GOOD_MOVE) {
                    good = false;
                    break;
                }
            }
            if(good) {
                mPregameHistoryLoaded = true;
            }
            else {
                mServer.sendPacketToClients(getAllClients(), new ErrorPacket(ErrorPacket.BAD_SAVE), PriorityTaskQueue.Priority.HIGH);
                for(ServerClient client : getAllClients()) {
                    removeClient(client);
                }
            }
        }
    }

    public void startGame() {
        mHasStarted = true;
        mCommandEngine.startGame();
    }

    public boolean isGameInProgress() {
        return mCommandEngine.isInGame();
    }

    public boolean hasGameStarted() {
        return mHasStarted;
    }

    public boolean hasLoadedGame() { return mPregameHistoryLoaded; }

    public Rules getRules() {
        if(mGame != null) return mGame.getRules();
        else return null;
    }

    public void setPassword(String password) {
        mBase64HashedPassword = PasswordHasher.hashPassword("", password);
    }

    public boolean tryPassword(String password) {
        if(!isPassworded()) return true;

        String hashedPassword = PasswordHasher.hashPassword("", password);
        if(mBase64HashedPassword.equals(hashedPassword)) return true;
        else return false;
    }

    public synchronized List<ServerClient> getAllClients() {
        List<ServerClient> clients = new ArrayList<>();
        if(mAttackerClient != null) clients.add(mAttackerClient);
        if(mDefenderClient != null) clients.add(mDefenderClient);
        clients.addAll(mSpectators);

        return clients;
    }

    public synchronized boolean tryJoinGame(ServerClient c, String password) {
        return !mHasStarted && tryJoinGame(c, password, true, true);
    }

    public synchronized boolean tryJoinGame(ServerClient c, String password, boolean attackers, boolean defenders) {
        boolean retval;
        if(!tryPassword(password)) {
            retval = false;
        }
        else if(attackers && mAttackerClient == null) {
            setAttackerClient(c);
            retval = true;
        }
        else if(defenders && mDefenderClient == null) {
            setDefenderClient(c);
            retval = true;
        }
        else {
            retval = false;
        }

        if(mAttackerClient != null && mDefenderClient != null) {
            mServer.getTaskQueue().pushTask(new StartGameTask(mServer, this, getAllClients(), new StartGamePacket(mGame.getRules())));
            if(mPregameHistoryLoaded) {
                mServer.sendPacketToClients(getAllClients(), new HistoryPacket(mPregameHistory, getRules().boardSize), PriorityTaskQueue.Priority.STANDARD);
            }
        }

        return retval;
    }

    public synchronized boolean trySpectateGame(ServerClient c, String password) {
        boolean retval;

        if(!tryPassword(password)) {
            retval = false;
        }
        else {
            addSpectator(c);
            retval = true;
        }

        return retval;
    }

    private synchronized void setAttackerClient(ServerClient attackerClient) {
        if(attackerClient == null) {
            mAttackerClient.setGame(null, GameRole.OUT_OF_GAME);
        }
        else {
            attackerClient.setGame(this, GameRole.ATTACKER);
            mAttackerPlayer.setClient(attackerClient);
        }

        mAttackerClient = attackerClient;
    }

    private synchronized void setDefenderClient(ServerClient defenderClient) {
        if(defenderClient == null) {
            mDefenderClient.setGame(null, GameRole.OUT_OF_GAME);
        }
        else {
            defenderClient.setGame(this, GameRole.DEFENDER);
            mDefenderPlayer.setClient(defenderClient);
        }

        mDefenderClient = defenderClient;
    }

    public void addSpectator(ServerClient client) {
        synchronized (mSpectators) {
            mSpectators.add(client);
        }
        client.setGame(this, GameRole.KIBBITZER);
    }

    public void removeSpectator(ServerClient client) {
        synchronized (mSpectators) {
            mSpectators.remove(client);
        }
        client.setGame(null, GameRole.OUT_OF_GAME);
    }

    public void removeClient(ServerClient client) {
        if(client.equals(mAttackerClient)) {
            setAttackerClient(null);
            if(mDefenderClient != null) {
                mServer.sendPacketToClient(mDefenderClient, new VictoryPacket(VictoryPacket.Victory.DEFENDER), PriorityTaskQueue.Priority.LOW);
                mServer.sendPacketToClient(mDefenderClient, new GameEndedPacket(), PriorityTaskQueue.Priority.LOW);
                mServer.sendPacketToClient(mDefenderClient, new ErrorPacket(ErrorPacket.OPPONENT_LEFT), PriorityTaskQueue.Priority.LOW);
            }

            mCommandEngine.networkVictory(VictoryPacket.Victory.DEFENDER);
        }
        else if(client.equals(mDefenderClient)) {
            setDefenderClient(null);

            if(mAttackerClient != null) {
                mServer.sendPacketToClient(mAttackerClient, new VictoryPacket(VictoryPacket.Victory.ATTACKER), PriorityTaskQueue.Priority.LOW);
                mServer.sendPacketToClient(mAttackerClient, new GameEndedPacket(), PriorityTaskQueue.Priority.LOW);
                mServer.sendPacketToClient(mAttackerClient, new ErrorPacket(ErrorPacket.OPPONENT_LEFT), PriorityTaskQueue.Priority.LOW);
            }

            mCommandEngine.networkVictory(VictoryPacket.Victory.ATTACKER);
        }
        else {
            removeSpectator(client);
        }

        if(mAttackerClient == null && mDefenderClient == null) {
            mServer.removeGame(this);
        }
    }

    public void shutdown() {
        if(mAttackerClient != null) removeClient(mAttackerClient);
        if(mDefenderClient != null) removeClient(mDefenderClient);

        for(ServerClient c : mSpectators) {
            c.setGame(null, GameRole.OUT_OF_GAME);
        }
    }

    public NetworkServerPlayer getPlayerForClient(ServerClient c) {
        if(c == mAttackerClient) return mAttackerPlayer;
        else if (c == mDefenderClient) return mDefenderPlayer;
        else return null;
    }

    public ServerClient getAttackerClient() {
        return mAttackerClient;
    }

    public NetworkServerPlayer getAttackerPlayer() { return mAttackerPlayer; }

    public ServerClient getDefenderClient() {
        return mDefenderClient;
    }

    public NetworkServerPlayer getDefenderPlayer() { return mDefenderPlayer; }

    public List<ServerClient> getSpectators() {
        return new ArrayList<>(mSpectators);
    }

    public boolean isPassworded() {
        return !mBase64HashedPassword.isEmpty();
    }

    public Game getGame() {
        return mGame;
    }

    public IntervalTask getClockUpdateTask() {
        return mClockUpdateTask;
    }

    private class ClockUpdateTask extends IntervalTask {
        @Override
        public void reset() {

        }

        @Override
        public void run() {
            if(mGame != null && mGame.getClock() != null) {
                mGame.getClock().updateClocks();
                mGame.getClock().updateClients();

                TimeSpec attacker = mGame.getClock().getClockEntry(true).toTimeSpec();
                TimeSpec defender = mGame.getClock().getClockEntry(false).toTimeSpec();

                ClockUpdatePacket packet = new ClockUpdatePacket(attacker, defender);

                // They can be null after the end of a game.
                if(mCommandEngine.isInGame()) {
                    if (mAttackerClient != null) mServer.sendPacketToClient(mAttackerClient, packet, PriorityTaskQueue.Priority.HIGH);
                    if (mDefenderClient != null) mServer.sendPacketToClient(mDefenderClient, packet, PriorityTaskQueue.Priority.HIGH);
                }

                mServer.sendPacketToClients(getSpectators(), packet, PriorityTaskQueue.Priority.LOW);
            }
        }
    }

    private class ServerUiCallback implements UiCallback {

        @Override
        public void gameStarting() {

        }

        @Override
        public void modeChanging(Mode mode, Object gameObject) {

        }

        @Override
        public void awaitingMove(Player player, boolean isAttackingSide) {

        }

        @Override
        public void timeUpdate(boolean currentSideAttackers) {

        }

        @Override
        public void moveResult(CommandResult result, MoveRecord move) {

        }

        @Override
        public void statusText(String text) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Command engine status: " + text);
        }

        @Override
        public void modalStatus(String title, String text) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Command engine status: " + title + " - " + text);
        }

        @Override
        public void gameStateAdvanced() {

        }

        @Override
        public void victoryForSide(Side side) {
            mServer.sendPacketToClients(getAllClients(), new VictoryPacket(side), PriorityTaskQueue.Priority.HIGH);
        }

        @Override
        public void gameFinished() {
            mServer.sendPacketToClients(getAllClients(), new GameEndedPacket(), PriorityTaskQueue.Priority.HIGH);
        }

        @Override
        public MoveRecord waitForHumanMoveInput() {
            return null;
        }

        @Override
        public boolean inGame() {
            return false;
        }
    }
}
