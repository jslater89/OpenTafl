package com.manywords.softworks.tafl.network.packet.ingame;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.network.packet.ClientInformation;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.notation.MoveSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 5/24/16.
 */
public class HistoryPacket extends NetworkPacket {
    public static final String PREFIX = "history";
    public final List<MoveRecord> moves;

    public static HistoryPacket parse(int dimension, String data) {
        data = data.replaceFirst(PREFIX, "");
        String[] records = data.split("\\|\\|");

        List<MoveRecord> moves = new ArrayList<>();

        if(data.trim().isEmpty()) return new HistoryPacket(moves);

        for(String record : records) {
            moves.add(MoveSerializer.loadMoveRecord(dimension, record.trim()));
        }

        return new HistoryPacket(moves);
    }

    public static HistoryPacket parseHistory(List<GameState> gameHistory) {
        List<MoveRecord> moves = new ArrayList<>(gameHistory.size());

        for(GameState state : gameHistory) {
            if(state.getExitingMove() != null) {
                moves.add(state.getExitingMove());
            }
        }

        return new HistoryPacket(moves);
    }

    public static HistoryPacket parse(List<MoveRecord> moveRecords) {
        List<MoveRecord> moves = new ArrayList<>(moveRecords.size());

        for(MoveRecord m : moveRecords) {
            moves.add(m);
        }

        return new HistoryPacket(moves);
    }

    public HistoryPacket(List<MoveRecord> moves) {
        this.moves = moves;
    }

    public String toString() {
        String result = PREFIX + " ";
        for(MoveRecord m : moves) {
            result += m.toString() + "||";
        }

        return result;
    }
}
