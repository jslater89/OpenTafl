package com.manywords.softworks.tafl.engine.ai.tables;

import com.manywords.softworks.tafl.OpenTafl;
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

    private int mArraySize = (1024 * 1024 * 500) / ENTRY_SIZE; // 500 mb, 16 bytes per entry
    private int mRequestedSize = 0;
    private long[] mZobristTable;
    private long[] mDataTable;

    private int mTableQueries = 0;
    private int mTableHits = 0;

    private int mInsertQueries = 0;
    private int mInsertsAdded = 0;

    private boolean mExact;

    /**
     *
     * @param size Size of the table in megabytes.
     */
    public TranspositionTable(int size) {
        this(size, false);
    }

    public TranspositionTable(int size, boolean exact) {
        mRequestedSize = size;
        mArraySize = size * 1024 * 1024 / ENTRY_SIZE;

        mZobristTable = new long[mArraySize];
        mDataTable = new long[mArraySize];
        mExact = exact;
    }

    public int size() {
        return mRequestedSize;
    }

    public void putValue(long zobrist, short value, int dataDepthOfSearch, int gameLength) {
        if(mRequestedSize == 0) return;
        mInsertQueries++;

        int index = (Math.abs((int)(zobrist % mArraySize)));
        boolean add = false;
        if(mZobristTable[index] == 0) add = true;

        if(!add) {
            long data = mDataTable[index];

            byte entryDepthOfSearch = (byte) ((data & DEPTH_MASK) >>> DEPTH_SHIFT);
            char entryAge = (char) ((data & AGE_MASK) >>> AGE_SHIFT);

            if (entryDepthOfSearch <= dataDepthOfSearch || (gameLength - entryAge) >= DISCARD_AFTER_PLIES) {
                add = true;
            }
        }

        if(add) {
            mInsertsAdded++;
            mZobristTable[index] = zobrist;
            mDataTable[index] = value | ((long) gameLength << AGE_SHIFT) | ((long) dataDepthOfSearch << DEPTH_SHIFT);
        }
    }

    public short getValue(long zobrist, int minDepthOfSearch, int gameLength) {
        if(mRequestedSize == 0) return Evaluator.NO_VALUE;
        mTableQueries++;

        int index = (Math.abs((int)(zobrist % mArraySize)));
        if(mZobristTable[index] != zobrist) return Evaluator.NO_VALUE;

        long data = mDataTable[index];
        byte entryDepthOfSearch = (byte)((data & DEPTH_MASK) >>> DEPTH_SHIFT);

        short age = (short)((data & AGE_MASK) >>> AGE_SHIFT);
        if(gameLength - age > DISCARD_AFTER_PLIES) {
            mZobristTable[index] = 0;
            return Evaluator.NO_VALUE;
        }

        if (mExact && entryDepthOfSearch != minDepthOfSearch) return Evaluator.NO_VALUE;
        if (!mExact && entryDepthOfSearch < minDepthOfSearch) return Evaluator.NO_VALUE;
        else {
            mTableHits++;
            return (short)((data & EVAL_MASK));
        }
    }

    public long getData(long zobrist) {
        int index = (Math.abs((int)(zobrist % mArraySize)));
        if(mZobristTable[index] != zobrist) return Evaluator.NO_VALUE;

        return mDataTable[index];
    }

    public String getTableStats() {
        return "Table hits/queries: " + mTableHits + "/" + mTableQueries + " Table inserts/attempts: " + mInsertsAdded + "/" + mInsertQueries;
    }

    public void resetTableStats() {
        mTableHits = mTableQueries = mInsertsAdded = mInsertQueries = 0;
    }
}
