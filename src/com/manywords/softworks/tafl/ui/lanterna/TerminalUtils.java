package com.manywords.softworks.tafl.ui.lanterna;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.window.BoardWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.CommandWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.StatusWindow;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineHost;

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

        TerminalBoardImage.init(g.getGameRules().getBoard().getBoardDimension());

        BoardWindow bw = new BoardWindow(BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant), g, callback);
        CommandWindow cw = new CommandWindow(g, callback);
        StatusWindow sw = new StatusWindow(g, callback);

        callback.onEnteringGame(g, bw, sw, cw);
        return g;
    }
}
