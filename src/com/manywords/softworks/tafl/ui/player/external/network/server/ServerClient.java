package com.manywords.softworks.tafl.ui.player.external.network.server;

import com.manywords.softworks.tafl.ui.player.external.network.server.tasks.HandleClientCommunicationTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by jay on 5/22/16.
 */
public class ServerClient {
    private NetworkServer mServer;
    private Socket mClientSocket;

    public ServerClient(NetworkServer server, Socket clientSocket) {
        mServer = server;
        mClientSocket = clientSocket;
    }

    private class SocketListener extends Thread {
        public void run() {
            String inputData = "";
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
            ) {
                while((inputData = in.readLine()) != null) {
                    mServer.getTaskQueue().pushStandardPriorityTask(new HandleClientCommunicationTask(inputData));
                }
            }
            catch(IOException e) {

            }
        }
    }
}
