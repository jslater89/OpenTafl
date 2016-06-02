package com.manywords.softworks.tafl.network.packet.ingame;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.rules.Side;

/**
 * Created by jay on 5/28/16.
 */
public class VictoryPacket extends NetworkPacket {
    public static final String PREFIX = "victory";
    public enum Victory {
        ATTACKER,
        DEFENDER,
        DRAW
    }

    public final Victory victory;

    public static VictoryPacket parse(String data) {
        data = data.replaceFirst("victory", "").trim();

        return new VictoryPacket(Victory.valueOf(data));
    }

    public VictoryPacket(Side side) {
        if(side == null) {
            victory = Victory.DRAW;
        }
        else if(side.isAttackingSide()) {
            victory = Victory.ATTACKER;
        }
        else {
            victory = Victory.DEFENDER;
        }
    }

    public VictoryPacket(Victory victory) {
        this.victory = victory;
    }

    @Override
    public String toString() {
        return PREFIX + " " + victory;
    }
}
