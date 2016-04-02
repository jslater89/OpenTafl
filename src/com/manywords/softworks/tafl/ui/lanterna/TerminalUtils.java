package com.manywords.softworks.tafl.ui.lanterna;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.window.BoardWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.CommandWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.StatusWindow;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineHost;

import java.io.File;

/**
 * Created by jay on 3/22/16.
 */
public class TerminalUtils {
    public static Game startGame(WindowBasedTextGUI gui, AdvancedTerminalHelper.TerminalCallback callback) {
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
        callback.onEnteringScreen(g, BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant));
        return g;
    }

    /**
     * Creates a playable game from a replay
     *
     * @param rg
     * @param gui
     * @param callback
     */
    public static Game startSavedGame(ReplayGame rg, WindowBasedTextGUI gui, AdvancedTerminalHelper.TerminalCallback callback) {
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

        if(g.getCurrentState().checkVictory() > 0) {
            MessageDialog.showMessageDialog(gui, "Game already ended", "This game record has already finished.\n\nTry loading it as a replay and using the 'play-here'\ncommand where you would like to take control.");
            return null;
        }

        callback.onEnteringScreen(g, "OpenTafl");
        return g;
    }

    public static void startReplay(ReplayGame rg, WindowBasedTextGUI gui, AdvancedTerminalHelper.TerminalCallback callback) {
        callback.onEnteringScreen(rg, "OpenTafl");
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
