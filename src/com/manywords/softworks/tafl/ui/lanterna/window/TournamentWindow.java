package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;

/**
 * Created by jay on 3/22/16.
 */
public class TournamentWindow extends BasicWindow {
    private AdvancedTerminalHelper.TerminalCallback mTerminalCallback;

    public TournamentWindow(AdvancedTerminalHelper.TerminalCallback callback) {
        mTerminalCallback = callback;
    }

    public AdvancedTerminalHelper.TerminalCallback getTerminalCallback() {
        return mTerminalCallback;
    }

    public void notifyGameFinished() {

    }
}
