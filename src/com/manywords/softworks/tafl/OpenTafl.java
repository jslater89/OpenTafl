package com.manywords.softworks.tafl;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.manywords.softworks.tafl.command.player.external.engine.ExternalEngineClient;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.network.client.HeadlessAIClient;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.notation.playtaflonline.PlayTaflOnlineJsonTranslator;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Variants;
import com.manywords.softworks.tafl.test.Benchmark;
import com.manywords.softworks.tafl.test.Test;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.SwingWindow;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.manywords.softworks.tafl.Log.Level.CRITICAL;

public class OpenTafl {
    private static enum Mode {
        WINDOW,
        GRAPHICAL_TERMINAL,
        DEBUG,
        TEST,
        EXTERNAL_ENGINE,
        FALLBACK,
        SERVER,
        HEADLESS_AI,
        BENCHMARK,
        READ_JSON, HELP
    }

    public static final String CURRENT_VERSION = "v0.4.6.4b";
    public static final int NETWORK_PROTOCOL_VERSION = 7;

    public static boolean devMode = false;
    private static Mode runMode = Mode.GRAPHICAL_TERMINAL;

    public static void main(String[] args) {
        Map<String, String> mapArgs = getArgs(args);

        //System.out.println(mapArgs);

        for (String arg : args) {
            if (arg.contains("--server") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.SERVER;
            }
            else if (arg.contains("--engine") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.EXTERNAL_ENGINE;
            }
            else if (arg.contains("--test") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.TEST;
                Log.level = CRITICAL;
            }
            else if (arg.contains("--window") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.WINDOW;
            }
            else if(arg.contains("--fallback") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.FALLBACK;
            }
            else if(arg.contains("--headless") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.HEADLESS_AI;
            }
            else if(arg.contains("--benchmark") && runMode == Mode.GRAPHICAL_TERMINAL) {
                Log.level = CRITICAL;
                runMode = Mode.BENCHMARK;
            }
            else if(arg.contains("--pto-json") && runMode == Mode.GRAPHICAL_TERMINAL) {
                runMode = Mode.READ_JSON;
            }
            else if(arg.contains("--help")) {
                runMode = Mode.HELP;
            }
            else if(arg.contains("--dev") || arg.contains("--debug")) {
                Log.level = Log.Level.VERBOSE;
                //runMode = Mode.DEBUG;
                devMode = true;
            }
            else if (arg.contains("--verbose")) {
                Log.level = Log.Level.VERBOSE;
            }
            else if (arg.contains("--normal")) {
                Log.level = Log.Level.NORMAL;
            }
            else if (arg.contains("--silent")) {
                Log.level = CRITICAL;
            }
        }

        directoryCheck();
        setupLogging();
        Coord.initialize();
        Variants.loadExternalRules(new File("external-rules.conf"), new File("user-rules"));
        TerminalSettings.loadFromFile();

        switch(runMode) {
            case SERVER:
                int threads = 4;
                if(mapArgs.containsKey("threads")) {
                    threads = Integer.parseInt(mapArgs.get("threads"));
                }
                NetworkServer ns = new NetworkServer(threads);

                // Blocks here
                ns.start();
                break;
            case HEADLESS_AI:
                try {
                    HeadlessAIClient client = HeadlessAIClient.startFromArgs(mapArgs);
                }
                catch(Exception e) {
                    Log.println(CRITICAL, "Failed to start headless AI client with error: " + e);
                    Log.stackTrace(CRITICAL, e);
                }
                break;
            case WINDOW:
                SwingWindow w = new SwingWindow();
                break;
            case GRAPHICAL_TERMINAL:
                DefaultTerminalFactory factory = new DefaultTerminalFactory();

                Font[] preferredFonts = new Font[7];
                preferredFonts[0] = new Font("Monospace", Font.PLAIN, TerminalSettings.fontSize);
                preferredFonts[1] = new Font("Monaco", Font.PLAIN, TerminalSettings.fontSize);
                preferredFonts[2] = new Font("Courier New", Font.PLAIN, TerminalSettings.fontSize);
                preferredFonts[3] = new Font("Courier", Font.PLAIN, TerminalSettings.fontSize);
                preferredFonts[4] = new Font("Ubuntu Mono", Font.PLAIN, TerminalSettings.fontSize);
                preferredFonts[5] = new Font("Terminus", Font.PLAIN, TerminalSettings.fontSize);
                preferredFonts[6] = new Font("Unifont", Font.PLAIN, TerminalSettings.fontSize);

                preferredFonts = AWTTerminalFontConfiguration.filterMonospaced(preferredFonts);
                if(preferredFonts.length == 0) throw new IllegalStateException("No monospaced fonts available. Try running OpenTafl with the --fallback option to use text-only mode.");

                SwingTerminalFontConfiguration font = SwingTerminalFontConfiguration.newInstance(preferredFonts);

                factory.setTerminalEmulatorFontConfiguration(font);

                Terminal t = factory.createSwingTerminal();
                AdvancedTerminal<? extends Terminal> th = new AdvancedTerminal<>(t);
                break;
            case DEBUG:
                Debug.run(mapArgs);
                break;
            case TEST:
                Test.run();
                break;
            case EXTERNAL_ENGINE:
                ExternalEngineClient.run();
                break;
            case FALLBACK:
                factory = new DefaultTerminalFactory();
                factory.setForceTextTerminal(true);
                try {
                    t = factory.createTerminal();
                    th = new AdvancedTerminal<>(t);
                } catch (IOException e) {
                    Log.println(CRITICAL, "Failed to start text terminal.");
                }

                //RawTerminal display = new RawTerminal();
                //display.runUi();
                break;
            case BENCHMARK:
                new Benchmark().run();
                break;
            case READ_JSON:
                String filename = mapArgs.get("--pto-json");
                Game g = PlayTaflOnlineJsonTranslator.readJsonFile(new File(filename));

                if(g == null) {
                    Log.println(CRITICAL, "Failed to load json file.");
                    System.exit(1);
                }
                else {
                    if(filename.endsWith(".json")) {
                        filename = filename.replaceFirst("\\.json$", ".otg");
                    }
                    else {
                        filename = filename + ".otg";
                    }

                    File f = new File(filename);
                    GameSerializer.writeGameToFile(g, f, false);
                }

                break;
            case HELP:
                printHelpMessage();
                break;
        }
    }

    private static Map<String, String> getArgs(String[] args) {
        Map<String, String> mapArgs = new HashMap<String, String>();

        String argName = "";
        String argBody = "";
        for (String arg : args) {
            if (arg.startsWith("--")) {
                if (!argName.isEmpty()) {
                    mapArgs.put(argName, argBody);
                }

                argName = arg;
                argBody = "";
            }
            else {
                if (argBody.isEmpty()) {
                    argBody = arg;
                }
                else {
                    argBody += " " + arg;
                }
            }
        }

        if(!argName.isEmpty()) {
            mapArgs.put(argName, argBody);
        }

        return mapArgs;
    }

    private static void directoryCheck() {
        if(!new File("saved-games/replays").exists()) {
            new File("saved-games/replays").mkdirs();
            new File("saved-games/headless-ai").mkdirs();
        }

        if(!new File("user-rules").exists()) {
            new File("user-rules").mkdirs();
        }

        if(!new File("saved-games/headless-ai").exists()) {
            new File("saved-games/headless-ai").mkdirs();
        }

        if(!new File("engines").exists()) {
            new File("engines").mkdirs();
        }

        if(!new File("log").exists()) {
            new File("log").mkdirs();
        }
    }

    private static void setupLogging() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Log.println(CRITICAL, "Uncaught exception! This OpenTafl is running as " + runMode);
            Log.println(CRITICAL, "Exception in thread " + t.getName());
            Log.println(CRITICAL, "Exception: " + e);
            Log.stackTrace(CRITICAL, e);
            Log.flushLog();

            System.exit(1);
        });


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Log.unsafeFlushLog();
            }
        });

        Log.setupFile();
    }

    private static void printHelpMessage() {
        System.out.println("OPENTAFL COMMAND USAGE");
        System.out.println("OpenTafl runs in several modes, each with a selection of options:");
        System.out.println();
        System.out.println("<no flags>: standard UI");
        System.out.println("--server: network server mode (runs on port 11541)");
        System.out.println("\t--threads [#]: number of worker threads to spawn");
        System.out.println("--engine: run as external engine");
        System.out.println("--test: run the built-in tests");
        System.out.println("--pto-json [filename]: read a PlayTaflOnline.com JSON game record and output an OpenTafl saved game with the same name.");
        System.out.println("--headless: run as a headless AI client");
        System.out.println("\t--server [address]: server address");
        System.out.println("\t--username [name]: server login username");
        System.out.println("\t--password [pw]: server login password");
        System.out.println("\t--engine [path]: path to the external engine config file to use");
        System.out.println("\t--create: run in game host mode");
        System.out.println("\t\t--rules [#]: rules variant to host (use 1-[number-of-variants] from in-game variant selector)");
        System.out.println("\t\t--clock [maintime-millis]+[overtime-millis]/[overtime-count]+[increment-millis]: set game clock");
        System.out.println("\t\t--side [attackers|defenders]: set the side to play");
        System.out.println("\t\t--game-password [pw]: password for created games");
        System.out.println("\t--join: run in join game mode");
        System.out.println("\t\t--opponent [username]: join game against opponent username");
        System.out.println("\t\t--game-password [pw]: password for game to join");
        System.out.println();
        System.out.println("The following flags apply to all modes: ");
        System.out.println();
        System.out.println("--chatty: print extra debug messages");
        System.out.println("--silent: print no messages");
    }
}
