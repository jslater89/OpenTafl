package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.engine.GameState;

public class GameTreeState extends GameState {
    public GameTreeState(GameTreeState copyState) {
        super(copyState);
    }

    public GameTreeState(int errorMoveResult) {
        super(errorMoveResult);
    }

    public GameTreeState(GameState copyState) {
        super(copyState);
    }
}
