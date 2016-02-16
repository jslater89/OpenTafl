package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.UiCallback;

import java.util.List;

public class LocalAi implements Player {
    private MoveCallback mCallback;
    private PlayerWorkerThread mWorker;

    @Override
    public void setCallback(MoveCallback c) {
        mCallback = c;
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int searchDepth) {
        ui.statusText("Waiting for computer move.");

        mWorker = new PlayerWorkerThread(new PlayerWorkerThread.PlayerWorkerRunnable() {
            private boolean mRunning = true;

            @Override
            public void cancel() {
                mRunning = false;
            }

            @Override
            public void run() {
                AiWorkspace workspace = new AiWorkspace(ui, game, game.getCurrentState(), 50);
                workspace.chatty = true;

                workspace.explore(searchDepth);
                //while(!workspace.isThreadPoolIdle()) { continue; }
                workspace.stopExploring();

                ui.statusText("# cutoffs/avg. to 1st a/b a/b");
                for (int i = 0; i < searchDepth; i++) {
                    String line = "Depth " + i + ": " + workspace.mAlphaCutoffs[i] + "/" + workspace.mBetaCutoffs[i];
                    if (workspace.mAlphaCutoffDistances[i] > 0) {
                        line += " " + workspace.mAlphaCutoffDistances[i] / workspace.mAlphaCutoffs[i];
                    } else {
                        line += " 0";
                    }
                    line += "/";

                    if (workspace.mBetaCutoffDistances[i] > 0) {
                        line += "" + workspace.mBetaCutoffDistances[i] / workspace.mBetaCutoffs[i];
                    } else {
                        line += "0";
                    }
                    ui.statusText(line);
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

                onMoveDecided(bestMove.getRootMove());
            }
        });
        mWorker.start();
    }

    @Override
    public void stop() {
        mWorker.cancel();
    }

    @Override
    public void onMoveDecided(MoveRecord record) {
        mCallback.onMoveDecided(record);
    }

    @Override
    public Type getType() {
        return Type.AI;
    }

}
