package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

public class CornerVictoryTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Fetlar.newFetlarTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());

        state.makeMove(new MoveRecord(Coord.get(0,2), Coord.get(0,0)));
        state = game.getCurrentState();

        assert state.checkVictory() == GameState.DEFENDER_WIN;
        assert state.getBoard().getOccupier(5, 5) == Taflman.EMPTY;
    }

}
