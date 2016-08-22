package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.OpenTafl;
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
public class Benchmark extends TaflTest {
    private static final int TIME = 30;
    private static final int LONG_TEST_FACTOR = 2;

    @Override
    public void statusText(String text) {
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, text);
    }

    public void run() {
        Rules brandub = Brandub.newBrandub7();
        Rules tablut = Tablut.newTablut9();
        Rules copenhagen = Copenhagen.newCopenhagen11();
        Rules tablut15 = BuiltInVariants.rulesForNameAndDimension("Tablut", 15);
        Game g;
        AiWorkspace w;
        int size, totalSize = 0;

        if(tablut15 == null) {
            throw new IllegalStateException("No tablut 15");
        }

        g = new Game(brandub, null);
        w = new AiWorkspace(this, g, g.getCurrentState(), 10);
        w.setBenchmarkMode(true);
        w.explore(TIME);
        w.printSearchStats();
        size = w.getGameTreeSize(30);
        totalSize += size;

        System.out.println("Brandub 7: " + size + " at " + size / (double) TIME + "/sec");

        g = new Game(tablut, null);
        w = new AiWorkspace(this, g, g.getCurrentState(), 10);
        w.setBenchmarkMode(true);
        w.explore(TIME);
        w.printSearchStats();
        size = w.getGameTreeSize(30);
        totalSize += size;

        System.out.println("Tablut 9: " + size + " at " + size / (double) TIME + "/sec");

        g = new Game(copenhagen, null);
        w = new AiWorkspace(this, g, g.getCurrentState(), 10);
        w.setBenchmarkMode(true);
        w.explore(TIME * LONG_TEST_FACTOR);
        w.printSearchStats();
        size = w.getGameTreeSize(30);
        totalSize += size;

        System.out.println("Copenhagen 11: " + size + " at " + size / (double) (TIME * LONG_TEST_FACTOR) + "/sec");

        g = new Game(tablut15, null);
        w = new AiWorkspace(this, g, g.getCurrentState(), 10);
        w.setBenchmarkMode(true);
        w.explore(TIME * LONG_TEST_FACTOR);
        w.printSearchStats();
        size = w.getGameTreeSize(30);
        totalSize += size;

        System.out.println("Tablut 15: " + size + " at " + size / (double) (TIME * LONG_TEST_FACTOR) + "/sec");

        System.out.println("************************");
        System.out.println("Final results: " +totalSize + " at " + totalSize / (double) ((TIME * 2) + (TIME * LONG_TEST_FACTOR * 2)) + "/sec");

        System.exit(0);
    }
}
