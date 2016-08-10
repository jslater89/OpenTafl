package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
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

        //System.out.println(rules1);
        //System.out.println(rules2);
        assert RulesSerializer.rulesEqual(rules1, rules2);
    }

}
