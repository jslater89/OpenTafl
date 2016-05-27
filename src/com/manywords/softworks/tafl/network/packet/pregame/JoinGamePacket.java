package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

import java.util.UUID;

/**
 * Created by jay on 5/26/16.
 */
public class JoinGamePacket extends NetworkPacket {
    public final UUID uuid;
    public final boolean spectate;
    public final String hashedPassword;

    public static JoinGamePacket parse(String data) {
        data = data.replaceFirst("join-game", "").trim();
        String[] split = data.split(" ");

        return new JoinGamePacket(UUID.fromString(split[0]), Boolean.parseBoolean(split[1]), split[2]);
    }

    public JoinGamePacket(UUID uuid, boolean spectate, String hashedPassword) {
        this.uuid = uuid;
        this.spectate = spectate;
        this.hashedPassword = hashedPassword;
    }

    public String toString() {
        return "join-game " + uuid + " " + spectate + " " + hashedPassword;
    }
}
