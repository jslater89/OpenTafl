package com.manywords.softworks.tafl.engine;

import java.util.Random;

/**
 * Created by jay on 12/28/15.
 */
public class XorshiftRandom extends Random {
    private long seed;

    public XorshiftRandom() {
        this.seed = System.currentTimeMillis();
    }
    public XorshiftRandom(long seed) {
        this.seed = seed;
    }

    protected synchronized int next(int nbits) {
        long x = this.seed;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        this.seed = x;
        x &= ((1L << nbits) -1);
        return (int) x;
    }
}
