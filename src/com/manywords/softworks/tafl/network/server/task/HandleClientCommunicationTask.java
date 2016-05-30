package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.packet.ingame.GameChatPacket;
import com.manywords.softworks.tafl.network.packet.ingame.GameEndedPacket;
import com.manywords.softworks.tafl.network.packet.ingame.MovePacket;
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
        if(data.startsWith(LobbyChatPacket.PREFIX)) {
            LobbyChatPacket packet = LobbyChatPacket.parse(data);
            mServer.sendPacketToAllClients(packet, PriorityTaskQueue.Priority.LOW);
        }
        else if(data.startsWith(LoginPacket.PREFIX)) {
            mServer.getTaskQueue().pushTask(new LoginTask(mServer, mClient, LoginPacket.parse(data)));
        }
        else if(data.startsWith(GameListPacket.PREFIX)) {
            mServer.getTaskQueue().pushTask(new SendPacketTask(GameListPacket.parse(mServer.getGames()), mClient), PriorityTaskQueue.Priority.LOW);
        }
        else if(data.startsWith(CreateGamePacket.PREFIX)) {
            mServer.getTaskQueue().pushTask(new CreateGameTask(mServer, mClient, CreateGamePacket.parse(data)));
        }
        else if(data.startsWith(LeaveGamePacket.PREFIX)) {
            mServer.getTaskQueue().pushTask(new LeaveGameTask(mServer, mClient, LeaveGamePacket.parse(data)));
        }
        else if(data.startsWith(JoinGamePacket.PREFIX)) {
            mServer.getTaskQueue().pushTask(new JoinGameTask(mServer, mClient, JoinGamePacket.parse(data)));
        }
        else if(data.startsWith(MovePacket.PREFIX)) {
            mServer.getTaskQueue().pushTask(new MoveTask(mServer, mClient, MovePacket.parse(data)), PriorityTaskQueue.Priority.HIGH);
        }
        else if(data.startsWith(GameChatPacket.PREFIX)) {
            mServer.getTaskQueue().pushTask(new GameChatTask(mServer, mClient, GameChatPacket.parse(data)), PriorityTaskQueue.Priority.LOW);
        }
        else if(data.startsWith(GameEndedPacket.PREFIX)) {
            SendVictoryTask.sendOnClientLeaving(mServer, mClient);
        }
    }

    @Override
    public void run() {
        System.out.println("Server received: " + mData);
        processPacket(mData);
    }
}
