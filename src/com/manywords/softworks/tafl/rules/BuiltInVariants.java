package com.manywords.softworks.tafl.rules;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.rules.tablut.FotevikenTablut;
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
            Tablut.newTablut9(),
            FotevikenTablut.newFotevikenTablut9()
    ));

    public static List<String> rulesDescriptions = new ArrayList<>(Arrays.asList(
            "1. Sea Battles 9x9",
            "2. Fetlar 11x11",
            "3. Copenhagen 11x11",
            "4. Copenhagen 11x11 (relaxed shieldwall variant)",
            "5. Berserk 11x11",
            "6. Brandub 7x7",
            "7. Tawlbwrdd 11x11",
            "8. Tablut 9x9",
            "9. Foteviken Tablut 9x9"
    ));

    public static Rules rulesForNameAndDimension(String name, int dimension) {
        for(Rules r : availableRules) {
            if(r.getName().equals(name) && r.boardSize == dimension) return r;
        }
        return null;
    }

    public static int indexForDescription(String description) {
        for(int i = 0; i < rulesDescriptions.size(); i++) {
            if(description.equals(rulesDescriptions.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public static Rules rulesForDescription(String description) {
        int i = indexForDescription(description);
        if(i != -1) {
            return availableRules.get(i);
        }
        else {
            return null;
        }
    }

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
            OpenTafl.logPrint(OpenTafl.LogLevel.NORMAL, "Couldn't read file: not found");
        } catch (IOException e) {
            OpenTafl.logPrint(OpenTafl.LogLevel.NORMAL, "Couldn't read file: read error");
        }

        if(OpenTafl.devMode) {
            dumpRules();
        }
    }

    private static void dumpRules() {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(new File("rules-dump.otr")));

            for(Rules r : availableRules) {
                String rulesString = RulesSerializer.getRulesRecord(r);
                w.write(rulesString + "\n");
            }

            w.flush();
            w.close();
        } catch (IOException e) {
            OpenTafl.logStackTrace(OpenTafl.LogLevel.NORMAL, e);
        }
    }
}
