package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.OpenTafl;
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
import java.util.*;

public class AiWorkspace extends Game {
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

    public long[] mAlphaCutoffs;
    public long[] mAlphaCutoffDistances;
    public long[] mBetaCutoffs;
    public long[] mBetaCutoffDistances;
    public static long[] mLastTimeToDepth;
    public static int[] mTimeToDepthAge;
    public int mRepetitionsIgnoreTranspositionTable = 0;

    private long mStartTime;
    private long mEndTime;

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
     */
    private int mMaxDepth = 25;

    /**
     * In milliseconds
     */
    private long mThinkTime = -1;
    private long mMainMillis = -1;
    private long mExtensionMillis = -1;
    private long mMaxThinkTime = -1;

    public boolean mNoTime = false;
    public boolean mExtensionTime = false;
    private boolean mBenchmarkMode = false;

    private final Object mTimeLock = new Object();

    private AiThreadPool mThreadPool;
    private GameState mOriginalStartingState;
    private UiCallback mUiCallback;

    public boolean chatty = OpenTafl.logLevel == OpenTafl.LogLevel.CHATTY;
    public boolean silent = OpenTafl.logLevel == OpenTafl.LogLevel.SILENT;

    public AiWorkspace(UiCallback ui, Game startingGame, GameState startingState, int transpositionTableSize) {
        super(startingGame.mZobristConstants, startingGame.getHistory(), startingGame.getRepetitions());

        if(mLastTimeToDepth == null) {
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
        if (mUseTranspositionTable > 0 && (transpositionTable == null || transpositionTable.size() != transpositionTableSize || !RulesSerializer.rulesEqual(startingGame.getRules().getOTRString(), lastRulesString))) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Creating new transposition table");
            if(transpositionTable == null) {
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Table was null");
            }
            else {
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Requested/size: " + transpositionTableSize + "/" + transpositionTable.size() + " Rules equal: " + RulesSerializer.rulesEqual(startingGame.getRules().getOTRString(), lastRulesString));
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

        if (mUseHistoryTable && (historyTable == null || historyTable.getDimension() != getRules().boardSize || !RulesSerializer.rulesEqual(startingGame.getRules().getOTRString(), lastRulesString))) {
            historyTable = new HistoryTable(getRules().boardSize);
        }

        lastRulesString = startingGame.getRules().getOTRString();
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

        // Moves the current side has made
        int movesMade = g.getHistory().size() / 2;
        int movesLeft = mainTimeMoves - movesMade;
        long mainTime = mClockLength.mainTime;
        long overtimeTime = mClockLength.overtimeTime;
        long overtimeCount = mClockLength.overtimeCount;
        long incrementTime = (mClockLength.incrementTime > 0 ? mClockLength.incrementTime : 3000);

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
                    return timePerMove;
                }
                else {
                    return mainTimeRemaining + overtimeTime;
                }
            }
            else {
                // Be very careful with time if we only have one overtime and no main time!
                if(entry.overtimeCount == 1 && mainTimeRemaining == 0) {
                    return entry.overtimeTime - 1000;
                }
                if(movesLeft > 0) {
                    long timePerMove = mainTimeRemaining / movesLeft;

                    // Save half a second, just to avoid using extra overtimes
                    return entry.overtimeTime + timePerMove - 500;
                }
                else {
                    // TODO: use multiple overtimes if things get dicey

                    // Save half a second, just to avoid using extra overtimes
                    return mainTimeRemaining + entry.overtimeTime - 500;
                }
            }
        }
    }

    private long estimatedTimeToDepth(int depth) {
        if(depth <= 1) return 0;
        if(depth >= mLastTimeToDepth.length) return 3600 * 1000;

        if(mTimeToDepthAge[depth] > 3) mLastTimeToDepth[depth] = 0;
        if(mLastTimeToDepth[depth] != 0) return mLastTimeToDepth[depth];

        return (long) (mLastTimeToDepth[depth - 1] * ((depth) % 2 == 0 ? 3.5 : 19));
    }

    private boolean canSearchToDepth(int depth) {
        long timeLeft = mStartTime + mThinkTime - System.currentTimeMillis();

        // We can start a deeper search
        return mBenchmarkMode || !(isTimeCritical() || timeLeft < (estimatedTimeToDepth(depth)));
    }

    private boolean isTimeCritical() {
        long timeLeft = mStartTime + mThinkTime - System.currentTimeMillis();
        return timeLeft < (long) Math.min(mThinkTime * 0.05, 250);
    }

    public void explore(int maxThinkTime) {
        transpositionTable.resetTableStats();
        mRepetitionsIgnoreTranspositionTable = 0;

        if(maxThinkTime == 0) maxThinkTime = 86400;
        mMaxThinkTime = maxThinkTime * 1000;

        mStartTime = System.currentTimeMillis();

        if(mTimeRemaining == null && mGame.getClock() != null) {
            mClockLength = mGame.getClock().toTimeSpec();
            mTimeRemaining = mGame.getClock().getClockEntry(mGame.getCurrentSide()).toTimeSpec();
        }
        long desiredTime = planTimeUsage(mGame, mTimeRemaining);
        mThinkTime = Math.min(desiredTime, mMaxThinkTime);
        if(chatty && mUiCallback != null) mUiCallback.statusText("Using " + mThinkTime + "msec, desired " + desiredTime);

        //mThreadPool.start();
        mMainMillis = (long) (mThinkTime * 0.85);
        mExtensionMillis = (long) (mThinkTime * 0.15) - 250;
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (mTimeLock) {
                    mExtensionTime = true;
                }
            }
        }, mMainMillis);

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (mTimeLock) {
                    mNoTime = true;
                };
            }
        }, mThinkTime - 250);

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
                if (isTimeCritical() || mNoTime || mExtensionTime) {
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


                // If we aren't out of time, we can save this tree as a known good tree.
                // Otherwise, we should restore the previous tree.
                if (!mExtensionTime && !mNoTime) {
                    mPreviousStartingState = mStartingState;
                    mLastTimeToDepth[depth] = finish - start;
                    mTimeToDepthAge[depth] = 0;
                }
                else {
                    if(mPreviousStartingState != null) mStartingState = mPreviousStartingState;

                    for(int i = depth; i < mMaxDepth; i++) {
                        mTimeToDepthAge[i]++;
                    }
                }
                double timeTaken = (finish - start) / 1000d;

                int size = getGameTreeSize(depth);
                double statesPerSec = size / ((finish - start) / 1000d);

                if (chatty && mUiCallback != null) {
                    mUiCallback.statusText("Depth " + depth + " explored " + size + " states in " + timeTaken + " sec at " + doubleFormat.format(statesPerSec) + "/sec");
                }

                depth++;
                deepestSearch = depth;
            }
            else {
                break;
            }
        }

        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "MAIN SEARCH");
        //OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, dumpEvaluationFor(0));



        continuationDepth = deepestSearch;
        if(mUseContinuationSearch) {
            long continuationStart = System.currentTimeMillis();
            while (true) {
                long timeSpent = continuationStart - mStartTime;
                long timeRemaining = mMainMillis - timeSpent;
                long timeRequired = estimatedTimeToDepth(continuationDepth - 1) / 2;

                // We have to go through the whole tree again in continuation search, but since we've
                // already searched most of it, we'll get some nodes out of the search if we have
                // even a little time remaining.
                if (timeRemaining < timeRequired) {
                    OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Skipping continuation search: " + timeRemaining + "msec left, " + timeRequired + " required");
                    break;
                }

                if (isTimeCritical() || mNoTime || mExtensionTime) {
                    break;
                }
                else if (firstExtension) {
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


            OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "CONTINUATION SEARCH");
            //OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, dumpEvaluationFor(0));
            //getTreeRoot().printTree("");
        }


        // Do the horizon search, looking quickly at the current best moves in the hopes of catching any dumb
        // refutations.


        if(mUseHorizonSearch) {
            // Can we afford a search to depth 3?
            currentHorizonDepth = deepestSearch;
            while (true) {
                if (!isTimeCritical()) {
                    if (firstHorizon) {
                        firstHorizon = false;
                        if (chatty && mUiCallback != null) {
                            mUiCallback.statusText("Running horizon on best moves...");
                        }
                    }

                    long horizonStartTime = System.currentTimeMillis();
                    long timeSpent = horizonStartTime - mStartTime;
                    long timeRemaining = mThinkTime - timeSpent - 250;
                    if(timeRemaining > estimatedTimeToDepth(3)) horizonDepth = 3;
                    else horizonDepth = 2;


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
                        currentHorizonDepth = continuationDepth + horizonDepth;
                        horizonStart += horizonCount;
                    }

                    boolean certainVictory = true;
                    int e = 0;
                    for (GameTreeNode branch : getTreeRoot().getBranches()) {
                        if (e < horizonStart) {
                            e++;
                            continue;
                        }

                        List<GameTreeNode> nodes = GameTreeState.getPathForChild(branch);
                        GameTreeNode n = nodes.get(nodes.size() - 1);
                        if (n.getVictory() == GameState.GOOD_MOVE) {
                            n.explore(currentHorizonDepth, continuationDepth, n.getAlpha(), n.getBeta(), mThreadPool, false);
                            certainVictory = false;
                            n.revalueParent(n.getDepth());
                        }

                        e++;
                        if (e > horizonStart + horizonCount) break;
                    }

                    OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Ran horizon search at depth: " + currentHorizonDepth + " starting index " + horizonStart + " ending index " + (horizonStart + horizonCount));

                    if (currentHorizonDepth > deepestSearch) deepestSearch = currentHorizonDepth;

                    if (certainVictory) break;
                    horizonIterations++;
                } else {
                    break;
                }
            }

            OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "HORIZON SEARCH");
            //OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, dumpEvaluationFor(0));
            //getTreeRoot().printTree("");
        }

        mDeepestSearch = deepestSearch;
        mEndTime = System.currentTimeMillis();
    }

    public void printSearchStats() {
        int nodes = getGameTreeSize(mLastDepth);
        int fullNodes = getGameTreeSize(mDeepestSearch);
        int size = getTreeRoot().mBranches.size();
        double observedBranching = ((mGame.mAverageBranchingFactor * mGame.mAverageBranchingFactorCount) + size) / (++mGame.mAverageBranchingFactorCount);
        mGame.mAverageBranchingFactor = observedBranching;

        if(chatty && mUiCallback != null) {
            mUiCallback.statusText("# cutoffs/avg. to 1st a/b a/b");
            for (int i = 0; i < mAlphaCutoffs.length && i < mDeepestSearch; i++) {
                String line = "Depth " + i + ": " + mAlphaCutoffs[i] + "/" + mBetaCutoffs[i];
                if (mAlphaCutoffDistances[i] > 0) {
                    line += " " + mAlphaCutoffDistances[i] / mAlphaCutoffs[i];
                } else {
                    line += " 0";
                }
                line += "/";

                if (mBetaCutoffDistances[i] > 0) {
                    line += "" + mBetaCutoffDistances[i] / mBetaCutoffs[i];
                } else {
                    line += "0";
                }
                mUiCallback.statusText(line);
            }


            mUiCallback.statusText("Finding best state...");
            GameTreeNode bestMove = getTreeRoot().getBestChild();
            mUiCallback.statusText("Best move: " + bestMove.getRootMove() + " with path...");

            List<GameTreeNode> bestPath = getTreeRoot().getBestPath();

            for (GameTreeNode node : bestPath) {
                mUiCallback.statusText("\t" + node.getEnteringMove());
            }
            mUiCallback.statusText("End of best path scored " + bestMove.getValue());

            mUiCallback.statusText("Transpositions ignored because of repetitions: " + mRepetitionsIgnoreTranspositionTable);
            mUiCallback.statusText("Observed/effective branching factor: " + doubleFormat.format(observedBranching) + "/" + doubleFormat.format(Math.pow(nodes, 1d / mLastDepth)));
            mUiCallback.statusText("Thought for: " + (mEndTime - mStartTime) + "msec. Tree sizes: main search " + nodes + " nodes, extension searches " + (fullNodes - nodes) + " nodes");
            mUiCallback.statusText("Overall speed: " + (fullNodes / ((mEndTime - mStartTime)/ 1000d)) + " nodes/sec");
            mUiCallback.statusText("Transposition table stats: " + transpositionTable.getTableStats());
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
                GameTreeState s = GameTreeState.getStateForMinimalNode(getTreeRoot(), (MinimalGameTreeNode) n);
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

        nextState.checkVictory();
        return nextState;
    }
}
