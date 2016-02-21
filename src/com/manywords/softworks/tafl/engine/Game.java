package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.UiCallback;

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
        this(rules, callback, null);
    }

    public Game(Rules rules, UiCallback callback, GameClock.TimeSpec timeSpec) {
        mGameRules = rules;

        if(timeSpec != null) {
            mClock = new GameClock(this, getGameRules().getAttackers(), getGameRules().getDefenders(), timeSpec);
        }

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
    private GameClock mClock;
    private UiCallback mCallback;
    private Rules mGameRules;
    private GameState mCurrentState;
    private List<GameState> mHistory;

    public void start() {
        mClock.start(getGameRules().getStartingSide());
    }

    public void finish() {
        mClock.stop();
    }

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

    public String getHistoryString() {
        String gameRecord = "";
        int count = 1;
        for(int i = 0; i < getHistory().size(); ) {
            gameRecord += count++ + ". ";
            if(i + 1 < getHistory().size()) {
                gameRecord += getHistory().get(i++).getExitingMove() + " " + getHistory().get(i++).getExitingMove() + "\n";
            }
            else {
                gameRecord += getHistory().get(i++).getExitingMove() + "\n";
            }

            if(i == getHistory().size()) break;
        }

        return gameRecord;
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
    public GameState advanceState(GameState currentState, GameState nextState, boolean advanceTurn, char berserkingTaflman, boolean recordState) {
        nextState.updateGameState(
                this,
                currentState,
                nextState.getBoard(),
                nextState.getAttackers(),
                nextState.getDefenders(),
                advanceTurn,
                true,
                berserkingTaflman);

        mCurrentState = nextState;
        mHistory.add(currentState);

        // Victory
        mCurrentState.checkVictory();
        return mCurrentState;
    }

    public void timeUpdate(Side side) {
        mCallback.timeUpdate(side);
    }

    public boolean historyContainsHash(long zobrist) {
        for (GameState historical : mHistory) {
            if (historical.mZobristHash == zobrist) return true;
        }

        return false;
    }
}
