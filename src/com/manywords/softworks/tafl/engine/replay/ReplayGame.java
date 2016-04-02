package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.engine.*;
import com.manywords.softworks.tafl.ui.RawTerminal;

import javax.xml.soap.Detail;
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

    private GameClock.TimeSpec mAttackerTimeLeft;
    private GameClock.TimeSpec mDefenderTimeLeft;

    private List<GameClock.TimeSpec> mAttackerTimeSpecByIndex;
    private List<GameClock.TimeSpec> mDefenderTimeSpecByIndex;

    /**
     * This constructor takes a game object and plays the given
     * moves on top of it.
     * @param game
     * @param movesToPlay
     */
    public ReplayGame(Game game, List<DetailedMoveRecord> movesToPlay) {
        for(MoveRecord m : movesToPlay) {
            int result = game.getCurrentState().makeMove(m);
            if(result < GameState.GOOD_MOVE) {
                RawTerminal.renderGameState(game.getCurrentState());
                System.out.println(m);
                throw new IllegalStateException("Failed to make moves! Error: " + result);
            }
        }

        mGame = game;
        mMoveHistory = movesToPlay;

        mGame.setCurrentState(mGame.getHistory().get(mStatePosition));

        for(int i = 0; i < movesToPlay.size(); i++) {
            mGame.getHistory().get(i).setExitingMove(movesToPlay.get(i));
            mGame.getHistory().get(i+1).setEnteringMove(movesToPlay.get(i));
        }

        setupFirstStatesList();
        setupTimeSpecLists();
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
        setupFirstStatesList();
        setupTimeSpecLists();
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

    public GameState getPreviousState() {
        if(mStatePosition - 1 >= 0) {
            return stateAtIndex(mStatePosition - 1);
        }
        else return null;
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
        for(int i = index + 1; i < historySize(); i++) {
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

    private void setupTimeSpecLists() {
        mAttackerTimeSpecByIndex = new ArrayList<>();
        mDefenderTimeSpecByIndex = new ArrayList<>();
        if(mGame.getClock() == null) return;

        mAttackerTimeSpecByIndex.add(mGame.getClock().toTimeSpec());
        mDefenderTimeSpecByIndex.add(mGame.getClock().toTimeSpec());

        // The last state doesn't have an exiting move
        System.out.println(historySize());
        for(int i = 0; i < historySize() - 1; i++) {
            GameState current = mGame.getHistory().get(i);

            if(current.getCurrentSide().isAttackingSide()) {
                // The other side's clock doesn't count down.
                mDefenderTimeSpecByIndex.add(mDefenderTimeSpecByIndex.get(i));

                // If we have a record, save it; otherwise, get the previous one as a best guess.
                DetailedMoveRecord dm = mMoveHistory.get(i);
                if(dm.getTimeRemaining() != null) {
                    mAttackerTimeSpecByIndex.add(dm.getTimeRemaining());
                }
                else {
                    mAttackerTimeSpecByIndex.add(mAttackerTimeSpecByIndex.get(i));
                }
            }
            else {
                mAttackerTimeSpecByIndex.add(mAttackerTimeSpecByIndex.get(i));

                DetailedMoveRecord dm = mMoveHistory.get(i);
                if(dm.getTimeRemaining() != null) {
                    mDefenderTimeSpecByIndex.add(dm.getTimeRemaining());
                }
                else {
                    mDefenderTimeSpecByIndex.add(mDefenderTimeSpecByIndex.get(i));
                }
            }
        }

        String remainingTimeString = mGame.getTagMap().get("remaining-time");
        if(remainingTimeString != null) {
            String[] remainingTimes = remainingTimeString.split(",");
            mAttackerTimeLeft = GameClock.getTimeSpecForGameNotationString(remainingTimes[0]);
            mDefenderTimeLeft = GameClock.getTimeSpecForGameNotationString(remainingTimes[1]);

            mAttackerTimeSpecByIndex.add(mAttackerTimeLeft);
            mDefenderTimeSpecByIndex.add(mDefenderTimeLeft);
        }
    }

    public GameClock.TimeSpec getTimeGuess(boolean isAttackingSide) {
        int index = mStatePosition;
        if(isAttackingSide && mAttackerTimeSpecByIndex.size() > mStatePosition) {
            return mAttackerTimeSpecByIndex.get(mStatePosition);
        }
        else if(isAttackingSide && mAttackerTimeSpecByIndex.size() > 0) {
            return mAttackerTimeSpecByIndex.get(mAttackerTimeSpecByIndex.size() - 1);
        }
        else if (!isAttackingSide && mDefenderTimeSpecByIndex.size() > mStatePosition) {
            return mDefenderTimeSpecByIndex.get(mStatePosition);
        }
        else if(!isAttackingSide && mDefenderTimeSpecByIndex.size() > 0) {
            return mDefenderTimeSpecByIndex.get(mDefenderTimeSpecByIndex.size() - 1);
        }

        return null;
    }
}
