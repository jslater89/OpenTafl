package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.*;
import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;

import java.util.*;

/**
 * Created by jay on 3/31/16.
 */
public class ReplayGame {
    private Game mGame;
    private List<GameState> mFirstStatesByTurn;
    private List<DetailedMoveRecord> mMoveHistory;
    private ReplayGameState mCurrentState = null;

    private TimeSpec mAttackerTimeLeft;
    private TimeSpec mDefenderTimeLeft;

    private Map<GameState, TimeSpec> mAttackerTimeSpecsByState;
    private Map<GameState, TimeSpec> mDefenderTimeSpecsByState;

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
                throw new IllegalStateException("Failed to make move " + m + "! Error: " + result);
            }

        }

        mMoveHistory = movesToPlay;
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Loaded game with moves: " + mMoveHistory);
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Game history after load: " + mGame.getHistory());
        setCurrentState((ReplayGameState) mGame.getHistory().get(0));

        /*
        for(int i = 0; i < movesToPlay.size(); i++) {
            mGame.getHistory().get(i).setExitingMove(movesToPlay.get(i));
            mGame.getHistory().get(i+1).setEnteringMove(movesToPlay.get(i));
        }
        */

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
                /*
                if(statePosition > mStatePosition && !modified) {
                    components[i] = Ansi.UNDERLINE + components[i] + Ansi.UNDERLINE_OFF;
                    modified = true;
                }*/
            }

            for(String s : components) {
                newString += s + " ";
            }
            newString += "\n";
        }

        return newString;
    }

    public ReplayGameState nextState() {
        ReplayGameState state = getCurrentState();
        if(state.getCanonicalChild() != null) setCurrentState(state.getCanonicalChild());
        return getCurrentState();
    }

    public GameState previousState() {
        ReplayGameState state = getCurrentState();
        if(state.getParent() != null) setCurrentState(state.getParent());
        return getCurrentState();
    }

    public GameState setPositionByAddress(MoveAddress address) {
        ReplayGameState state = getStateByAddress(address);
        setCurrentState(state);
        return getCurrentState();
    }

    public void prepareForGameStart() {
        ReplayGameState state = getStateByAddress("1a");
        if(state == null) throw new IllegalStateException("Can't start a game from a zero-length replay");

        ReplayGameState nextState = null;
        while((nextState = state.getCanonicalChild()) != null) {
            state = nextState;
        }

        prepareForGameStart(state.getMoveAddress());
    }

    public void prepareForGameStart(MoveAddress address) {
        setCurrentState(getStateByAddress(address));
        List<GameState> toRetain = new ArrayList<>(historySize());
        for(GameState state : mGame.getHistory()) {
            ReplayGameState replayState = (ReplayGameState) state;

            if(replayState.getMoveAddress().equals(address)) {
                toRetain.add(replayState);
                break;
            }
            else {
                toRetain.add(replayState);
            }
        }

        mGame.getHistory().retainAll(toRetain);
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

    private void setCurrentState(ReplayGameState replayGameState) {
        mGame.setCurrentState(replayGameState);
        mCurrentState = replayGameState;
    }

    public ReplayGameState getCurrentState() {
        return mCurrentState;
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
        mAttackerTimeSpecsByState = new LinkedHashMap<>();
        mDefenderTimeSpecsByState = new LinkedHashMap<>();
        if(mGame.getClock() == null) return;

        mAttackerTimeSpecsByState.put(mGame.getHistory().get(0), mGame.getClock().toTimeSpec());
        mDefenderTimeSpecsByState.put(mGame.getHistory().get(0), mGame.getClock().toTimeSpec());

        // The last state doesn't have an exiting move
        for(int i = 1; i < historySize() - 1; i++) {
            GameState previous = mGame.getHistory().get(i - 1);
            GameState current = mGame.getHistory().get(i);

            if(current.getCurrentSide().isAttackingSide()) {
                // The other side's clock doesn't count down.
                if(mDefenderTimeSpecsByState.size() > 0) {
                    mDefenderTimeSpecsByState.put(current, mDefenderTimeSpecsByState.get(previous));
                }
                else {
                    mDefenderTimeSpecsByState.put(current, mGame.getClock().toTimeSpec());
                }

                // If we have a record, save it; otherwise, get the previous one as a best guess.
                DetailedMoveRecord dm = mMoveHistory.get(i);
                if(dm.getTimeRemaining() != null) {
                    mAttackerTimeSpecsByState.put(current, dm.getTimeRemaining());
                }
                else if (mAttackerTimeSpecsByState.size() > 0){
                    mAttackerTimeSpecsByState.put(current, mAttackerTimeSpecsByState.get(previous));
                }
                else {
                    mAttackerTimeSpecsByState.put(current, mGame.getClock().toTimeSpec());
                }
            }
            else {
                if(mAttackerTimeSpecsByState.size() > 0) {
                    mAttackerTimeSpecsByState.put(current, mAttackerTimeSpecsByState.get(previous));
                }
                else {
                    mAttackerTimeSpecsByState.put(current, mGame.getClock().toTimeSpec());
                }

                DetailedMoveRecord dm = mMoveHistory.get(i);
                if(dm.getTimeRemaining() != null) {
                    mDefenderTimeSpecsByState.put(current, dm.getTimeRemaining());
                }
                else if (mDefenderTimeSpecsByState.size() > 0){
                    mDefenderTimeSpecsByState.put(current, mDefenderTimeSpecsByState.get(previous));
                }
                else {
                    mDefenderTimeSpecsByState.put(current, mGame.getClock().toTimeSpec());
                }
            }
        }

        String remainingTimeString = mGame.getTagMap().get("time-remaining");
        if(remainingTimeString != null) {
            GameState state = getGame().getHistory().get(getGame().getHistory().size() - 1);
            String[] remainingTimes = remainingTimeString.split(",");
            mAttackerTimeLeft = GameClock.getTimeSpecForGameNotationString(remainingTimes[0]);
            mDefenderTimeLeft = GameClock.getTimeSpecForGameNotationString(remainingTimes[1]);

            mAttackerTimeSpecsByState.put(state, mAttackerTimeLeft);
            mDefenderTimeSpecsByState.put(state, mDefenderTimeLeft);
        }
    }

    public TimeSpec getTimeGuess(boolean isAttackingSide) {
        GameState currentState = getCurrentState();
        if(isAttackingSide && mAttackerTimeSpecsByState.containsKey(currentState)) {
            return mAttackerTimeSpecsByState.get(currentState);
        }
        else if(isAttackingSide && mAttackerTimeSpecsByState.size() > 0) {
            return getLastTimeSpec(mAttackerTimeSpecsByState);
        }
        else if (!isAttackingSide && mDefenderTimeSpecsByState.containsKey(currentState)) {
            return mDefenderTimeSpecsByState.get(currentState);
        }
        else if(!isAttackingSide && mDefenderTimeSpecsByState.size() > 0) {
            return getLastTimeSpec(mDefenderTimeSpecsByState);
        }

        return null;
    }

    private TimeSpec getLastTimeSpec(Map<?, TimeSpec> map) {
        Iterator<TimeSpec> i = map.values().iterator();
        TimeSpec ts = null;
        while(i.hasNext()) {
            ts = i.next();
        }
        return ts;
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
