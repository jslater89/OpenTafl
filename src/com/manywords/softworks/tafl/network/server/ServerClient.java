package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.network.server.packet.NetworkPacket;
import com.manywords.softworks.tafl.network.server.task.HandleClientCommunicationTask;

import java.io.*;
import java.net.Socket;

/**
 * Created by jay on 5/22/16.
 */
public class ServerClient {
    private NetworkServer mServer;
    private Socket mClientSocket;

    private SocketListener mSocketListener;
    private PrintWriter mClientWriter;

    public ServerClient(NetworkServer server, Socket clientSocket) {
        mServer = server;
        mClientSocket = clientSocket;

        mSocketListener = new SocketListener();
        mSocketListener.start();

        try {
            mClientWriter = new PrintWriter(new OutputStreamWriter(mClientSocket.getOutputStream()), true);
        } catch (IOException e) {
            System.err.println("Failed to connect to client!");
            mServer.onDisconnect(this);
        }
    }

    public void writePacket(NetworkPacket packet) {
        mClientWriter.println(packet.toString());
    }

    private class SocketListener extends Thread {
        public void run() {
            System.out.println("Client connecting: " + mClientSocket.getInetAddress());
            String inputData = "";
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
            ) {
                while((inputData = in.readLine()) != null) {
                    mServer.getTaskQueue().pushStandardPriorityTask(new HandleClientCommunicationTask(mServer, inputData));
                }
            }
            catch(IOException e) {
                System.out.println("Client connection error: " + e);
            }

            System.out.println("Client disconnecting: " + mClientSocket.getInetAddress());
            try {
                mClientSocket.close();
            } catch (IOException e) {
                // Best effort
            }
            mServer.onDisconnect(ServerClient.this);
        }
    }
}
