package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jay on 6/14/16.
 */
public class DummyTestServer {
    private ServerSocket mServerSocket;
    private boolean mRunning = true;

    private ReadThread mReadThread;
    private PrintWriter mWriter;

    public DummyTestServer() {

    }

    public void start() {
        try {
            mServerSocket = new ServerSocket(11541);

            while(mRunning) {
                Socket clientSocket = mServerSocket.accept();
                mWriter = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                mReadThread = new ReadThread(clientSocket);
                mReadThread.start();
            }
        } catch (IOException e) {
            Log.println(Log.Level.VERBOSE, e);
        } finally {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                // best effort
            }
        }
    }

    public void stop() {
        mReadThread.cancel();
        try {
            mServerSocket.close();
            mWriter.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public void prodStartGame() {
        prodClient("start-game dim:11 name:Copenhagen atkf:y tfr:w sw:s efe:y start:/3ttttt3/5t5/11/t4T4t/t3TTT3t/tt1TTKTT1tt/t3TTT3t/t4T4t/11/5t5/3ttttt3/");
    }

    public void prodVictory() {
        prodClient("victory DEFENDER");
    }

    public void prodClient(String packet) {
        mWriter.println(packet);
        mWriter.flush();
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
                    //System.out.println("Dummy server received: " + inputData);

                    if(inputData.startsWith("login")) {
                        prodClient("success");
                    }
                    else if(inputData.startsWith("create-game")) {
                        prodClient("success attackers");
                    }
                    else if(inputData.startsWith("leave-game")) {

                    }
                    else if(inputData.startsWith("game-list")) {
                        prodClient("game-list");
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
