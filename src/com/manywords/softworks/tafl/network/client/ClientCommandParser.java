package com.manywords.softworks.tafl.network.client;

import com.manywords.softworks.tafl.network.packet.ErrorPacket;
import com.manywords.softworks.tafl.network.packet.LobbyChatPacket;

/**
 * Created by jay on 5/23/16.
 */
public class ClientCommandParser {
    public static void handlePacket(ClientServerConnection.ClientServerCallback callback, String data) {
        if(data.startsWith("lobby-chat")) {
            LobbyChatPacket packet = LobbyChatPacket.parse(data);
            callback.onChatMessageReceived(packet.sender, packet.message);
        }
        else if(data.startsWith("error")) {
            ErrorPacket packet = ErrorPacket.parse(data);
            callback.onErrorReceived(packet.error);
        }
        else if(data.startsWith("success")) {
            callback.onSuccessReceived();
        }
    }
}
