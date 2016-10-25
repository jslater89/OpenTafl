package com.manywords.softworks.tafl.network.packet.ingame;

import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.notation.MoveSerializer;
import com.manywords.softworks.tafl.notation.NotationParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 5/24/16.
 */
public class HistoryPacket extends NetworkPacket {
    public static final String PREFIX = "history";
    public final List<DetailedMoveRecord> moves;
    public final int boardSize;

    public static HistoryPacket parse(String data) {
        data = data.replaceFirst(PREFIX, "");
        String[] parts = data.trim().split(" ");
        int dimension = Integer.parseInt(parts[0]);

        data = data.replaceFirst("" + dimension, "").trim();

        List<DetailedMoveRecord> moves = new ArrayList<>();
        List<TimeSpec> timeSpecs = new ArrayList<>();

        if(parts.length < 2 || parts[1].trim().isEmpty()) {
            return new HistoryPacket(moves, dimension);
        }

        String[] records = data.split("\\|\\|");

        String[] recordParts;
        for(String record : records) {
            recordParts = record.split(" ");

            try {
                DetailedMoveRecord move = MoveSerializer.loadMoveRecord(dimension, recordParts[0]);
                if(recordParts.length == 2) {
                    move.setTimeRemaining(TimeSpec.parseMachineReadableString(recordParts[1]));
                }

                moves.add(move);
            }
            catch(NotationParseException e) {
                throw new IllegalStateException(e.toString());
            }
        }

        return new HistoryPacket(moves, dimension);
    }

    public static HistoryPacket parseHistory(List<GameState> gameHistory, int boardSize) {
        List<DetailedMoveRecord> moves = new ArrayList<>(gameHistory.size());
        List<TimeSpec> timeSpecs = new ArrayList<>(gameHistory.size());

        for(GameState state : gameHistory) {
            DetailedMoveRecord move = (DetailedMoveRecord) state.getExitingMove();
            if(move != null) {
                moves.add(move);

                if(move.getTimeRemaining() != null) {
                    timeSpecs.add(move.getTimeRemaining());
                }
                else {
                    timeSpecs.add(TimeSpec.emptyTimeSpec());
                }
            }
        }

        return new HistoryPacket(moves, boardSize);
    }

    public HistoryPacket(List<DetailedMoveRecord> moves, int boardSize) {
        this.moves = new ArrayList<>();
        this.moves.addAll(moves);

        this.boardSize = boardSize;
    }

    public String toString() {
        String result = PREFIX + " ";
        result += boardSize + " ";
        for(int i = 0; i < moves.size(); i++) {
            DetailedMoveRecord m = moves.get(i);
            TimeSpec ts = (m.getTimeRemaining() != null ? m.getTimeRemaining() : TimeSpec.emptyTimeSpec());
            result += m.toString() + " " + ts.toMachineReadableString() + "||";
        }

        return result;
    }
}
