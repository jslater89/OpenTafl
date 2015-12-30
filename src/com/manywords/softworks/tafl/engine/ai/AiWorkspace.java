package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.engine.ai.tables.DeepeningTable;
import com.manywords.softworks.tafl.engine.ai.tables.TranspositionTable;

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
    private DeepeningTable mDeepeningTable;
    private GameState mOriginalStartingState;

    public boolean chatty = false;

    public AiWorkspace(Game startingGame, GameState startingState, int transpositionTableSize) {
        super(startingGame.mZobristConstants, startingGame.getHistory());
        setGameRules(startingGame.getGameRules());
        setUiCallback(null);
        mOriginalStartingState = startingState;
        mGame = startingGame;
        mThreadPool = new AiThreadPool(Math.min(1, Runtime.getRuntime().availableProcessors() - 1));

        if (transpositionTable == null || transpositionTable.size() != transpositionTableSize) {
            transpositionTable = new TranspositionTable(transpositionTableSize);
        }
    }

    public DeepeningTable getDeepeningTable() {
        return mDeepeningTable;
    }

    final int AVERAGE_BRANCHING = 50;

    public void explore(int maxDepth) {
        //mThreadPool.start();

        for (int depth = 1; depth <= maxDepth; depth++) {
            mDeepeningTable = new DeepeningTable(maxDepth + 1);
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

            int branches = mStartingState.getBranches().size();
            List<GameTreeNode> bestPath = mStartingState.getBestPath();
            for (GameTreeNode node : bestPath) {
                branches += node.getBranches().size();
            }

            double statesPerSec = size / ((finish - start) / 1000d);
            double observedBranching = (double) branches / (double) depth;

            if (chatty) {
                System.out.println("Depth " + depth + " explored " + size + " states in " + timeTaken + " sec at " + statesPerSec + "/sec");
                System.out.println("Observed/effective branching factor: " + doubleFormat.format(observedBranching) + "/" + doubleFormat.format(Math.pow(size, 1d / depth)));
            }
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
    public void advanceState(GameState currentState, boolean advanceTurn, char berserkingTaflman, boolean recordState) {

    }
}
