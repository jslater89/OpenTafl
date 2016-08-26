package com.manywords.softworks.tafl.engine.ai.tables;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;

/**
 * Created by jay on 8/23/16.
 */
public class HistoryTable {
    // Indexed by: side to move (0 or 1), from-square (0-coord-index), to-square.
    private final int[][][] mTable;
    private final int mDimension;

    private static final int ATTACKER_INDEX = 0;
    private static final int DEFENDER_INDEX = 1;

    public HistoryTable(int dimension) {
        mTable = new int[2][dimension * dimension][dimension * dimension];
        mDimension = dimension;
    }

    public void putMove(boolean isAttackingSide, int remainingDepth, MoveRecord move) {
        int sideIndex = (isAttackingSide ? ATTACKER_INDEX : DEFENDER_INDEX);
        int firstMoveIndex = Coord.getIndex(mDimension, move.start);
        int secondMoveIndex = Coord.getIndex(mDimension, move.end);
        mTable[sideIndex][firstMoveIndex][secondMoveIndex] += (remainingDepth * remainingDepth);
    }

    public int getRating(boolean isAttackingSide, MoveRecord move) {
        int sideIndex = (isAttackingSide ? ATTACKER_INDEX : DEFENDER_INDEX);
        int firstMoveIndex = Coord.getIndex(mDimension, move.start);
        int secondMoveIndex = Coord.getIndex(mDimension, move.end);

        return mTable[sideIndex][firstMoveIndex][secondMoveIndex];
    }

    public int getDimension() {
        return mDimension;
    }
}
