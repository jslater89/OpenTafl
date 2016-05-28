package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.command.CommandEngine;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;
import com.manywords.softworks.tafl.command.player.NetworkServerPlayer;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.network.packet.pregame.StartGamePacket;
import com.manywords.softworks.tafl.network.server.task.StartGameTask;
import com.manywords.softworks.tafl.network.server.task.interval.IntervalTask;
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
    private ClockUpdateTask mClockUpdateTask = new ClockUpdateTask();

    private ServerClient mAttackerClient;
    private NetworkServerPlayer mAttackerPlayer;
    private ServerClient mDefenderClient;
    private NetworkServerPlayer mDefenderPlayer;
    private final List<ServerClient> mSpectators = new ArrayList<>();

    private String mBase64HashedPassword = "";

    public ServerGame() {
        uuid = UUID.randomUUID();
    }

    public ServerGame(NetworkServer server, UUID uuid) {
        this.uuid = uuid;
        mServer = server;
    }

    public void setRules(Rules r) {
        mGame = new Game(r, mUiCallback);

        // The game clock will be updated by our interval task.
        if(mGame.getClock() != null) {
            mGame.getClock().setServerMode(true);
        }

        mAttackerPlayer = new NetworkServerPlayer(mServer);
        mDefenderPlayer = new NetworkServerPlayer(mServer);
        mCommandEngine = new CommandEngine(mGame, mUiCallback, mAttackerPlayer, mDefenderPlayer);
    }

    public void startGame() {
        mCommandEngine.startGame();
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
        return tryJoinGame(c, password, true, true);
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
        }
        else if(client.equals(mDefenderClient)) {
            setDefenderClient(null);
        }
        else {
            removeSpectator(client);
        }

        if(mAttackerClient == null && mDefenderClient == null) {
            mServer.removeGame(this);
        }
    }

    public void shutdown() {
        if(mAttackerClient != null) mAttackerClient.setGame(null, GameRole.OUT_OF_GAME);
        if(mDefenderClient != null) mDefenderClient.setGame(null, GameRole.OUT_OF_GAME);

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
        return mSpectators;
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
        public void timeUpdate(Side side) {

        }

        @Override
        public void moveResult(CommandResult result, MoveRecord move) {

        }

        @Override
        public void statusText(String text) {
            System.out.println("Command engine status: " + text);
        }

        @Override
        public void modalStatus(String title, String text) {
            System.out.println("Command engine status: " + title + " - " + text);
        }

        @Override
        public void gameStateAdvanced() {

        }

        @Override
        public void victoryForSide(Side side) {

        }

        @Override
        public void gameFinished() {

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
