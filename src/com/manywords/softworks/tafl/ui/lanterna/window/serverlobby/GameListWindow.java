package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

/**
 * Created by jay on 5/23/16.
 */
public class GameListWindow extends BasicWindow {
    public interface GameListWindowHost {

    }
    LogicalScreen.TerminalCallback mTerminalCallback;
    GameListWindowHost mHost;

    public GameListWindow(LogicalScreen.TerminalCallback terminalCallback, GameListWindowHost host) {
        super("Game List");

        mTerminalCallback = terminalCallback;
        mHost = host;
        Panel p = new Panel();

        setComponent(p);
    }

    public void notifyFocus(boolean focused) {
        if(focused) {
            setTitle("**GAME LIST**");
        }
        else {
            setTitle("Game List");
        }
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handledByScreen = mTerminalCallback.handleKeyStroke(key);
        return handledByScreen || super.handleInput(key);
    }
}
