package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;

class EdgeVictoryTest extends TaflTest implements UiCallback {

    @Override
    public void gameStateAdvanced() {
        // TODO Auto-generated method stub

    }

    @Override
    public void victoryForSide(Side side) {
        // TODO Auto-generated method stub

    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
    }

    @Override
    public void run() {
        Rules rules = SeaBattle.newStrongKingTest();
        Game game = new Game(rules, null);

        GameState state = game.getCurrentState();
        state.moveTaflman(state.getPieceAt(4, 5), state.getSpaceAt(4, 8));

        state = game.getCurrentState();
        state.moveTaflman(state.getPieceAt(4, 4), state.getSpaceAt(4, 7));

        state = game.getCurrentState();
        state.moveTaflman(state.getPieceAt(4, 8), state.getSpaceAt(5, 8));

        state = game.getCurrentState();
        state.moveTaflman(state.getPieceAt(4, 7), state.getSpaceAt(0, 7));

        state = game.getCurrentState();
        assert state.checkVictory() == GameState.DEFENDER_WIN;

        assert state.getBoard().getOccupier(4, 4) == Taflman.EMPTY;
    }

}
