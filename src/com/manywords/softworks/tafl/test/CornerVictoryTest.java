package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.UiCallback;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;

class CornerVictoryTest extends TaflTest implements UiCallback {

    @Override
    public void gameStateAdvanced() {
        // TODO Auto-generated method stub

    }

    @Override
    public void victoryForSide(Side side) {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        Rules rules = Fetlar.newFetlarTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        state.moveTaflman(state.getPieceAt(0, 2), state.getSpaceAt(0, 0));
        state = game.getCurrentState();

        assert state.checkVictory() == GameState.DEFENDER_WIN;
        assert state.getBoard().getOccupier(5, 5) == Taflman.EMPTY;
    }

}
