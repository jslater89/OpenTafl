package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.notation.TaflmanCodes;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.rules.brandub.Magpie;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.test.TaflTest;

public class RulesSerializerConsistencyTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Copenhagen.newLargeEdgeFortTest();
        String rules1 = rules.getOTRString();

        rules = RulesSerializer.loadRulesRecord(rules1);
        String rules2 = rules.getOTRString();

        //System.out.println(rules1);
        //System.out.println(rules2);
        assert RulesSerializer.rulesEqual(rules1, rules2);

        rules = Magpie.newMagpie7();
        rules1 = rules.getOTRString();

        rules = RulesSerializer.loadRulesRecord(rules1);
        rules2 = rules.getOTRString();

        assert RulesSerializer.rulesEqual(rules1, rules2);
        assert rules.getTaflmanSpeedLimit(Taflman.ALL_TAFLMAN_TYPES[TaflmanCodes.K]) == 1;
        assert rules.getTaflmanSpeedLimit(Taflman.ALL_TAFLMAN_TYPES[TaflmanCodes.t]) == -1;

        rules = Berserk.newBerserk11();
        rules1 = rules.getOTRString();
        rules1 += " ks:m";

        rules = RulesSerializer.loadRulesRecord(rules1);
        rules2 = rules.getOTRString();

        assert RulesSerializer.rulesEqual(rules1, rules2);
    }

}
