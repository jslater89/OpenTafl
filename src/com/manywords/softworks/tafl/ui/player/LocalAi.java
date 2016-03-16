package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;
import com.manywords.softworks.tafl.ui.UiCallback;

import java.util.List;

public class LocalAi extends Player {
    private MoveCallback mCallback;
    private UiWorkerThread mWorker;

    @Override
    public void setCallback(MoveCallback c) {
        mCallback = c;
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int thinkTime) {
        ui.statusText("Waiting for computer move.");

        mWorker = new UiWorkerThread(new UiWorkerThread.UiWorkerRunnable() {
            private boolean mRunning = true;

            @Override
            public void cancel() {
                mRunning = false;
            }

            @Override
            public void run() {
                AiWorkspace workspace = new AiWorkspace(ui, game, game.getCurrentState(), 50);
                workspace.chatty = true;

                workspace.explore(thinkTime);
                //while(!workspace.isThreadPoolIdle()) { continue; }
                workspace.stopExploring();

                ui.statusText("# cutoffs/avg. to 1st a/b a/b");
                for (int i = 0; i < workspace.mAlphaCutoffs.length; i++) {
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


                ui.statusText("Finding best state...");
                GameTreeNode bestMove = workspace.getTreeRoot().getBestChild();
                ui.statusText("Best move: " + bestMove.getRootMove() + " with path...");

                List<GameTreeNode> bestPath = workspace.getTreeRoot().getBestPath();

                for (GameTreeNode node : bestPath) {
                    ui.statusText("\t" + node.getEnteringMove());
                }
                ui.statusText("End of best path scored " + bestMove.getValue());
                //System.out.println("Best path zobrist: " + bestMove.getZobrist());

                onMoveDecided(bestMove.getRootMove());
            }
        });
        mWorker.start();
    }

    @Override
    public void moveResult(int moveResult) {

    }

    @Override
    public void opponentMove(MoveRecord move) {

    }

    @Override
    public void stop() {
        if(mWorker != null) mWorker.cancel();
    }

    @Override
    public void timeUpdate() {

    }

    @Override
    public void onMoveDecided(MoveRecord record) {
        mCallback.onMoveDecided(this, record);
    }

    @Override
    public Type getType() {
        return Type.AI;
    }

}
