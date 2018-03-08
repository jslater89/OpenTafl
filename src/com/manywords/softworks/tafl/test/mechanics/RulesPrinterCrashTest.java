package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Variants;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.HumanReadableRulesPrinter;

public class RulesPrinterCrashTest extends TaflTest {
    @Override
    public void run() {
        try {
            for(Rules r : Variants.availableRules) {
                HumanReadableRulesPrinter.getHumanReadableRules(r);
            }
        }
        catch(Exception e) {
            assert false;
        }
    }
}
