package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.network.server.task.HandleClientCommunicationTask;
import com.manywords.softworks.tafl.network.server.task.interval.GameListUpdateTask;
import com.manywords.softworks.tafl.network.server.task.interval.IntervalTask;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 5/22/16.
 */
public class ServerClient {
    private NetworkServer mServer;
    private Socket mClientSocket;

    protected String mUsername;

    private SocketListener mSocketListener;
    private PrintWriter mClientWriter;

    private List<IntervalTask> mLobbyTasks;
    private List<IntervalTask> mInGameTasks;

    private ServerGame mGame;
    private boolean mInGame;
    private GameRole mGameRole = GameRole.OUT_OF_GAME;

    protected ServerClient() {
        if(!(this instanceof DummyServerClient)) throw new IllegalStateException("No-arg constructor is for dummy client only!");
    }

    public ServerClient(NetworkServer server, Socket clientSocket) {
        mServer = server;
        mClientSocket = clientSocket;

        mLobbyTasks = new ArrayList<>(1);
        mInGameTasks = new ArrayList<>(1);

        mLobbyTasks.add(new GameListUpdateTask(mServer, this));

        mSocketListener = new SocketListener();
        mSocketListener.start();

        try {
            mClientWriter = new PrintWriter(new OutputStreamWriter(mClientSocket.getOutputStream()), true);
        } catch (IOException e) {
            Log.println(Log.Level.NORMAL, "Failed to connect to client!");
            mServer.onDisconnect(this);
        }
    }

    public void disconnect() {
        mServer.onDisconnect(this);
        try {
            mClientSocket.close();
        } catch (IOException e) {
            // best effort
        }
    }

    public ServerGame getGame() {
        return mGame;
    }

    public void setGame(ServerGame game, GameRole role) {
        mGame = game;
        mGameRole = role;
    }

    public GameRole getGameRole() {
        return mGameRole;
    }

    public String getUsername() {
        return mUsername;
    }

    public List<IntervalTask> getLobbyTasks() {
        return mLobbyTasks;
    }

    public void writePacket(NetworkPacket packet) {
        if(!mClientSocket.isClosed()) {
            mClientWriter.println(packet.toString());
        }
    }

    public void onRegistered(String username) {
        mUsername = username;
        mServer.clientEnteringLobby(this);
    }

    private class SocketListener extends Thread {
        public void run() {
            mServer.standardPrint("Client connecting: " + mClientSocket.getInetAddress());
            mServer.onConnect(ServerClient.this);
            String inputData = "";
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
            ) {
                while((inputData = in.readLine()) != null) {
                    mServer.getTaskQueue().pushTask(new HandleClientCommunicationTask(mServer, ServerClient.this, inputData), PriorityTaskQueue.Priority.STANDARD);
                }
            }
            catch(IOException e) {
                mServer.chattyPrint("Client connection error: " + e);
            }

            mServer.standardPrint("Client disconnecting: " + mClientSocket.getInetAddress());
            try {
                mClientSocket.close();
            } catch (IOException e) {
                // Best effort
            }
            mServer.onDisconnect(ServerClient.this);
        }
    }
}
