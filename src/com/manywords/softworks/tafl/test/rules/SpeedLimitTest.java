package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.brandub.Magpie;
import com.manywords.softworks.tafl.test.TaflTest;

/**
 * Created by jay on 8/10/16.
 */
public class SpeedLimitTest extends TaflTest {
    @Override
    public void run() {
        Rules r = Magpie.newMagpie7();
        Game g = new Game(r, null);

        GameState s = g.getCurrentState();
        char taflman = s.getPieceAt(0, 3);
        assert Taflman.getAllowableDestinations(s, taflman).size() == 2;
    }
}
