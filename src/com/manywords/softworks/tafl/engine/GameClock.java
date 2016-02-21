package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;

/**
 * Created by jay on 2/20/16.
 */
public class GameClock {
    private final Game mGame;
    private final long mMainTimeMillis;
    private final long mIncrementMillis;
    private final long mOvertimeMillis;
    private final long mOvertimeCount;

    private UpdateThread mUpdateThread;
    private boolean mOutOfTime = false;

    private static final int ATTACKERS = 0;
    private static final int DEFENDERS = 1;
    private final ClockEntry[] mClocks = new ClockEntry[2];
    private int mCurrentPlayer = ATTACKERS;

    private long mLastStartTime = 0;

    public GameClock(Game g, Side attackers, Side defenders, TimeSpec timeSpec) {
        this(g, attackers, defenders, timeSpec.mainTime, timeSpec.incrementTime, timeSpec.overtimeTime, timeSpec.overtimeCount);
    }

    public GameClock(Game g, Side attackers, Side defenders, long mainTime, long incrementTime, long overtimeTime, int overtimeCount) {
        mGame = g;
        mMainTimeMillis = mainTime;
        mIncrementMillis = incrementTime;
        mOvertimeMillis = overtimeTime;
        mOvertimeCount = overtimeCount;

        mClocks[ATTACKERS] = new ClockEntry(this, attackers, mainTime, overtimeTime, overtimeCount);
        mClocks[DEFENDERS] = new ClockEntry(this, defenders, mainTime, overtimeTime, overtimeCount);
    }

    public ClockEntry start(Side startingSide) {
        mUpdateThread = new UpdateThread();
        mUpdateThread.start();

        if(startingSide.isAttackingSide()) {
            mCurrentPlayer = ATTACKERS;
            return mClocks[ATTACKERS];
        }
        else {
            mCurrentPlayer = DEFENDERS;
            return mClocks[DEFENDERS];
        }
    }

    public void stop() {
        mUpdateThread.cancel();
    }

    /**
     * Change sides on the game clock, decrementing the current player's clock entry and
     * returning the new player's clock entry.
     * @return
     */
    public ClockEntry slap(boolean switchSides) {
        ClockEntry clock;
        synchronized (mClocks) {
            updateClocks();

            if(switchSides) mCurrentPlayer = ++mCurrentPlayer % 2;

            clock = mClocks[mCurrentPlayer];
            if(clock.mMainTimeMillis > 0) {
                clock.mMainTimeMillis += mIncrementMillis;
            }
            else {
                clock.mOvertimeMillis = mOvertimeMillis + mIncrementMillis;
            }
        }
        mGame.timeUpdate(mClocks[mCurrentPlayer].mSide);
        return mClocks[mCurrentPlayer];
    }

    private class UpdateThread extends Thread {
        private boolean mRunning = true;
        private static final int UPDATE_INTERVAL = 100;

        @Override
        public void run() {
            while(mRunning) {
                try {
                    Thread.sleep(UPDATE_INTERVAL);
                } catch (InterruptedException e) {
                    // no-op
                }

                updateClocks();
                mGame.timeUpdate(mClocks[mCurrentPlayer].mSide);
            }
        }

        public void cancel() {
            mRunning = false;
        }
    }

    private void updateClocks() {
        if(mOutOfTime) return;

        synchronized (mClocks) {
            ClockEntry currentEntry = mClocks[mCurrentPlayer];
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - mLastStartTime;
            mLastStartTime = currentTime;

            long leftover = 0;
            if (currentEntry.mMainTimeMillis > 0) {
                currentEntry.mMainTimeMillis -= elapsed;
                if(currentEntry.mMainTimeMillis < 0) {
                    leftover = -currentEntry.mMainTimeMillis;
                    currentEntry.mMainTimeMillis = 0;
                }
            }
            else {
                leftover = elapsed;
            }

            if (currentEntry.mOvertimeMillis > 0 && currentEntry.mOvertimeCount > 0) {
                currentEntry.mOvertimeMillis -= leftover;

                if(currentEntry.mOvertimeMillis < 0) {
                    leftover = -currentEntry.mOvertimeMillis;

                    currentEntry.mOvertimeCount--;
                    if(currentEntry.mOvertimeCount > 0) {
                        // Don't care about the case where we lose two overtimes at once,
                        // because we check often enough that we'll never hit that.
                        currentEntry.mOvertimeMillis = mOvertimeMillis - leftover;
                    }
                    else {
                        mOutOfTime = true;
                    }
                }
            }
            else {
                mOutOfTime = true;
            }
        }
    }

    public static class TimeSpec {
        long mainTime = 0;
        long overtimeTime = 0;
        int overtimeCount = 0;
        long incrementTime = 0;
    }

    public static class ClockEntry {
        private GameClock mGameClock;
        private Side mSide;
        private long mMainTimeMillis;
        private long mOvertimeMillis;
        private int mOvertimeCount;

        public ClockEntry(GameClock clock, Side side, long mainTime, long overtimeTime, int overtimeCount) {
            mGameClock = clock;
            mSide = side;
            mMainTimeMillis = mainTime;
            mOvertimeMillis = overtimeTime;
            mOvertimeCount = overtimeCount;
        }

        public GameClock getClock() {
            return mGameClock;
        }

        public long getMainTime() {
            return mMainTimeMillis;
        }

        public int getOvertimeCount() {
            return mOvertimeCount;
        }
    }
}
