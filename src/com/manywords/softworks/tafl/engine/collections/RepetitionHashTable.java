package com.manywords.softworks.tafl.engine.collections;

/**
 * Created by jay on 8/22/16.
 */
public class RepetitionHashTable {
    public static final int ARRAY_SIZE = (1024 * 1024 * 5) / 9; // 9 byte entry size: long zobrist, byte count; 10mb total
    private long[] mZobrists;
    private byte[] mRepetitionCounts;

    public RepetitionHashTable() {
        mZobrists = new long[ARRAY_SIZE];
        mRepetitionCounts = new byte[ARRAY_SIZE];
    }

    public RepetitionHashTable(RepetitionHashTable other) {
        mZobrists = new long[ARRAY_SIZE];
        mRepetitionCounts = new byte[ARRAY_SIZE];
        System.arraycopy(other.mZobrists, 0, mZobrists, 0, ARRAY_SIZE);
        System.arraycopy(other.mRepetitionCounts, 0, mRepetitionCounts, 0, ARRAY_SIZE);
    }

    private int getIndex(long zobrist) {
        int index = Math.abs((int) (zobrist % ARRAY_SIZE));
        int iterations = 0;
        while(mZobrists[index] != zobrist && mZobrists[index] != 0) {
            index++; // linear probing!
            iterations++;

            if(index >= ARRAY_SIZE) index = 0;

            if(iterations > 10) throw new IllegalStateException();
        }

        return index;
    }

    public void increment(long zobrist) {
        int i = getIndex(zobrist);
        mZobrists[i] = zobrist;
        mRepetitionCounts[i]++;
    }

    public void decrement(long zobrist) {
        int i = getIndex(zobrist);
        mRepetitionCounts[i]--;
        if(mRepetitionCounts[i] <= 0) mZobrists[i] = 0;
    }

    public int getRepetitionCount(long zobrist) {
        int i = getIndex(zobrist);
        return mRepetitionCounts[i];
    }
}
