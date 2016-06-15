package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.network.server.task.HandleClientCommunicationTask;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 6/14/16.
 */
public class DummyTestServer {
    private ServerSocket mServerSocket;
    private boolean mRunning = true;

    private ReadThread mReadThread;
    private PrintWriter mWriter;

    public DummyTestServer() {
        try {
            mServerSocket = new ServerSocket(11541);

            while(mRunning) {
                Socket clientSocket = mServerSocket.accept();
                mReadThread = new ReadThread(clientSocket);
                mWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            }
        } catch (IOException e) {

        } finally {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                // best effort
            }
        }
    }

    public String lastPacketReceived;

    private class ReadThread extends Thread {
        private boolean mRunning = true;
        private Socket mClient;

        public ReadThread(Socket client) {
            mClient = client;
        }

        @Override
        public void run() {
            String inputData;
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
            ) {
                while((inputData = in.readLine()) != null) {
                    lastPacketReceived = inputData;

                    if(inputData.startsWith("login")) {
                        mWriter.println("success");
                    }
                }
            }
            catch(IOException e) {
                // Whatever
            }

            try {
                mClient.close();
            } catch (IOException e) {
                // Best effort
            }
        }

        public void cancel() {
            mRunning = false;
        }
    }
}
