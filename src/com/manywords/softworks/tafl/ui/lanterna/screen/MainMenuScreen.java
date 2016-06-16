package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.window.mainmenu.MainMenuWindow;

/**
 * Created by jay on 4/2/16.
 */
public class MainMenuScreen extends LogicalScreen {

    private Window mActiveWindow;

    public void setActive(AdvancedTerminal t, WindowBasedTextGUI gui) {
        super.setActive(t, gui);
        mTerminalCallback = new MainMenuTerminalCallback();

        Window mainMenuWindow = new MainMenuWindow(mTerminalCallback);
        mActiveWindow = mainMenuWindow;
        mainMenuWindow.setHints(TerminalThemeConstants.CENTERED);
        mTerminalCallback.onMenuNavigation(mainMenuWindow);
    }

    @Override
    public void onResized(Terminal terminal, TerminalSize terminalSize) {

    }

    @Override
    public void setInactive() {
        super.setInactive();
        if(mActiveWindow != null) {
            mGui.removeWindow(mActiveWindow);
        }
    }

    private class MainMenuTerminalCallback extends DefaultTerminalCallback {
        @Override
        public void onMenuNavigation(Window destination) {
            if(destination == null) {

                /* crashes bash?
                try {
                    mTerminal.exitPrivateMode();
                } catch (IOException e) {
                    // Best effort
                }
                */

                TerminalSettings.saveToFile();
                System.exit(0);
            }

            mGui.removeWindow(mGui.getActiveWindow());
            destination.setHints(TerminalThemeConstants.CENTERED);
            mActiveWindow = destination;
            mGui.addWindowAndWait(destination);
        }

        @Override
        public void setSelfplayWindow(Window tournamentWindow) {
            mTerminal.setSelfplayWindow(tournamentWindow);
        }

        @Override
        public void changeActiveScreen(LogicalScreen screen) {
            OpenTafl.logPrint(OpenTafl.LogLevel.CHATTY, "Changing screen");
            mTerminal.changeActiveScreen(screen);
        }
    }
}
