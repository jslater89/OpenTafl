package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.packet.ingame.ClockUpdatePacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.server.ServerGame;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

/**
 * Created by jay on 7/15/16.
 */
public class UnscheduledClockUpdateTask implements Runnable {
    private NetworkServer mServer;
    private ServerClient mClient;
    public UnscheduledClockUpdateTask(NetworkServer server, ServerClient client) {
        mServer = server;
        mClient = client;
    }

    @Override
    public void run() {
        ServerGame g = mClient.getGame();
        if(g != null && g.getGame() != null && g.getGame().getClock() != null) {
            GameClock c = g.getGame().getClock();

            TimeSpec attackerClock = c.getClockEntry(true).toTimeSpec();
            TimeSpec defenderClock = c.getClockEntry(false).toTimeSpec();

            mServer.sendPacketToClient(mClient, new ClockUpdatePacket(attackerClock, defenderClock), PriorityTaskQueue.Priority.HIGH);
        }
    }
}
