package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.RawTerminal;

public class LocalHuman implements Player {
    private MoveCallback mCallback;
    private PlayerWorkerThread mWorker;

    @Override
    public void setCallback(MoveCallback c) {
        mCallback = c;
    }

    @Override
    public void getNextMove(RawTerminal ui, Game game, int searchDepth) {
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
                while (ui.inGame() && mRunning) {
                    r = ui.waitForInGameInput();
                    if (r != null) {
                        mCallback.onMoveDecided(r);
                        return;
                    }
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
    public Type getType() {
        return Type.HUMAN;
    }
}
