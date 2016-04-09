package com.manywords.softworks.tafl.rules;

import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.rules.tablut.Tablut;
import com.manywords.softworks.tafl.rules.tawlbwrdd.Tawlbwrdd;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuiltInVariants {
    public static List<Rules> availableRules = new ArrayList<>(Arrays.asList(
            SeaBattle.newSeaBattle9(),
            Fetlar.newFetlar11(),
            Copenhagen.newCopenhagen11(),
            Copenhagen.newCopenhagen11RelaxedShieldwall(),
            Berserk.newBerserk11(),
            Brandub.newBrandub7(),
            Tawlbwrdd.newTawlbwrdd11(),
            Tablut.newTablut9()
    ));

    public static List<String> rulesDescriptions = new ArrayList<>(Arrays.asList(
            "1. Sea Battles 9x9",
            "2. Fetlar 11x11",
            "3. Copenhagen 11x11",
            "4. Copenhagen 11x11 (relaxed shieldwall variant)",
            "5. Berserk 11x11",
            "6. Brandub 7x7",
            "7. Tawlbwrdd 11x11",
            "8. Tablut 9x9"
    ));

    public static void loadExternalRules(File externalRulesFile) {
        if(!externalRulesFile.exists()) return;

        try {
            BufferedReader r = new BufferedReader(new FileReader(externalRulesFile));

            String line = "";
            while((line = r.readLine()) != null) {
                Rules rules = RulesSerializer.loadRulesRecord(line);
                String description = rulesDescriptions.size() + 1 + ". " + rules.getName() + " " + rules.boardSize + "x" + rules.boardSize;

                availableRules.add(rules);
                rulesDescriptions.add(description);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't read file: not found");
        } catch (IOException e) {
            System.out.println("Couldn't read file: read error");

        }
    }
}
