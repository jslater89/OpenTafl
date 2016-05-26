package com.manywords.softworks.tafl.network.client;

import com.manywords.softworks.tafl.network.packet.*;

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
        public void onChatMessageReceived(String sender, String message);
        public void onSuccessReceived();
        public void onErrorReceived(String message);
        public void onGameListReceived(List<ClientGameInformation> games);
        public void onDisconnect(boolean planned);
    }

    public enum State {
        DISCONNECTED,
        LOGGED_IN,
        CREATING_GAME,
        HOSTING,
    }

    private State mCurrentState = State.DISCONNECTED;

    private final String hostname;
    private final int port;

    private Socket mServer;
    private PrintWriter mServerWriter;
    private Thread mReadThread;

    private boolean mPlannedDisconnect = false;

    private ClientServerCallback mExternalCallback;
    private ClientServerCallback mInternalCallback = new InternalCallback();

    public ClientServerConnection(String hostname, int port, ClientServerCallback callback) {
        this.hostname = hostname;
        this.port = port;
        mExternalCallback = callback;
    }

    public boolean connect(String username, String hashedPassword) {
        try {
            mPlannedDisconnect = false;
            mServer = new Socket(hostname, port);
            mServerWriter = new PrintWriter(new OutputStreamWriter(mServer.getOutputStream()), true);

            mReadThread = new ReadThread();
            mReadThread.start();

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

    public void sendCreateGameMessage(CreateGamePacket packet) {
        setState(State.CREATING_GAME);
        mServerWriter.println(packet);
    }

    public void sendCancelGameMessage(UUID uuid) {
        mServerWriter.println(new CancelGamePacket(uuid));
        setState(State.LOGGED_IN);
    }

    public void sendRegistrationMessage(String username, String hashedPassword) {
        mServerWriter.println(new LoginPacket(username, hashedPassword));
    }

    public void sendChatMessage(String sender, String message) {
        mServerWriter.println(new LobbyChatPacket(sender, message));
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
                    System.out.println("Client received: " + inputData);
                    ClientCommandParser.handlePacket(mInternalCallback, inputData);
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
        mCurrentState = newState;
        mInternalCallback.onStateChanged(newState);
    }

    private class InternalCallback implements ClientServerCallback {

        @Override
        public void onStateChanged(State newState) {
            switch(newState) {
                case LOGGED_IN:
                case HOSTING:
                    requestGameUpdate();
                    break;
            }
            mExternalCallback.onStateChanged(newState);
        }

        @Override
        public void onChatMessageReceived(String sender, String message) {
            mExternalCallback.onChatMessageReceived(sender, message);
        }

        @Override
        public void onSuccessReceived() {
            switch(mCurrentState) {

                case DISCONNECTED:
                    setState(State.LOGGED_IN);
                    break;
                case LOGGED_IN:
                    break;
                case CREATING_GAME:
                    setState(State.HOSTING);
                    break;
                case HOSTING:
                    break;
            }
        }

        @Override
        public void onErrorReceived(String message) {
            /*if(message.equals(ErrorPacket.GAME_CANCELED)) {
                requestGameUpdate();
            }*/

            switch(mCurrentState) {

                case DISCONNECTED:
                    mExternalCallback.onErrorReceived(message);
                    break;
                case LOGGED_IN:
                    break;
                case CREATING_GAME:
                    setState(State.LOGGED_IN);
                    mExternalCallback.onErrorReceived(message);
                    break;
                case HOSTING:
                    break;
            }
        }

        @Override
        public void onGameListReceived(List<ClientGameInformation> games) {
            mExternalCallback.onGameListReceived(games);
        }

        @Override
        public void onDisconnect(boolean planned) {

        }
    }
}
