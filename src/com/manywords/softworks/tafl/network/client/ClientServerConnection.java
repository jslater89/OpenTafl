package com.manywords.softworks.tafl.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by jay on 5/23/16.
 */
public class ClientServerConnection {
    private final String hostname;
    private final int port;

    private Socket mServer;
    private Thread mReadThread;

    public ClientServerConnection(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public boolean connect() {
        try {
            mServer = new Socket(hostname, port);

            return true;
        } catch (IOException e) {
            return false;
        }
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
                    //TODO: Do something with it, I guess
                }
            }
            catch(IOException e) {
                System.out.println("Server connection error: " + e);
            }
        }
    }
}
