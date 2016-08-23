package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.engine.collections.RepetitionHashTable;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.UiCallback;

import java.util.*;
import java.util.regex.Pattern;

public class Game {
    public static class Tag {
        public static final String EVENT = "event";
        public static final String SITE = "site";
        public static final String DATE = "date";
        public static final String ROUND = "round";
        public static final String ATTACKERS = "attackers";
        public static final String DEFENDERS = "defenders";
        public static final String RESULT = "result";
        public static final String ANNOTATOR = "annotator";
        public static final String COMPILER = "compiler";
        public static final String TIME_CONTROL = "time-control";
        public static final String TIME_REMAINING = "time-remaining";
        public static final String TERMINATION = "termination";
        public static final String VARIANT = "variant";
        public static final String START_COMMENT = "start-comment";
        public static final String RULES = "rules";
        public static final String POSITION = "position";
    }
    public Game(long[][][] zobristTable, List<GameState> history, RepetitionHashTable repetitions) {
        if (!(this instanceof AiWorkspace)) {
            throw new IllegalArgumentException("Empty constructor is only for AiWorkspace!");
        }

        mZobristConstants = zobristTable;
        mHistory = history;

        // AiWorkspace undoes everything it does to this, so we don't need to instantiate a new one. Technically.
        mRepetitions = repetitions;
    }

    public Game(Rules rules, UiCallback callback) {
        this(rules, callback, null);
    }

    public Game(Rules rules, UiCallback callback, TimeSpec timeSpec) {
        this(rules, callback, timeSpec, false);
    }

    public Game(Rules rules, UiCallback callback, TimeSpec timeSpec, boolean serverGame) {
        mGameRules = rules;

        if(timeSpec != null) {
            mClock = new GameClock(this, timeSpec);
            if(serverGame) {
                mClock.setServerMode(true);
            }
        }

        int boardSquares = rules.getBoard().getBoardDimension() * rules.getBoard().getBoardDimension();
        mZobristConstants = new long[ZOBRIST_ELEMENTS][boardSquares][Taflman.ALL_TAFLMAN_TYPES.length];
        Random r = new XorshiftRandom(10201989);
        for (int i = 0; i < boardSquares; i++) {
            for (int j = 0; j < Taflman.ALL_TAFLMAN_TYPES.length; j++) {
                mZobristConstants[ZOBRIST_BOARD][i][j] = r.nextLong();
            }
        }

        mZobristConstants[ZOBRIST_STATE][ZOBRIST_TURN][ZOBRIST_TURN_ATTACKERS] = r.nextInt();
        mZobristConstants[ZOBRIST_STATE][ZOBRIST_TURN][ZOBRIST_TURN_DEFENDERS] = r.nextInt();

        mHistory = new ArrayList<GameState>();
        mRepetitions = new RepetitionHashTable();

        // Create a new state off of the game rules.
        mCurrentState = new GameState(this, mGameRules);

        // Add the starting state to the history.
        mHistory.add(mCurrentState);
        mRepetitions.increment(mCurrentState.mZobristHash);

        mCallback = callback;
    }

    // Used for replays
    public Game(Game copyGame) {
        // Primitives/final variables: not changed or copied by value anyway
        mZobristConstants = copyGame.mZobristConstants;
        mAverageBranchingFactor = copyGame.mAverageBranchingFactor;
        mAverageBranchingFactorCount = copyGame.mAverageBranchingFactorCount;

        // Things we don't need to copy: if they exist, we want the same ones
        mClock = copyGame.mClock;
        mCallback = copyGame.mCallback;
        mGameRules = copyGame.mGameRules;
        mTagMap = copyGame.mTagMap;

        // Game states: we want copies of these.
        mHistory = new ArrayList<>();
        for(GameState copyState : copyGame.getHistory()) {
            GameState copied = new GameState(copyState);
            copied.mGame = this;
            mHistory.add(copied);
        }
        mCurrentState = mHistory.get(mHistory.size() - 1);

        // We need a new one here: the replay will play out the whole game in its copy, remember.
        mRepetitions = new RepetitionHashTable();
    }

    // Two parts to the main zobrist array: the board array (space index, piece type)
    // and the state array: currently only turn, may also include other state-specific
    // information.
    public static final int ZOBRIST_ELEMENTS = 2;

    // The zobrist board arrays are at index 0 in the zobrist array.
    public static final int ZOBRIST_BOARD = 0;

    // The zobrist state arrays are at index 1 in the zobrist array.
    public static final int ZOBRIST_STATE = 1;

    // The zobrist turn array is the first index in the zobrist state array.
    public static final int ZOBRIST_TURN = 0;
    public static final int ZOBRIST_TURN_ATTACKERS = 0;
    public static final int ZOBRIST_TURN_DEFENDERS = 1;

    public final long[][][] mZobristConstants;
    public double mAverageBranchingFactor = 0;
    public int mAverageBranchingFactorCount = 0;
    private RepetitionHashTable mRepetitions;
    private GameClock mClock;
    private UiCallback mCallback;
    private Rules mGameRules;
    private GameState mCurrentState;
    private List<GameState> mHistory;
    private Map<String, String> mTagMap = new LinkedHashMap<String, String>();

    public void start() {
        if(mClock != null) {
            mClock.start(getCurrentSide());
        }
    }

    public void finish() {
        if(mClock != null) {
            mClock.stop();
        }
    }

    public void setTagMap(Map<String, String> tagMap) {
        mTagMap = tagMap;
    }

    public Map<String, String> getTagMap() {
        return mTagMap;
    }

    public UiCallback getUiCallback() {
        return mCallback;
    }

    public void loadClock() {
        if(mTagMap != null && mTagMap.containsKey("time-control")) {
            String clockLengthString = mTagMap.get("time-control");
            TimeSpec clockLength = GameClock.getTimeSpecForGameNotationString(clockLengthString);

            mClock = new GameClock(this, clockLength);

            if(mTagMap.containsKey("time-remaining")) {
                String remainingTimeString = mTagMap.get("time-remaining");
                String[] remainingTimes = remainingTimeString.split(",");

                TimeSpec attackerTime = GameClock.getTimeSpecForGameNotationString(remainingTimes[0]);
                TimeSpec defenderTime = GameClock.getTimeSpecForGameNotationString(remainingTimes[1]);

                GameClock.ClockEntry attackerClock = mClock.getClockEntry(true);
                attackerClock.setTime(attackerTime);

                GameClock.ClockEntry defenderClock = mClock.getClockEntry(false);
                defenderClock.setTime(defenderTime);
            }
        }
    }

    public GameClock getClock() { return mClock; }

    public void setClock(GameClock clock) { mClock = clock; }

    public void setUiCallback(UiCallback mCallback) {
        this.mCallback = mCallback;
    }

    public Rules getRules() {
        return mGameRules;
    }

    public void setGameRules(Rules mGameRules) {
        this.mGameRules = mGameRules;
    }

    public void setCurrentState(GameState state) {
        mCurrentState = state;
    }

    public GameState getCurrentState() {
        return mCurrentState;
    }

    public RepetitionHashTable getRepetitions() {
        return mRepetitions;
    }

    public List<GameState> getHistory() {
        return mHistory;
    }

    public List<DetailedMoveRecord> getMoveHistory() {
        List<DetailedMoveRecord> moves = new ArrayList<>(mHistory.size());
        for(GameState state : mHistory) {
            DetailedMoveRecord m = state.mDetailedExitingMove;
            if(m != null) {
                moves.add(m);
            }
            else if(state.getExitingMove() != null) {
                throw new IllegalStateException("Missing detailed exiting move");
            }
        }

        return moves;
    }

    public String getHistoryString() {
        ReplayGame rg = ReplayGame.copyGameToReplay(this);
        return rg.getUncommentedHistoryString(true);
    }

    public String getCommentedHistoryString() {
        ReplayGame rg = ReplayGame.copyGameToReplay(this);
        return rg.getCommentedHistoryString(true);
    }

    private String getCommentedStringForMoves(int turnNumber, List<GameState> states) {


        String commentedString = turnNumber + ". ";

        for(GameState state : states) {
            commentedString += ((DetailedMoveRecord) state.getExitingMove()) + " ";
        }
        commentedString += "\n";

        commentedString += "[";
        for(GameState state : states) {
            DetailedMoveRecord m = (DetailedMoveRecord) state.getExitingMove();
            String timeString = m.getTimeRemaining() != null ? m.getTimeRemaining().toString() + " " : "";
            commentedString += "|" + timeString + m.getComment();
        }
        commentedString = commentedString.replaceFirst("\\|", "");
        commentedString += "]\n";

        return commentedString;
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

    public GameState advanceState(GameState currentState, GameState nextState, boolean advanceTurn, char berserkingTaflman, boolean recordState) {
        nextState.updateGameState(
                this,
                currentState,
                nextState.getBoard(),
                nextState.getAttackers(),
                nextState.getDefenders(),
                true, // update zobrist
                berserkingTaflman);

        if(recordState) {
            mCurrentState = nextState;
            mHistory.add(mCurrentState);
            mRepetitions.increment(mCurrentState.mZobristHash);

            if (mClock != null) {
                mClock.slap(advanceTurn);
            }

            // Victory
            mCurrentState.checkVictory();
            return mCurrentState;
        }
        else {
            nextState.checkVictory();
            return nextState;
        }
    }

    public boolean historyContainsHash(long zobrist) {
        return mRepetitions.getRepetitionCount(zobrist) > 0;
    }
}
