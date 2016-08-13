package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.brandub.Magpie;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

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

        assert Taflman.getAllowableDestinations(s, taflman).size() == 4;

        s = g.getCurrentState();
        s.setCurrentSide(s.getDefenders());
        s.makeMove(new MoveRecord(Coord.get(2,3), Coord.get(2, 0)));

        s = g.getCurrentState();
        s.setCurrentSide(s.getDefenders());
        s.makeMove(new MoveRecord(Coord.get(3,3), Coord.get(2, 3)));

        s = g.getCurrentState();
        taflman = s.getPieceAt(2,3);
        assert Taflman.isKing(taflman);
        assert Taflman.getAllowableDestinations(s, taflman).size() == 3;
    }
}
