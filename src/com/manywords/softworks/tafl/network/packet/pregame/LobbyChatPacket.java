package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/23/16.
 */
public class LobbyChatPacket extends NetworkPacket {
    public final String sender;
    public final String message;

    public static LobbyChatPacket parse(String data) {
        String[] splitOnQuotes = data.split("\"");
        return new LobbyChatPacket(splitOnQuotes[1], splitOnQuotes[2].trim());
    }

    public LobbyChatPacket(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String toString() {
        return "lobby-chat \"" + sender + "\" " + message;
    }
}
