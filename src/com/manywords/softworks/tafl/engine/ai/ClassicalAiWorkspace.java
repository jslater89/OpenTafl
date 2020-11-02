package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.ui.UiCallback;

public abstract class ClassicalAiWorkspace extends AbstractAiWorkspace {
    public ClassicalAiWorkspace(UiCallback ui, Game startingGame, GameState startingState) {
        super(ui, startingGame, startingState);
    }
}
