package com.manywords.softworks.tafl.ui.selfplay;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;

import java.io.File;

/**
 * Created by jay on 3/22/16.
 */
public class MatchResult {
    private Game[] games = new Game[2];

    private File[] engines = new File[2];

    private File[] winners = new File[2];

    private int[] gameLengths = new int[2];

    public void setEngines(File engine1, File engine2) {
        engines[0] = engine1;
        engines[1] = engine2;
    }

    public void setWinner(int index, File engine) {
        winners[index] = engine;
    }

    public void setGame(int index, Game g) {
        games[index] = g;
        gameLengths[index] = g.getHistory().size();
    }

    public Game getGame(int index) {
        return games[index];
    }

    public File getEngine(int index) {
        return engines[index];
    }

    public File getWinner(int index) {
        return winners[index];
    }

    public int getLength(int index) {
        return gameLengths[index];
    }

    public File getMatchWinner() {
        if(games[0] == null || games[1] == null) throw new IllegalStateException();
        if(engines[0] == null || engines[1] == null) throw new IllegalStateException();

        if(games[0].getCurrentState().checkVictory() == GameState.DRAW && games[1].getCurrentState().checkVictory() == GameState.DRAW) {
            return null;
        }
        else if (games[0].getCurrentState().checkVictory() == GameState.DRAW) {
            return winners[1];
        }
        else if (games[1].getCurrentState().checkVictory() == GameState.DRAW) {
            return winners[0];
        }

        // If the same engine won both games, that one wins.
        if(winners[0] == winners[1] && winners[0] != null) {
            return winners[0];
        }
        else {
            if(gameLengths[0] < gameLengths[1]) {
                return winners[0];
            }
            else if(gameLengths[1] < gameLengths[0]){
                return winners[1];
            }
            else {
                return null;
            }
        }
    }
}
