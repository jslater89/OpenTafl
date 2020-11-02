package com.manywords.softworks.tafl.command.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AbstractAiWorkspace;
import com.manywords.softworks.tafl.engine.ai.alphabeta.FishyWorkspace;
import com.manywords.softworks.tafl.engine.ai.alphabeta.AlphaBetaGameTreeNode;
import com.manywords.softworks.tafl.ui.UiCallback;

public class LocalAi extends Player {
    private PlayerCallback mCallback;
    private UiWorkerThread mWorker;
    private AbstractAiWorkspace mWorkspace;
    @Override
    public void setCallback(PlayerCallback c) {
        mCallback = c;
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int thinkTime) {
        ui.statusText("Waiting for computer move.");

        mWorker = new UiWorkerThread(new UiWorkerThread.UiWorkerRunnable() {
            private boolean mRunning = true;

            @Override
            public void cancel() {
                mWorkspace.crashStop();
                mRunning = false;
            }

            @Override
            public void run() {
                mWorkspace = new FishyWorkspace(ui, game, game.getCurrentState(), 50);
                mWorkspace.chatty = true;

                mWorkspace.explore(thinkTime);
                //while(!mWorkspace.isThreadPoolIdle()) { continue; }
                mWorkspace.stopExploring();

                AlphaBetaGameTreeNode bestMove = mWorkspace.getTreeRoot().getBestChild();
                onMoveDecided(bestMove.getRootMove());
                mWorkspace.printSearchStats();
            }
        });
        mWorker.start();
    }

    public AbstractAiWorkspace getWorkspace() {
        return mWorkspace;
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
