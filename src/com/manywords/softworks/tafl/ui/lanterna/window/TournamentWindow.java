package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.tournament.TournamentRunner;

/**
 * Created by jay on 3/22/16.
 */
public class TournamentWindow extends BasicWindow {
    private AdvancedTerminalHelper.TerminalCallback mTerminalCallback;
    private TournamentRunner mRunner;

    public TournamentWindow(AdvancedTerminalHelper.TerminalCallback callback) {
        mTerminalCallback = callback;
        mRunner = new TournamentRunner(this, 1);

        mRunner.startTournament();
    }

    public TournamentRunner getRunner() {
        return mRunner;
    }

    public AdvancedTerminalHelper.TerminalCallback getTerminalCallback() {
        return mTerminalCallback;
    }

    public void notifyGameFinished(Game g) {
        mRunner.notifyGameFinished(g);
    }
}
