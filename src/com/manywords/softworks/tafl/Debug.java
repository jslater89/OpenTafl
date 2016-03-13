package com.manywords.softworks.tafl;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.player.ExternalEnginePlayer;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineClient;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineHost;

import java.io.IOException;
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
