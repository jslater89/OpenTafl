package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.rules.Side;

public interface UiCallback {
    public void gameStateAdvanced();

    public void victoryForSide(Side side);
}
