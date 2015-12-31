package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    public Game(long[][] zobristTable, List<GameState> history) {
        if (!(this instanceof AiWorkspace)) {
            throw new IllegalArgumentException("Empty constructor is only for AiWorkspace!");
        }

        mZobristConstants = zobristTable;
        mHistory = history;
    }

    public Game(Rules rules, UiCallback callback) {
        mGameRules = rules;

        int boardSquares = rules.getBoard().getBoardDimension() * rules.getBoard().getBoardDimension();
        mZobristConstants = new long[boardSquares][Taflman.COUNT_TYPES * 2];
        Random r = new XorshiftRandom(10201989);
        for (int i = 0; i < boardSquares; i++) {
            for (int j = 0; j < Taflman.COUNT_TYPES * 2; j++) {
                mZobristConstants[i][j] = r.nextLong();
            }
        }

        Coord.initialize(rules.getBoard().getBoardDimension());
        mHistory = new ArrayList<GameState>();

        // Create a new state off of the game rules.
        mCurrentState = new GameState(this, mGameRules);

        Taflman.initialize(this, mGameRules);

        mCallback = callback;
    }

    public final long[][] mZobristConstants;
    public double mAverageBranchingFactor = 0;
    public int mAverageBranchingFactorCount = 0;
    private UiCallback mCallback;
    private Rules mGameRules;
    private GameState mCurrentState;
    private List<GameState> mHistory;

    public UiCallback getUiCallback() {
        return mCallback;
    }

    public void setUiCallback(UiCallback mCallback) {
        this.mCallback = mCallback;
    }

    public Rules getGameRules() {
        return mGameRules;
    }

    public void setGameRules(Rules mGameRules) {
        this.mGameRules = mGameRules;
    }

    public GameState getCurrentState() {
        return mCurrentState;
    }

    public List<GameState> getHistory() {
        return mHistory;
    }

    /**
     * Sides do not always go in strict sequence!
     * If the berserker rule is in effect, one side
     * might take several turns in a row.
     *
     * @return
     */
    public Side getCurrentSide() {
        return mCurrentState.getCurrentSide();
    }

    // TODO: pass StateEvent object to notify UI of happenings
    // in callback
    public void advanceState(GameState currentState, boolean advanceTurn, char berserkingTaflman, boolean recordState) {
        GameState nextState;
        if (mGameRules.getBerserkMode() > 0 && berserkingTaflman != Taflman.EMPTY) {
            nextState = new GameState(
                    this,
                    currentState,
                    currentState.getBoard(),
                    currentState.getAttackers(),
                    currentState.getDefenders(),
                    berserkingTaflman);
        } else {
            nextState = new GameState(
                    this,
                    currentState,
                    currentState.getBoard(),
                    currentState.getAttackers(),
                    currentState.getDefenders(), true, true);
        }
        mCurrentState = nextState;
        mHistory.add(currentState);

        // Victory
        int result = currentState.checkVictory();

        if (result < 0) {
            if (mCallback != null) {
                mCallback.victoryForSide(result == GameState.ATTACKER_WIN ? currentState.getAttackers() : currentState.getDefenders());
            }
        } else {
            // Game continues
            if (mCallback != null) {
                mCallback.gameStateAdvanced();
            }
        }
    }

    public boolean historyContainsPosition(GameState state) {
        for (GameState historical : mHistory) {
            if (state.mZobristHash == historical.mZobristHash) {
                return true;
            }
        }

        return false;
    }

    public boolean historyContainsHash(long zobrist) {
        for (GameState historical : mHistory) {
            if (historical.mZobristHash == zobrist) return true;
        }

        return false;
    }
}
