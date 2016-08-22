package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.rules.tablut.Tablut;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jay on 6/22/16.
 */
public class Benchmark extends TaflTest {
    private static final int TIME = 120;
    private static final int LONG_TEST_FACTOR = 1;

    public static final Map<Integer, Integer> LONG_DEPTH;
    static {
        LONG_DEPTH = new HashMap<>();
        LONG_DEPTH.put(7, 6);
        LONG_DEPTH.put(9, 5);
        LONG_DEPTH.put(11, 4);
        LONG_DEPTH.put(15, 4);
    }

    public static final Map<Integer, Integer> SHORT_DEPTH;
    static {
        SHORT_DEPTH = new HashMap<>();
        SHORT_DEPTH.put(7, 5);
        SHORT_DEPTH.put(9, 4);
        SHORT_DEPTH.put(11, 3);
        SHORT_DEPTH.put(15, 3);
    }


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
        int size = 0, totalSize = 0;
        long start = 0, end = 0, total = 0;

        Map<Integer, Integer> desiredDepths = LONG_DEPTH;

        if(tablut15 == null) {
            throw new IllegalStateException("No tablut 15");
        }

        // BRANDUB -------------------------------

        g = new Game(brandub, null);
        w = new AiWorkspace(this, g, g.getCurrentState(), 10);

        w.setMaxDepth(desiredDepths.get(7));
        w.allowContinuation(false);
        w.allowHorizon(false);
        w.setBenchmarkMode(true);

        start = System.currentTimeMillis();
        //w.explore(TIME);
        end = System.currentTimeMillis();
        //w.printSearchStats();
        //size = w.getGameTreeSize(10);
        totalSize += size;

        OpenTafl.logPrintln(OpenTafl.LogLevel.SILENT, "Brandub 7: finished in " + ((end - start) / 1000d) + " sec, searching " + size + " nodes to depth " + desiredDepths.get(7));

        // TABLUT --------------------------------

        g = new Game(tablut, null);
        w = new AiWorkspace(this, g, g.getCurrentState(), 10);

        w.setMaxDepth(desiredDepths.get(9));
        w.allowContinuation(false);
        w.allowHorizon(false);
        w.setBenchmarkMode(true);

        start = System.currentTimeMillis();
        //w.explore(TIME);
        end = System.currentTimeMillis();
        //w.printSearchStats();
        //size = w.getGameTreeSize(10);
        totalSize += size;

        OpenTafl.logPrintln(OpenTafl.LogLevel.SILENT, "Tablut 9: finished in " + ((end - start) / 1000d) + " sec, searching " + size + " nodes to depth " + desiredDepths.get(9));

        // COPENHAGEN ----------------------------
        g = new Game(copenhagen, null);
        w = new AiWorkspace(this, g, g.getCurrentState(), 10);

        w.setMaxDepth(desiredDepths.get(11));
        w.allowContinuation(false);
        w.allowHorizon(false);
        w.setBenchmarkMode(true);

        start = System.currentTimeMillis();
        w.explore(TIME);
        end = System.currentTimeMillis();
        w.printSearchStats();
        size = w.getGameTreeSize(10);
        totalSize += size;

        OpenTafl.logPrintln(OpenTafl.LogLevel.SILENT, "Copenhagen 11: finished in " + ((end - start) / 1000d) + " sec, searching " + size + " nodes to depth " + desiredDepths.get(11));

        // TABLUT 15 -----------------------------

        g = new Game(tablut15, null);
        w = new AiWorkspace(this, g, g.getCurrentState(), 10);

        w.setMaxDepth(desiredDepths.get(15));
        w.allowContinuation(false);
        w.allowHorizon(false);
        w.setBenchmarkMode(true);

        start = System.currentTimeMillis();
        w.explore(TIME);
        end = System.currentTimeMillis();
        w.printSearchStats();
        size = w.getGameTreeSize(10);
        totalSize += size;

        OpenTafl.logPrintln(OpenTafl.LogLevel.SILENT, "Tablut 15: finished in " + ((end - start) / 1000d) + " sec, searching " + size + " nodes to depth " + desiredDepths.get(15));

//        System.out.println("************************");
//        System.out.println("Final results: " +totalSize + " at " + totalSize / (double) ((TIME * 2) + (TIME * LONG_TEST_FACTOR * 2)) + "/sec");

        System.exit(0);
    }
}
