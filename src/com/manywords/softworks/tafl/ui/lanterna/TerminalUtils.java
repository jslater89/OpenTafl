package com.manywords.softworks.tafl.ui.lanterna;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.ui.lanterna.screen.GameScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineHost;

import java.io.File;

/**
 * Created by jay on 3/22/16.
 */
public class TerminalUtils {
    public static Game startGame(WindowBasedTextGUI gui, LogicalScreen.TerminalCallback callback) {
        if(TerminalSettings.attackers == TerminalSettings.ENGINE && !ExternalEngineHost.validateEngineFile(TerminalSettings.attackerEngineFile)) {
            MessageDialog.showMessageDialog(gui, "Incomplete configuration", "Attacker engine missing configuration file!");
            return null;
        }
        if(TerminalSettings.defenders == TerminalSettings.ENGINE && !ExternalEngineHost.validateEngineFile(TerminalSettings.defenderEngineFile)) {
            MessageDialog.showMessageDialog(gui, "Incomplete configuration", "Defender engine missing configuration file!");
            return null;
        }

        GameClock.TimeSpec ts = TerminalSettings.timeSpec;
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

    /**
     * Creates a playable game from a replay
     *
     * @param rg
     * @param gui
     * @param callback
     */
    public static Game startSavedGame(ReplayGame rg, WindowBasedTextGUI gui, LogicalScreen.TerminalCallback callback) {
        if(TerminalSettings.attackers == TerminalSettings.ENGINE && !ExternalEngineHost.validateEngineFile(TerminalSettings.attackerEngineFile)) {
            MessageDialog.showMessageDialog(gui, "Incomplete configuration", "Attacker engine missing configuration file!");
            return null;
        }
        if(TerminalSettings.defenders == TerminalSettings.ENGINE && !ExternalEngineHost.validateEngineFile(TerminalSettings.defenderEngineFile)) {
            MessageDialog.showMessageDialog(gui, "Incomplete configuration", "Defender engine missing configuration file!");
            return null;
        }

        rg.prepareForGameStart();
        Game g = rg.getGame();

        if(rg.getTimeGuess(true) != null && rg.getTimeGuess(false) != null) {
            GameClock.TimeSpec attackerClock = rg.getTimeGuess(true);
            GameClock.TimeSpec defenderClock = rg.getTimeGuess(false);

            if(g.getClock() == null) {
                g.setClock(new GameClock(g, g.getCurrentState().getAttackers(), g.getCurrentState().getDefenders(), attackerClock));
            }
            g.getClock().getClockEntry(true).setTime(attackerClock);
            g.getClock().getClockEntry(false).setTime(defenderClock);
        }
        else if(TerminalSettings.timeSpec.mainTime != 0 || TerminalSettings.timeSpec.overtimeTime != 0) {
            GameClock clock = new GameClock(g, g.getCurrentState().getAttackers(), g.getCurrentState().getDefenders(), TerminalSettings.timeSpec);

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
}
