package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.ai.tables.TranspositionTable;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;

class RulesSerializerConsistencyTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Copenhagen.newLargeEdgeFortTest();
        String rules1 = rules.getOTRString();

        rules = RulesSerializer.getRulesForString(rules1);
        String rules2 = rules.getOTRString();

        assert RulesSerializer.rulesEqual(rules1, rules2);
    }

}
