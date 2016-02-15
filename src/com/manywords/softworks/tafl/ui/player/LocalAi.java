package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.util.List;

public class LocalAi implements Player {
    private MoveCallback mCallback;
    private PlayerWorkerThread mWorker;

    @Override
    public void setCallback(MoveCallback c) {
        mCallback = c;
    }

    @Override
    public void getNextMove(RawTerminal ui, Game game, int searchDepth) {
        System.out.println("Waiting for computer move.");

        mWorker = new PlayerWorkerThread(new PlayerWorkerThread.PlayerWorkerRunnable() {
            private boolean mRunning = true;

            @Override
            public void cancel() {
                mRunning = false;
            }

            @Override
            public void run() {
                AiWorkspace workspace = new AiWorkspace(game, game.getCurrentState(), 50);
                workspace.chatty = true;

                workspace.explore(searchDepth);
                //while(!workspace.isThreadPoolIdle()) { continue; }
                workspace.stopExploring();

                System.out.println("# cutoffs/avg. to 1st a/b a/b");
                for (int i = 0; i < searchDepth; i++) {
                    System.out.print("Depth " + i + ": " + workspace.mAlphaCutoffs[i] + "/" + workspace.mBetaCutoffs[i]);
                    if (workspace.mAlphaCutoffDistances[i] > 0) {
                        System.out.print(" " + workspace.mAlphaCutoffDistances[i] / workspace.mAlphaCutoffs[i]);
                    } else {
                        System.out.print(" 0");
                    }
                    System.out.print("/");

                    if (workspace.mBetaCutoffDistances[i] > 0) {
                        System.out.print(workspace.mBetaCutoffDistances[i] / workspace.mBetaCutoffs[i]);
                    } else {
                        System.out.print("0");
                    }

                    System.out.println();
                }


                System.out.println("Finding best state...");
                GameTreeNode bestMove = workspace.getTreeRoot().getBestChild();
                System.out.println("Best move: " + bestMove.getRootMove() + " with path...");

                List<GameTreeNode> bestPath = workspace.getTreeRoot().getBestPath();

                for (GameTreeNode node : bestPath) {
                    System.out.println("\t" + node.getEnteringMove());
                }
                System.out.println("End of best path scored " + bestMove.getValue());
                //System.out.println("Best path zobrist: " + bestMove.getZobrist());

                mCallback.onMoveDecided(bestMove.getRootMove());
            }
        });
        mWorker.start();
    }

    @Override
    public void stop() {
        mWorker.cancel();
    }

    @Override
    public Type getType() {
        return Type.AI;
    }

}
