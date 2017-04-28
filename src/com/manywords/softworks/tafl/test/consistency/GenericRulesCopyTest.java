package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.GenericRules;
import com.manywords.softworks.tafl.test.TaflTest;

/**
 * Created by jay on 4/15/17.
 */
public class GenericRulesCopyTest extends TaflTest {

    @Override
    public void run() {
        try {
            GenericRules overwrite = (GenericRules) RulesSerializer.loadRulesRecord("dim:9 name:Sea_Battle esc:e atkf:y ka:n nj:n cj:n cor: cen: start:/3ttt3/4t4/4T4/t3T3t/ttTTKTTtt/t3T3t/4T4/4t4/3ttt3/");
            GenericRules tablut = (GenericRules) RulesSerializer.loadRulesRecord("dim:9 name:Tablut esc:e atkf:y ks:c nj:n cor: cens: cenh: start:/3ttt3/4t4/4T4/t3T3t/ttTTKTTtt/t3T3t/4T4/4t4/3ttt3/");

            overwrite.copyNonDimensionalRules(tablut);
            overwrite.copyDimensionalRules(tablut);

            assert RulesSerializer.rulesEqual(RulesSerializer.getRulesRecord(overwrite, true), RulesSerializer.getRulesRecord(tablut, true));

            GenericRules magpie = (GenericRules) RulesSerializer.loadRulesRecord("dim:7 name:Magpie surf:n atkf:y ks:m spd:-1,-1,-1,-1,-1,-1,-1,1, start:/3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/");

            overwrite = GenericRules.copyRules(magpie);

            Log.println(Log.Level.CHATTY, "copied rules: " + RulesSerializer.getRulesRecord(overwrite, true));
            Log.println(Log.Level.CHATTY, "orignl rules: " + RulesSerializer.getRulesRecord(magpie, true));
            assert RulesSerializer.rulesEqual(RulesSerializer.getRulesRecord(overwrite, true), RulesSerializer.getRulesRecord(magpie, true));
        }
        catch (NotationParseException e) {
            e.printStackTrace();
        }
    }
}
