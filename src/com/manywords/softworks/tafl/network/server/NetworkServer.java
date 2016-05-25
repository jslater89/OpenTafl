package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.network.server.task.SendPacketTask;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;
import com.manywords.softworks.tafl.network.server.thread.ServerThread;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class for the OpenTafl server. Handles starting up things and initial reception of network packets.
 *
 * The TCP listeners here instantiate a ServerClient object,
 *
 * The path of a network packet:
 *
 * 1. Received here, task created.
 * 2. Task entered into appropriate queue.
 * 3. A network thread is notified, if necessary.
 * 4. The thread handles the request, including any required database/state updates &c.
 * 5. The thread responds to the client, if necessary.
 *
 * n.b. everything must be thread-safe.
 */
public class NetworkServer {
    private PriorityTaskQueue mTaskQueue;
    private List<ServerThread> mThreadPool;

    private final List<ServerClient> mClients;
    private final List<ServerGame> mGames;

    private boolean mRunning = true;

    public NetworkServer(int threadCount) {
        mTaskQueue = new PriorityTaskQueue(this);
        mThreadPool = new ArrayList<>(threadCount);
        mClients = new ArrayList<>(64);
        mGames = new ArrayList<>(32);

        for(int i = 0; i < threadCount; i++) {
            mThreadPool.add(new ServerThread(mTaskQueue));
        }

        startServer();
    }

    public PriorityTaskQueue getTaskQueue() {
        return mTaskQueue;
    }
    public List<ServerClient> getClients() { return mClients; }

    public void sendPacketToAllClients(NetworkPacket packet, PriorityTaskQueue.Priority priority) {
        for(ServerClient client : mClients) {
            mTaskQueue.pushTask(new SendPacketTask(packet, client), priority);
        }
    }

    public void sendPacketToClient(ServerClient client, NetworkPacket packet, PriorityTaskQueue.Priority priority) {
        mTaskQueue.pushTask(new SendPacketTask(packet, client), priority);
    }

    public void notifyThreadIfNecessary() {
        for(ServerThread thread : mThreadPool) {
            if(thread.isWaiting()) {
                thread.notifyThisThread();
                return;
            }
        }
    }

    private void startServer() {
        for(ServerThread thread : mThreadPool) {
            thread.start();
        }

        try (
            ServerSocket socket = new ServerSocket(11541);
        ) {
            while(mRunning) {
                Socket clientSocket = socket.accept();
                addClient(new ServerClient(this, clientSocket));
            }
        } catch (IOException e) {
            System.out.println("Failed to start server socket");
            System.exit(-1);
        }

        System.out.println("Server stopping.");
    }

    /**
     * Returns a copy of the games list.
     * @return
     */
    public List<ServerGame> getGames() {
        synchronized (mGames) {
            return new ArrayList<>(mGames);
        }
    }

    public void onDisconnect(ServerClient c) {
        synchronized (mClients) {
            mClients.remove(c);
        }
    }

    private void addClient(ServerClient c) {
        synchronized (mClients) {
            mClients.add(c);
        }
    }
}
