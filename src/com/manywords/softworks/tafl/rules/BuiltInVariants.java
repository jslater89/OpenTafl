package com.manywords.softworks.tafl.rules;

import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;

import java.util.Arrays;
import java.util.List;

public class BuiltInVariants {
    public static List<Rules> availableRules = Arrays.asList(
            SeaBattle.newSeaBattle9(),
            Fetlar.newFetlar11(),
            Copenhagen.newCopenhagen11(),
            Copenhagen.newCopenhagen11RelaxedShieldwall(),
            Berserk.newBerserk11(),
            Brandub.newBrandub7()
    );

    public static List<String> rulesDescriptions = Arrays.asList(
            "1. Sea Battles 9x9",
            "2. Fetlar 11x11",
            "3. Copenhagen 11x11",
            "4. Copenhagen 11x11 (relaxed shieldwall variant)",
            "5. Berserk 11x11",
            "6. Brandub 7x7"
    );
}
