package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.UiCallback;

public class LocalHuman implements Player {
    private MoveCallback mCallback;
    private PlayerWorkerThread mWorker;

    @Override
    public void setCallback(MoveCallback c) {
        mCallback = c;
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int searchDepth) {
        System.out.println("Waiting for human move.");

        mWorker = new PlayerWorkerThread(new PlayerWorkerThread.PlayerWorkerRunnable() {
            private boolean mRunning = true;

            @Override
            public void cancel() {
                mRunning = false;
            }

            @Override
            public void run() {
                MoveRecord r = null;
                if (ui.inGame() && mRunning) {
                    r = ui.waitForHumanMoveInput();
                    onMoveDecided(r);
                }
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
        return Type.HUMAN;
    }
}
