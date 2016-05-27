package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.packet.pregame.*;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;


public class HandleClientCommunicationTask implements Runnable {
    private NetworkServer mServer;
    private ServerClient mClient;
    private String mData;

    public HandleClientCommunicationTask(NetworkServer server, ServerClient client, String data) {
        mServer = server;
        mClient = client;
        mData = data;
    }

    private void processPacket(String data) {
        if(data.startsWith("lobby-chat")) {
            LobbyChatPacket packet = LobbyChatPacket.parse(data);
            mServer.sendPacketToAllClients(packet, PriorityTaskQueue.Priority.LOW);
        }
        else if(data.startsWith("login")) {
            mServer.getTaskQueue().pushTask(new LoginTask(mServer, mClient, LoginPacket.parse(data)));
        }
        else if(data.startsWith("game-list")) {
            mServer.getTaskQueue().pushTask(new SendPacketTask(GameListPacket.parse(mServer.getGames()), mClient), PriorityTaskQueue.Priority.LOW);
            //mServer.getTaskQueue().pushTask(new SendPacketTask(GameListPacket.parse(NetworkDummyDataGenerator.getDummyGames(mServer, new Random().nextInt(20))), mClient), PriorityTaskQueue.Priority.LOW);
        }
        else if(data.startsWith("create-game")) {
            mServer.getTaskQueue().pushTask(new CreateGameTask(mServer, mClient, CreateGamePacket.parse(data)));
        }
        else if(data.startsWith("cancel-game")) {
            mServer.getTaskQueue().pushTask(new CancelGameTask(mServer, mClient, CancelGamePacket.parse(data)));
        }
    }

    @Override
    public void run() {
        System.out.println("Server received: " + mData);
        processPacket(mData);
    }
}
