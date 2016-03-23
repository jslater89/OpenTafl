package com.manywords.softworks.tafl.ui.tournament;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
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
    private Game mLastGame = null;

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

    public void notifyGameFinished(Game g) {
        mLastGame = g;
    }

    private void finishTournament() {
        // TODO: write a summary to a file

        mHost.getTerminalCallback().setTournamentWindow(null);
        mHost.getTerminalCallback().onMenuNavigation(mHost);
    }

    private void runTournamentMatch() {
        mCurrentMatch = new MatchResult();

        System.out.println("Running match between " + mFirstEngineFile.getName() + " and " + mSecondEngineFile.getName());
        TerminalSettings.attackerEngineFile = mFirstEngineFile;
        TerminalSettings.defenderEngineFile = mSecondEngineFile;


        // This is a blocking call (sets this as the UI thread), so when it returns, the game
        // is over.
        System.out.println("Running game 1");
        TerminalUtils.startGame(mHost.getTextGUI(), mHost.getTerminalCallback());

        while(mLastGame == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mCurrentMatch.game1 = mLastGame;
        mLastGame = null;
        if(mCurrentMatch.game1.getCurrentState().checkVictory() == GameState.ATTACKER_WIN) {
            System.out.println(mFirstEngineFile.getName() + " wins game 1");
        }
        else if(mCurrentMatch.game1.getCurrentState().checkVictory() == GameState.DEFENDER_WIN) {
            System.out.println(mSecondEngineFile.getName() + " wins game 1");
        }

        TerminalSettings.attackerEngineFile = mSecondEngineFile;
        TerminalSettings.defenderEngineFile = mFirstEngineFile;

        // Same blocking call.
        System.out.println("Running game 2");
        TerminalUtils.startGame(mHost.getTextGUI(), mHost.getTerminalCallback());

        while(mLastGame == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mCurrentMatch.game2 = mLastGame;
        mLastGame = null;
        if(mCurrentMatch.game2.getCurrentState().checkVictory() == GameState.ATTACKER_WIN) {
            System.out.println(mSecondEngineFile.getName() + " wins game 2");
        }
        else if(mCurrentMatch.game2.getCurrentState().checkVictory() == GameState.DEFENDER_WIN) {
            System.out.println(mFirstEngineFile.getName() + " wins game 2");
        }

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
