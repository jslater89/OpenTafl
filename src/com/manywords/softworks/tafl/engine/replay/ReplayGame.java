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

        mGame.getHistory().add(mGame.getCurrentState());
        mGame.setCurrentState(mGame.getHistory().get(mStatePosition));

        for(int i = 0; i < movesToPlay.size(); i++) {
            mGame.getHistory().get(i).setExitingMove(movesToPlay.get(i));
        }
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

        mGame.getHistory().add(mGame.getCurrentState());
        mGame.setCurrentState(mGame.getHistory().get(mStatePosition));
    }

    public Game getGame() {
        return mGame;
    }

    public String getHistoryStringWithPositionMarker() {
        String historyString = mGame.getHistoryString();
        String[] lines = historyString.split("\n");

        String newString = "";
        int statePosition = 0;
        boolean modified = false;
        for (String line : lines) {
            String[] components = line.split(" ");
            statePosition += components.length - 1;

            if (statePosition >= mStatePosition && !modified) {
                int component = statePosition - mStatePosition - 1;
                components[component] = components[component] + "*";

                for(String s : components) {
                    newString += s + " ";
                }
                newString += "\n";

                modified = true;
            }
            else {
                newString += line + "\n";
            }
        }

        return newString;
    }

    public GameState nextState() {
        if(mStatePosition < historySize() - 1) {
            mStatePosition++;
        }

        return stateAtIndex(mStatePosition);
    }

    public GameState previousState() {
        if(mStatePosition > 0) {
            mStatePosition--;
        }

        return stateAtIndex(mStatePosition);
    }

    public GameState setPosition(int i) {
        if(i >= historySize() && i < 0) {
            throw new IllegalArgumentException("Index " + i + " out of range: max is " + (mGame.getHistory().size() - 1));
        }

        mStatePosition = i;

        return stateAtIndex(mStatePosition);
    }

    public void prepareForGameStart() {
        stateAtIndex(historySize() - 1);
        mGame.getHistory().remove(historySize() - 1);
    }

    private GameState stateAtIndex(int i) {
        mGame.setCurrentState(mGame.getHistory().get(i));
        return mGame.getHistory().get(i);
    }

    public int historySize() {
        return mGame.getHistory().size();
    }

    public GameState getCurrentState() {
        return mGame.getHistory().get(mStatePosition);
    }
}
