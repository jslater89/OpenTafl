package com.manywords.softworks.tafl.ui.selfplay;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.window.selfplay.SelfplayResultWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.selfplay.SelfplayWindow;
import com.manywords.softworks.tafl.command.player.external.engine.EngineSpec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jay on 3/22/16.
 */
public class SelfplayRunner {
    private EngineSpec mFirstEngineSpec;
    private EngineSpec mSecondEngineSpec;
    private int mMatchCount;
    private TimeSpec mGameTimeSpec;
    private List<MatchResult> mMatchResults;
    private SelfplayWindow mHost;

    private MatchResult mCurrentMatch;
    private Game mLastGame = null;

    private boolean mFinished = false;

    public SelfplayRunner(SelfplayWindow host, int matchCount) {
        mHost = host;
        mMatchCount = matchCount;

        mFirstEngineSpec = TerminalSettings.attackerEngineSpec;
        mSecondEngineSpec = TerminalSettings.defenderEngineSpec;
        mGameTimeSpec = TerminalSettings.timeSpec;
        mMatchResults = new ArrayList<>(mMatchCount);
    }

    public void setMatchCount(int count) {
        mMatchCount = count;
    }

    public void startTournament() {
        mHost.getTerminalCallback().setSelfplayWindow(mHost);
        String title = "Match 1: " + mFirstEngineSpec.name + " v. " + mSecondEngineSpec;
        mHost.setTitle(title);

        runTournamentMatch();
    }

    public boolean tournamentFinished() {
        return mFinished;
    }

    public List<MatchResult> getMatchResults() {
        return mMatchResults;
    }

    public void notifyGameFinished(Game g) {
        mLastGame = g;
    }

    public String getTitle() {
        int matchCount = getMatchResults().size() + 1;
        int firstEngineVictories = 0;
        int secondEngineVictories = 0;

        boolean firstIsAttacker = TerminalSettings.attackerEngineSpec == mFirstEngineSpec;

        for(MatchResult r : getMatchResults()) {
            if(r.getMatchWinner() == mFirstEngineSpec) firstEngineVictories++;
            if(r.getMatchWinner() == mSecondEngineSpec) secondEngineVictories++;
        }

        String title = "Match " + matchCount + ": " + mFirstEngineSpec.name + " (" + (firstIsAttacker ? "atk, " : "def, ") + "w" + firstEngineVictories +") v. " + mSecondEngineSpec.name + " (" + (firstIsAttacker ? "def, " : "atk, ") + "w" + secondEngineVictories +")";

        return title;
    }

    public void finishTournament() {
        if(mMatchResults.size() > 0) {
            StringBuilder builder = new StringBuilder();

            builder.append("Tournament results\n");
            builder.append("Engine 1: ").append(mMatchResults.get(0).getEngine(0).toString()).append("\n");
            builder.append("Engine 2: ").append(mMatchResults.get(0).getEngine(1).toString()).append("\n");
            builder.append("\n");

            int i = 1;
            int matchResultsByEngine[] = new int[2];
            for (MatchResult result : mMatchResults) {
                builder.append("Match ").append(i++).append("\n");
                builder.append("Match winner: ").append(drawOrName(result.getMatchWinner())).append("\n");
                builder.append("\tGame 1 victor: ").append(drawOrName(result.getWinner(0))).append(" Moves: ").append(result.getLength(0)).append("\n");
                builder.append("\tGame 2 victor: ").append(drawOrName(result.getWinner(1))).append(" Moves: ").append(result.getLength(1)).append("\n");

                if (result.getMatchWinner() != null) {
                    if (result.getMatchWinner().equals(result.getEngine(0))) {
                        matchResultsByEngine[0]++;
                    }
                    else if (result.getMatchWinner().equals(result.getEngine(1))) {
                        matchResultsByEngine[1]++;
                    }
                }

                builder.append("\n");
            }

            builder.append(mMatchResults.get(0).getEngine(0).toString()).append(" won: ").append(matchResultsByEngine[0]).append(" matches\n");
            builder.append(mMatchResults.get(0).getEngine(1).toString()).append(" won: ").append(matchResultsByEngine[1]).append(" matches\n");

            String tourneySummary = builder.toString();

            File f = new File("selfplay-results");
            f.mkdirs();
            if (f.exists()) {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmm");
                String tourneyFolderName = format.format(new Date()) + "-result";
                File tourneyFolder = new File(f, tourneyFolderName);
                tourneyFolder.mkdir();

                if (tourneyFolder.exists()) {
                    int matchIndex = 1;
                    for (MatchResult result : mMatchResults) {
                        File game1File = new File(tourneyFolder, "match" + matchIndex + "game1.otg");
                        File game2File = new File(tourneyFolder, "match" + matchIndex + "game2.otg");

                        matchIndex++;

                        try {
                            FileWriter fw = new FileWriter(game1File);
                            fw.write(GameSerializer.getGameRecord(result.getGame(0), true));
                            fw.flush();

                            fw = new FileWriter(game2File);
                            fw.write(GameSerializer.getGameRecord(result.getGame(1), true));
                            fw.flush();
                        } catch (IOException e) {
                            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Failed to write selfplay result");
                        }
                    }

                    File summaryFile = new File(tourneyFolder, "summary.txt");
                    try {
                        FileWriter fw = new FileWriter(summaryFile);
                        fw.write(tourneySummary);
                        fw.flush();
                    } catch (IOException e) {
                        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Failed to write selfplay summary");
                    }
                }
            }
        }

        mFinished = true;

        mHost.getTerminalCallback().setSelfplayWindow(null);
        mHost.getTerminalCallback().onMenuNavigation(new SelfplayResultWindow(mHost.getTerminalCallback(), this));
    }

    public static String drawOrName(EngineSpec f) {
        return (f == null? "Draw" : f.toString());
    }

    private void runTournamentMatch() {
        mCurrentMatch = new MatchResult();

        mCurrentMatch.setEngines(mFirstEngineSpec, mSecondEngineSpec);
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Running match " + (mMatchResults.size() + 1) + "/" + mMatchCount + " between " + mFirstEngineSpec.toString() + " and " + mSecondEngineSpec.toString());
        TerminalSettings.attackerEngineSpec = mFirstEngineSpec;
        TerminalSettings.defenderEngineSpec = mSecondEngineSpec;


        // This is a blocking call (sets this as the UI thread), so when it returns, the game
        // is over.
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Running game 1");
        TerminalUtils.startGame(mHost.getTextGUI(), mHost.getTerminalCallback());

        while(mLastGame == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                OpenTafl.logStackTrace(OpenTafl.LogLevel.CHATTY, e);
            }
        }

        mCurrentMatch.setGame(0, mLastGame);
        mLastGame = null;
        if(mCurrentMatch.getGame(0).getCurrentState().checkVictory() == GameState.ATTACKER_WIN) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, mFirstEngineSpec.toString() + " wins game 1");
            mCurrentMatch.setWinner(0, mFirstEngineSpec);
        }
        else if(mCurrentMatch.getGame(0).getCurrentState().checkVictory() == GameState.DEFENDER_WIN) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, mSecondEngineSpec.toString() + " wins game 1");
            mCurrentMatch.setWinner(0, mSecondEngineSpec);
        }

        TerminalSettings.attackerEngineSpec = mSecondEngineSpec;
        TerminalSettings.defenderEngineSpec = mFirstEngineSpec;

        // Same blocking call.
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Running game 2");
        TerminalUtils.startGame(mHost.getTextGUI(), mHost.getTerminalCallback());

        while(mLastGame == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                OpenTafl.logStackTrace(OpenTafl.LogLevel.CHATTY, e);
            }
        }

        mCurrentMatch.setGame(1, mLastGame);
        mLastGame = null;
        if(mCurrentMatch.getGame(1).getCurrentState().checkVictory() == GameState.ATTACKER_WIN) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, mSecondEngineSpec.toString() + " wins game 2");
            mCurrentMatch.setWinner(1, mSecondEngineSpec);
        }
        else if(mCurrentMatch.getGame(1).getCurrentState().checkVictory() == GameState.DEFENDER_WIN) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, mFirstEngineSpec.toString() + " wins game 2");
            mCurrentMatch.setWinner(1, mFirstEngineSpec);
        }

        // Restore previous settings
        TerminalSettings.attackerEngineSpec = mFirstEngineSpec;
        TerminalSettings.defenderEngineSpec = mSecondEngineSpec;

        mMatchResults.add(mCurrentMatch);
        if(mMatchResults.size() >= mMatchCount) {
            finishTournament();
        }
        else {
            runTournamentMatch();
        }
    }
}
