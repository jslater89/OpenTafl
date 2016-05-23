package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.input.KeyStroke;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

/**
 * Created by jay on 5/23/16.
 */
public class GameDetailWindow extends BasicWindow {
    LogicalScreen.TerminalCallback mTerminalCallback;
    public GameDetailWindow(LogicalScreen.TerminalCallback terminalCallback) {
        super("Game Details");

        mTerminalCallback = terminalCallback;

        Panel p = new Panel();

        setComponent(p);
    }

    public void notifyFocus(boolean focused) {
        if(focused) {
            setTitle("**GAME DETAILS**");
        }
        else {
            setTitle("Game Details");
        }
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handledByScreen = mTerminalCallback.handleKeyStroke(key);
        return handledByScreen || super.handleInput(key);
    }
}
