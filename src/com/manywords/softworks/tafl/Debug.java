package com.manywords.softworks.tafl;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.player.ExternalEnginePlayer;
import com.manywords.softworks.tafl.ui.player.external.ExternalEngineClient;

import java.io.IOException;
import java.util.Map;

public class Debug {
    public static void run(Map<String, String> args) {
        if(args.containsKey("--engine")) {
            ExternalEngineClient.run();
        }
        else {
            ExternalEnginePlayer p = new ExternalEnginePlayer();
            p.setupEngine(null);

            DefaultTerminalFactory factory = new DefaultTerminalFactory();
            Terminal t = null;

            if (args.size() == 1) {
                t = factory.createSwingTerminal();
            }
            else {
                factory.setForceTextTerminal(true);
                try {
                    t = factory.createTerminal();
                } catch (IOException e) {
                    System.out.println("Unable to start.");
                }
            }

            if (t != null) {
                AdvancedTerminalHelper<? extends Terminal> th = new AdvancedTerminalHelper<>(t);
            }
            else {
                System.out.println("Exiting");
            }
        }
    }
}
