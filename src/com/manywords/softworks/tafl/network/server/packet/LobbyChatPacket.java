package com.manywords.softworks.tafl.network.server.packet;

/**
 * Created by jay on 5/23/16.
 */
public class LobbyChatPacket extends NetworkPacket {
    public final String sender;
    public final String message;

    public LobbyChatPacket(String data) {
        String[] splitOnQuotes = data.split("\"");
        sender = splitOnQuotes[1];
        message = splitOnQuotes[2].trim();
    }

    public LobbyChatPacket(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String toString() {
        return "lobby-chat \"" + sender + "\" " + message;
    }
}
