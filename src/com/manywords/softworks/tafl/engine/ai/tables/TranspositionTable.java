package com.manywords.softworks.tafl.engine.ai.tables;

import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;

/**
 * Created by jay on 12/16/15.
 */
public class TranspositionTable {
    private static final int DISCARD_AFTER_PLIES = 10;
    private static final int ENTRY_SIZE = 32; // observed for a 64-bit JVM
    //  8 bytes overhead
    //  8 bytes zobrist
    //  2 bytes value
    //  1 byte  depth
    //+ 2 bytes age
    //---------------
    // 21 bytes total
    public class Entry {
        public long zobrist;
        public short value;
        public byte searchedToDepth = -1;
        public char age = 0;

        public String toString() {
            return value + " at depth " + searchedToDepth;
        }
    }

    private int mMaxSize = (1024 * 1024 * 500) / ENTRY_SIZE; // 500 mb, 20 bytes per entry
    private int mRequestedSize = 0;
    private Entry[] mTranspositionTable;

    /**
     *
     * @param size Size of the table in megabytes.
     */
    public TranspositionTable(int size) {
        mRequestedSize = size;
        mMaxSize = size * 1024 * 1024 / ENTRY_SIZE;

        mTranspositionTable = new Entry[mMaxSize];
        for(int i = 0; i < mMaxSize; i++) {
            mTranspositionTable[i] = new Entry();
        }
    }

    public int size() {
        return mRequestedSize;
    }

    public void putValue(long zobrist, short value, int searchedToDepth, int gameLength) {
        Entry e = mTranspositionTable[Math.abs((int)(zobrist % mMaxSize))];
        if (e.searchedToDepth < searchedToDepth || (gameLength - e.age) >= DISCARD_AFTER_PLIES) {
            e.zobrist = zobrist;
            e.value = value;
            e.searchedToDepth = (byte) searchedToDepth;
            e.age = (char) gameLength;
        }
    }

    public short getValue(long zobrist, int minDepth, int gameLength) {
        Entry e = mTranspositionTable[Math.abs((int)(zobrist % mMaxSize))];
        if (e.searchedToDepth < minDepth) return Evaluator.NO_VALUE;
        else if (e.zobrist != zobrist) return Evaluator.NO_VALUE;
        else {
            return e.value;
        }
    }
}
