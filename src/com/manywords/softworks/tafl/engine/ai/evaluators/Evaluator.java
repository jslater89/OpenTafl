package com.manywords.softworks.tafl.engine.ai.evaluators;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;

/**
 * Created by jay on 12/29/15.
 */
public interface Evaluator {
    public static final short NO_VALUE = -11541;
    public static final short ATTACKER_WIN = 5050;
    public static final short DEFENDER_WIN = -5050;

    public void initialize(Rules r);
    public short evaluate(GameState state, int maxDepth, int depth);
}
