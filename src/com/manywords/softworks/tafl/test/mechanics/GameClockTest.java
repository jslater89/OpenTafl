package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

public class GameClockTest extends TaflTest implements UiCallback {
    boolean mTimeExpired;
    boolean mExpiredForAttackers;
    Game mGame;

    @Override
    public void run() {
        // n.b. all times here are padded by 150 msec or so, because our tick thread
        // only runs once per tenth-second.

        // Let time run out for the starting player.
        Rules rules = Berserk.newCommanderCornerCaptureKingTest();
        mGame = new Game(rules, null, new TimeSpec(500, 0, 0, 0));

        Side defenders = mGame.getRules().getDefenders();
        Side attackers = mGame.getRules().getAttackers();

        mGame.getClock().setCallback(mClockCallback);

        //System.out.println("Starting test 1");
        mGame.start();

        try {
            Thread.sleep(650);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert mTimeExpired;
        assert !mExpiredForAttackers;

        // Have the first player slap the clock, and let time run out for the second.

        mExpiredForAttackers = false;
        mTimeExpired = false;
        mGame = new Game(rules, null, new TimeSpec(500, 0, 0, 0));
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
        assert mExpiredForAttackers;

        // Test overtimes running down

        mExpiredForAttackers = false;
        mTimeExpired = false;
        mGame = new Game(rules, null, new TimeSpec(500, 500, 2, 0));
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
        assert !mExpiredForAttackers;

        // Test increment time adding
        mExpiredForAttackers = false;
        mTimeExpired = false;
        mGame = new Game(rules, null, new TimeSpec(500, 0, 0, 250));
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
        assert !mExpiredForAttackers;

        // Test server-style slow ticks.
        mExpiredForAttackers = false;
        mTimeExpired = false;
        mGame = new Game(rules, null, new TimeSpec(0, 500, 4, 0));
        mGame.getClock().setCallback(mClockCallback);
        mGame.getClock().setServerMode(true);

        mGame.start();

        try {
            Thread.sleep(2750);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mGame.getClock().updateClocks();

        System.out.println(mGame.getClock().getClockEntry(false));

        assert mTimeExpired;
        assert !mExpiredForAttackers;
    }

    private final GameClock.GameClockCallback mClockCallback = new GameClock.GameClockCallback() {
        @Override
        public void timeUpdate(boolean currentSideAttackers) {
            //System.out.println(mGame.getClock().getClockEntry(currentSide));
        }

        @Override
        public void timeExpired(boolean currentSideAttackers) {
            //System.out.println("Finishing test");
            //System.out.println("Expired: " + mGame.getClock().getClockEntry(currentSide));
            mGame.finish();
            mTimeExpired = true;
            mExpiredForAttackers = currentSideAttackers;
        }
    };

    @Override
    public void gameStarting() {

    }

    @Override
    public void modeChanging(Mode mode, Object gameObject) {

    }

    @Override
    public void awaitingMove(Player player, boolean isAttackingSide) {

    }

    @Override
    public void timeUpdate(boolean currentSideAttackers) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {

    }

    @Override
    public void modalStatus(String title, String text) {

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
