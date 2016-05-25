package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.test.TaflTest;

public class EdgeFortEscapeFailedTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Copenhagen.newLargeEdgeFortFailedTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        //RawTerminal.renderGameState(state);
        assert state.checkVictory() == 0;
    }

}
