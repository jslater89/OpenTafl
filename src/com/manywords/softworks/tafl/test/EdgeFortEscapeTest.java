package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;

class EdgeFortEscapeTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Copenhagen.newLargeEdgeFortTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == 0;


        state.moveTaflman(state.getPieceAt(2, 5), state.getSpaceAt(2, 6));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == GameState.DEFENDER_WIN;

        state.moveTaflman(state.getPieceAt(2, 0), state.getSpaceAt(2, 1));
        state = game.getCurrentState();

        state.moveTaflman(state.getPieceAt(2, 8), state.getSpaceAt(2, 7));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == GameState.DEFENDER_WIN;
    }

}
