package com.manywords.softworks.tafl.ui.tournament;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.window.MainMenuWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.TournamentWindow;

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

    public void finishTournament() {
        if(mMatchResults.size() > 0) {
            StringBuilder builder = new StringBuilder();

            builder.append("Tournament results\n");
            builder.append("Engine 1: ").append(mMatchResults.get(0).getEngine(0).getName()).append("\n");
            builder.append("Engine 2: ").append(mMatchResults.get(0).getEngine(1).getName()).append("\n");
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

            builder.append(mMatchResults.get(0).getEngine(0).getName()).append(" won: ").append(matchResultsByEngine[0]).append(" matches\n");
            builder.append(mMatchResults.get(0).getEngine(1).getName()).append(" won: ").append(matchResultsByEngine[1]).append(" matches\n");

            String tourneySummary = builder.toString();

            File f = new File("tournament-results");
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
                            fw.write(GameSerializer.getGameRecord(result.getGame(0)));
                            fw.flush();

                            fw = new FileWriter(game2File);
                            fw.write(GameSerializer.getGameRecord(result.getGame(1)));
                            fw.flush();
                        } catch (IOException e) {
                            System.out.println("Failed to write tournament result");
                        }
                    }

                    File summaryFile = new File(tourneyFolder, "summary.txt");
                    try {
                        FileWriter fw = new FileWriter(summaryFile);
                        fw.write(tourneySummary);
                        fw.flush();
                    } catch (IOException e) {
                        System.out.println("Failed to write tournament summary");
                    }
                }
            }
        }

        mHost.getTerminalCallback().setTournamentWindow(null);
        mHost.getTerminalCallback().onMenuNavigation(new MainMenuWindow(mHost.getTerminalCallback()));
    }

    private String drawOrName(File f) {
        return (f == null? "Draw" : f.getName());
    }

    private void runTournamentMatch() {
        mCurrentMatch = new MatchResult();

        mCurrentMatch.setEngines(mFirstEngineFile, mSecondEngineFile);
        System.out.println("Running match " + (mMatchResults.size() + 1) + "/" + mMatchCount + " between " + mFirstEngineFile.getName() + " and " + mSecondEngineFile.getName());
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

        mCurrentMatch.setGame(0, mLastGame);
        mLastGame = null;
        if(mCurrentMatch.getGame(0).getCurrentState().checkVictory() == GameState.ATTACKER_WIN) {
            System.out.println(mFirstEngineFile.getName() + " wins game 1");
            mCurrentMatch.setWinner(0, mFirstEngineFile);
        }
        else if(mCurrentMatch.getGame(0).getCurrentState().checkVictory() == GameState.DEFENDER_WIN) {
            System.out.println(mSecondEngineFile.getName() + " wins game 1");
            mCurrentMatch.setWinner(0, mSecondEngineFile);
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

        mCurrentMatch.setGame(1, mLastGame);
        mLastGame = null;
        if(mCurrentMatch.getGame(1).getCurrentState().checkVictory() == GameState.ATTACKER_WIN) {
            System.out.println(mSecondEngineFile.getName() + " wins game 2");
            mCurrentMatch.setWinner(1, mSecondEngineFile);
        }
        else if(mCurrentMatch.getGame(1).getCurrentState().checkVictory() == GameState.DEFENDER_WIN) {
            System.out.println(mFirstEngineFile.getName() + " wins game 2");
            mCurrentMatch.setWinner(1, mFirstEngineFile);
        }

        // Restore previous settings
        TerminalSettings.attackerEngineFile = mFirstEngineFile;
        TerminalSettings.defenderEngineFile = mSecondEngineFile;

        mMatchResults.add(mCurrentMatch);
        if(mMatchResults.size() >= mMatchCount) {
            finishTournament();
        }
        else {
            runTournamentMatch();
        }
    }
}
