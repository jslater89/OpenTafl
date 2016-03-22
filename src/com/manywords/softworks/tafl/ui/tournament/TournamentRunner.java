package com.manywords.softworks.tafl.ui.tournament;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.window.TournamentWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 3/22/16.
 */
public class TournamentRunner {
    private File mFirstEngineFile;
    private File mSecondEngineFile;
    private int mMatchCount;
    private GameClock.TimeSpec mGameTimeSpec;
    private List<MatchResult> mMatchResults;
    private TournamentWindow mHost;

    private MatchResult mCurrentMatch;

    public TournamentRunner(TournamentWindow host, int matchCount) {
        mHost = host;
        mMatchCount = matchCount;

        mFirstEngineFile = TerminalSettings.attackerEngineFile;
        mSecondEngineFile = TerminalSettings.defenderEngineFile;
        mGameTimeSpec = TerminalSettings.timeSpec;
        mMatchResults = new ArrayList<>(mMatchCount);
    }

    public void startTournament() {
        mHost.getTerminalCallback().setTournamentWindow(mHost);

        runTournamentMatch();
    }

    private void finishTournament() {
        mHost.getTerminalCallback().setTournamentWindow(null);

        // TODO: write a summary to a file
    }

    private void runTournamentMatch() {
        mCurrentMatch = new MatchResult();

        TerminalSettings.attackerEngineFile = mFirstEngineFile;
        TerminalSettings.defenderEngineFile = mSecondEngineFile;


        // This is a blocking call (sets this as the UI thread), so when it returns, the game
        // is over.
        Game g1 = TerminalUtils.startGame(mHost.getTextGUI(), mHost.getTerminalCallback());
        mCurrentMatch.game1 = g1;

        TerminalSettings.attackerEngineFile = mSecondEngineFile;
        TerminalSettings.defenderEngineFile = mFirstEngineFile;

        // Same blocking call.
        Game g2 = TerminalUtils.startGame(mHost.getTextGUI(), mHost.getTerminalCallback());
        mCurrentMatch.game2 = g2;

        // TODO: work out the match result here, so we don't have to recalculate it every time

        mMatchResults.add(mCurrentMatch);
        if(mMatchResults.size() >= mMatchCount) {
            finishTournament();
        }
        else {
            runTournamentMatch();
        }
    }
}
