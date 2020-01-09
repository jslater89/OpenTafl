package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.test.TaflTest;

public class GuardTest extends TaflTest {
    @SuppressWarnings("AssertWithSideEffects")
    @Override
    public void run() {
        String guardTestRules = "dim:7 name:Guard_Test atkf:y sw:s linc:y starti:/3tT2/1tG3t/3tt2/t1tKG1t/3tt2/1TTG3/1tt4/";
        Rules rules;
        try {
            rules = RulesSerializer.loadRulesRecord(guardTestRules);
        }
        catch (NotationParseException e) {
            assert false;
            return;
        }

        Game g = new Game(rules, null);

        GameState s = g.getCurrentState();
        assert GameState.GOOD_MOVE == s.makeMove(new MoveRecord(Coord.get(6, 5), Coord.get(3, 5)));

        s = g.getCurrentState();
        assert Taflman.isGuard(s.getPieceAt(2, 5));
        s.makeMove(new MoveRecord(Coord.get(2, 5), Coord.get(2, 6)));

        s = g.getCurrentState();
        assert Taflman.getPackedSide(s.getPieceAt(3,6)) == Taflman.SIDE_ATTACKERS;
        s.makeMove(new MoveRecord(Coord.get(6,3), Coord.get(5,3)));

        s = g.getCurrentState();
        assert Taflman.isKing(s.getPieceAt(3, 3));
        assert Taflman.isGuard(s.getPieceAt(4, 3));
        s.makeMove(new MoveRecord(Coord.get(3,1), Coord.get(3,0)));

        s = g.getCurrentState();
        assert Taflman.getPackedSide(s.getPieceAt(2, 0)) == Taflman.SIDE_ATTACKERS;
        assert Taflman.getPackedSide(s.getPieceAt(1, 0)) == Taflman.SIDE_ATTACKERS;
    }
}
