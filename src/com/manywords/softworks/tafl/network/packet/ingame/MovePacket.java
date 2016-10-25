package com.manywords.softworks.tafl.network.packet.ingame;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.notation.MoveSerializer;
import com.manywords.softworks.tafl.notation.NotationParseException;

/**
 * Created by jay on 5/26/16.
 */
public class MovePacket extends NetworkPacket {
    public static final java.lang.String PREFIX = "move-record";
    public final MoveRecord move;

    public static MovePacket parse(int dimension, String data) {
        data = data.replaceFirst(PREFIX, "").trim();
        MoveRecord m = null;
        try {
            m = MoveSerializer.loadMoveRecord(dimension, data);
        }
        catch(NotationParseException e) {
            throw new IllegalStateException(e.toString());
        }
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
