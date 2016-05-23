package com.manywords.softworks.tafl.ui.player.external.network.server;

import com.manywords.softworks.tafl.ui.player.external.network.server.threads.PriorityTaskQueue;
import com.manywords.softworks.tafl.ui.player.external.network.server.threads.ServerThread;

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

    private boolean mRunning;

    public NetworkServer(int threadCount) {
        mTaskQueue = new PriorityTaskQueue(this);
        mThreadPool = new ArrayList<>(threadCount);
        mClients = new ArrayList<>(64);

        for(int i = 0; i < threadCount; i++) {
            mThreadPool.add(new ServerThread(mTaskQueue));
        }

        startServer();
    }

    public PriorityTaskQueue getTaskQueue() {
        return mTaskQueue;
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
    }

    private void addClient(ServerClient c) {
        synchronized (mClients) {
            mClients.add(c);
        }
    }
}
