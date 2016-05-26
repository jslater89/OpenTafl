package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.network.server.ServerClock;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.RawTerminal;
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
        this(rules, callback, timeSpec, false);
    }

    public Game(Rules rules, UiCallback callback, GameClock.TimeSpec timeSpec, boolean serverGame) {
        mGameRules = rules;

        if(timeSpec != null) {
            mClock = new GameClock(this, getRules().getAttackers(), getRules().getDefenders(), timeSpec);
            if(serverGame) {
                mClock.setServerMode(true);
            }
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

        // Add the starting state to the history.
        mHistory.add(mCurrentState);

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
            GameClock.TimeSpec clockLength = GameClock.getTimeSpecForGameNotationString(clockLengthString);

            mClock = new GameClock(this, getCurrentState().getAttackers(), getCurrentState().getDefenders(), clockLength);

            if(mTagMap.containsKey("time-remaining")) {
                String remainingTimeString = mTagMap.get("time-remaining");
                String[] remainingTimes = remainingTimeString.split(",");

                GameClock.TimeSpec attackerTime = GameClock.getTimeSpecForGameNotationString(remainingTimes[0]);
                GameClock.TimeSpec defenderTime = GameClock.getTimeSpecForGameNotationString(remainingTimes[1]);

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

    public List<GameState> getHistory() {
        return mHistory;
    }

    public String getHistoryString() {
        String gameRecord = "";
        gameRecord = Pattern.compile("\\[.*?\\]", Pattern.DOTALL).matcher(getCommentedHistoryString()).replaceAll("");
        gameRecord = Pattern.compile("\n\n").matcher(gameRecord).replaceAll("\n");

        return gameRecord;
    }

    public String getCommentedHistoryString() {
        String gameRecord = "";
        int turnCount = 1;

        int i = 0;
        List<GameState> thisTurn = new ArrayList<>();
        boolean startingSideAttackers = getRules().getStartingSide().isAttackingSide();
        boolean otherSideWent = false;
        while(i < getHistory().size()) {
            GameState s = getHistory().get(i++);
            if(s.getExitingMove() == null) break;

            if(!otherSideWent && s.getCurrentSide().isAttackingSide() == startingSideAttackers) {
                thisTurn.add(s);
            }
            else if(s.getCurrentSide().isAttackingSide() != startingSideAttackers) {
                thisTurn.add(s);
                otherSideWent = true;
            }
            else if(otherSideWent && s.getCurrentSide().isAttackingSide() == startingSideAttackers) {
                gameRecord += getCommentedStringForMoves(turnCount, thisTurn);

                thisTurn.clear();
                thisTurn.add(s);
                turnCount++;
                otherSideWent = false;
            }

        }

        if(thisTurn.size() > 0) {
            gameRecord += getCommentedStringForMoves(turnCount, thisTurn);
        }

        return gameRecord;
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

    // TODO: pass StateEvent object to notify UI of happenings
    // in callback
    public GameState advanceState(GameState currentState, GameState nextState, boolean advanceTurn, char berserkingTaflman, boolean recordState) {
        nextState.updateGameState(
                this,
                currentState,
                nextState.getBoard(),
                nextState.getAttackers(),
                nextState.getDefenders(),
                true, // update zobrist
                berserkingTaflman);

        mCurrentState = nextState;
        mHistory.add(mCurrentState);

        if(mClock != null) {
            mClock.slap(advanceTurn);
        }

        // Victory
        mCurrentState.checkVictory();
        return mCurrentState;
    }

    public boolean historyContainsHash(long zobrist) {
        for (GameState historical : mHistory) {
            if (historical.mZobristHash == zobrist) return true;
        }

        return false;
    }
}
