package com.manywords.softworks.tafl.engine.ai.evaluators;

import com.manywords.softworks.tafl.engine.ai.GameTreeState;

/**
 * Created by jay on 12/29/15.
 */
public interface Evaluator {
    public static final short NO_VALUE = -11541;
    public static final short ATTACKER_WIN = 5050;
    public static final short DEFENDER_WIN = -5050;

    public short evaluate(GameTreeState state);
}
