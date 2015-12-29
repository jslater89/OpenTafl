package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.ui.RawTerminal;

public class DoubleCaptureTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = SeaBattle.newSeaBattle9();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        RawTerminal display = new RawTerminal();

		/*
		 * 1 4 -> 1 2
		 * 3 4 -> 3 1
		 * 8 3 -> 8 2
		 * 4 2 -> 2 2
		 * 3 8 -> 3 2
		 */

        state.moveTaflman(state.getPieceAt(1, 4), state.getSpaceAt(1, 2));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(3, 4), state.getSpaceAt(3, 1));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(8, 3), state.getSpaceAt(8, 2));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(4, 2), state.getSpaceAt(2, 2));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(3, 8), state.getSpaceAt(3, 2));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert state.getPieceAt(3, 1) == Taflman.EMPTY;
        assert state.getPieceAt(2, 2) == Taflman.EMPTY;
    }

}
