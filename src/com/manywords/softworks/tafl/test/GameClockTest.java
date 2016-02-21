package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.player.Player;

class GameClockTest extends TaflTest implements UiCallback {
    boolean mTimeExpired;
    Side mSideExpiredFor;
    Game mGame;

    @Override
    public void run() {
        // n.b. all times here are padded by 150 msec or so, because our tick thread
        // only runs once per tenth-second.
        
        // Let time run out for the starting player.
        Rules rules = Berserk.newCommanderCornerCaptureKingTest();
        mGame = new Game(rules, null, new GameClock.TimeSpec(500, 0, 0, 0));

        Side defenders = mGame.getGameRules().getDefenders();
        Side attackers = mGame.getGameRules().getAttackers();

        mGame.getClock().setCallback(mClockCallback);

        //System.out.println("Starting test 1");
        mGame.start();

        try {
            Thread.sleep(650);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert mTimeExpired;
        assert !mSideExpiredFor.isAttackingSide();

        // Have the first player slap the clock, and let time run out for the second.

        mSideExpiredFor = null;
        mTimeExpired = false;
        mGame = new Game(rules, null, new GameClock.TimeSpec(500, 0, 0, 0));
        mGame.getClock().setCallback(mClockCallback);

        //System.out.println("Starting test 2");
        mGame.start();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //System.out.println("Slapping");
        mGame.getClock().slap(true);

        try {
            Thread.sleep(650);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert mTimeExpired;
        assert mSideExpiredFor.isAttackingSide();

        // Test overtimes running down

        mSideExpiredFor = null;
        mTimeExpired = false;
        mGame = new Game(rules, null, new GameClock.TimeSpec(500, 500, 2, 0));
        mGame.getClock().setCallback(mClockCallback);

        //System.out.println("Starting test 3");
        mGame.start();

        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // In the first overtime, so slap and check what we have.
        GameClock.ClockEntry entry = mGame.getClock().getClockEntry(defenders);

        assert entry.getMainTime() == 0;
        assert entry.getOvertimeCount() == 2;

        mGame.getClock().slap(true);
        mGame.getClock().slap(true);

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // We're still in the first overtime, and haven't used all 500msec
        mGame.getClock().slap(true);
        mGame.getClock().slap(true);

        entry = mGame.getClock().getClockEntry(defenders);

        assert entry.getOvertimeCount() == 2;

        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // We've used our first 500msec, plus 250 more msec, so we're into the second
        // overtime.
        mGame.getClock().slap(true);
        mGame.getClock().slap(true);

        entry = mGame.getClock().getClockEntry(defenders);

        assert entry.getOvertimeCount() == 1;

        try {
            Thread.sleep(650);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Sleeping for another 600msec pushes us past the end of the clock.
        assert mTimeExpired;
        assert !mSideExpiredFor.isAttackingSide();

        // Test increment time adding
        mSideExpiredFor = null;
        mTimeExpired = false;
        mGame = new Game(rules, null, new GameClock.TimeSpec(500, 0, 0, 250));
        mGame.getClock().setCallback(mClockCallback);

        //System.out.println("Starting test 3");
        mGame.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert !mTimeExpired;

        try {
            Thread.sleep(350);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert mTimeExpired;
        assert !mSideExpiredFor.isAttackingSide();
    }

    private final GameClock.GameClockCallback mClockCallback = new GameClock.GameClockCallback() {
        @Override
        public void timeUpdate(Side currentSide) {
            //System.out.println(mGame.getClock().getClockEntry(currentSide));
        }

        @Override
        public void timeExpired(Side currentSide) {
            //System.out.println("Finishing test");
            //System.out.println("Expired: " + mGame.getClock().getClockEntry(currentSide));
            mGame.finish();
            mTimeExpired = true;
            mSideExpiredFor = currentSide;
        }
    };

    @Override
    public void gameStarting() {

    }

    @Override
    public void awaitingMove(Player player, boolean isAttackingSide) {

    }

    @Override
    public void timeUpdate(Side side) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {

    }

    @Override
    public void gameStateAdvanced() {

    }

    @Override
    public void victoryForSide(Side side) {

    }

    @Override
    public void gameFinished() {

    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
    }

    @Override
    public boolean inGame() {
        return false;
    }
}
