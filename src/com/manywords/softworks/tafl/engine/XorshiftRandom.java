package com.manywords.softworks.tafl.engine;

import java.util.Random;

/**
 * Created by jay on 12/28/15.
 */
public class XorshiftRandom extends Random {
    private long seed1;
    private long seed2;

    public XorshiftRandom() { this(System.currentTimeMillis()); }
    public XorshiftRandom(long seed) {
        this.seed1 = seed;
        this.seed2 = ~seed;
    }

    protected synchronized int next(int nbits) {
        long x = this.seed1;
        long y = this.seed2;
        this.seed1 = y;

        x ^= (x << 23); // a
        this.seed2 = x ^ y ^ (x >>> 17) & (y >>> 26); //b, c
        x = seed2 + y;

        x &= ((1L << nbits) -1);
        return (int) x;
    }
}
