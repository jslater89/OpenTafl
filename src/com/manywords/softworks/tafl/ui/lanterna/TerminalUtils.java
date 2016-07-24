package com.manywords.softworks.tafl.ui.lanterna;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.ui.lanterna.screen.GameScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;

import java.io.*;
import java.util.List;

/**
 * Created by jay on 3/22/16.
 */
public class TerminalUtils {
    public static void runOnUiThread(WindowBasedTextGUI gui, Runnable task) {
        if(gui == null || Thread.currentThread().equals(gui.getGUIThread().getThread())) {
            task.run();
        }
        else {
            gui.getGUIThread().invokeLater(task);
        }
    }

    public static Game startGame(WindowBasedTextGUI gui, LogicalScreen.TerminalCallback callback) {
        if(TerminalSettings.attackers == TerminalSettings.ENGINE && TerminalSettings.attackerEngineSpec == null) {
            MessageDialog.showMessageDialog(gui, "Incomplete configuration", "Attacker engine missing configuration file!");
            return null;
        }
        if(TerminalSettings.defenders == TerminalSettings.ENGINE && TerminalSettings.defenderEngineSpec == null) {
            MessageDialog.showMessageDialog(gui, "Incomplete configuration", "Defender engine missing configuration file!");
            return null;
        }

        TimeSpec ts = TerminalSettings.timeSpec;
        Game g;
        if(ts.mainTime == 0 && (ts.overtimeTime == 0 ||ts.overtimeCount == 0)) {
            g = new Game(BuiltInVariants.availableRules.get(TerminalSettings.variant), callback.getUiCallback());
        }
        else {
            g = new Game(BuiltInVariants.availableRules.get(TerminalSettings.variant), callback.getUiCallback(), ts);
        }

        // Blocks here
        GameScreen gameScreen = new GameScreen(g, BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant));
        callback.changeActiveScreen(gameScreen);
        return g;
    }

    public static Game startNetworkGame(
            WindowBasedTextGUI gui,
            LogicalScreen.TerminalCallback callback,
            ClientServerConnection connection,
            Rules rules,
            TimeSpec timeSpec,
            List<DetailedMoveRecord> history) {

        Game g;
        if(timeSpec == null || timeSpec.mainTime == 0 && (timeSpec.overtimeTime == 0 || timeSpec.overtimeCount == 0)) {
            g = new Game(rules, callback.getUiCallback());
        }
        else {
            g = new Game(rules, callback.getUiCallback(), timeSpec);
        }

        GameScreen gameScreen = new GameScreen(g, rules.toString());
        gameScreen.setHistory(history);
        gameScreen.setServerConnection(connection);
        callback.changeActiveScreen(gameScreen);
        return g;
    }

    /**
     * Creates a playable game from a replay
     *
     * @param rg
     * @param gui
     * @param callback
     */
    public static Game startSavedGame(ReplayGame rg, WindowBasedTextGUI gui, LogicalScreen.TerminalCallback callback) {
        if(TerminalSettings.attackers == TerminalSettings.ENGINE && TerminalSettings.attackerEngineSpec == null) {
            MessageDialog.showMessageDialog(gui, "Incomplete configuration", "Attacker engine missing configuration file!");
            return null;
        }
        if(TerminalSettings.defenders == TerminalSettings.ENGINE && TerminalSettings.defenderEngineSpec == null) {
            MessageDialog.showMessageDialog(gui, "Incomplete configuration", "Defender engine missing configuration file!");
            return null;
        }

        rg.prepareForGameStart();
        Game g = rg.getGame();

        if(g.getClock() == null && (TerminalSettings.timeSpec.mainTime != 0 || TerminalSettings.timeSpec.overtimeTime != 0)) {
            GameClock clock = new GameClock(g, TerminalSettings.timeSpec);

            g.setClock(clock);
        }

        if(g.getCurrentState().checkVictory() > 0) {
            MessageDialog.showMessageDialog(gui, "Game already ended", "This game record has already finished.\n\nTry loading it as a replay and using the 'play-here'\ncommand where you would like to take control.");
            return null;
        }

        GameScreen gameScreen = new GameScreen(g, "OpenTafl");
        callback.changeActiveScreen(gameScreen);
        return g;
    }

    public static void startReplay(ReplayGame rg, WindowBasedTextGUI gui, LogicalScreen.TerminalCallback callback) {
        GameScreen gameScreen = new GameScreen(rg, "OpenTafl");
        callback.changeActiveScreen(gameScreen);
    }

    public static File showFileChooserDialog(WindowBasedTextGUI gui, String title, String actionLabel, File directory) {
        if(!directory.exists()) return null;

        FileDialogBuilder builder = new FileDialogBuilder();
        builder.setSelectedFile(directory);
        builder.setTitle(title);
        builder.setActionLabel(actionLabel);
        return builder.build().showDialog(gui);
    }

    public static PrintStream newDummyPrintStream() {
        PrintStream ps = new PrintStream(new BufferedOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }));

        return ps;
    }
}
