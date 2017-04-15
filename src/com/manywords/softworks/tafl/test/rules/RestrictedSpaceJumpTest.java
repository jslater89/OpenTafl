package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.test.TaflTest;

/**
 * Created by jay on 4/14/17.
 */
public class RestrictedSpaceJumpTest extends TaflTest {
    @Override
    public void run() {
        Rules r = null;
        try {
            r = RulesSerializer.loadRulesRecord("dim:13 name:ParBerserk atkf:n tfr:w ks:m kj:r cen: cenp:TCNK ber:m starti:/4t1ctt4/1t5t5/6c6/13/3tt3N3t/tt3TTT3tt/c1c2T1T1Tc1c/tt1T4T2tt/2t5Tt3/N1t3T1t4/K6c5/t5c4t1/5t1t5/");
        }
        catch (NotationParseException e) {
            assert false;
        }

        Game g = new Game(r, this);

        int result = g.getCurrentState().makeMove(new MoveRecord(Coord.get("a3"), Coord.get("a1")));

        assert result == GameState.DEFENDER_WIN;

        try {
            r = RulesSerializer.loadRulesRecord("dim:13 name:ParBerserk atkf:n tfr:w kj:r ks:m cenp:TCNK ber:m starti:/4ttctt4/5t1t5/6c6/6T6/t3T3N3t/tt3TTT3tt/c1cT1tKT1Tc1c/tt3TTT3tt/t3N3T3t/6T6/6c6/5t1t5/4ttctt4/");
        }
        catch (NotationParseException e) {
            assert false;
        }

        g = new Game(r, this);
        result = g.getCurrentState().makeMove(new MoveRecord(Coord.get("g7"), Coord.get("e7")));

        assert result == GameState.GOOD_MOVE;
    }
}
