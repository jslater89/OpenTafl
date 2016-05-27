package com.manywords.softworks.tafl.network.packet.ingame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/26/16.
 */
public class MoveResultPacket extends NetworkPacket {
    public int moveResult;

    public static MoveResultPacket parse(String data) {
        data = data.replaceFirst("move-result", "").trim();

        return new MoveResultPacket(Integer.parseInt(data));
    }

    public MoveResultPacket(int moveResult) {
        this.moveResult = moveResult;
    }

    @Override
    public String toString() {
        return "move-result " + moveResult;
    }
}
