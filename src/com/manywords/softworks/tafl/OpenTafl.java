package com.manywords.softworks.tafl;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.network.client.HeadlessAIClient;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.test.Test;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.SwingWindow;
import com.manywords.softworks.tafl.command.player.external.engine.ExternalEngineClient;

import java.io.File;
import java.util.Arrays;
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
        HEADLESS_AI
    }

    public static boolean DEV_MODE = false;
    public static final String CURRENT_VERSION = "v0.3.2.0b";
    public static final int NETWORK_PROTOCOL_VERSION = 4;

    public static void main(String[] args) {
        Map<String, String> mapArgs = getArgs(args);
        Mode runMode = Mode.ADVANCED_TERMINAL;

        System.out.println(mapArgs);

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
            else if (arg.contains("--debug") && runMode == Mode.ADVANCED_TERMINAL) {
                //runMode = Mode.DEBUG;
            }
            else if (arg.contains("--window") && runMode == Mode.ADVANCED_TERMINAL) {
                runMode = Mode.WINDOW;
            }
            else if(arg.contains("--fallback") && runMode == Mode.ADVANCED_TERMINAL) {
                runMode = Mode.FALLBACK;
            }
            else if(arg.contains("--dev")) {
                DEV_MODE = true;
            }
            else if(arg.contains("--headless") && runMode == Mode.ADVANCED_TERMINAL) {
                runMode = Mode.HEADLESS_AI;
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
                    System.out.println("Failed to start headless AI client with error: " + e);
                    e.printStackTrace(System.out);
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
}
