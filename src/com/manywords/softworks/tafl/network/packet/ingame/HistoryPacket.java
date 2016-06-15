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
    public final int boardSize;

    public static HistoryPacket parse(String data) {
        data = data.replaceFirst(PREFIX, "");
        String[] parts = data.trim().split(" ");
        int dimension = Integer.parseInt(parts[0]);

        List<MoveRecord> moves = new ArrayList<>();
        if(parts.length < 2 || parts[1].trim().isEmpty()) {
            return new HistoryPacket(moves, dimension);
        }
        String[] records = parts[1].split("\\|\\|");


        if(data.trim().isEmpty()) return new HistoryPacket(moves, dimension);

        for(String record : records) {
            moves.add(MoveSerializer.loadMoveRecord(dimension, record.trim()));
        }

        return new HistoryPacket(moves, dimension);
    }

    public static HistoryPacket parseHistory(List<GameState> gameHistory, int boardSize) {
        List<MoveRecord> moves = new ArrayList<>(gameHistory.size());

        for(GameState state : gameHistory) {
            if(state.getExitingMove() != null) {
                moves.add(state.getExitingMove());
            }
        }

        return new HistoryPacket(moves, boardSize);
    }

    public static HistoryPacket parse(List<MoveRecord> moveRecords, int boardSize) {
        List<MoveRecord> moves = new ArrayList<>(moveRecords.size());

        for(MoveRecord m : moveRecords) {
            moves.add(m);
        }

        return new HistoryPacket(moves, boardSize);
    }

    public HistoryPacket(List<MoveRecord> moves, int boardSize) {
        this.moves = moves;
        this.boardSize = boardSize;
    }

    public String toString() {
        String result = PREFIX + " ";
        result += boardSize + " ";
        for(MoveRecord m : moves) {
            result += m.toString() + "||";
        }

        return result;
    }
}
