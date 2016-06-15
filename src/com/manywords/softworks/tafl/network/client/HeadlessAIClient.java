package com.manywords.softworks.tafl.network.client;

import com.manywords.softworks.tafl.command.CommandEngine;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.ExternalEnginePlayer;
import com.manywords.softworks.tafl.command.player.Player;
import com.manywords.softworks.tafl.command.player.external.engine.EngineSpec;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.network.packet.ClientInformation;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.ingame.VictoryPacket;
import com.manywords.softworks.tafl.network.packet.pregame.CreateGamePacket;
import com.manywords.softworks.tafl.network.packet.pregame.JoinGamePacket;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.UiCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jay on 6/14/16.
 */
public class HeadlessAIClient {
    private EngineSpec mEngineConfig;
    private ExternalEnginePlayer mPlayer;

    private ClientServerConnection mConnection;
    private String mLastServerError;

    private Game mGame;
    private CommandEngine mCommandEngine;
    private UiCallback mUiCallback = new HeadlessUICallback();
    private ClientServerConnection.ClientServerCallback mServerCallback = new HeadlessServerCallback();

    private boolean mAttackingSide;

    // create game mode:
        // Create game mode: automatically create games, leave immediately when games end and create new game.
        // Leave server when terminated.
    // join game mode:
        // Join a game against the named player, if that player/game combo is present on the server. If not,
        // leave server.
    private boolean mCreateGame;
    private boolean mJoinGame;

    // Create game mode variables
    private Rules mRules;
    private TimeSpec mClockSetting;
    private String mGamePassword;

    // Join game mode variables
    private String mOpponentUsername;

    public static HeadlessAIClient startFromArgs(Map<String, String> args) {
        return startFromArgs(args, true);
    }

    public static HeadlessAIClient startFromArgs(Map<String, String> args, boolean chatty) {
        boolean create = false;
        boolean join = false;
        Rules r = null;
        String server = "";
        File engineFile = null;
        String username = "";
        String password = "";
        boolean attackers = false;
        TimeSpec ts = null;
        String gamePassword = "";
        String opponentUsername = "";

        for(Map.Entry<String, String> entry : args.entrySet()) {
            if(entry.getKey().contains("--create")) create = true;
            if(entry.getKey().contains("--join")) join = true;

            if(entry.getKey().contains("--server")) server = entry.getValue();
            if(entry.getKey().contains("--engine")) engineFile = new File(entry.getValue());
            if(entry.getKey().contains("--side")) attackers = entry.getValue().contains("attackers");
            if(entry.getKey().contains("--username")) username = entry.getValue().trim();
            if(entry.getKey().contains("--password")) password = entry.getValue().trim();

            if(entry.getKey().contains("--rules")) r = BuiltInVariants.availableRules.get(Integer.parseInt(entry.getValue()) - 1);
            if(entry.getKey().contains("--clock")) ts = TimeSpec.parseMachineReadableString(entry.getValue(), "\\+");
            if(entry.getKey().contains("--game-password")) gamePassword = entry.getValue().trim();

            if(entry.getKey().contains("--opponent")) opponentUsername = entry.getValue().trim();
        }

        if((create && join) || (!create && !join)) {
            throw new IllegalArgumentException("Use one of --join or --create in headless mode");
        }

        if(server.isEmpty()) throw new IllegalArgumentException("Missing server address");
        if(engineFile == null || !engineFile.exists()) throw new IllegalArgumentException("Server engine file does not exist");
        if(username.isEmpty()) throw new IllegalArgumentException("Missing username");
        if(password.isEmpty()) throw new IllegalArgumentException("Missing password");
        HeadlessAIClient c = new HeadlessAIClient(server, 11541, engineFile, attackers, username, password, chatty);

        if(create) {
            if(r == null) throw new IllegalArgumentException("No rules given");
            if(ts == null) throw new IllegalArgumentException("No clock specification given");

            c.setCreateGameMode(r, ts, gamePassword);
        }
        else {
            if(opponentUsername == null) throw new IllegalArgumentException("No opponent username given");

            c.setJoinGameMode(opponentUsername, gamePassword);
        }
        return c;
    }

    public HeadlessAIClient(String serverAddress, int port, File engineSpecFile, boolean attackers, String username, String password) {
        this(serverAddress, port, engineSpecFile, attackers, username, password, true);
    }

    public HeadlessAIClient(String serverAddress, int port, File engineSpecFile, boolean attackers, String username, String password, boolean chatty) {
        if(!EngineSpec.validateEngineFile(engineSpecFile)) {
            throw new IllegalArgumentException("Bad engine spec file!");
        }

        mAttackingSide = attackers;
        mEngineConfig = new EngineSpec(engineSpecFile);

        mConnection = new ClientServerConnection(serverAddress, port, mServerCallback);
        mConnection.connect(username, PasswordHasher.hashPassword(username, password));

        int retryAttempts = 0;
        while(mConnection.getCurrentState() != ClientServerConnection.State.LOGGED_IN && retryAttempts < 5) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) { }
            mConnection.println("Connecting...");
            retryAttempts++;
        }

        if(mConnection.getCurrentState() != ClientServerConnection.State.LOGGED_IN) {
            throw new IllegalStateException("Failed to connect! Error message: " + mLastServerError);
        }

        // Logged in.
        mConnection.println("Connected.");
    }

    public ClientServerConnection getConnection() {
        return mConnection;
    }

    private void setCreateGameMode(Rules rules, TimeSpec ts, String gamePassword) {
        mCreateGame = true;

        mRules = rules;
        mClockSetting = ts;
        if(!gamePassword.isEmpty()) {
            mGamePassword = PasswordHasher.hashPassword("", gamePassword);
        }
        else {
            mGamePassword = PasswordHasher.NO_PASSWORD;
        }

        createServerGame();
    }

    private void setJoinGameMode(String opponentUsername, String gamePassword) {
        mJoinGame = true;

        mOpponentUsername = opponentUsername;
        mGamePassword = gamePassword;

        mConnection.requestGameUpdate();
    }

    private void createServerGame() {
        if(mConnection.getCurrentState() == ClientServerConnection.State.LOGGED_IN) {
            String password = (mGamePassword.equals(PasswordHasher.NO_PASSWORD) ? mGamePassword : PasswordHasher.hashPassword("", mGamePassword));
            CreateGamePacket packet = new CreateGamePacket(UUID.randomUUID(), mAttackingSide, password, mRules.getOTRString(), mClockSetting);
            mConnection.sendCreateGameMessage(packet);
        }
    }

    private void createGameLocally(Rules r, List<MoveRecord> history) {
        mGame = new Game(r, mUiCallback);
        GameClock clock = new GameClock(mGame, mClockSetting);
        mGame.setClock(clock);

        if(mAttackingSide) {
            mCommandEngine = new CommandEngine(mGame, mUiCallback, createExternalEnginePlayer(), mConnection.getNetworkPlayer());
        }
        else {
            mCommandEngine = new CommandEngine(mGame, mUiCallback, mConnection.getNetworkPlayer(), createExternalEnginePlayer());
        }

        mCommandEngine.startGame();
    }

    private Player createExternalEnginePlayer() {
        mPlayer = new ExternalEnginePlayer();
        mPlayer.setEngineSpec(mEngineConfig);
        return mPlayer;
    }

    private void leaveGame() {
        if(mJoinGame) {
            if(mGame != null) {
                String date = new SimpleDateFormat("yyyy.MM.dd.HH.mm").format(new Date());
                File saveFile = new File("saved-games/headless-ai", "ai-game." + date + ".otg");
                GameSerializer.writeGameToFile(mGame, saveFile, true);
            }

            mConnection.disconnect();
            System.exit(0);
        }
        else if(mCreateGame) {
            if (mCommandEngine == null) return;

            mCommandEngine.finishGameQuietly();

            String date = new SimpleDateFormat("yyyy.MM.dd.HH.mm").format(new Date());
            File saveFile = new File("saved-games/headless-ai", "ai-game." + date + ".otg");
            GameSerializer.writeGameToFile(mGame, saveFile, true);

            mGame = null;
            mCommandEngine = null;

            mConnection.sendLeaveGameMessage();
        }
    }

    private class HeadlessServerCallback implements ClientServerConnection.ClientServerCallback {

        @Override
        public void onStateChanged(ClientServerConnection.State newState) {
            if(newState == ClientServerConnection.State.LOGGED_IN) {
                if(mCreateGame) {
                    createServerGame();
                }
            }
        }

        @Override
        public void onChatMessageReceived(ClientServerConnection.ChatType type, String sender, String message) {

        }

        @Override
        public void onSuccessReceived(String message) {

        }

        @Override
        public void onErrorReceived(String message) {
            mLastServerError = message;
            System.out.println("Received error: " + message);

            if(mJoinGame && mCommandEngine == null) {
                System.out.println("Error joining game: quitting");
            }
        }

        @Override
        public void onGameListReceived(List<GameInformation> games) {
            if(mJoinGame && mCommandEngine == null) {
                for(GameInformation game : games) {
                    System.out.println(mOpponentUsername + "-" + game.attackerUsername);
                    if(mOpponentUsername.equals(game.attackerUsername) || mOpponentUsername.equals(game.defenderUsername)) {
                        mAttackingSide = game.freeSideAttackers();
                        mClockSetting = game.clockSetting;

                        if(mClockSetting == null) {
                            System.out.println("Headless client can only play timed games.");
                            mConnection.disconnect();
                            System.exit(0);
                        }

                        mConnection.sendJoinGameMessage(game, new JoinGamePacket(UUID.fromString(game.uuid), false, PasswordHasher.hashPassword("", mGamePassword)));
                    }
                }
            }
        }

        @Override
        public void onClientListReceived(List<ClientInformation> clients) {

        }

        @Override
        public void onDisconnect(boolean planned) {

        }

        @Override
        public Game getGame() {
            return mGame;
        }

        @Override
        public void onStartGame(Rules r, List<MoveRecord> history) {
            createGameLocally(r, history);
        }

        @Override
        public void onHistoryReceived(List<MoveRecord> moves) {

        }

        @Override
        public void onServerMoveReceived(MoveRecord move) {

        }

        @Override
        public void onClockUpdateReceived(TimeSpec attackerClock, TimeSpec defenderClock) {
            mGame.getClock().handleNetworkTimeUpdate(attackerClock, defenderClock);
        }

        @Override
        public void onVictory(VictoryPacket.Victory victory) {
            leaveGame();
        }
    }

    private class HeadlessUICallback implements UiCallback {

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

        }

        @Override
        public void modalStatus(String title, String text) {

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
            return mCommandEngine.isInGame();
        }
    }
}
