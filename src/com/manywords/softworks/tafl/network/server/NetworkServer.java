package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.network.server.task.SendPacketTask;
import com.manywords.softworks.tafl.network.server.task.interval.BucketedIntervalTaskHolder;
import com.manywords.softworks.tafl.network.server.task.interval.IntervalTask;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;
import com.manywords.softworks.tafl.network.server.thread.ServerTickThread;
import com.manywords.softworks.tafl.rules.Rules;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private ServerTickThread mTickThread;

    private BucketedIntervalTaskHolder mGameClockTasks;
    private BucketedIntervalTaskHolder mGameListUpdateTasks;

    private final List<ServerClient> mClients;
    private final List<ServerClient> mLobbyClients;
    private final List<ServerGame> mGames;

    private boolean mRunning = true;

    public NetworkServer(int threadCount) {
        mTaskQueue = new PriorityTaskQueue(threadCount);
        mClients = new ArrayList<>(64);
        mLobbyClients = new ArrayList<>(64);
        mGames = new ArrayList<>(32);

        mTickThread = new ServerTickThread();

        mGameListUpdateTasks = new BucketedIntervalTaskHolder(mTaskQueue, 1000, 2, PriorityTaskQueue.Priority.LOW);
        mGameClockTasks = new BucketedIntervalTaskHolder(mTaskQueue, 1000, 5, PriorityTaskQueue.Priority.HIGH);

        mTickThread.addTaskHolder(mGameListUpdateTasks);
        mTickThread.addTaskHolder(mGameClockTasks);

        startServer();
    }

    public PriorityTaskQueue getTaskQueue() {
        return mTaskQueue;
    }
    public List<ServerClient> getClients() { return mClients; }
    public boolean hasClientNamed(String username) {
        synchronized (mClients) {
            for (ServerClient c : mClients) {
                if (username.equals(c.getUsername())) return true;
            }
        }

        return false;
    }

    public void sendPacketToAllClients(NetworkPacket packet, PriorityTaskQueue.Priority priority) {
        for(ServerClient client : mClients) {
            mTaskQueue.pushTask(new SendPacketTask(packet, client), priority);
        }
    }

    public void sendPacketToClient(ServerClient client, NetworkPacket packet, PriorityTaskQueue.Priority priority) {
        mTaskQueue.pushTask(new SendPacketTask(packet, client), priority);
    }

    private void startServer() {
        mTaskQueue.start();
        mTickThread.start();

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

    public ServerGame getGame(UUID gameUUID) {
        ServerGame g = null;

        synchronized (mGames) {
            for(ServerGame game : mGames) {
                if(game.uuid.equals(gameUUID)) {
                    g = game;
                    break;
                }
            }
        }

        return g;
    }

    public boolean createGame(ServerClient client, UUID gameUUID, String passwordHash, Rules rules, boolean attackingSide) {
        if(client.getGame() != null) {
            return false;
        }

        ServerGame g = new ServerGame(this, gameUUID);
        g.setRules(rules);
        if(!passwordHash.equals("none")) {
            g.setPassword(passwordHash);
        }

        if(attackingSide) {
            g.setAttackerClient(client);
        }
        else {
            g.setDefenderClient(client);
        }

        synchronized (mGames) {
            mGames.add(g);
        }

        return true;
    }

    /**
     * Called not when the actual game is done, but when both clients have exited the game.
     * @return
     */
    public void removeGame(ServerGame g) {
        synchronized (mGames) {
            mGames.remove(g);
        }

        g.shutdown();
    }

    public void onDisconnect(ServerClient c) {
        synchronized (mClients) {
            mClients.remove(c);
        }

        // If a party to the game leaves the server, stop the game.
        if(c.getGame() != null && (c.getGameRole() == ServerClient.GameRole.ATTACKER || c.getGameRole() == ServerClient.GameRole.DEFENDER)) {
            removeGame(c.getGame());
        }
        clientExitingLobby(c);
    }

    private void clientEnteringLobby(ServerClient c) {
        synchronized (mLobbyClients) {
            mLobbyClients.add(c);
        }

        for(IntervalTask t : c.getLobbyTasks()) {
            mGameListUpdateTasks.addBucketTask(t);
        }
    }

    private void clientExitingLobby(ServerClient c) {
        synchronized (mLobbyClients) {
            mLobbyClients.remove(c);
        }

        for(IntervalTask t : c.getLobbyTasks()) {
            mGameListUpdateTasks.removeBucketTask(t);
        }
    }

    private void addClient(ServerClient c) {
        synchronized (mClients) {
            mClients.add(c);
        }

        clientEnteringLobby(c);
    }
}
