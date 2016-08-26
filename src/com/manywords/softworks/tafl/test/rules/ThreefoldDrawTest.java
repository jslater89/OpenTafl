package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

public class ThreefoldDrawTest extends TaflTest implements UiCallback {

    @Override
    public void run() {
        Rules rules = Fetlar.newFetlar11();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());

        // Target: start position

        // First time at the position
        //RawTerminal.renderGameState(state);
        state.makeMove(new MoveRecord(Coord.get(5,3), Coord.get(4,3)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.DRAW;

        state.makeMove(new MoveRecord(Coord.get(5,1), Coord.get(4,1)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.DRAW;

        state.makeMove(new MoveRecord(Coord.get(4,3), Coord.get(5,3)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.DRAW;

        state.makeMove(new MoveRecord(Coord.get(4,1), Coord.get(5,1)));
        state = game.getCurrentState();
        // Second time at the position
        //RawTerminal.renderGameState(state);
        /*
        for(GameState s : game.getHistory()) {
            System.out.println(s.mZobristHash);
        }
        */


        assert state.checkVictory() != GameState.DRAW;

        state.makeMove(new MoveRecord(Coord.get(5,3), Coord.get(4,3)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.DRAW;

        state.makeMove(new MoveRecord(Coord.get(5,1), Coord.get(4,1)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.DRAW;

        state.makeMove(new MoveRecord(Coord.get(4,3), Coord.get(5,3)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.DRAW;

        state.makeMove(new MoveRecord(Coord.get(4,1), Coord.get(5,1)));
        state = game.getCurrentState();
        // Third time at the position
        //RawTerminal.renderGameState(state);

        /*
        for(GameState s : game.getHistory()) {
            System.out.println(s.mZobristHash);
        }
        */

        assert state.checkVictory() == GameState.DRAW;
    }

}
