package com.manywords.softworks.tafl.network.client;

import com.manywords.softworks.tafl.network.server.packet.LobbyChatPacket;

import java.io.*;
import java.net.Socket;

/**
 * Created by jay on 5/23/16.
 */
public class ClientServerConnection {
    public interface ClientServerCallback {
        public void onChatMessageReceived(String sender, String message);
    }

    private final String hostname;
    private final int port;

    private Socket mServer;
    private PrintWriter mServerWriter;
    private Thread mReadThread;

    private ClientServerCallback mCallback;

    public ClientServerConnection(String hostname, int port, ClientServerCallback callback) {
        this.hostname = hostname;
        this.port = port;
        mCallback = callback;
    }

    public boolean connect() {
        try {
            mServer = new Socket(hostname, port);
            mServerWriter = new PrintWriter(new OutputStreamWriter(mServer.getOutputStream()), true);

            mReadThread = new ReadThread();
            mReadThread.start();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void disconnect() {
        try {
            mServer.close();
        } catch (IOException e) {
            // Best effort
        }
    }

    public void sendChatMessage(String sender, String message) {
        mServerWriter.println("lobby-chat \"" + sender + "\" " + message);
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
                    ServerCommandParser.handlePacket(mCallback, inputData);
                }
            }
            catch(IOException e) {
                System.out.println("Server connection error: " + e);
            }
        }
    }
}
