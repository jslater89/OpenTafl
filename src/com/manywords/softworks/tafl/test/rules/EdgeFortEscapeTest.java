package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
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
        state.makeMove(new MoveRecord(Coord.get(2,5), Coord.get(2,6)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == 0;

        state.makeMove(new MoveRecord(Coord.get(2,0), Coord.get(2,1)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        // This makes it an invincible shape.
        state.makeMove(new MoveRecord(Coord.get(1,4), Coord.get(1,6)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == GameState.DEFENDER_WIN;

        state.makeMove(new MoveRecord(Coord.get(2,1), Coord.get(2,0)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(2,8), Coord.get(2,7)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == GameState.DEFENDER_WIN;
    }

}
