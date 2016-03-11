package com.manywords.softworks.tafl;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.test.Test;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.SwingWindow;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineClient;

import java.util.HashMap;
import java.util.Map;

public class OpenTafl {
    private static enum Mode {
        WINDOW,
        ADVANCED_TERMINAL,
        DEBUG,
        TEST,
        EXTERNAL_ENGINE,
        FALLBACK
    }
    public static void main(String[] args) {
        Map<String, String> mapArgs = getArgs(args);
        Mode runMode = Mode.ADVANCED_TERMINAL;

        for (String arg : args) {
            if (arg.contains("--engine")) {
                runMode = Mode.EXTERNAL_ENGINE;
            }
            else if (arg.contains("--test")) {
                runMode = Mode.TEST;
            }
            else if (arg.contains("--debug")) {
                runMode = Mode.DEBUG;
            }
            else if (arg.contains("--window")) {
                runMode = Mode.WINDOW;
            }
            else if(arg.contains("--fallback")) {
                runMode = Mode.FALLBACK;
            }
        }

        switch(runMode) {
            case WINDOW:
                SwingWindow w = new SwingWindow();
                break;
            case ADVANCED_TERMINAL:
                DefaultTerminalFactory factory = new DefaultTerminalFactory();
                Terminal t = null;

                t = factory.createSwingTerminal();

                AdvancedTerminalHelper<? extends Terminal> th = new AdvancedTerminalHelper<>(t);
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

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-v")) {
                if (i + 1 < args.length) {
                    mapArgs.put("-v", args[i + 1]);
                    i++;
                }
            }

            else if (args[i].equals("-d")) {
                if (i + 1 < args.length) {
                    mapArgs.put("-d", args[i + 1]);
                    i++;
                }
            }

            else {
                mapArgs.put(args[i], "");
            }
        }

        return mapArgs;
    }
}
