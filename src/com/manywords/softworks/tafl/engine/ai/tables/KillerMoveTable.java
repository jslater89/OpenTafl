package com.manywords.softworks.tafl.engine.ai.tables;

import com.manywords.softworks.tafl.engine.MoveRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 8/18/16.
 */
public class KillerMoveTable {
    private MoveRecord[][] killers;
    private final int maxDepth;
    public final int killersToKeep;

    public KillerMoveTable(int maxDepth, int killersToKeep) {
        killers = new MoveRecord[maxDepth][killersToKeep];
        this.maxDepth = maxDepth;
        this.killersToKeep = killersToKeep;
    }

    public void reset() {
        for(int i = 0; i < killers.length; i++) {
            for(int j = 0; j < killers[i].length; j++) {
                killers[i][j] = null;
            }
        }
    }

    public int getDepth() {
        return maxDepth;
    }

    public int movesToKeep() {
        return killersToKeep;
    }

    public void putMove(int depth, MoveRecord move) {
        if(depth >= killers.length) return;

        // If there are null moves, add it in place of one of those.
        boolean added = false;
        for(int i = 0; i < killersToKeep; i++) {
            if(killers[depth][i] == null) killers[depth][i] = move;
            added = true;
        }

        // Otherwise, replace the last one.
        if(!added && killersToKeep > 0) {
            killers[depth][killers[depth].length - 1] = move;
        }
    }

    public List<MoveRecord> getMoves(int depth) {
        List<MoveRecord> moves = new ArrayList<>();

        if(depth >= killers.length) return moves;


        for(int i = 0; i < killersToKeep; i++) {
            moves.add(killers[depth][i]);
        }

        return moves;
    }

    /**
     * Returns a rating for the given move.
     * @param depth
     * @param record (killersToKeep - index) for moves in the table, or -1 for moves not in the table.
     * @return
     */
    public int rateMove(int depth, MoveRecord record) {
        if(depth >= killers.length) return -1;

        for(int rank = killersToKeep; rank > 0; rank--) {
            int index = killersToKeep - rank;

            if(record.softEquals(killers[depth][index])) return rank;
        }

        return -1;
    }
}
