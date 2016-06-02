package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

import java.util.UUID;

/**
 * Created by jay on 5/26/16.
 */
public class LeaveGamePacket extends NetworkPacket {
    public static final String PREFIX = "leave-game";
    public final UUID uuid;

    public static LeaveGamePacket parse(String data) {
        data = data.replaceFirst("leave-game", "").trim();

        return new LeaveGamePacket(UUID.fromString(data));
    }

    public LeaveGamePacket(UUID uuid) {
        this.uuid = uuid;
    }

    public String toString() {
        return PREFIX + " " + uuid;
    }
}
