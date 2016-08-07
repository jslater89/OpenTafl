package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.test.TaflTest;
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

        state.makeMove(new MoveRecord(Coord.get(1,4), Coord.get(1,2)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(3,4), Coord.get(3,1)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(8,3), Coord.get(8,2)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(4,2), Coord.get(2,2)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(3,8), Coord.get(3,2)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert state.getPieceAt(3, 1) == Taflman.EMPTY;
        assert state.getPieceAt(2, 2) == Taflman.EMPTY;
    }

}
