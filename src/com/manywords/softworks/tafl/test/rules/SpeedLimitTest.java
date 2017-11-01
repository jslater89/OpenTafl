package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.notation.TaflmanCodes;
import com.manywords.softworks.tafl.rules.Coord;
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

        try {
            r = RulesSerializer.loadRulesRecord("dim:7 name:Magpie surf:n atkf:y ks:m spd:-1,-1,-1,-1,-1,-1,-1,1, starti:/3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/");
        }
        catch (NotationParseException e) {
            assert false;
        }

        assert r.getTaflmanSpeedLimit(Taflman.ALL_TAFLMAN_TYPES[TaflmanCodes.K]) == 1;
    }
}
