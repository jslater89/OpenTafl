package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

import java.util.UUID;

/**
 * Created by jay on 5/26/16.
 */
public class SpectateGamePacket extends JoinGamePacket {
    public static final String PREFIX = "spectate";
    private final Type type;

    public static SpectateGamePacket parse(String data) {
        data = data.replaceFirst(PREFIX, "").trim();
        String[] split = data.split(" ");

        return new SpectateGamePacket(UUID.fromString(split[0]), Boolean.parseBoolean(split[1]), split[2]);
    }

    public SpectateGamePacket(UUID uuid, boolean spectate, String hashedPassword) {
        super(uuid, spectate, hashedPassword);

        type = Type.SPECTATE;
    }

    @Override
    public Type getType() {
        return type;
    }

    public String toString() {
        return PREFIX + " " + uuid + " " + spectate + " " + hashedPassword;
    }
}
