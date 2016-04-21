package com.manywords.softworks.tafl;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineClient;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

public class Debug {
    public static void run(Map<String, String> args) {
        if(args.containsKey("--engine")) {
            ExternalEngineClient.run();
        }
        else {
            /*Rules r = Brandub.newBrandub7();
            Game g = new Game(r, null, new GameClock.TimeSpec(300000, 30000, 3, 5000));
            ExternalEnginePlayer p = new ExternalEnginePlayer();
            p.setGame(g);
            p.setupEngine(null);

            ExternalEngineHost h = p.getExternalEngineHost();
            h.start(p.getGame());
            h.clockUpdate(p.getGame().getClock().getClockEntry(g.getCurrentState().getAttackers()), p.getGame().getClock().getClockEntry(g.getCurrentState().getDefenders()));
            h.analyze(5, 30);
            */

            DefaultTerminalFactory factory = new DefaultTerminalFactory();
            Terminal t = null;

            if(args.containsKey("--text-terminal")) {
                factory.setForceTextTerminal(true);
                try {
                    t = factory.createTerminal();
                    System.setOut(TerminalUtils.newDummyPrintStream());
                    System.setErr(TerminalUtils.newDummyPrintStream());
                } catch (IOException e) {
                    System.out.println("Unable to start.");
                }
            }
            else {
                t = factory.createSwingTerminal();
            }

            if (t != null) {
                AdvancedTerminal<? extends Terminal> th = new AdvancedTerminal<>(t);
            }
            else {
                System.out.println("Exiting: no terminal built");
            }
        }
    }
}
