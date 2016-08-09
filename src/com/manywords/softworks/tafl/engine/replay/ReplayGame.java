package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.command.HumanCommandParser;
import com.manywords.softworks.tafl.engine.*;
import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.ui.Ansi;

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
    public ReplayGame(Game game, List<DetailedMoveRecord> movesToPlay, List<GameSerializer.VariationContainer> variationsToPlay) {
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

        for(GameSerializer.VariationContainer container : variationsToPlay) {
            Variation v = getVariationByAddress(container.address);

            ReplayGameState rootForVariation;
            if(v == null) {
                MoveAddress rootStateAddress = new MoveAddress(container.address.getElementsBefore(container.address.getElements().size() - 2));
                rootForVariation = getStateByAddress(rootStateAddress);
            }
            else {
                // make a new variation off of the last thing in this variation.
                rootForVariation = v.getStates().get(v.getStates().size() - 1);
            }

            if(rootForVariation != null) {
                ReplayGameState inVariation = rootForVariation;
                for(DetailedMoveRecord m : container.moves) {
                    ReplayGameState state = inVariation.makeVariation(m);
                    if(state.getLastMoveResult() >= GameState.GOOD_MOVE) {
                        inVariation = state;
                    }
                    else {
                        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Failed to apply move: " + m);
                    }
                }
            }
            else {
                OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Failed to find root for variation container: " + variationsToPlay);
            }
        }

        mMoveHistory = movesToPlay;
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Loaded game with moves: " + mMoveHistory);
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "And variations: " + variationsToPlay);
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Game history after load: " + mGame.getHistory());
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

        return new ReplayGame(copiedGame, moves, new ArrayList<>());
    }

    public Game getGame() {
        return mGame;
    }

    public String getHistoryStringWithPositionMarker() {
        return getHistoryCommandString(mGame.getHistory(), getCurrentState().getMoveAddress());

//        String historyString = mGame.getHistoryString();
//        String[] lines = historyString.split("\n");
//
//        String newString = "";
//        int statePosition = 0;
//        boolean modified = false;
//        for (String line : lines) {
//            String[] components = line.split(" ");
//
//            for(int i = 1; i < components.length; i++) {
//                statePosition++;
//                /*
//                if(statePosition > mStatePosition && !modified) {
//                    components[i] = Ansi.UNDERLINE + components[i] + Ansi.UNDERLINE_OFF;
//                    modified = true;
//                }*/
//            }
//
//            for(String s : components) {
//                newString += s + " ";
//            }
//            newString += "\n";
//        }
//
//        return newString;
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
        if(address.getElements().size() % 2 == 1) {
            // 12. means 12a
            if(address.getLastElement().moveIndex == -1) {
                address.getLastElement().moveIndex = 0;
            }
        }

        ReplayGameState state = getStateByAddress(address);

        if(state == null && address.getLastElement().equals(new MoveAddress.Element(1, 0))) {
            Variation v = getVariationByAddress(new MoveAddress(address.getAllRootElements()));
                if(v != null){
                state = v.getRoot();
            }
        }

        if(state != null) setCurrentState(state);
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

    public int historySize() {
        return mGame.getHistory().size();
    }

    public void setCurrentState(ReplayGameState replayGameState) {
        if(replayGameState == null) throw new IllegalArgumentException("Set current state to null");

        mGame.setCurrentState(replayGameState);
        mCurrentState = replayGameState;
    }

    public ReplayGameState getCurrentState() {
        return mCurrentState;
    }

    public ReplayGameState makeVariation(MoveRecord move) {
        ReplayGameState state = (ReplayGameState) getCurrentState();
        ReplayGameState variationState = state.makeVariation(move);

        if(state.getLastMoveResult() >= GameState.GOOD_MOVE) {
            // Don't set errors into current state, oy.
            setCurrentState(variationState);
        }

        return getCurrentState();
    }

    public boolean deleteVariation(MoveAddress moveAddress) {
        ReplayGameState rootState = getStateByAddress(new MoveAddress(new ArrayList<>(), moveAddress.getRootElement()));

        if(rootState == null)  {
            return false;
        }

        boolean deleted = rootState.deleteVariation(moveAddress);

        // Is the current state deleted? If so, find the first parent that isn't.
        if(deleted) {
            ReplayGameState currentState = getCurrentState();
            MoveAddress currentAddress = currentState.getMoveAddress();

            while(getStateByAddress(currentAddress) == null) {
                currentState = currentState.getParent();
                if(currentState != null) currentAddress = currentState.getMoveAddress();
                else break;
            }

            if(getStateByAddress(currentAddress) != null && currentState != getCurrentState()) {
                setCurrentState(currentState);
            }

            return true;
        }
        return false;
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

    public Variation getVariationByAddress(MoveAddress moveAddress) {
        MoveAddress prefix = new MoveAddress(moveAddress.getAllRootElements());

        ReplayGameState state = getStateByAddress(prefix);

        if(state != null) {
            if(state.getVariations().size() > (moveAddress.getLastElement().rootIndex - 1)) {
                return state.getVariations().get(moveAddress.getLastElement().rootIndex - 1);
            }
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
        if(startingPoint != null && startingPoint.getMoveAddress().equals(moveAddress)) {
            result = startingPoint;
        }

        return result;
    }

    public ReplayGameState getStateByAddress(String s) {
        return getStateByAddress(MoveAddress.parseAddress(s));
    }

    public static String getHistoryCommandString(List<GameState> history) {
        return getHistoryCommandString(history, null);
    }

    public static String getHistoryCommandString(List<GameState> history, MoveAddress highlightAddress) {
        StringBuilder resultString = new StringBuilder();
        int historyPosition = 0;
        ReplayGameState state = (ReplayGameState) history.get(historyPosition);
        List<ReplayGameState> currentTurn = new ArrayList<>();
        List<Variation> currentTurnVariations = new ArrayList<>();
        int currentTurnIndex = 1;

        while(state != null) {
            // Get all states in a given turn.
            if(state.getMoveAddress().getLastElement().rootIndex == currentTurnIndex) {
                currentTurn.add(state);
                currentTurnVariations.addAll(state.getVariations());
            }
            else {
                finishHistoryTurn(currentTurn, currentTurnVariations, highlightAddress, resultString);

                currentTurn.clear();
                currentTurnVariations.clear();
                currentTurnIndex = state.getMoveAddress().getLastElement().rootIndex;
                currentTurn.add(state);
                currentTurnVariations.addAll(state.getVariations());
            }

            historyPosition += 1;
            if(historyPosition == history.size()) {
                finishHistoryTurn(currentTurn, currentTurnVariations, highlightAddress, resultString);
                break;
            }
            state = (ReplayGameState) history.get(historyPosition);
        }

        // Print the turn header.

        // For each state, get all variations and place in list.

        // For each variation, do this method.

        return resultString.toString();
    }

    private static void finishHistoryTurn(List<ReplayGameState> currentTurn, List<Variation> currentTurnVariations, MoveAddress highlightAddress, StringBuilder resultString) {
        boolean first = true;
        for(ReplayGameState turnState : currentTurn) {
            if(first) {
                first = false;

                int paddingCount = turnState.getMoveAddress().getLastElement().moveIndex;
                MoveAddress a = new MoveAddress(turnState.getMoveAddress());
                a.getLastElement().moveIndex = 0;

                resultString.append(a);
                for(int i = 0; i < paddingCount; i++) {
                    resultString.append(" .....");
                }
                resultString.append(" ");
            }
            // else append b., c., etc.

            if(turnState.getMoveAddress().equals(highlightAddress)) {
                resultString.append(Ansi.UNDERLINE);
            }

            if(turnState.getMoveAddress().getElements().size() == 1) {
                if (turnState.getExitingMove() != null) resultString.append(turnState.getExitingMove());
            }
            else {
                if (turnState.getEnteringMove() != null) resultString.append(turnState.getEnteringMove());
            }

            if(turnState.getMoveAddress().equals(highlightAddress)) {
                resultString.append(Ansi.UNDERLINE_OFF);
            }
            resultString.append(" ");
        }
        resultString.append("\n");

        for(Variation v : currentTurnVariations) {
            List<GameState> variationStates = new ArrayList<>();
            variationStates.addAll(v.getStates());
            resultString.append(getHistoryCommandString(variationStates, highlightAddress));
        }
    }

    public void dumpHistory() {
        for(GameState state : mGame.getHistory()) {
            ((ReplayGameState) state).dumpTree();
        }
    }
}
