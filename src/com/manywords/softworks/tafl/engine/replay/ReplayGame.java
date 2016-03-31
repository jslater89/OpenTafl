package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 3/31/16.
 */
public class ReplayGame {
    private Game mGame;
    private List<GameState> mFirstStatesByTurn;
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

        setupFirstStatesList();
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
        setupFirstStatesList();
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

            if (statePosition > mStatePosition && !modified) {
                System.out.println(statePosition);
                System.out.println(mStatePosition);
                int component = mStatePosition % 2 + 1;
                System.out.println(component);
                components[component] = "*" + components[component];

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

    public int setPositionByState(GameState state) {
        int i = 0;
        for(GameState history : mGame.getHistory()) {
            if(state.equals(history)) {
                setPosition(i);
                return i;
            }
            else {
                i++;
            }
        }

        return -1;
    }

    public GameState setTurnIndex(int turn) {
        setPositionByState(mFirstStatesByTurn.get(turn));
        return getCurrentState();
    }

    public GameState setPosition(int i) {
        if(i >= historySize() || i < 0) {
            return null;
        }

        mStatePosition = i;

        return stateAtIndex(mStatePosition);
    }

    public int getPosition() {
        return mStatePosition;
    }

    public void prepareForGameStart() {
        prepareForGameStart(historySize() - 1);
    }

    public void prepareForGameStart(int index) {
        stateAtIndex(index);
        List<GameState> toRemove = new ArrayList<>(historySize() - index);
        for(int i = index; i < historySize(); i++) {
            toRemove.add(mGame.getHistory().get(i));
        }
        mGame.getHistory().removeAll(toRemove);
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

    private void setupFirstStatesList() {
        mFirstStatesByTurn = new ArrayList<>(historySize() / 2);

        GameState firstState = mGame.getHistory().get(0);
        boolean otherSideWent = false;
        mFirstStatesByTurn.add(firstState);
        for(GameState state : mGame.getHistory()) {
            if(state.getCurrentSide().isAttackingSide() == firstState.getCurrentSide().isAttackingSide()) {
                if(otherSideWent) {
                    mFirstStatesByTurn.add(state);
                    otherSideWent = false;
                }
            }
            else {
                otherSideWent = true;
            }
        }
    }
}
