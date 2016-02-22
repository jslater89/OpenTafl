package com.manywords.softworks.tafl;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.test.Test;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.SwingWindow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OpenTafl {
    public static void main(String[] args) {
        Map<String, String> mapArgs = getArgs(args);
        boolean window = false;
        boolean advanced = true;

        for (String arg : args) {
            if(arg.contains("--fallback")) {
                window = false;
                advanced = false;
            }
            else if (arg.contains("--debug")) {
                Debug.run(mapArgs);
                System.exit(0);
            } else if (arg.contains("--test")) {
                Test.run();
                System.exit(0);
            } else if (arg.contains("--window")) {
                advanced = false;
                window = true;
            }
        }

        if(advanced) {
            DefaultTerminalFactory factory = new DefaultTerminalFactory();
            Terminal t = null;

            t = factory.createSwingTerminal();

            AdvancedTerminalHelper<? extends Terminal> th = new AdvancedTerminalHelper<>(t);
        }
        else if (window) {
            SwingWindow w = new SwingWindow();
        }
        else {
            RawTerminal display = new RawTerminal();
            display.runUi();
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
