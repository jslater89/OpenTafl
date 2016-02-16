package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.engine.ai.tables.TranspositionTable;
import com.manywords.softworks.tafl.ui.UiCallback;

import java.text.DecimalFormat;
import java.util.List;

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

    private AiThreadPool mThreadPool;
    private GameState mOriginalStartingState;
    private UiCallback mUiCallback;

    public boolean chatty = false;

    public AiWorkspace(UiCallback ui, Game startingGame, GameState startingState, int transpositionTableSize) {
        super(startingGame.mZobristConstants, startingGame.getHistory());
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

    final int AVERAGE_BRANCHING = 50;

    public void explore(int maxDepth) {
        //mThreadPool.start();

        for (int depth = 1; depth <= maxDepth; depth++) {
            mAlphaCutoffs = new long[maxDepth];
            mAlphaCutoffDistances = new long[maxDepth];
            mBetaCutoffs = new long[maxDepth];
            mBetaCutoffDistances = new long[maxDepth];

            long start = System.currentTimeMillis();
            mStartingState = new GameTreeState(this, new GameState(mOriginalStartingState));
            mStartingState.explore(depth, maxDepth, Short.MIN_VALUE, Short.MAX_VALUE, mThreadPool);
            long finish = System.currentTimeMillis();

            double timeTaken = (finish - start) / 1000d;

            int size = getGameTreeSize();
            double statesPerSec = size / ((finish - start) / 1000d);

            if (chatty && mUiCallback != null) {
                mUiCallback.statusText("Depth " + depth + " explored " + size + " states in " + timeTaken + " sec at " + doubleFormat.format(statesPerSec) + "/sec");
            }
        }

        int nodes = getGameTreeSize();
        int size = getTreeRoot().mBranches.size();
        double observedBranching = ((mGame.mAverageBranchingFactor * mGame.mAverageBranchingFactorCount) + size) / (++mGame.mAverageBranchingFactorCount);
        //System.out.println(size + " " + mGame.mAverageBranchingFactor + " " + mGame.mAverageBranchingFactorCount + " " + observedBranching);
        mGame.mAverageBranchingFactorCount += 1;
        mGame.mAverageBranchingFactor = observedBranching;

        if(chatty && mUiCallback != null) {
            mUiCallback.statusText("Observed/effective branching factor: " + doubleFormat.format(observedBranching) + "/" + doubleFormat.format(Math.pow(nodes, 1d / maxDepth)));
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

    public int getGameTreeSize() {
        return mStartingState.countChildren();
    }

    /**
     * The GameTreeStates handle all turn advancement.
     */
    @Override
    public int advanceState(GameState currentState, boolean advanceTurn, char berserkingTaflman, boolean recordState) {
        return 0;
    }
}
