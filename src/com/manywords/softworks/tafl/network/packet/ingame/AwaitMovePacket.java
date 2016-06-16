package com.manywords.softworks.tafl.network.packet.ingame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/26/16.
 */
public class AwaitMovePacket extends NetworkPacket {
    public static final String PREFIX = "await-move";
    public final boolean attackingSide;

    public static AwaitMovePacket parse(String data) {
        if(data.contains("attackers")) {
            return new AwaitMovePacket(true);
        }
        else {
            return new AwaitMovePacket(false);
        }
    }

    public AwaitMovePacket(boolean attackingSide) {
        this.attackingSide = attackingSide;
    }

    public String toString() {
        return PREFIX + " " + (attackingSide ? "attackers" : "defenders");
    }
}
