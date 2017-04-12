package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.lanterna.window.selfplay.SelfplayWindow;

/**
 * Created by jay on 4/2/16.
 */
public abstract class LogicalScreen {
    static final int FOCUS_FORWARD = 0;
    static final int FOCUS_BACKWARD = 1;

    public interface TerminalCallback {
        public void onMenuNavigation(Window destination);
        public void changeActiveScreen(LogicalScreen screen);
        public void onEnteringGameScreen(Game g, String title);
        public void onEnteringGameScreen(ReplayGame rg, String title);
        public void onEnteringReplay(ReplayGame rg);
        public void onEnteringGame(Game g);
        public void handleInGameCommand(String command);
        public boolean handleKeyStroke(KeyStroke key);
        public UiCallback getUiCallback();
        public void setSelfplayWindow(Window tournamentWindow);
    }

    protected TerminalCallback mTerminalCallback = new TrueDefaultTerminalCallback();
    protected WindowBasedTextGUI mGui;
    protected AdvancedTerminal mTerminal;

    protected SelfplayWindow mSelfplayWindow;

    public void setSelfplayWindow(SelfplayWindow selfplayWindow) {
        mSelfplayWindow = selfplayWindow;
    }

    public void setActive(AdvancedTerminal t, WindowBasedTextGUI gui) {
        mTerminal = t;
        mGui = gui;
    }

    public void setInactive() {

    }

    public abstract void onResized(Terminal terminal, TerminalSize terminalSize);

    public TerminalCallback getTerminalCallback() {
        return mTerminalCallback;
    }

    private static class TrueDefaultTerminalCallback extends DefaultTerminalCallback {
        @Override
        public void changeActiveScreen(LogicalScreen screen) {
            // no-op--here to prevent NPEs
        }
    }

    public abstract static class DefaultTerminalCallback implements TerminalCallback {

        @Override
        public void onMenuNavigation(Window destination) {

        }

        @Override
        public abstract void changeActiveScreen(LogicalScreen screen);

        @Override
        public void onEnteringGameScreen(Game g, String title) {

        }

        @Override
        public void onEnteringGameScreen(ReplayGame rg, String title) {

        }

        @Override
        public void onEnteringReplay(ReplayGame rg) {

        }

        @Override
        public void onEnteringGame(Game g) {

        }

        @Override
        public void handleInGameCommand(String command) {

        }

        @Override
        public boolean handleKeyStroke(KeyStroke key) {
            return false;
        }

        @Override
        public UiCallback getUiCallback() {
            return null;
        }

        @Override
        public void setSelfplayWindow(Window tournamentWindow) {

        }
    }
}
