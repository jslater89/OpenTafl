package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

public class ThreefoldVictoryTest extends TaflTest implements UiCallback {

    @Override
    public void run() {
        Rules rules = Berserk.newBerserk11();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        // Target: start position

        // First time at the position
        //RawTerminal.renderGameState(state);
        state.makeMove(new MoveRecord(Coord.get(5,3), Coord.get(4,3)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.ATTACKER_WIN;

        state.makeMove(new MoveRecord(Coord.get(5,1), Coord.get(4,1)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.ATTACKER_WIN;

        state.makeMove(new MoveRecord(Coord.get(4,3), Coord.get(5,3)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.ATTACKER_WIN;

        state.makeMove(new MoveRecord(Coord.get(4,1), Coord.get(5,1)));
        state = game.getCurrentState();
        // Second time at the position
        //RawTerminal.renderGameState(state);

        assert state.checkVictory() != GameState.ATTACKER_WIN;

        state.makeMove(new MoveRecord(Coord.get(5,3), Coord.get(4,3)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.ATTACKER_WIN;

        state.makeMove(new MoveRecord(Coord.get(5,1), Coord.get(4,1)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.ATTACKER_WIN;

        state.makeMove(new MoveRecord(Coord.get(4,3), Coord.get(5,3)));
        state = game.getCurrentState();

        assert state.checkVictory() != GameState.ATTACKER_WIN;

        state.makeMove(new MoveRecord(Coord.get(4,1), Coord.get(5,1)));
        state = game.getCurrentState();
        // Third time at the position
        //RawTerminal.renderGameState(state);

        // In berserk, the player forcing the repetition loses.
        assert state.checkVictory() == GameState.ATTACKER_WIN;
    }

}
