package com.manywords.softworks.tafl.network.client;

import com.manywords.softworks.tafl.command.player.NetworkClientPlayer;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.ingame.GameChatPacket;
import com.manywords.softworks.tafl.network.packet.ingame.GameEndedPacket;
import com.manywords.softworks.tafl.network.packet.ingame.MovePacket;
import com.manywords.softworks.tafl.network.packet.ingame.VictoryPacket;
import com.manywords.softworks.tafl.network.packet.pregame.*;
import com.manywords.softworks.tafl.network.packet.utility.ErrorPacket;
import com.manywords.softworks.tafl.network.packet.utility.SuccessPacket;
import com.manywords.softworks.tafl.network.server.GameRole;
import com.manywords.softworks.tafl.rules.Rules;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

/**
 * Created by jay on 5/23/16.
 */
public class ClientServerConnection {
    public interface ClientServerCallback {
        public void onStateChanged(State newState);
        public void onChatMessageReceived(ChatType type, String sender, String message);
        public void onSuccessReceived(String message);
        public void onErrorReceived(String message);
        public void onGameListReceived(List<GameInformation> games);
        public void onDisconnect(boolean planned);
        public void onStartGame(Rules r);
        public void onServerMoveReceived(MoveRecord move);
        public void onClockUpdateReceived(TimeSpec attackerClock, TimeSpec defenderClock);
        public void onVictory(VictoryPacket.Victory victory);
    }

    public enum ChatType {
        LOBBY,
        GAME,
        SPECTATOR
    }

    public enum State {
        DISCONNECTED,
        LOGGED_IN,
        CREATING_GAME,
        JOINING_GAME,
        IN_PREGAME,
        IN_GAME,
        IN_POSTGAME,
    }

    private State mCurrentState = State.DISCONNECTED;

    private final String hostname;
    private final int port;

    private Socket mServer;
    private PrintWriter mServerWriter;
    private Thread mReadThread;

    private String mUsername;
    private UUID mServerGameUUID;
    private GameRole mGameRole = GameRole.OUT_OF_GAME;
    private NetworkClientPlayer mNetworkPlayer;
    private GameInformation mLastJoinedGame = null;

    private boolean mPlannedDisconnect = false;

    private ClientServerCallback mExternalCallback;
    private ClientServerCallback mInternalCallback = new InternalCallback();

    public ClientServerConnection(String hostname, int port, ClientServerCallback callback) {
        this.hostname = hostname;
        this.port = port;
        mExternalCallback = callback;
    }

    public void setCallback(ClientServerCallback callback) {
        mExternalCallback = callback;
    }

    public boolean connect(String username, String hashedPassword) {
        try {
            mPlannedDisconnect = false;
            mServer = new Socket(hostname, port);
            mServerWriter = new PrintWriter(new OutputStreamWriter(mServer.getOutputStream()), true);

            mReadThread = new ReadThread();
            mReadThread.start();

            mUsername = username;
            sendRegistrationMessage(username, hashedPassword);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void disconnect() {
        try {
            mPlannedDisconnect = true;
            if(mServer != null) mServer.close();

            mServer = null;
        } catch (IOException e) {
            // Best effort
        }
    }

    public NetworkClientPlayer getNetworkPlayer() {
        mNetworkPlayer = new NetworkClientPlayer(this);
        return mNetworkPlayer;
    }

    public State getCurrentState() {
        return mCurrentState;
    }

    public GameRole getGameRole() {
        return mGameRole;
    }

    public void sendCreateGameMessage(CreateGamePacket packet) {
        setState(State.CREATING_GAME);
        mLastJoinedGame = packet.toGameInformation();
        mServerGameUUID = packet.uuid;
        mServerWriter.println(packet);
    }

    public void sendLeaveGameMessage() {
        if(mServerGameUUID != null) {
            mServerWriter.println(new LeaveGamePacket(mServerGameUUID));
        }
        if(mCurrentState != State.DISCONNECTED && mCurrentState != State.LOGGED_IN) {
            setState(State.LOGGED_IN);
        }
    }

    public String getUsername() {
        return mUsername;
    }

    public GameInformation getLastJoinedGameInfo() {
        return mLastJoinedGame;
    }

    public TimeSpec getLastClockSetting() {
        if(mLastJoinedGame != null) {
            return mLastJoinedGame.clockSetting;
        }
        else return null;
    }

    public void sendRegistrationMessage(String username, String hashedPassword) {
        mServerWriter.println(new LoginPacket(username, hashedPassword));
    }

    public void sendChatMessage(ChatType type, String sender, String message) {
        if(type == ChatType.LOBBY) mServerWriter.println(new LobbyChatPacket(sender, message));
        else if(type == ChatType.GAME) mServerWriter.println(new GameChatPacket(sender, message));
        else if(type == ChatType.SPECTATOR) mServerWriter.println(new GameChatPacket(sender, message));
    }

    public void sendJoinGameMessage(GameInformation gameInfo, JoinGamePacket packet) {
        if(mCurrentState == State.LOGGED_IN) {
            mLastJoinedGame = gameInfo;
            setState(State.JOINING_GAME);
            mServerGameUUID = packet.uuid;
            mServerWriter.println(packet);
        }
        else {
            mInternalCallback.onErrorReceived(ErrorPacket.ALREADY_HOSTING);
        }
    }

    public void sendMoveDecidedMessage(MoveRecord move) {
        mServerWriter.println(new MovePacket(move));
    }

    public void sendGameEndedMessage() {
        mServerWriter.println(new GameEndedPacket());
    }

    public void requestGameUpdate() {
        mServerWriter.println("game-list");
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            System.out.println("Connecting to server: " + mServer.getInetAddress());
            String inputData = "";
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(mServer.getInputStream()));
            ) {
                while((inputData = in.readLine()) != null) {
                    try {
                        System.out.println("Client received: " + inputData);
                        ClientCommandParser.handlePacket(mInternalCallback, inputData);
                    }
                    catch(Exception e) {
                        System.out.println("Encountered exception reading from server: ");
                        e.printStackTrace(System.out);
                    }
                }
            }
            catch(IOException e) {
                System.out.println("Server connection error: " + e);
            }

            System.out.println("Disconnected from server");
            mExternalCallback.onDisconnect(mPlannedDisconnect);
        }
    }

    private void setState(State newState) {
        System.out.println("State change: " + mCurrentState + " to " + newState);
        mCurrentState = newState;
        mInternalCallback.onStateChanged(newState);
    }

    private class InternalCallback implements ClientServerCallback {

        @Override
        public void onStateChanged(State newState) {
            switch(newState) {
                case LOGGED_IN:
                    mGameRole = GameRole.OUT_OF_GAME;
                case IN_PREGAME:
                    requestGameUpdate();
                    break;
            }
            mExternalCallback.onStateChanged(newState);
        }

        @Override
        public void onChatMessageReceived(ChatType type, String sender, String message) {
            mExternalCallback.onChatMessageReceived(type, sender, message);
        }

        @Override
        public void onSuccessReceived(String message) {
            switch(mCurrentState) {

                case DISCONNECTED:
                    setState(State.LOGGED_IN);
                    break;
                case LOGGED_IN:
                    break;
                case JOINING_GAME:
                case CREATING_GAME:
                case IN_PREGAME:
                case IN_GAME:
                    // If we joined as the attackers, the other player is the defender.
                    if(message.equals(SuccessPacket.JOINED_ATTACKERS)) {
                        mGameRole = GameRole.ATTACKER;
                    }
                    else if(message.equals(SuccessPacket.JOINED_DEFENDERS)) {
                        mGameRole = GameRole.DEFENDER;
                    }
                    setState(State.IN_PREGAME);
                    break;
            }
        }

        @Override
        public void onErrorReceived(String message) {
            if(message.equals(ErrorPacket.LEAVE_GAME)) {
                requestGameUpdate();
            }

            switch(mCurrentState) {

                case DISCONNECTED:
                    mExternalCallback.onErrorReceived(message);
                    break;
                case LOGGED_IN:
                    break;
                case JOINING_GAME:
                case CREATING_GAME:
                    setState(State.LOGGED_IN);
                    mExternalCallback.onErrorReceived(message);
                    break;
                case IN_PREGAME:
                    break;
            }
        }

        @Override
        public void onGameListReceived(List<GameInformation> games) {
            mExternalCallback.onGameListReceived(games);
        }

        @Override
        public void onDisconnect(boolean planned) {

        }

        @Override
        public void onStartGame(Rules r) {
            setState(State.IN_GAME);
            mExternalCallback.onStartGame(r);
        }

        @Override
        public void onServerMoveReceived(MoveRecord move) {
            mNetworkPlayer.onMoveDecided(move);
        }

        @Override
        public void onClockUpdateReceived(TimeSpec attackerClock, TimeSpec defenderClock) {
            mExternalCallback.onClockUpdateReceived(attackerClock, defenderClock);
        }

        @Override
        public void onVictory(VictoryPacket.Victory victory) {
            mExternalCallback.onVictory(victory);
        }
    }
}
