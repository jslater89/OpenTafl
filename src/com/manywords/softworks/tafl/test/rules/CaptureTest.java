package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

public class CaptureTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = SeaBattle.newSeaBattle9();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        RawTerminal display = new RawTerminal();

        state.moveTaflman(state.getPieceAt(4, 5), state.getSpaceAt(1, 5));
        state = game.getCurrentState();
        state.moveTaflman(state.getPieceAt(3, 4), state.getSpaceAt(3, 1));
        state = game.getCurrentState();
        //display.renderGameState(state);

        //8,5 and 3,8
        state.moveTaflman(state.getPieceAt(3, 8), state.getSpaceAt(3, 2));
        state = game.getCurrentState();
        state.moveTaflman(state.getPieceAt(8, 5), state.getSpaceAt(2, 5));
        state = game.getCurrentState();

        assert state.getPieceAt(3, 1) == Taflman.EMPTY;
        assert state.getPieceAt(1, 5) == Taflman.EMPTY;
    }

}
