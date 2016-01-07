package com.manywords.softworks.tafl.engine.ai.tables;

import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;

/**
 * Created by jay on 12/16/15.
 */
public class TranspositionTable {
    // Data format:
    // bytes 0-2 - evaluation
    // bytes 2-4 - depth from game start
    // byte  5   - depth searched from this node

    private static final long BYTE_MASK = 255;
    private static final long EVAL_MASK = BYTE_MASK + (BYTE_MASK << 8) ;
    private static final long AGE_MASK = (BYTE_MASK << 16) + (BYTE_MASK << 24);
    private static final long DEPTH_MASK = (BYTE_MASK << 32);
    private static final int EVAL_SHIFT = 0;
    private static final int AGE_SHIFT = 16;
    private static final int DEPTH_SHIFT = 32;

    private static final int DISCARD_AFTER_PLIES = 10;

    // 8 bytes of zobrist table
    // 8 bytes of data table;
    private static final int ENTRY_SIZE = 16;

    private int mMaxSize = (1024 * 1024 * 500) / ENTRY_SIZE; // 500 mb, 16 bytes per entry
    private int mRequestedSize = 0;
    private long[] mZobristTable;
    private long[] mDataTable;

    /**
     *
     * @param size Size of the table in megabytes.
     */
    public TranspositionTable(int size) {
        mRequestedSize = size;
        mMaxSize = size * 1024 * 1024 / ENTRY_SIZE;

        mZobristTable = new long[mMaxSize];
        mDataTable = new long[mMaxSize];
    }

    public int size() {
        return mRequestedSize;
    }

    public void putValue(long zobrist, short value, int searchedToDepth, int gameLength) {
        int index = (Math.abs((int)(zobrist % mMaxSize)));

        boolean add = false;
        if(mZobristTable[index] == 0) add = true;

        if(!add) {
            long data = mDataTable[index];

            byte entryDepth = (byte) ((data & DEPTH_MASK) >>> DEPTH_SHIFT);
            char entryAge = (char) ((data & AGE_MASK) >>> AGE_SHIFT);

            if (entryDepth < searchedToDepth || (gameLength - entryAge) >= DISCARD_AFTER_PLIES) {
                add = true;
            }
        }

        if(add) {
            mZobristTable[index] = zobrist;
            mDataTable[index] = value | ((long) gameLength << AGE_SHIFT) | ((long) searchedToDepth << DEPTH_SHIFT);
        }
    }

    public short getValue(long zobrist, int minDepth, int gameLength) {
        int index = (Math.abs((int)(zobrist % mMaxSize)));
        if(mZobristTable[index] != zobrist) return Evaluator.NO_VALUE;

        long data = mDataTable[index];
        byte entryDepth = (byte)((data & DEPTH_MASK) >>> DEPTH_SHIFT);

        if (entryDepth < minDepth) return Evaluator.NO_VALUE;
        else return (short)((data & EVAL_MASK));
    }

    public long getData(long zobrist) {
        int index = (Math.abs((int)(zobrist % mMaxSize)));
        if(mZobristTable[index] != zobrist) return Evaluator.NO_VALUE;

        return mDataTable[index];
    }
}
