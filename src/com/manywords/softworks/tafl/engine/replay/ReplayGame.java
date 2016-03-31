package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 3/31/16.
 */
public class ReplayGame {
    private Game mGame;
    private List<DetailedMoveRecord> mMoveHistory;
    private int mStatePosition = 0;
    private int mMovePosition = -1;

    /**
     * This constructor takes a game object and plays the given
     * moves on top of it.
     * @param game
     * @param movesToPlay
     */
    public ReplayGame(Game game, List<DetailedMoveRecord> movesToPlay) {
        for(MoveRecord m : movesToPlay) {
            game.getCurrentState().makeMove(m);
        }

        mGame = game;
        mMoveHistory = movesToPlay;

        mGame.setCurrentState(mGame.getHistory().get(mStatePosition));
    }

    /**
     * This constructor takes a game and wraps it in its current
     * state.
     * @param game
     */
    public ReplayGame(Game game) {
        mGame = game;
        mMoveHistory = new ArrayList<DetailedMoveRecord>();

        for(GameState state : game.getHistory()) {
            if(state.getExitingMove() != null) {
                mMoveHistory.add((DetailedMoveRecord) state.getExitingMove());
            }
        }

        mGame.setCurrentState(mGame.getHistory().get(mStatePosition));
    }

    public Game getGame() {
        return mGame;
    }

    public GameState nextState() {
        if(mStatePosition < historySize() - 1) {
            mStatePosition++;
            mMovePosition++;
        }

        return stateAtIndex(mStatePosition);
    }

    public GameState previousState() {
        if(mStatePosition > 0) {
            mStatePosition--;
            mMovePosition--;
        }

        return stateAtIndex(mStatePosition);
    }

    public GameState setPosition(int i) {
        if(i >= mGame.getHistory().size() && i < 0) {
            throw new IllegalArgumentException("Index " + i + " out of range: max is " + (mGame.getHistory().size() - 1));
        }

        mStatePosition = i;
        mMovePosition = i - 1;

        return stateAtIndex(mStatePosition);
    }

    private GameState stateAtIndex(int i) {
        return mGame.getHistory().get(mStatePosition);
    }

    public int historySize() {
        return mGame.getHistory().size();
    }

    public GameState getCurrentState() {
        return mGame.getHistory().get(mStatePosition);
    }

    public DetailedMoveRecord getEnteringMove() {
        if(mMovePosition > 0) return mMoveHistory.get(mMovePosition - 1);
        else return null;
    }

    public DetailedMoveRecord getExitingMove() {
        if(mMovePosition < mMoveHistory.size()) return mMoveHistory.get(mMovePosition);
        else return null;
    }
}
