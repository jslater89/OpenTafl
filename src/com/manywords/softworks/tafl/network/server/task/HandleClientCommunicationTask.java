package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.packet.LobbyChatPacket;


public class HandleClientCommunicationTask implements Runnable {
    private NetworkServer mServer;
    private String mData;

    public HandleClientCommunicationTask(NetworkServer server, String data) {
        mServer = server;
        mData = data;
    }

    private void processPacket(String data) {
        if(data.startsWith("lobby-chat")) {
            LobbyChatPacket packet = new LobbyChatPacket(data);
            mServer.sendPacketToAllClients(packet);
        }
    }

    @Override
    public void run() {
        System.out.println("Server received: " + mData);
        processPacket(mData);
    }
}
