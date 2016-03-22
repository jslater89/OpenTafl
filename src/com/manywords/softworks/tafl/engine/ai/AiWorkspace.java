package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.engine.ai.tables.TranspositionTable;
import com.manywords.softworks.tafl.ui.UiCallback;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AiWorkspace extends Game {
    public static TranspositionTable transpositionTable = null;

    private static final DecimalFormat doubleFormat = new DecimalFormat("#.00");
    public static final Evaluator evaluator = new FishyEvaluator();

    private Game mGame;
    private GameClock.TimeSpec mClockLength;
    private GameClock.TimeSpec mTimeRemaining;
    private GameTreeState mStartingState;

    public long[] mAlphaCutoffs;
    public long[] mAlphaCutoffDistances;
    public long[] mBetaCutoffs;
    public long[] mBetaCutoffDistances;
    public static long[] mLastTimeToDepth;

    private long mStartTime;
    private long mEndTime;
    /**
     * In milliseconds
     */
    private long mThinkTime = -1;
    private long mMaxThinkTime = -1;

    public boolean mNoTime = false;
    public boolean mExtensionTime = false;

    private final Object mTimeLock = new Object();

    private AiThreadPool mThreadPool;
    private GameState mOriginalStartingState;
    private UiCallback mUiCallback;

    public boolean chatty = false;

    public AiWorkspace(UiCallback ui, Game startingGame, GameState startingState, int transpositionTableSize) {
        super(startingGame.mZobristConstants, startingGame.getHistory());
        mLastTimeToDepth = new long[20];
        setGameRules(startingGame.getGameRules());
        setUiCallback(null);
        mOriginalStartingState = startingState;
        mGame = startingGame;
        mUiCallback = ui;
        mThreadPool = new AiThreadPool(Math.min(1, Runtime.getRuntime().availableProcessors() - 1));

        if (transpositionTable == null || transpositionTable.size() != transpositionTableSize) {
            transpositionTable = new TranspositionTable(transpositionTableSize);
        }
    }

    public static void resetTranspositionTable() {
        transpositionTable = new TranspositionTable((transpositionTable != null? transpositionTable.size() : 5));
    }

    public void setTimeRemaining(GameClock.TimeSpec length, GameClock.TimeSpec entry) {
        mClockLength = length;
        mTimeRemaining = entry;
    }

    private long planTimeUsage(Game g, GameClock.TimeSpec entry) {
        // Math.minned against
        if(entry == null) return Long.MAX_VALUE;
        // Aim to make a certain number of moves in main time, using overtimes
        // for the rest.
        int boardDimension = g.getGameRules().getBoard().getBoardDimension();
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
            if(movesLeft > 0 && (entry.mainTime > movesLeft * overtimeTime)) {
                long timePerMove = mainTimeRemaining / movesLeft;
                if(mainTimeRemaining + overtimeTime > timePerMove) {
                    return timePerMove;
                }
                else {
                    return mainTimeRemaining + overtimeTime;
                }
            }
            else {
                if(movesLeft > 0) {
                    long timePerMove = mainTimeRemaining / movesLeft;
                    return entry.overtimeTime + timePerMove;
                }
                else {
                    // TODO: use multiple overtimes if things get dicey
                    return mainTimeRemaining + entry.overtimeTime;
                }
            }
        }
    }

    private boolean canDoDeeperSearch(int nextDepth) {
        long timeLeft = mStartTime + mThinkTime - System.currentTimeMillis();
        long timeToPreviousDepth = mLastTimeToDepth[nextDepth - 1];

        // We can start a deeper search
        return !(isTimeCritical() || timeLeft < (timeToPreviousDepth * 2));
    }

    private boolean isTimeCritical() {
        long timeLeft = mStartTime + mThinkTime - System.currentTimeMillis();
        return timeLeft < (long) Math.min(mThinkTime * 0.05, 250);
    }

    public void explore(int maxThinkTime) {
        mMaxThinkTime = maxThinkTime * 1000;

        mStartTime = System.currentTimeMillis();
        int maxDepth = 10;

        if(mTimeRemaining == null && mGame.getClock() != null) {
            mClockLength = mGame.getClock().toTimeSpec();
            mTimeRemaining = mGame.getClock().getClockEntry(mGame.getCurrentSide()).toTimeSpec();
        }
        long desiredTime = planTimeUsage(mGame, mTimeRemaining);
        mThinkTime = Math.min(desiredTime, mMaxThinkTime);
        if(chatty && mUiCallback != null) mUiCallback.statusText("Using " + mThinkTime + "msec, desired " + desiredTime);

        //mThreadPool.start();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (mTimeLock) {
                    mExtensionTime = true;
                }
            }
        }, (int) (mThinkTime * 0.9));

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (mTimeLock) {
                    mNoTime = true;
                };
            }
        }, mThinkTime - (Math.min((int) (mThinkTime * 0.05), 250))); // save 1/4 second or 5%, whichever is less

        boolean firstExtension = true;
        final int extensionDepth = 2;
        final int extensionCount = 5;
        final int extensionLimit = 6;
        int extensionStart = 0;
        int deepestExtension = 1;
        int extensionIterations = 0;
        int depth;
        int treeSizePreExtension = 0;

        // Do the main search
        for (depth = 1; depth <= maxDepth;) {
            if (canDoDeeperSearch(depth)) {
                if (isTimeCritical() || mNoTime || mExtensionTime) {
                    break;
                }

                mAlphaCutoffs = new long[maxDepth];
                mAlphaCutoffDistances = new long[maxDepth];
                mBetaCutoffs = new long[maxDepth];
                mBetaCutoffDistances = new long[maxDepth];

                long start = System.currentTimeMillis();
                mStartingState = new GameTreeState(this, new GameState(mOriginalStartingState));
                mStartingState.explore(depth, maxDepth, Short.MIN_VALUE, Short.MAX_VALUE, mThreadPool);
                long finish = System.currentTimeMillis();

                if (!mNoTime) {
                    mLastTimeToDepth[depth] = finish - start;
                }
                double timeTaken = (finish - start) / 1000d;

                int size = getGameTreeSize(depth);
                double statesPerSec = size / ((finish - start) / 1000d);

                if (chatty && mUiCallback != null) {
                    mUiCallback.statusText("Depth " + depth + " explored " + size + " states in " + timeTaken + " sec at " + doubleFormat.format(statesPerSec) + "/sec");
                }
                depth++;
                deepestExtension = depth;
            }
            else {
                break;
            }
        }

        // Do the extension search
        while(true) {
            if (!isTimeCritical()) {
                if (firstExtension) {
                    firstExtension = false;
                    if (chatty && mUiCallback != null) {
                        mUiCallback.statusText("Running extension search to fill time...");
                    }
                }

                // Do an extension search on the best-known moves.
                getTreeRoot().getBranches().sort(new Comparator<GameTreeNode>() {
                    @Override
                    public int compare(GameTreeNode o1, GameTreeNode o2) {
                        // Sort by value high to low
                        if (getTreeRoot().isMaximizingNode()) {
                            return -(o1.getValue() - o2.getValue());
                        }
                        else {
                            // low to high
                            return (o1.getValue() - o2.getValue());
                        }
                    }
                });

                deepestExtension += extensionDepth;

                if (deepestExtension > depth + extensionLimit) {
                    deepestExtension = depth + extensionDepth;
                    extensionStart += extensionCount;
                }

                boolean certainVictory = true;
                int e = 0;
                for (GameTreeNode branch : getTreeRoot().getBranches()) {
                    if (e < extensionStart) {
                        e++;
                        continue;
                    }

                    List<GameTreeNode> nodes = GameTreeState.getPathForChild(branch);
                    GameTreeNode n = nodes.get(nodes.size() - 1);
                    n.explore(deepestExtension, depth, n.getAlpha(), n.getBeta(), mThreadPool);
                    if (n.getVictory() == GameState.GOOD_MOVE) {
                        certainVictory = false;
                    }
                    n.revalueParent(n.getDepth());

                    e++;
                    if (e > extensionStart + extensionCount) break;
                }

                if (certainVictory) break;
                extensionIterations++;
            }
            else {
                break;
            }
        }

        mEndTime = System.currentTimeMillis();

        int nodes = getGameTreeSize(depth);
        int fullNodes = getGameTreeSize(depth + extensionLimit);
        int size = getTreeRoot().mBranches.size();
        double observedBranching = ((mGame.mAverageBranchingFactor * mGame.mAverageBranchingFactorCount) + size) / (++mGame.mAverageBranchingFactorCount);
        mGame.mAverageBranchingFactor = observedBranching;

        if(chatty && mUiCallback != null) {
            mUiCallback.statusText("Observed/effective branching factor: " + doubleFormat.format(observedBranching) + "/" + doubleFormat.format(Math.pow(nodes, 1d / maxDepth)));
            mUiCallback.statusText("Thought for: " + (mEndTime - mStartTime) + "msec, extended by " + (fullNodes - nodes) + " extra nodes");
        }
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
                true,
                true,
                berserkingTaflman);

        nextState.checkVictory();
        return nextState;
    }
}
