package com.manywords.softworks.tafl.network.packet.ingame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/28/16.
 */
public class GameEndedPacket extends NetworkPacket {
    public GameEndedPacket() { }

    @Override
    public String toString() {
        return "game-ended";
    }
}
