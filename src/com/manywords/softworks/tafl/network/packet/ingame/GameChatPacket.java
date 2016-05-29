package com.manywords.softworks.tafl.network.packet.ingame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/28/16.
 */
public class GameChatPacket extends NetworkPacket {
    public static final String PREFIX = "game-chat";
    public final String sender;
    public final String message;

    public static GameChatPacket parse(String data) {
        String[] splitOnQuotes = data.split("\"");
        return new GameChatPacket(splitOnQuotes[1], splitOnQuotes[2].trim());
    }

    public GameChatPacket(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String toString() {
        return PREFIX + " \"" + sender + "\" " + message;
    }
}