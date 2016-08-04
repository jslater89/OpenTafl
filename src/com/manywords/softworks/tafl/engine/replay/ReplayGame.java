package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.engine.*;
import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.ui.Ansi;
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

    private TimeSpec mAttackerTimeLeft;
    private TimeSpec mDefenderTimeLeft;

    private List<TimeSpec> mAttackerTimeSpecByIndex;
    private List<TimeSpec> mDefenderTimeSpecByIndex;

    /**
     * This constructor takes a game object and plays the given
     * moves on top of it.
     * @param game
     * @param movesToPlay
     */
    public ReplayGame(Game game, List<DetailedMoveRecord> movesToPlay) {
        GameState currentState = game.getCurrentState();
        ReplayGameState replayState = new ReplayGameState(this, currentState);
        replayState.setMoveAddress(MoveAddress.newRootAddress());

        game.getHistory().replaceAll(gameState -> {
            if(gameState == currentState) {
                return replayState;
            }
            else {
                return gameState;
            }
        });
        mGame = game;
        mGame.setCurrentState(replayState);

        for(MoveRecord m : movesToPlay) {
            int result = mGame.getCurrentState().makeMove(m);
            if(result < GameState.GOOD_MOVE) {
                RawTerminal.renderGameState(game.getCurrentState());
                System.out.println(m);
                throw new IllegalStateException("Failed to make moves! Error: " + result);
            }

        }

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
    public static ReplayGame copyGameToReplay(Game game) {
        Game copiedGame = new Game(game);
        List<DetailedMoveRecord> moves = new ArrayList<>();

        for(GameState state : copiedGame.getHistory()) {
            if(state.getExitingMove() != null) {
                moves.add((DetailedMoveRecord) state.getExitingMove());
            }
        }

        GameState copiedStartingState = copiedGame.getHistory().get(0);
        copiedGame.getHistory().clear();
        copiedGame.getHistory().add(copiedStartingState);
        copiedGame.setCurrentState(copiedStartingState);

        return new ReplayGame(copiedGame, moves);
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

            for(int i = 1; i < components.length; i++) {
                statePosition++;
                if(statePosition > mStatePosition && !modified) {
                    components[i] = Ansi.UNDERLINE + components[i] + Ansi.UNDERLINE_OFF;
                    modified = true;
                }
            }

            for(String s : components) {
                newString += s + " ";
            }
            newString += "\n";
        }

        return newString;
    }

    public GameState nextState() {
        if(mStatePosition < historySize() - 1) {
            mStatePosition++;
        }

        return goToStateAtIndex(mStatePosition);
    }

    public GameState previousState() {
        if(mStatePosition > 0) {
            mStatePosition--;
        }

        return goToStateAtIndex(mStatePosition);
    }

    private int setPositionByState(GameState state) {
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

        return goToStateAtIndex(mStatePosition);
    }

    public int getPosition() {
        return mStatePosition;
    }

    public void prepareForGameStart() {
        prepareForGameStart(historySize() - 1);
    }

    public void prepareForGameStart(int index) {
        setPosition(index);
        List<GameState> toRemove = new ArrayList<>(historySize() - index);
        for(int i = index + 1; i < historySize(); i++) {
            toRemove.add(mGame.getHistory().get(i));
        }
        mGame.getHistory().removeAll(toRemove);
    }

    private ReplayGameState goToStateAtIndex(int i) {
        mGame.setCurrentState(mGame.getHistory().get(i));
        return getStateAtIndex(i);
    }

    private ReplayGameState getStateAtIndex(int i) {
        return (ReplayGameState) mGame.getHistory().get(i);
    }

    public int historySize() {
        return mGame.getHistory().size();
    }

    public GameState getCurrentState() {
        return mGame.getHistory().get(mStatePosition);
    }

    public ReplayGameState makeVariation(MoveRecord move) {
        ReplayGameState state = (ReplayGameState) getCurrentState();
        ReplayGameState variationState = state.makeVariation(move);

        return variationState;
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
        for(int i = 1; i < historySize() - 1; i++) {
            GameState current = mGame.getHistory().get(i);

            if(current.getCurrentSide().isAttackingSide()) {
                // The other side's clock doesn't count down.
                if(mDefenderTimeSpecByIndex.size() > 0) {
                    mDefenderTimeSpecByIndex.add(mDefenderTimeSpecByIndex.get(mDefenderTimeSpecByIndex.size() - 1));
                }
                else {
                    mDefenderTimeSpecByIndex.add(mGame.getClock().toTimeSpec());
                }

                // If we have a record, save it; otherwise, get the previous one as a best guess.
                DetailedMoveRecord dm = mMoveHistory.get(i);
                if(dm.getTimeRemaining() != null) {
                    mAttackerTimeSpecByIndex.add(dm.getTimeRemaining());
                }
                else if (mAttackerTimeSpecByIndex.size() > 0){
                    mAttackerTimeSpecByIndex.add(mAttackerTimeSpecByIndex.get(mAttackerTimeSpecByIndex.size() - 1));
                }
                else {
                    mAttackerTimeSpecByIndex.add(mGame.getClock().toTimeSpec());
                }
            }
            else {
                if(mAttackerTimeSpecByIndex.size() > 0) {
                    mAttackerTimeSpecByIndex.add(mAttackerTimeSpecByIndex.get(mAttackerTimeSpecByIndex.size() - 1));
                }
                else {
                    mAttackerTimeSpecByIndex.add(mGame.getClock().toTimeSpec());
                }

                DetailedMoveRecord dm = mMoveHistory.get(i);
                if(dm.getTimeRemaining() != null) {
                    mDefenderTimeSpecByIndex.add(dm.getTimeRemaining());
                }
                else if (mDefenderTimeSpecByIndex.size() > 0){
                    mDefenderTimeSpecByIndex.add(mDefenderTimeSpecByIndex.get(mDefenderTimeSpecByIndex.size() - 1));
                }
                else {
                    mDefenderTimeSpecByIndex.add(mGame.getClock().toTimeSpec());
                }
            }
        }

        String remainingTimeString = mGame.getTagMap().get("time-remaining");
        if(remainingTimeString != null) {
            String[] remainingTimes = remainingTimeString.split(",");
            mAttackerTimeLeft = GameClock.getTimeSpecForGameNotationString(remainingTimes[0]);
            mDefenderTimeLeft = GameClock.getTimeSpecForGameNotationString(remainingTimes[1]);

            mAttackerTimeSpecByIndex.add(mAttackerTimeLeft);
            mDefenderTimeSpecByIndex.add(mDefenderTimeLeft);
        }
    }

    public TimeSpec getTimeGuess(boolean isAttackingSide) {
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

    public ReplayGameState getStateByAddress(MoveAddress moveAddress) {
        MoveAddress.Element element = moveAddress.getRootElement();

        ReplayGameState startingPoint = null;
        for(GameState state : mGame.getHistory()) {
            ReplayGameState rgs = (ReplayGameState) state;
            if(rgs.getMoveAddress().getRootElement().equals(element)) {
                startingPoint = rgs;
                break;
            }
        }

        ReplayGameState result = null;
        if(startingPoint != null && moveAddress.getNonRootElements().size() > 0) {
            result = startingPoint.findVariationState(new MoveAddress(moveAddress.getNonRootElements()));
        }
        else {
            result = startingPoint;
        }

        return result;
    }

    public ReplayGameState getStateByAddress(String s) {
        return getStateByAddress(MoveAddress.parseAddress(s));
    }

    public void dumpHistory() {
        for(GameState state : mGame.getHistory()) {
            ((ReplayGameState) state).dumpTree();
        }
    }
}
