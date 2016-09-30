package com.manywords.softworks.tafl.engine.ai.tables;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;

/**
 * Created by jay on 8/23/16.
 */
public class HistoryTable {
    // Indexed by: side to move (0 or 1), from-square (0-coord-index), to-square.
    private final int[][][] mCutoffTable;
    private final int[][][] mOccurrenceTable;
    private final int mDimension;

    private static final int ATTACKER_INDEX = 0;
    private static final int DEFENDER_INDEX = 1;

    public HistoryTable(int dimension) {
        mCutoffTable = new int[2][dimension * dimension][dimension * dimension];
        mOccurrenceTable = new int[2][dimension * dimension][dimension * dimension];
        mDimension = dimension;
    }

    public void putMove(boolean isAttackingSide, int remainingDepth, MoveRecord move, boolean cutoff) {
        int sideIndex = (isAttackingSide ? ATTACKER_INDEX : DEFENDER_INDEX);
        int firstMoveIndex = Coord.getIndex(mDimension, move.start);
        int secondMoveIndex = Coord.getIndex(mDimension, move.end);
        mOccurrenceTable[sideIndex][firstMoveIndex][secondMoveIndex] += 1;

        if(cutoff)
            mCutoffTable[sideIndex][firstMoveIndex][secondMoveIndex] += remainingDepth * remainingDepth;
    }

    public float getRating(boolean isAttackingSide, MoveRecord move) {
        int sideIndex = (isAttackingSide ? ATTACKER_INDEX : DEFENDER_INDEX);
        int firstMoveIndex = Coord.getIndex(mDimension, move.start);
        int secondMoveIndex = Coord.getIndex(mDimension, move.end);

        int occurrences = mOccurrenceTable[sideIndex][firstMoveIndex][secondMoveIndex];
        if(occurrences == 0) return 0;
        else
            return mCutoffTable[sideIndex][firstMoveIndex][secondMoveIndex] / occurrences;
    }

    public int getDimension() {
        return mDimension;
    }
}
