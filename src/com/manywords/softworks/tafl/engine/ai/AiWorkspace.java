package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.engine.Game;
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

    public boolean mHasTime = true;

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

    private long planTimeUsage(Game g) {
        return 10000;
    }

    private boolean canDoDeeperSearch(int nextDepth) {
        long timeLeft = mStartTime + mThinkTime - System.currentTimeMillis();
        long timeToPreviousDepth = mLastTimeToDepth[nextDepth - 1];
        long timeToNextDepth = mLastTimeToDepth[nextDepth];

        // We can't do a deeper search if we have less time than the recorded time
        // to the next depth, or if we have less than eight times the time it took
        // to get to the previous depth.
        return !(timeLeft < (8 * timeToPreviousDepth) || timeLeft < timeToNextDepth);
    }

    private boolean isTimeCritical() {
        long timeLeft = mStartTime + mThinkTime - System.currentTimeMillis();
        return timeLeft < 500;
    }

    public void explore(int maxDepth) {
        mStartTime = System.currentTimeMillis();
        if(mThinkTime == -1) {
            mThinkTime = planTimeUsage(mGame);
        }

        //mThreadPool.start();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                mHasTime = false;
                t.cancel();
                t.purge();
            }
        }, mThinkTime);

        final int extensionDepth = 2;
        final int extensionCount = 5;
        int deepestExtension = 1;
        int extensionIterations = 0;
        int depth;
        for (depth = 1; depth <= maxDepth;) {
            if(canDoDeeperSearch(depth)) {
                mAlphaCutoffs = new long[maxDepth];
                mAlphaCutoffDistances = new long[maxDepth];
                mBetaCutoffs = new long[maxDepth];
                mBetaCutoffDistances = new long[maxDepth];

                long start = System.currentTimeMillis();
                mStartingState = new GameTreeState(this, new GameState(mOriginalStartingState));
                mStartingState.explore(depth, maxDepth, Short.MIN_VALUE, Short.MAX_VALUE, mThreadPool);
                long finish = System.currentTimeMillis();

                if (mHasTime) {
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
                if(isTimeCritical()) break;

                if(chatty && mUiCallback != null) {
                    mUiCallback.statusText("Extension search to depth " + deepestExtension);
                }

                // Do an extension search on the three best known moves.
                getTreeRoot().getBranches().sort(new Comparator<GameTreeNode>() {
                    @Override
                    public int compare(GameTreeNode o1, GameTreeNode o2) {
                        // Sort in reverse order of value
                        return -(o1.getValue() - o2.getValue());
                    }
                });
                deepestExtension += extensionDepth;
                for(int i = 0; i < extensionCount; i++) {
                    if(getTreeRoot().getBranches().size() > i) {
                        List<GameTreeNode> nodes = getTreeRoot().getNthPath(i);
                        GameTreeNode n = nodes.get(nodes.size() - 1);
                        n.explore(deepestExtension, deepestExtension, Short.MIN_VALUE, Short.MAX_VALUE, mThreadPool);
                        n.revalueParent();
                    }
                }
                extensionIterations++;
            }
        }

        mEndTime = System.currentTimeMillis();

        int nodes = getGameTreeSize(depth);
        int size = getTreeRoot().mBranches.size();
        double observedBranching = ((mGame.mAverageBranchingFactor * mGame.mAverageBranchingFactorCount) + size) / (++mGame.mAverageBranchingFactorCount);
        mGame.mAverageBranchingFactor = observedBranching;

        if(chatty && mUiCallback != null) {
            mUiCallback.statusText("Observed/effective branching factor: " + doubleFormat.format(observedBranching) + "/" + doubleFormat.format(Math.pow(nodes, 1d / maxDepth)));
            mUiCallback.statusText("Thought for: " + (mEndTime - mStartTime) + "msec, extension iterations: " + extensionIterations);
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
    public int advanceState(GameState currentState, boolean advanceTurn, char berserkingTaflman, boolean recordState) {
        return 0;
    }
}
