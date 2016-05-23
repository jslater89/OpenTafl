package com.manywords.softworks.tafl.ui.player.external.network.server;

import com.manywords.softworks.tafl.ui.player.external.network.server.threads.NetworkServerThread;

import java.util.List;

/**
 * The main class for the OpenTafl server. Handles starting up things and initial reception of network packets.
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
    private List<NetworkServerThread> mThreadPool;

    public void notifyThreadIfNecessary() {
        for(NetworkServerThread thread : mThreadPool) {
            if(thread.isWaiting()) {
                thread.notifyThisThread();
                return;
            }
        }
    }
}
