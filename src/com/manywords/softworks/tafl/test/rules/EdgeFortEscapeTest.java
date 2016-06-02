package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

public class EdgeFortEscapeTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Copenhagen.newLargeEdgeFortTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());

        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == 0;

        // This is not an invincible shape: the top taflman can be captured
        state.moveTaflman(state.getPieceAt(2, 5), state.getSpaceAt(2, 6));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == 0;

        state.moveTaflman(state.getPieceAt(2, 0), state.getSpaceAt(2, 1));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        // This makes it an invincible shape.
        state.moveTaflman(state.getPieceAt(1, 4), state.getSpaceAt(1, 6));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == GameState.DEFENDER_WIN;

        state.moveTaflman(state.getPieceAt(2, 1), state.getSpaceAt(2, 0));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(2, 8), state.getSpaceAt(2, 7));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == GameState.DEFENDER_WIN;
    }

}
