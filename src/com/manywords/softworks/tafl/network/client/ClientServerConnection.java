package com.manywords.softworks.tafl.network.client;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Created by jay on 5/23/16.
 */
public class ClientServerConnection {
    public interface ClientServerCallback {
        public void onChatMessageReceived(String sender, String message);
        public void onSuccessReceived();
        public void onErrorReceived(String message);
        public void onGameListReceived(List<ClientGameInformation> games);
    }

    public enum State {
        DISCONNECTED,
        LOGGED_IN,
    }

    private State mCurrentState = State.DISCONNECTED;

    private final String hostname;
    private final int port;

    private Socket mServer;
    private PrintWriter mServerWriter;
    private Thread mReadThread;

    private ClientServerCallback mExternalCallback;
    private ClientServerCallback mInternalCallback = new InternalCallback();

    public ClientServerConnection(String hostname, int port, ClientServerCallback callback) {
        this.hostname = hostname;
        this.port = port;
        mExternalCallback = callback;
    }

    public boolean connect(String username, String salt, String hashedPassword) {
        try {
            mServer = new Socket(hostname, port);
            mServerWriter = new PrintWriter(new OutputStreamWriter(mServer.getOutputStream()), true);

            mReadThread = new ReadThread();
            mReadThread.start();

            sendRegistrationMessage(username, salt, hashedPassword);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void disconnect() {
        try {
            if(mServer != null) mServer.close();

            mServer = null;
        } catch (IOException e) {
            // Best effort
        }
    }

    public void sendRegistrationMessage(String username, String salt, String hashedPassword) {
        mServerWriter.println("login \"" + username + "\" " + salt + " " + hashedPassword);
    }

    public void sendChatMessage(String sender, String message) {
        mServerWriter.println("lobby-chat \"" + sender + "\" " + message);
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
        }
    }

    private class InternalCallback implements ClientServerCallback {

        @Override
        public void onChatMessageReceived(String sender, String message) {
            mExternalCallback.onChatMessageReceived(sender, message);
        }

        @Override
        public void onSuccessReceived() {
            if(mCurrentState == State.DISCONNECTED) mCurrentState = State.LOGGED_IN;
        }

        @Override
        public void onErrorReceived(String message) {
            if(mCurrentState == State.DISCONNECTED) mExternalCallback.onErrorReceived(message);
        }

        @Override
        public void onGameListReceived(List<ClientGameInformation> games) {
            mExternalCallback.onGameListReceived(games);
        }
    }
}
