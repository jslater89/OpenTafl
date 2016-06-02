package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/23/16.
 */
public class LobbyChatPacket extends NetworkPacket {
    public static final String PREFIX = "lobby-chat";
    public final String sender;
    public final String message;

    public static LobbyChatPacket parse(String data) {
        String sender = data.split("\"")[1];
        String message = data.replaceFirst(PREFIX + "\\s+?\".*?\"", "").trim();
        return new LobbyChatPacket(sender, message);
    }

    public LobbyChatPacket(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String toString() {
        return PREFIX + " " + "\"" + sender + "\" " + message;
    }
}
