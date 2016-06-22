package com.manywords.softworks.tafl;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.network.client.HeadlessAIClient;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.test.Benchmark;
import com.manywords.softworks.tafl.test.Test;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.SwingWindow;
import com.manywords.softworks.tafl.command.player.external.engine.ExternalEngineClient;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class OpenTafl {
    private static enum Mode {
        WINDOW,
        ADVANCED_TERMINAL,
        DEBUG,
        TEST,
        EXTERNAL_ENGINE,
        FALLBACK,
        SERVER,
        HEADLESS_AI,
        BENCHMARK,
        HELP
    }

    public static enum LogLevel {
        CHATTY,
        NORMAL,
        SILENT
    }

    public static final String CURRENT_VERSION = "v0.3.2.0b";
    public static final int NETWORK_PROTOCOL_VERSION = 4;

    public static boolean devMode = false;
    public static LogLevel logLevel = LogLevel.NORMAL;

    public static void main(String[] args) {
        Map<String, String> mapArgs = getArgs(args);
        Mode runMode = Mode.ADVANCED_TERMINAL;

        //System.out.println(mapArgs);

        for (String arg : args) {
            if (arg.contains("--server") && runMode == Mode.ADVANCED_TERMINAL) {
                runMode = Mode.SERVER;
            }
            else if (arg.contains("--engine") && runMode == Mode.ADVANCED_TERMINAL) {
                runMode = Mode.EXTERNAL_ENGINE;
            }
            else if (arg.contains("--test") && runMode == Mode.ADVANCED_TERMINAL) {
                runMode = Mode.TEST;
            }
            else if (arg.contains("--window") && runMode == Mode.ADVANCED_TERMINAL) {
                runMode = Mode.WINDOW;
            }
            else if(arg.contains("--fallback") && runMode == Mode.ADVANCED_TERMINAL) {
                runMode = Mode.FALLBACK;
            }
            else if(arg.contains("--headless") && runMode == Mode.ADVANCED_TERMINAL) {
                runMode = Mode.HEADLESS_AI;
            }
            else if(arg.contains("--benchmark") && runMode == Mode.ADVANCED_TERMINAL) {
                logLevel = LogLevel.SILENT;
                runMode = Mode.BENCHMARK;
            }
            else if(arg.contains("--help")) {
                runMode = Mode.HELP;
            }
            else if(arg.contains("--dev") || arg.contains("--debug")) {
                logLevel = LogLevel.CHATTY;
                devMode = true;
            }
            else if (arg.contains("--chatty")) {
                logLevel = LogLevel.CHATTY;
            }
            else if (arg.contains("--silent")) {
                logLevel = LogLevel.SILENT;
            }
        }

        directoryCheck();
        Coord.initialize();
        BuiltInVariants.loadExternalRules(new File("external-rules.conf"));

        switch(runMode) {
            case SERVER:
                // Blocks here
                int threads = 4;
                if(mapArgs.containsKey("threads")) {
                    threads = Integer.parseInt(mapArgs.get("threads"));
                }
                NetworkServer ns = new NetworkServer(threads);
                ns.start();
                break;
            case HEADLESS_AI:
                try {
                    HeadlessAIClient client = HeadlessAIClient.startFromArgs(mapArgs);
                }
                catch(Exception e) {
                    OpenTafl.logPrint(LogLevel.SILENT, "Failed to start headless AI client with error: " + e);
                    OpenTafl.logStackTrace(LogLevel.SILENT, e);
                }
                break;
            case WINDOW:
                SwingWindow w = new SwingWindow();
                break;
            case ADVANCED_TERMINAL:
                DefaultTerminalFactory factory = new DefaultTerminalFactory();
                Terminal t = null;

                t = factory.createSwingTerminal();

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
                RawTerminal display = new RawTerminal();
                display.runUi();
                break;
            case BENCHMARK:
                Benchmark.run();
                break;
            case HELP:
                printHelpMessage();
                break;
        }
    }

    public static Map<String, String> getArgs(String[] args) {
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

    public static void logPrint(LogLevel messageLevel, Object o) {
        if(logLevel == LogLevel.CHATTY) {
            // Incidental messages
            System.out.println(o.toString());
        }
        else if(logLevel == LogLevel.NORMAL && messageLevel != LogLevel.CHATTY) {
            // Normal messages
            System.out.println(o.toString());
        }
        else if(messageLevel == LogLevel.SILENT) {
            // Critical errors which should always be displayed
            System.out.println(o.toString());
        }
    }

    public static void logStackTrace(LogLevel messageLevel, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logPrint(messageLevel, sw.toString());
    }

    private static void directoryCheck() {
        if(!new File("saved-games/replays").exists()) {
            new File("saved-games/replays").mkdirs();
            new File("saved-games/headless-ai").mkdirs();
        }

        if(!new File("saved-games/headless-ai").exists()) {
            new File("saved-games/headless-ai").mkdirs();
        }

        if(!new File("engines").exists()) {
            new File("engines").mkdirs();
        }
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
