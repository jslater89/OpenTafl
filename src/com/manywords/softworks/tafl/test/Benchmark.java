package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.rules.tablut.Tablut;

/**
 * Created by jay on 6/22/16.
 */
public class Benchmark {
    public static void run() {
        Rules brandub = Brandub.newBrandub7();
        Rules tablut = Tablut.newTablut9();
        Rules copenhagen = Copenhagen.newCopenhagen11();
        Rules tablut15 = BuiltInVariants.rulesForNameAndDimension("Tablut", 15);
        Game g;
        AiWorkspace w;
        int size, totalSize = 0;

        assert tablut15 != null;

        g = new Game(brandub, null);
        w = new AiWorkspace(null, g, g.getCurrentState(), 0);
        w.explore(30);
        size = w.getGameTreeSize(30);
        totalSize += size;

        System.out.println("Brandub 7: " + size + " at " + size / 30d + "/sec");

        g = new Game(tablut, null);
        w = new AiWorkspace(null, g, g.getCurrentState(), 0);
        w.explore(30);
        size = w.getGameTreeSize(30);
        totalSize += size;

        System.out.println("Tablut 9: " + size + " at " + size / 30d + "/sec");

        g = new Game(copenhagen, null);
        w = new AiWorkspace(null, g, g.getCurrentState(), 0);
        w.explore(60);
        size = w.getGameTreeSize(30);
        totalSize += size;

        System.out.println("Copenhagen 11: " + size + " at " + size / 60d + "/sec");

        g = new Game(tablut15, null);
        w = new AiWorkspace(null, g, g.getCurrentState(), 0);
        w.explore(60);
        size = w.getGameTreeSize(30);
        totalSize += size;

        System.out.println("Tablut 15: " + size + " at " + size / 60d + "/sec");

        System.out.println("************************");
        System.out.println("Final results: " +totalSize + " at " + totalSize / 180d + "/sec");

        System.exit(0);
    }
}
