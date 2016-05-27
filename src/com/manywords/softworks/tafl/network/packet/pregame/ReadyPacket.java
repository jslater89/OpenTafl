package com.manywords.softworks.tafl.network.packet.pregame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/26/16.
 */
public class ReadyPacket extends NetworkPacket {
    public ReadyPacket parse(String data) {
        return new ReadyPacket();
    }

    public ReadyPacket() { }

    @Override
    public String toString() {
        return "ready-for-game";
    }
}
