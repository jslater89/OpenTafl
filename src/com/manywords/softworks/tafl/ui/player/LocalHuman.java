package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;

public class LocalHuman extends Player {
    private MoveCallback mCallback;
    private UiWorkerThread mWorker;

    @Override
    public void setCallback(MoveCallback c) {
        mCallback = c;
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int thinkTime) {
        ui.statusText("Waiting for human move.");

        mWorker = new UiWorkerThread(new UiWorkerThread.UiWorkerRunnable() {
            private boolean mRunning = true;

            @Override
            public void cancel() {
                mRunning = false;
            }

            @Override
            public void run() {
                if (ui.inGame() && mRunning) {
                    ui.waitForHumanMoveInput();
                }
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
        return Type.HUMAN;
    }
}
