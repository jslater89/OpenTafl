package com.manywords.softworks.tafl.ui.selfplay;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.command.player.external.engine.EngineSpec;

/**
 * Created by jay on 3/22/16.
 */
public class MatchResult {
    private Game[] games = new Game[2];

    private EngineSpec[] engines = new EngineSpec[2];

    private EngineSpec[] winners = new EngineSpec[2];

    private int[] gameLengths = new int[2];

    public void setEngines(EngineSpec engine1, EngineSpec engine2) {
        engines[0] = engine1;
        engines[1] = engine2;
    }

    public void setWinner(int index, EngineSpec engine) {
        winners[index] = engine;
    }

    public void setGame(int index, Game g) {
        games[index] = g;
        gameLengths[index] = g.getHistory().size();
    }

    public Game getGame(int index) {
        return games[index];
    }

    public EngineSpec getEngine(int index) {
        return engines[index];
    }

    public EngineSpec getWinner(int index) {
        return winners[index];
    }

    public int getLength(int index) {
        return gameLengths[index];
    }

    public EngineSpec getMatchWinner() {
        if(games[0] == null || games[1] == null) throw new IllegalStateException();
        if(engines[0] == null || engines[1] == null) throw new IllegalStateException();

        int wonByFirstEngine = 0;
        int wonBySecondEngine = 0;

        for(EngineSpec winner : winners) {
            if(winner != null) {
                if(winner == engines[0]) wonByFirstEngine++;
                else if(winner == engines[1]) wonBySecondEngine++;
            }
        }

        if(wonByFirstEngine > wonBySecondEngine) return engines[0];
        else if(wonBySecondEngine > wonByFirstEngine) return engines[1];
        else if (winners[0] != null && winners[1] != null) {
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
        else return null;
    }
}
