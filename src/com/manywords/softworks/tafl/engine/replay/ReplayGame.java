package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.command.CommandParser;
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
    public enum ReplayMode {
        REPLAY,
        PUZZLE_LOOSE,
        PUZZLE_STRICT;

        public boolean isPuzzleMode() {
            return this == PUZZLE_LOOSE || this == PUZZLE_STRICT;
        }
    }
    private Game mGame;
    private List<GameState> mFirstStatesByTurn;
    private List<DetailedMoveRecord> mMoveHistory;
    private ReplayGameState mCurrentState = null;

    private TimeSpec mAttackerTimeLeft;
    private TimeSpec mDefenderTimeLeft;

    private Map<GameState, TimeSpec> mAttackerTimeSpecsByState;
    private Map<GameState, TimeSpec> mDefenderTimeSpecsByState;

    private boolean mDirty = false;

    private ReplayMode mMode = ReplayMode.REPLAY;
    private MoveAddress mPuzzlePrestart = null; // No default prestart position
    private MoveAddress mPuzzleStart = MoveAddress.newRootAddress();
    private Set<GameState> mPuzzleStatesExplored = new HashSet<>();

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
            if(result < GameState.LOWEST_NONERROR_RESULT) {
                throw new IllegalStateException("Failed to make move " + m + "! Error: " + result);
            }

        }

        dumpHistory();

        variationsToPlay.sort((o1, o2) -> o1.address.getElements().size() - o2.address.getElements().size());

        for(GameSerializer.VariationContainer container : variationsToPlay) {
            Variation v = getVariationByAddress(new MoveAddress(container.address.getAllRootElements()));

            ReplayGameState rootForVariation;
            if(v == null) {
                MoveAddress rootStateAddress = new MoveAddress(container.address.getElementsBefore(container.address.getElements().size() - 2));
                rootForVariation = getStateByAddress(rootStateAddress);
                if(rootForVariation.getMoveAddress().getElements().size() == 1) {
                    rootForVariation = rootForVariation.getParent();
                }
            }
            else {
                // make a new variation off of the last thing in this variation.
                rootForVariation = v.getStates().get(v.getStates().size() - 1);
            }

            if(rootForVariation != null) {
                ReplayGameState inVariation = rootForVariation;
                for(DetailedMoveRecord m : container.moves) {
                    ReplayGameState state = inVariation.makeVariation(m);
                    if(state.getLastMoveResult() >= GameState.LOWEST_NONERROR_RESULT) {
                        inVariation = state;
                    }
                    else {
                        OpenTafl.logPrintln(OpenTafl.LogLevel.SILENT, "Failed to apply move: " + m + " with result " + state.getLastMoveResult());
                        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Variation container address: " + container.address);
                        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Root state address: " + inVariation.getMoveAddress());
                        break;
                    }
                }
            }
            else {
                OpenTafl.logPrintln(OpenTafl.LogLevel.SILENT, "Failed to find root for variation container: " + container);
            }
        }

        mMoveHistory = movesToPlay;
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Created replay game with moves: " + mMoveHistory);
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "And variations: " + variationsToPlay);
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Game history after load: " + mGame.getHistory());
        setCurrentState((ReplayGameState) mGame.getHistory().get(0));

        setupFirstStatesList();
        setupTimeSpecLists();

        Map<String, String> tagMap = game.getTagMap();
        if(tagMap.containsKey("puzzle-mode")) {
            if(tagMap.get("puzzle-mode").equals("loose")) {
                mMode = ReplayMode.PUZZLE_LOOSE;
            }
            else if(tagMap.get("puzzle-mode").equals("strict")) {
                mMode = ReplayMode.PUZZLE_STRICT;
            }

            if(mMode != ReplayMode.REPLAY) {
                if (tagMap.containsKey("puzzle-prestart")) {
                    MoveAddress address = MoveAddress.parseAddress(tagMap.get("puzzle-prestart"));
                    if (address != null) {
                        mPuzzlePrestart = address;
                        mPuzzleStart = address;
                    }
                }

                if (tagMap.containsKey("puzzle-start")) {
                    MoveAddress address = MoveAddress.parseAddress(tagMap.get("puzzle-start"));
                    if (address != null) {
                        mPuzzleStart = address;
                    }
                }

                MoveAddress startPosition = (mPuzzlePrestart != null ? mPuzzlePrestart : mPuzzleStart);
                ReplayGameState state = getStateByAddress(startPosition);
                if(state != null) {
                    setCurrentState(state);
                }
            }
        }

        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "mode/prestart/start " + mMode + "/" + mPuzzlePrestart + "/" + mPuzzleStart);
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

    public ReplayMode getMode() { return mMode; }

    public void setMode(ReplayMode mode) {
        mMode = mode;
    }

    public void markDirty() {
        mDirty = true;
    }

    public void markClean() {
        mDirty = false;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public boolean isInPuzzlePrestart() {
        ReplayGameState currentState = getCurrentState();
        ReplayGameState puzzlePrestart = (mPuzzlePrestart == null ? null : getStateByAddress(mPuzzlePrestart));
        ReplayGameState puzzleStart = getStateByAddress(mPuzzleStart);

        if(mMode == ReplayMode.REPLAY) return false; // If we aren't in a puzzle, we aren't in prestart
        if(puzzlePrestart == null) return false; // If there is no prestart, obviously, we aren't in it
        if(puzzleStart.getMoveAddress().equals(puzzlePrestart.getMoveAddress())) return false; // If start and prestart are the same, we can't be in the latter

        // If we are or are after the puzzle prestart position, but aren't and aren't after the puzzle start position, we're in prestart.
        return currentState.getMoveAddress().isOrIsAfter(mPuzzlePrestart) && currentState.getMoveAddress().isBefore(mPuzzleStart);
    }

    public String getReplayModeInGameHistoryString() {
        if(mMode.isPuzzleMode()) {
            List<GameState> truncatedHistory = new ArrayList<>();
            MoveAddress startAddress = (mPuzzlePrestart != null ? mPuzzlePrestart : mPuzzleStart);
            ReplayGameState state = getStateByAddress(startAddress);

            while(state != null) {
                MoveAddress address = state.getMoveAddress();
                if(address.isBetween(mPuzzlePrestart, mPuzzleStart) || mPuzzleStatesExplored.contains(state)) {
                    truncatedHistory.add(state);
                }
                state = state.getCanonicalChild();
            }

            return getHistoryString(truncatedHistory, getCurrentState().getMoveAddress(), false, false);
        }
        else {
            return getHistoryString(mGame.getHistory(), getCurrentState().getMoveAddress(), false, false);
        }
    }

    // Codes for the navigational methods
    public enum NavigationResult {
        SUCCESS,
        INVALID_ARGUMENT,
        END_OF_GAME,
        PUZZLE_DISALLOWS_NAVIGATION
    }
    public NavigationResult nextState(int childVariation) {
        ReplayGameState state = getCurrentState();
        ReplayGameState nextState;
        if(childVariation == CommandParser.ReplayNext.CANONICAL_CHILD) {
            nextState = state.getCanonicalChild();
        }
        else {
            List<Variation> variations = state.getVariations();
            if(variations.size() > childVariation - 1) {
                nextState = variations.get(childVariation - 1).getRoot();
            }
            else {
                nextState = null;
            }
        }

        if (nextState != null) {
            if (mMode.isPuzzleMode()) {
                if (nextState.getMoveAddress().isAfter(mPuzzleStart) && mPuzzleStatesExplored.contains(nextState)) {
                    setCurrentState(nextState);
                    return NavigationResult.SUCCESS;
                }
                else if (nextState.getMoveAddress().isAfter(mPuzzlePrestart) && nextState.getMoveAddress().isOrIsBefore(mPuzzleStart)) {
                    setCurrentState(nextState);
                    return NavigationResult.SUCCESS;
                }
                else {
                    return NavigationResult.PUZZLE_DISALLOWS_NAVIGATION;
                }
            }
            else {
                setCurrentState(nextState);
                return NavigationResult.SUCCESS;
            }
        }
        else {
            return NavigationResult.END_OF_GAME;
        }
    }

    public NavigationResult previousState() {
        ReplayGameState state = getCurrentState();
        ReplayGameState previousState = state.getParent();

        if(previousState != null) {
            if(mMode.isPuzzleMode()) {
                if (previousState.getMoveAddress().isAfter(mPuzzleStart) && mPuzzleStatesExplored.contains(previousState)) {
                    setCurrentState(previousState);
                    return NavigationResult.SUCCESS;
                }
                else if (previousState.getMoveAddress().isOrIsAfter(mPuzzlePrestart) && previousState.getMoveAddress().isOrIsBefore(mPuzzleStart)) {
                    setCurrentState(previousState);
                    return NavigationResult.SUCCESS;
                }
                else {
                    return NavigationResult.PUZZLE_DISALLOWS_NAVIGATION;
                }
            }
            else {
                setCurrentState(previousState);
                return NavigationResult.SUCCESS;
            }
        }
        else {
            return NavigationResult.INVALID_ARGUMENT;
        }
    }

    public NavigationResult setPositionByAddress(MoveAddress address) {
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

        // Don't allow jumping to before the puzzle start (or prestart, if present)
        if(mMode.isPuzzleMode()) {
            if (mPuzzlePrestart != null && address.isBefore(mPuzzlePrestart)) return NavigationResult.PUZZLE_DISALLOWS_NAVIGATION;
            else if (mPuzzlePrestart == null && address.isBefore(mPuzzleStart)) return NavigationResult.PUZZLE_DISALLOWS_NAVIGATION;

            if(address.isAfter(mPuzzleStart) && !mPuzzleStatesExplored.contains(state)) {
                return NavigationResult.PUZZLE_DISALLOWS_NAVIGATION;
            }
        }

        if(state != null) {
            setCurrentState(state);
            return NavigationResult.SUCCESS;
        }
        else {
            return NavigationResult.INVALID_ARGUMENT;
        }
    }

    public void prepareForGameStart() {
        ReplayGameState state = getStateByAddress(MoveAddress.newRootAddress());
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

        if(mMode.isPuzzleMode()) {
            mPuzzleStatesExplored.add(replayGameState);
        }

        mGame.setCurrentState(replayGameState);
        mCurrentState = replayGameState;
    }

    public ReplayGameState getCurrentState() {
        return mCurrentState;
    }

    public boolean moveExistsFromCurrentState(MoveRecord move) {
        return moveExistsFromState(move, getCurrentState());
    }

    public boolean moveExistsFromState(MoveRecord move, ReplayGameState state) {
        boolean moveExists = false;

        if(state.getCanonicalChild() != null && state.getCanonicalChild().getEnteringMove().softEquals(move)) moveExists = true;

        for(Variation v : state.getVariations()) {
            if(v.getRoot().getEnteringMove().equals(move)) moveExists = true;
            if(moveExists) break;
        }

        return moveExists;
    }

    public ReplayGameState makeVariation(MoveRecord move) {
        boolean moveExists = moveExistsFromCurrentState(move);
        if(mMode == ReplayMode.PUZZLE_STRICT) {
            if(!moveExists) return new ReplayGameState(GameState.STRICT_PUZZLE_MISSING_MOVE);
        }

        ReplayGameState state = getCurrentState();

        // Don't allow variations after a victory
        if(state.getLastMoveResult() > GameState.HIGHEST_NONTERMINAL_RESULT) {
            return state;
        }

        ReplayGameState variationState = state.makeVariation(move);

        if(variationState.getLastMoveResult() >= GameState.LOWEST_NONERROR_RESULT) {
            // Don't set errors into current state, oy.

            // TODO: when this is flagified, correct the logic
            if(mMode.isPuzzleMode() && !moveExists && variationState.getLastMoveResult() == GameState.GOOD_MOVE) {
                variationState.setLastMoveResult(GameState.GOOD_MOVE_NOT_IN_PUZZLE);
            }

            setCurrentState(variationState);
            mDirty = true;
        }
        else {
            return new ReplayGameState(variationState.getLastMoveResult());
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
            mDirty = true;
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
        if(state.getMoveAddress().getElements().size() == 1) {
            state = state.getParent();
        }

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
            result = startingPoint.getParent().findVariationState(new MoveAddress(moveAddress.getNonRootElements()));
        }
        if(startingPoint != null && startingPoint.getMoveAddress().equals(moveAddress)) {
            result = startingPoint;
        }

        return result;
    }

    public ReplayGameState getStateByAddress(String s) {
        return getStateByAddress(MoveAddress.parseAddress(s));
    }

    public String getUncommentedHistoryString(boolean truncateRootMoves) {
        return getHistoryString(mGame.getHistory(), null, truncateRootMoves, false);
    }

    public String getCommentedHistoryString(boolean truncateRootMoves) {
        return getHistoryString(mGame.getHistory(), null, truncateRootMoves, true);
    }
    public static String getHistoryString(List<GameState> history, MoveAddress highlightAddress, boolean truncateRootMoves, boolean includeComments) {
        return getHistoryString(history, highlightAddress, truncateRootMoves, includeComments, "");
    }

    public static String getHistoryString(List<GameState> history, MoveAddress highlightAddress, boolean truncateRootMoves, boolean includeComments, String prefix) {
        StringBuilder resultString = new StringBuilder();
        int historyPosition = 0;
        ReplayGameState state = (ReplayGameState) history.get(historyPosition);
        List<ReplayGameState> currentTurn = new ArrayList<>();
        List<Variation> currentTurnVariations = new ArrayList<>();
        int currentTurnIndex = 0;

        while(state != null) {
            // Get all states in a given turn.
            if(state.getMoveAddress().getLastElement().rootIndex == currentTurnIndex) {
                currentTurn.add(state);
                if(state.getParent() != null && (state.getParent().getMoveAddress().getElements().size() == state.getMoveAddress().getElements().size())) {
                    currentTurnVariations.addAll(state.getParent().getVariations());
                }
            }
            else {
                finishHistoryTurn(currentTurn, currentTurnVariations, highlightAddress, resultString, truncateRootMoves, includeComments, prefix);

                currentTurn.clear();
                currentTurnVariations.clear();
                currentTurnIndex = state.getMoveAddress().getLastElement().rootIndex;
                currentTurn.add(state);
                if(state.getParent() != null && (state.getParent().getMoveAddress().getElements().size() == state.getMoveAddress().getElements().size())) {
                    currentTurnVariations.addAll(state.getParent().getVariations());
                }
            }

            historyPosition += 1;
            if(historyPosition == history.size()) {
                currentTurnVariations.addAll(state.getVariations()); // required for puzzles, where the last known state might have variations
                finishHistoryTurn(currentTurn, currentTurnVariations, highlightAddress, resultString, truncateRootMoves, includeComments, prefix);
                break;
            }
            state = (ReplayGameState) history.get(historyPosition);
        }

        return resultString.toString();
    }

    private static void finishHistoryTurn(List<ReplayGameState> currentTurn, List<Variation> currentTurnVariations, MoveAddress highlightAddress, StringBuilder resultString, boolean truncateRootMoves, boolean includeComments, String prefix) {
        boolean first = true;
        boolean emptyTurn = false;

        // 1. States in the current turn
        for(ReplayGameState turnState : currentTurn) {
            if(first) {
                if(turnState.getEnteringMove() == null) {
                    emptyTurn = true;
                    break;
                }

                first = false;

                int paddingCount = turnState.getMoveAddress().getLastElement().moveIndex;
                MoveAddress a = new MoveAddress(turnState.getMoveAddress());
                a.getLastElement().moveIndex = 0;

                if(truncateRootMoves && a.getElements().size() == 1) {
                    if(!includeComments) resultString.append(prefix);
                    resultString.append(a.getLastElement().rootIndex);
                    resultString.append(".");
                }
                else {
                    if(!includeComments) resultString.append(prefix);
                    resultString.append(a);
                }

                for(int i = 0; i < paddingCount; i++) {
                    resultString.append(" .....");
                }
                resultString.append(" ");
            }
            // else append b., c., etc.

            if(turnState.getMoveAddress().equals(highlightAddress)) {
                resultString.append(Ansi.UNDERLINE);
            }

            boolean appended = false;
            if (turnState.getEnteringMove() != null) {
                resultString.append(turnState.getEnteringMove());
                appended = true;
            }

            if(turnState.getMoveAddress().equals(highlightAddress)) {
                resultString.append(Ansi.UNDERLINE_OFF);
            }

            if(appended) {
                resultString.append(" ");
            }
        }
        if(!first) resultString.append("\n");

        // 2. Comments, if necessary
        if(includeComments && !emptyTurn && !first) {
            first = true;
            resultString.append("[");

            for(ReplayGameState turnState : currentTurn) {
                DetailedMoveRecord moveOfInterest = null;
                if (turnState.getEnteringMove() != null) moveOfInterest = (DetailedMoveRecord) turnState.getEnteringMove();

                if(moveOfInterest != null) {

                    String timeString = moveOfInterest.getTimeRemaining() != null ? moveOfInterest.getTimeRemaining().toString() + " " : "";
                    String commentString = moveOfInterest.getComment();

                    if(!first) resultString.append("|");
                    resultString.append(timeString);
                    resultString.append(commentString);
                }

                if(first) {
                    first = false;
                }
            }

            resultString.append("]");
            resultString.append("\n\n");
        }

        // 3. Variations
        for(Variation v : currentTurnVariations) {
            List<GameState> variationStates = new ArrayList<>();
            variationStates.addAll(v.getStates());
            resultString.append(getHistoryString(variationStates, highlightAddress, truncateRootMoves, includeComments, prefix + "   "));
            if(!includeComments) resultString.append("\n");

        }
    }

    public void dumpHistory() {
        for(GameState state : mGame.getHistory()) {
            ((ReplayGameState) state).dumpTree();
        }
    }
}
