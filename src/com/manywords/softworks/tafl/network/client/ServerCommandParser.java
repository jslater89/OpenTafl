package com.manywords.softworks.tafl.network.client;

import com.manywords.softworks.tafl.network.server.packet.LobbyChatPacket;

/**
 * Created by jay on 5/23/16.
 */
public class ServerCommandParser {
    public static void handlePacket(ClientServerConnection.ClientServerCallback callback, String data) {
        if(data.startsWith("lobby-chat")) {
            LobbyChatPacket packet = new LobbyChatPacket(data);
            callback.onChatMessageReceived(packet.sender, packet.message);
        }
    }
}
