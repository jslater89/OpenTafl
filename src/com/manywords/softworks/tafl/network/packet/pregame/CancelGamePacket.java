package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

import java.util.UUID;

/**
 * Created by jay on 5/26/16.
 */
public class CancelGamePacket extends NetworkPacket {
    public final UUID uuid;

    public static CancelGamePacket parse(String data) {
        data = data.replaceFirst("cancel-game", "").trim();

        return new CancelGamePacket(UUID.fromString(data));
    }

    public CancelGamePacket(UUID uuid) {
        this.uuid = uuid;
    }

    public String toString() {
        return "cancel-game " + uuid;
    }
}
