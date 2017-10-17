package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.engine.ai.tables.HistoryTable;
import com.manywords.softworks.tafl.engine.ai.tables.KillerMoveTable;
import com.manywords.softworks.tafl.engine.ai.tables.TranspositionTable;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.ui.UiCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AiWorkspace extends Game {
    private static final long POST_SEARCH_PAD = 500;

    private static String lastRulesString = "";
    public static TranspositionTable transpositionTable = null;
    public static KillerMoveTable killerMoveTable = null;
    public static HistoryTable historyTable = null;
    public static Evaluator evaluator = new FishyEvaluator();

    private int mTranspositionTableSize = 5;
    private static final int KILLER_MOVES = 3;

    private static final DecimalFormat doubleFormat = new DecimalFormat("#.00");

    private Game mGame;
    private TimeSpec mClockLength;
    private TimeSpec mTimeRemaining;
    private GameTreeState mStartingState;
    private GameTreeState mPreviousStartingState;
    private GameTreeNode mBestStatePreHorizon;

    public long[] mAlphaCutoffs;
    public long[] mAlphaCutoffDistances;
    public long[] mBetaCutoffs;
    public long[] mBetaCutoffDistances;
    public static long[] mLastTimeToDepth;
    public static int[] mTimeToDepthAge;
    public int mRepetitionsIgnoreTranspositionTable = 0;

    private long mPrepStartTime;
    private long mPrepEndTime;
    private long mThinkStartTime;
    private long mThinkEndTime;

    private int mLastDepth;
    private int mDeepestSearch;

    // These are only used internally
    private boolean mIterativeDeepening = true;
    private boolean mUseContinuationSearch = true;
    private boolean mUseHorizonSearch = true;
    private boolean mUseKillerMoves = true; // sets table size to 0, so it ignores puts
    private int mUseTranspositionTable = TRANSPOSITION_TABLE_ON; // sets table size to 0, so it ignores puts

    // These need getters
    private boolean mAlphaBetaPruning = true;
    private boolean mDoMoveOrdering = true;
    private boolean mUseHistoryTable = true;

    /**
     * In depth from the root, inclusive (root == 0, e.g. depth 5 searches to nodes with depth of 5)
     * Continuation and horizon searches can exceed mMaxDepth
     */
    private int mMaxDepth = 25;

    /**
     * In milliseconds
     */
    private long mThinkTime = -1;
    private long mMainMillis = -1;
    private long mHorizonMillis = -1;
    private long mMaxThinkTime = -1;

    public boolean mNoTime = false;
    public boolean mContinuationTime = false;
    public boolean mHorizonTime = false;
    private boolean mBenchmarkMode = false;

    private final Object mTimeLock = new Object();

    private AiThreadPool mThreadPool;
    private GameState mOriginalStartingState;
    private UiCallback mUiCallback;

    public boolean chatty = Log.level == Log.Level.VERBOSE;
    public boolean silent = Log.level == Log.Level.CRITICAL;

    public AiWorkspace(UiCallback ui, Game startingGame, GameState startingState, int transpositionTableSize) {
        super(startingGame.mZobristConstants, startingGame.getHistory(), startingGame.getRepetitions());
        mPrepStartTime = System.currentTimeMillis();

        evaluator.initialize(startingGame.getRules());

        if(mLastTimeToDepth == null || mLastTimeToDepth.length != (mMaxDepth + 1)) {
            mLastTimeToDepth = new long[mMaxDepth + 1];
            mTimeToDepthAge = new int[mMaxDepth + 1];
        }

        mTranspositionTableSize = transpositionTableSize;
        setGameRules(startingGame.getRules());

        mOriginalStartingState = startingState;
        mGame = startingGame;
        mUiCallback = ui;
        mThreadPool = new AiThreadPool(Math.min(1, Runtime.getRuntime().availableProcessors() - 1));

        // If the transposition table is null, the size is different, or the rules are different, create a new transposition table.
        if (mUseTranspositionTable > 0 && (transpositionTable == null || transpositionTable.size() != transpositionTableSize || !RulesSerializer.rulesEqual(startingGame.getRules().getOTRString(false), lastRulesString))) {
            Log.println(Log.Level.NORMAL, "Creating new transposition table");
            if(transpositionTable == null) {
                Log.println(Log.Level.VERBOSE, "Table was null");
            }
            else {
                Log.println(Log.Level.VERBOSE, "Requested/size: " + transpositionTableSize + "/" + transpositionTable.size() + " Rules equal: " + RulesSerializer.rulesEqual(startingGame.getRules().getOTRString(false), lastRulesString));
            }

            if(mUseTranspositionTable == TRANSPOSITION_TABLE_ON) {
                transpositionTable = new TranspositionTable(mTranspositionTableSize);
            }
            else {
                transpositionTable = new TranspositionTable(mTranspositionTableSize, true);
            }
        }
        else if (mUseTranspositionTable == TRANSPOSITION_TABLE_OFF) {
            transpositionTable = new TranspositionTable(0);
        }

        if (mUseKillerMoves && (killerMoveTable == null || killerMoveTable.getDepth() != mMaxDepth || killerMoveTable.movesToKeep() != KILLER_MOVES)) {
            killerMoveTable = new KillerMoveTable(mMaxDepth, KILLER_MOVES);
        }
        else {
            killerMoveTable = new KillerMoveTable(mMaxDepth, 0);
        }

        killerMoveTable.reset();

        if (mUseHistoryTable && (historyTable == null || historyTable.getDimension() != getRules().boardSize || !RulesSerializer.rulesEqual(startingGame.getRules().getOTRString(false), lastRulesString))) {
            historyTable = new HistoryTable(getRules().boardSize);
        }

        lastRulesString = startingGame.getRules().getOTRString(false);
        mPrepEndTime = System.currentTimeMillis();

        Log.println(Log.Level.VERBOSE, "Spent on prep time: " + (mPrepEndTime - mPrepStartTime) + "ms");
    }

    public static void resetTranspositionTable() {
        transpositionTable = new TranspositionTable((transpositionTable != null? transpositionTable.size() : 5));
    }

    public void setMaxDepth(int depth) {
        mMaxDepth = depth;

        if (mUseKillerMoves && (killerMoveTable == null || killerMoveTable.getDepth() != mMaxDepth || killerMoveTable.movesToKeep() != KILLER_MOVES)) {
            killerMoveTable = new KillerMoveTable(mMaxDepth, KILLER_MOVES);
        }

        mLastTimeToDepth = new long[mMaxDepth + 1];
        mTimeToDepthAge = new int[mMaxDepth + 1];
    }

    public void setBenchmarkMode(boolean on) {
        mBenchmarkMode = on;
    }

    public void allowCutoffs(boolean allow) {
        mAlphaBetaPruning = allow;
    }

    // Getters and setters for turning things on and off
    public void allowIterativeDeepening(boolean allow) {
        mIterativeDeepening = allow;
    }

    public void allowContinuation(boolean allow) {
        mUseContinuationSearch = allow;
    }

    public void allowHorizon(boolean allow) {
        mUseHorizonSearch = allow;
    }

    public void allowMoveOrdering(boolean allow) {
        mDoMoveOrdering = allow;
    }

    public void allowKillerMoves(boolean allow) {
        mUseKillerMoves = allow;
        if(allow) {
            killerMoveTable = new KillerMoveTable(mMaxDepth, KILLER_MOVES);
        }
        else {
            killerMoveTable = new KillerMoveTable(mMaxDepth, 0);
        }
    }

    public void allowHistoryTable(boolean allow) {
        mUseHistoryTable = allow;
    }

    public static final int TRANSPOSITION_TABLE_OFF = 0;
    public static final int TRANSPOSITION_TABLE_EXACT_ONLY = 1;
    public static final int TRANSPOSITION_TABLE_ON = 2;
    public void allowTranspositionTable(int mode) {
        mUseTranspositionTable = mode;
        if(mode == TRANSPOSITION_TABLE_ON) {
            transpositionTable = new TranspositionTable(mTranspositionTableSize);
        }
        else if(mode == TRANSPOSITION_TABLE_EXACT_ONLY) {
            transpositionTable = new TranspositionTable(mTranspositionTableSize, true);
        }
        else {
            transpositionTable = new TranspositionTable(0);
        }
    }

    public boolean areCutoffsAllowed() {
        return mAlphaBetaPruning;
    }

    public boolean isMoveOrderingAllowed() {
        return mDoMoveOrdering;
    }

    public boolean isHistoryTableAllowed() {
        return mUseHistoryTable;
    }

    public void setTimeRemaining(TimeSpec length, TimeSpec entry) {
        mClockLength = length;
        mTimeRemaining = entry;
    }

    public void crashStop() {
        mNoTime = true;
    }

    private long planTimeUsage(Game g, TimeSpec entry) {
        // Math.minned against
        if(entry == null) return Long.MAX_VALUE;
        // Aim to make a certain number of moves in main time, using overtimes
        // for the rest.
        int boardDimension = g.getRules().getBoard().getBoardDimension();
        int mainTimeMoves;
        if(boardDimension == 7) {
            mainTimeMoves = 6;
        }
        else if(boardDimension == 9) {
            mainTimeMoves = 10;
        }
        else mainTimeMoves = 20;

        Log.println(Log.Level.VERBOSE, "Time remaining: " + entry);

        // Moves the current side has made
        int movesMade = g.getHistory().size() / 2;
        int movesLeft = mainTimeMoves - movesMade;
        long mainTime = mClockLength.mainTime;
        long overtimeTime = mClockLength.overtimeTime;
        long overtimeCount = mClockLength.overtimeCount;
        long incrementTime = mClockLength.incrementTime;

        if(overtimeCount == 0 || overtimeTime == 0) {
            if(movesMade < mainTimeMoves / 2) {
                // Opening game: use 5% of the time
                long openingTime = (long) (mainTime * 0.05);
                return openingTime / (mainTimeMoves / 2) + (long)(incrementTime * 0.9);
            }
            else if (entry.mainTime > mainTime * 0.2) {
                // Midgame: expect to make about mainTimeMoves for the next 75% of the time.
                long midgameTime = (long) (mainTime * 0.75);
                return midgameTime / (mainTimeMoves) + (long)(incrementTime * 0.9);
            }
            else {
                // Endgame: use a constant portion of the main time, until that turns out to be three seconds,
                // then just use three seconds and hope we're close enough to a win to make that work.
                long remainingTime = entry.mainTime / mainTimeMoves;
                return Math.max(remainingTime, 3);
            }
        }
        else {
            // If we do have overtime, work out the time we have left per main time move and return that. Otherwise
            // return the overtime time.
            long mainTimeRemaining = entry.mainTime;

            // If we still want to make moves in main time, and if main time would allow us to consider those moves
            // for longer than just spending one overtime plus leftover main time every time, figure that out.
            if(movesLeft > 0 && (mainTimeRemaining > movesLeft * overtimeTime)) {
                long timePerMove = mainTimeRemaining / movesLeft;
                if(mainTimeRemaining + overtimeTime > timePerMove) {
                    Log.println(Log.Level.VERBOSE, "Spending main time only");
                    return timePerMove;
                }
                else {
                    Log.println(Log.Level.VERBOSE, "Using an overtime period plus main time");
                    return mainTimeRemaining + overtimeTime;
                }
            }
            else {
                // Be very careful with time if we only have one overtime and no main time!
                if(entry.overtimeCount == 1 && mainTimeRemaining == 0) {
                    Log.println(Log.Level.VERBOSE, "Emergency overtime use");
                    return entry.overtimeTime - 1000;
                }
                else if(movesLeft > 0 && mainTimeRemaining > 0) {
                    Log.println(Log.Level.VERBOSE, "Dividing main time into overtime");
                    long timePerMove = mainTimeRemaining / movesLeft;

                    // Save half a second, just to avoid using extra overtimes
                    return entry.overtimeTime + timePerMove - 750;
                }
                else {
                    // Save half a second, just to avoid using extra overtimes
                    Log.println(Log.Level.VERBOSE, "Overtime only");
                    return mainTimeRemaining + entry.overtimeTime - 750;
                }
            }
        }
    }

    private long estimatedTimeToDepth(int depth) {
        if(depth <= 1) return 0;
        if(depth >= mLastTimeToDepth.length) return 3600 * 1000;

        if(mTimeToDepthAge[depth] > 3) mLastTimeToDepth[depth] = 0;
        if(mLastTimeToDepth[depth] != 0) return mLastTimeToDepth[depth];

        long result = (mLastTimeToDepth[depth - 1] * ((depth) % 2 == 0 ? 10 : 19));

        if(result == 0) return estimatedTimeToDepth(depth - 1) * ((depth) % 2 == 0 ? 10 : 19);
        else return result;
    }

    private boolean canSearchToDepth(int depth) {
        long timeLeft = mThinkStartTime + mThinkTime - System.currentTimeMillis();

        // We can start a deeper search
        return mBenchmarkMode || !(isTimeCritical() || timeLeft < (estimatedTimeToDepth(depth)));
    }

    private boolean isTimeCritical() {
        long timeLeft = mThinkStartTime + mThinkTime - System.currentTimeMillis();
        return timeLeft < (long) Math.min(mThinkTime * 0.05, POST_SEARCH_PAD);
    }

    public void explore(int maxThinkTime) {
        mThinkStartTime = System.currentTimeMillis();
        Log.println(Log.Level.VERBOSE, "In between time: " + (mThinkStartTime - mPrepEndTime) + "ms");
        transpositionTable.resetTableStats();
        mRepetitionsIgnoreTranspositionTable = 0;
        mBestStatePreHorizon = null;

        if(maxThinkTime == 0) maxThinkTime = 86400;
        mMaxThinkTime = maxThinkTime * 1000;

        if(mTimeRemaining == null && mGame.getClock() != null) {
            mClockLength = mGame.getClock().toTimeSpec();
            mTimeRemaining = mGame.getClock().getClockEntry(mGame.getCurrentSide()).toTimeSpec();
        }
        long desiredTime = planTimeUsage(mGame, mTimeRemaining);
        mThinkTime = Math.min(desiredTime, mMaxThinkTime);

        // Spend 85% of the time on main search, and 10% on continuation searches (implicitly), and 5% on horizon searches.
        mMainMillis = (long) (mThinkTime * 0.85);
        mHorizonMillis = Math.max((long) (mThinkTime * 0.05) - POST_SEARCH_PAD, (long) 750);

        if(chatty && mUiCallback != null) mUiCallback.statusText("Using " + mThinkTime + "msec, desired " + desiredTime);

        Timer t = new Timer();

        // n.b. if delays are 0, then we've been called with very little time, so we should just do our best and
        // get out with a move.
        // Main search happens here

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (mTimeLock) {
                    mContinuationTime = true;
                }
            }
        }, mMainMillis);

        // Continuation search happens here

        long delay = mThinkTime - mHorizonMillis;
        if(delay > 0) {
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (mTimeLock) {
                        if (mUseHorizonSearch) mHorizonTime = true;
                    }
                }
            }, delay);
        }

        // Horizon search happens here

        delay = mThinkTime - POST_SEARCH_PAD;
        if(delay > 0) {
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (mTimeLock) {
                        mNoTime = true;
                    }
                }
            }, mThinkTime - POST_SEARCH_PAD);
        }

        // Time's up!

        boolean firstExtension = true;
        boolean firstHorizon = true;
        final int continuationLimit = 5;
        int continuationDepth;

        int horizonDepth = 2;
        final int horizonCount = 5;
        final int horizonLimit = 6;
        int currentHorizonDepth = 0;
        int horizonStart = 0;
        int deepestSearch = 1;
        int horizonIterations = 0;
        int depth;
        int treeSizePreExtension = 0;

        GameTreeState.workspace = this;

        // Do the main search
        depth = 1;
        if(!mIterativeDeepening) depth = mMaxDepth;

        for (;depth <= mMaxDepth;) {
            mLastDepth = depth;

            if (canSearchToDepth(depth)) {
                if (isTimeCritical() || mNoTime || mHorizonTime || mContinuationTime) {
                    break;
                }

                mAlphaCutoffs = new long[mMaxDepth];
                mAlphaCutoffDistances = new long[mMaxDepth];
                mBetaCutoffs = new long[mMaxDepth];
                mBetaCutoffDistances = new long[mMaxDepth];

                long start = System.currentTimeMillis();
                mStartingState = new GameTreeState(this, new GameState(mOriginalStartingState));
                mStartingState.explore(depth, mMaxDepth, Short.MIN_VALUE, Short.MAX_VALUE, mThreadPool, false);
                long finish = System.currentTimeMillis();

                double timeTaken = (finish - start) / 1000d;

                // If we aren't out of time, we can save this tree as a known good tree.
                // Otherwise, we should restore the previous tree.
                if (!mHorizonTime && !mContinuationTime && !mNoTime) {
                    mPreviousStartingState = mStartingState;
                    mLastTimeToDepth[depth] = finish - start;
                    mTimeToDepthAge[depth] = 0;

                    int size = getGameTreeSize(depth);
                    double statesPerSec = size / ((finish - start) / 1000d);

                    if (chatty && mUiCallback != null) {
                        mUiCallback.statusText("Depth " + depth + " explored " + size + " states in " + timeTaken + " sec at " + doubleFormat.format(statesPerSec) + "/sec");
                    }

                    deepestSearch = depth;
                    depth++;
                }
                else {
                    if(mPreviousStartingState != null) mStartingState = mPreviousStartingState;

                    if(chatty && mUiCallback != null) {
                        mUiCallback.statusText("Failed to complete tree at depth " + depth);
                        mUiCallback.statusText("Time taken: " + timeTaken + " sec, which is " + doubleFormat.format((timeTaken / (mLastTimeToDepth[depth - 1] / 1000d))) + "x time to previous depth");
                    }

                    for(int i = depth; i < mMaxDepth; i++) {
                        mTimeToDepthAge[i]++;
                    }
                }
            }
            else {
                break;
            }
        }

        Log.println(Log.Level.VERBOSE, "MAIN SEARCH");
        //OpenTafl.println(OpenTafl.Level.VERBOSE, dumpEvaluationFor(0));



        continuationDepth = deepestSearch + 1;
        // If extension searches are allowed,
        if(mUseContinuationSearch) {
            long continuationStart = System.currentTimeMillis();
            while (true) {
                long continuationTime = mThinkTime - mHorizonMillis;
                long timeSpent = System.currentTimeMillis() - mThinkStartTime;
                long timeRemaining = continuationTime - timeSpent;
                long timeRequired = estimatedTimeToDepth(continuationDepth - 1) / 2;

                // We have to go through the whole tree again in continuation search, but since we've
                // already searched most of it, we'll get some nodes out of the search if we have
                // even a little time remaining.
                if (isTimeCritical() || mNoTime || (mUseHorizonSearch && (timeRemaining < timeRequired || mHorizonTime))) {
                    Log.println(Log.Level.VERBOSE,
                            "Skipping continuation search to depth " + continuationDepth + ": " + timeRemaining + "msec left, " + timeRequired
                                    + " required, critical/no/horizon time: " + isTimeCritical() + " " + mNoTime + " " + mHorizonTime);
                    break;
                }
                else {
                    Log.println(Log.Level.VERBOSE,
                            "Doing continuation search to depth " + continuationDepth + ": " + timeRemaining + "msec left, " + timeRequired
                                    + " required, critical/no/horizon time: " + isTimeCritical() + " " + mNoTime + " " + mHorizonTime);
                }

                if (firstExtension) {
                    firstExtension = false;
                    if (chatty && mUiCallback != null) {
                        mUiCallback.statusText("Running continuation search to fill time...");
                    }
                }

                if (continuationDepth >= mMaxDepth + continuationLimit) break;

                if (chatty && mUiCallback != null) {
                    mUiCallback.statusText("Continuation search at depth " + continuationDepth);
                }

                mStartingState.explore(continuationDepth, continuationDepth - 1, Short.MIN_VALUE, Short.MAX_VALUE, mThreadPool, true);

                if (continuationDepth > deepestSearch) deepestSearch = continuationDepth;
                continuationDepth++;
            }
            long continuationEnd = System.currentTimeMillis();

            double timeTaken = (continuationEnd - continuationStart) / 1000d;
            if (chatty && timeTaken > 0.5 && mUiCallback != null) {
                mUiCallback.statusText("Continuation searches took " + timeTaken + " sec");
            }


            Log.println(Log.Level.VERBOSE, "CONTINUATION SEARCH");
            //OpenTafl.println(OpenTafl.Level.VERBOSE, dumpEvaluationFor(0));
            //getTreeRoot().printTree("");
        }


        // Do the horizon search, looking quickly at the current best moves in the hopes of catching any dumb
        // refutations.

        if(mUseHorizonSearch) {
            /*
            Can we afford a search to depth 3?
            */

            final boolean isAttackingSide = mStartingState.getCurrentSide().isAttackingSide();
            getTreeRoot().getBranches().sort((o1, o2) -> {
                if(o1.getValue() > o2.getValue()) return isAttackingSide ? -1 : 1;
                if(o1.getValue() < o2.getValue()) return isAttackingSide ? 1 : -1;
                return 0;
            });

            mBestStatePreHorizon = getTreeRoot().getBranches().get(0);

            currentHorizonDepth = deepestSearch;
            while (true) {
                long timeRemaining = (mThinkTime - (System.currentTimeMillis() - mThinkStartTime - POST_SEARCH_PAD));
                if (!isTimeCritical()) {
                    if (firstHorizon) {
                        firstHorizon = false;
                        if (chatty && mUiCallback != null) {
                            mUiCallback.statusText("Running horizon on best moves...");
                        }
                    }

                    horizonDepth = 2;
                    while(timeRemaining > estimatedTimeToDepth(horizonDepth) * 2) {
                        horizonDepth++;
                    }

                    // Horizon depth -1 because it gets incremented before it fails the test.
                    horizonDepth = Math.min(horizonDepth - 1, deepestSearch - 1);
                    Log.println(Log.Level.VERBOSE, "Extra horizon depth: " + horizonDepth + " (in " + estimatedTimeToDepth(horizonDepth) + "/" + timeRemaining + ")");
                    Log.println(Log.Level.VERBOSE, "Current deepest search: " + deepestSearch);


                    // Do an extension search on the best known moves.
                    getTreeRoot().getBranches().sort((o1, o2) -> {
                        // Sort by value high to low
                        if (getTreeRoot().isMaximizingNode()) {
                            return -(o1.getValue() - o2.getValue());
                        } else {
                            // low to high
                            return (o1.getValue() - o2.getValue());
                        }
                    });

                    currentHorizonDepth += horizonDepth;

                    if (currentHorizonDepth > continuationDepth + horizonLimit) {
                        currentHorizonDepth = continuationDepth + horizonLimit;
                    }

                    if(currentHorizonDepth == deepestSearch) break;

                    // Fall out early if we've searched too deep

                    boolean certainVictory = true;
                    int horizonOptions = getTreeRoot().getBranches().size();

                    for (GameTreeNode branch : getTreeRoot().getBranches()) {
                        List<GameTreeNode> nodes = GameTreeState.getPathStartingWithNode(branch);
                        GameTreeNode n = nodes.get(nodes.size() - 1);
                        if (n.getVictory() == GameState.GOOD_MOVE) {
                            short oldValue = n.getValue();
                            n.explore(currentHorizonDepth, continuationDepth, n.getAlpha(), n.getBeta(), mThreadPool, false);

                            certainVictory = false;
                            n.revalueParent(n.getDepth());
                        }
                    }

                    Log.println(Log.Level.VERBOSE, "Ran horizon search at depth: " + currentHorizonDepth
                            + " starting index " + horizonStart + " ending index " + (horizonStart + horizonCount) + " with " + timeRemaining + "msec");


                    if (currentHorizonDepth > deepestSearch) deepestSearch = currentHorizonDepth;

                    if (certainVictory) {
                        Log.println(Log.Level.VERBOSE, "Quitting horizon search: no nodes left to search, or certain victory");
                        break;
                    }
                    horizonIterations++;
                } else {
                    Log.println(Log.Level.VERBOSE, "Quitting horizon search: out of time: " + timeRemaining + "msec left");
                    break;
                }
            }

            Log.println(Log.Level.VERBOSE, "HORIZON SEARCH");
            //OpenTafl.println(OpenTafl.Level.VERBOSE, dumpEvaluationFor(0));
            //getTreeRoot().printTree("");
        }

        mDeepestSearch = deepestSearch;
        mThinkEndTime = System.currentTimeMillis();
        Log.println(Log.Level.NORMAL, "Think time: " + (mThinkEndTime - mThinkStartTime) + "ms");
        Log.println(Log.Level.NORMAL, "Overall time spent on search: " + (mThinkEndTime - mPrepStartTime) + "ms");
    }

    public void printSearchStats() {
        int nodes = getGameTreeSize(mLastDepth);
        int fullNodes = getGameTreeSize(mDeepestSearch);
        int size = getTreeRoot().mBranches.size();
        double observedBranching = ((mGame.mAverageBranchingFactor * mGame.mAverageBranchingFactorCount) + size) / (++mGame.mAverageBranchingFactorCount);
        mGame.mAverageBranchingFactor = observedBranching;

        if(chatty && mUiCallback != null) {
//            mUiCallback.statusText("# cutoffs/avg. to 1st a/b a/b");
//            for (int i = 0; i < mAlphaCutoffs.length && i < mDeepestSearch; i++) {
//                String line = "Depth " + i + ": " + mAlphaCutoffs[i] + "/" + mBetaCutoffs[i];
//                if (mAlphaCutoffDistances[i] > 0) {
//                    line += " " + mAlphaCutoffDistances[i] / mAlphaCutoffs[i];
//                } else {
//                    line += " 0";
//                }
//                line += "/";
//
//                if (mBetaCutoffDistances[i] > 0) {
//                    line += "" + mBetaCutoffDistances[i] / mBetaCutoffs[i];
//                } else {
//                    line += "0";
//                }
//                mUiCallback.statusText(line);
//            }


            mUiCallback.statusText("Finding best state...");
            GameTreeNode bestMove = getTreeRoot().getBestChild();
            mUiCallback.statusText("Best move: " + bestMove.getRootMove() + " with path...");

            List<GameTreeNode> bestPath = getTreeRoot().getBestPath();

            /* Saving this: handy for debugging issues with searches not getting to the bottom of trees
            for(GameTreeNode child : getTreeRoot().getBranches()) {
                List<GameTreeNode> pathForChild = GameTreeState.getPathStartingWithNode(child);
                OpenTafl.print(OpenTafl.Level.VERBOSE, "" +  child.getEnteringMove() + ": " + pathForChild.size());

                String modifier = (pathForChild.get(pathForChild.size() - 1).valueFromTransposition() ? "T" : "");
                OpenTafl.println(OpenTafl.Level.VERBOSE, modifier);
            }
            */


            GameTreeNode leafNode = null;
            for (GameTreeNode node : bestPath) {
                mUiCallback.statusText("\t" + node.getEnteringMove());
                if(node.getBranches().size() == 0) leafNode = node;
            }
            mUiCallback.statusText("Best move scored " + bestMove.getValue());

            if(leafNode != null && leafNode.getVictory() > GameTreeState.HIGHEST_NONTERMINAL_RESULT && Math.abs(bestMove.getValue()) < 5000) {
                Log.println(Log.Level.VERBOSE, "Found an incorrect best path!");

                GameTreeNode node = getTreeRoot();
                Log.println(Log.Level.VERBOSE, "Root: " + node.getEnteringMoveSequence() + " value: " + node.getValue() + " children: " + node.getBranches().size() + " maximizing? " + node.isMaximizingNode());
                for(GameTreeNode child : node.getBranches()) {
                    Log.println(Log.Level.VERBOSE, "Child: " + child.getEnteringMove() + " value: " + child.getValue());
                }

                int depth = 1;
                for(GameTreeNode n : bestPath) {
                    for(int i = 0; i < depth; i++) Log.print(Log.Level.VERBOSE, "  ");

                    Log.println(Log.Level.VERBOSE, "Node: " + n.getEnteringMoveSequence() + " value: " + n.getValue() + " children: " + n.getBranches().size() + " maximizing? " + n.isMaximizingNode());
                    for(GameTreeNode child : n.getBranches()) {
                        for(int i = 0; i < depth; i++) Log.print(Log.Level.VERBOSE, "  ");
                        Log.println(Log.Level.VERBOSE, "Child: " + child.getEnteringMove() + " value: " + child.getValue());
                    }

                    if(depth + 1 < bestPath.size()) {
                        if(!n.getBestChild().getEnteringMove().softEquals(bestPath.get(depth).getEnteringMove())) {
                            for(int i = 0; i < depth; i++) Log.print(Log.Level.VERBOSE, "  ");
                            Log.println(Log.Level.VERBOSE, "Next node in best path has entering move: " + bestPath.get(depth).getEnteringMove() + " but our best child has: " + n.getBestChild().getEnteringMove());
                        }
                    }

                    depth++;
                }
            }

            mUiCallback.statusText("Transpositions ignored because of repetitions: " + mRepetitionsIgnoreTranspositionTable);
            mUiCallback.statusText("Observed/effective branching factor: " + doubleFormat.format(observedBranching) + "/" + doubleFormat.format(Math.pow(nodes, 1d / mLastDepth)));
            mUiCallback.statusText("Thought for: " + (mThinkEndTime - mThinkStartTime) + "msec. Tree sizes: main search " + nodes + " nodes, extension searches " + (fullNodes - nodes) + " nodes");
            mUiCallback.statusText("Overall speed: " + (fullNodes / ((mThinkEndTime - mThinkStartTime)/ 1000d)) + " nodes/sec");
            mUiCallback.statusText("Transposition table stats: " + transpositionTable.getTableStats());

            Log.println(Log.Level.VERBOSE, "Best state == best state pre-horizon? " + getTreeRoot().getBestChild().equals(mBestStatePreHorizon));
        }
    }

    public String dumpEvaluationFor(int childIndex) {
        ((FishyEvaluator) evaluator).debug = true;

        GameTreeNode node = getTreeRoot().getNthChild(childIndex);

        int depth = 0;
        for(int i = 0; i < getTreeRoot().getBranches().size(); i++) {
            if(getTreeRoot().getNthPath(i).size() > depth) {
                depth = getTreeRoot().getNthPath(i).size();
            }
        }

        List<GameTreeNode> sequence = new ArrayList<>();
        while(node != null) {
            sequence.add(node);
            node = node.getBestChild();
        }

        String debugString = "";
        FishyEvaluator e = (FishyEvaluator) evaluator;
        for(GameTreeNode n: sequence) {
            if(n instanceof MinimalGameTreeNode) {
                GameTreeState s = GameTreeState.getStateForNode(getTreeRoot(), (MinimalGameTreeNode) n);
                s.mCurrentMaxDepth = depth;
                evaluator.evaluate(s, s.mCurrentMaxDepth, s.mDepth);
                debugString += e.debugString + "\n-------------------------------------------\n";
            }
            else {
                GameTreeState s = (GameTreeState) n;
                s.mCurrentMaxDepth = depth;
                evaluator.evaluate(s, s.mCurrentMaxDepth, s.mDepth);
                debugString += e.debugString + "\n-------------------------------------------\n";
            }
        }

        ((FishyEvaluator) evaluator).debug = false;

        return debugString;
    }

    public void stopExploring() {
        mThreadPool.requestStop();
        System.gc();
    }

    public boolean isThreadPoolIdle() {
        return mThreadPool.checkFinished();
    }

    public GameTreeState getTreeRoot() {
        return mStartingState;
    }

    public int getGameTreeSize(int toDepth) {
        return mStartingState.countChildren(toDepth);
    }

    public List<List<MoveRecord>> getAllEnteringSequences() {
        return getTreeRoot().getAllEnteringSequences();
    }

    /**
     * The GameTreeStates handle all turn advancement.
     */
    @Override
    public GameState advanceState(GameState currentState, GameState nextState, boolean advanceTurn, char berserkingTaflman, boolean recordState) {
        nextState.updateGameState(
                this,
                currentState,
                nextState.getBoard(),
                nextState.getAttackers(),
                nextState.getDefenders(),
                true, // update Zobrist
                berserkingTaflman);

        return nextState;
    }
}
