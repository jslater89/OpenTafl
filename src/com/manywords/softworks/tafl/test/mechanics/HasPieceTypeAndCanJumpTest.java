package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.test.TaflTest;

public class HasPieceTypeAndCanJumpTest extends TaflTest {
    @Override
    public void run() {
        String rulesRecord = "dim:7 name:Guard_Test atkf:y sw:s mj:y cj:n kj:n linc:y starti:/1gGcCnN/1mM4/7/7/7/7/7/";
        Rules rules = null;
        try {
            rules = RulesSerializer.loadRulesRecord(rulesRecord);
        }
        catch (NotationParseException e) {
            assert false;
            return;
        }
        assert rules.getAttackers().hasCommanders();
        assert rules.getDefenders().hasCommanders();

        assert rules.getAttackers().hasKnights();
        assert rules.getDefenders().hasKnights();

        assert rules.getAttackers().hasMercenaries();
        assert rules.getDefenders().hasMercenaries();

        assert rules.getAttackers().hasGuards();
        assert rules.getDefenders().hasGuards();

        assert rules.canSideJump(rules.getAttackers());
        assert rules.canSideJump(rules.getDefenders());

        rulesRecord = "dim:7 name:Guard_Test atkf:y sw:s gj:y mj:n cj:n kj:n linc:y starti:/1gGcCnN/1mM4/7/7/7/7/7/";
        try {
            rules = RulesSerializer.loadRulesRecord(rulesRecord);
        }
        catch (NotationParseException e) {
            assert false;
            return;
        }

        assert rules.canSideJump(rules.getAttackers());
        assert rules.canSideJump(rules.getDefenders());
    }
}
