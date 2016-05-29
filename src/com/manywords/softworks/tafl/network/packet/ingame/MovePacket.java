package com.manywords.softworks.tafl.network.packet.ingame;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.notation.MoveSerializer;

/**
 * Created by jay on 5/26/16.
 */
public class MovePacket extends NetworkPacket {
    public static final java.lang.String PREFIX = "move-record";
    public final MoveRecord move;

    public static MovePacket parse(String data) {
        data = data.replaceFirst(PREFIX, "").trim();
        MoveRecord m = MoveSerializer.loadMoveRecord(data);
        return new MovePacket(m);
    }

    public MovePacket(MoveRecord move) {
        this.move = move;
    }

    @Override
    public String toString() {
        return PREFIX + " " + move.toString();
    }
}
