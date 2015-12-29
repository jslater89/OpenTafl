package com.manywords.softworks.tafl;

import com.manywords.softworks.tafl.test.Test;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.Window;

import java.util.HashMap;
import java.util.Map;

public class OpenTafl {
    public static void main(String[] args) {
        Map<String, String> mapArgs = getArgs(args);
        boolean window = false;

        for (String arg : args) {
            if (arg.contains("--debug")) {
                Debug.run(mapArgs);
                System.exit(0);
            } else if (arg.contains("--test")) {
                Test.run();
                System.exit(0);
            } else if (arg.contains("--window")) {
                window = true;
            }
        }

        if (window) {
            Window w = new Window();
        } else {
            RawTerminal display = new RawTerminal();
            display.runUi();
            System.exit(0);
        }
    }

    public static Map<String, String> getArgs(String[] args) {
        Map<String, String> mapArgs = new HashMap<String, String>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-v")) {
                if (i + 1 < args.length) {
                    mapArgs.put("-v", args[i + 1]);
                }
            }

            if (args[i].equals("-d")) {
                if (i + 1 < args.length) {
                    mapArgs.put("-d", args[i + 1]);
                }
            }
        }

        return mapArgs;
    }
}
