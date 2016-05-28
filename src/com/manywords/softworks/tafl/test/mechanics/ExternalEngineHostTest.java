package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.ExternalEnginePlayer;
import com.manywords.softworks.tafl.command.player.Player;
import com.manywords.softworks.tafl.command.player.external.engine.ExternalEngineHost;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 3/19/16.
 */
public class ExternalEngineHostTest extends TaflTest implements UiCallback {

    @Override
    public void run() {
        PipedInputStream inFromEngine = new PipedInputStream();
        PipedOutputStream outToEngine = new PipedOutputStream();
        EngineOutputReader r = new EngineOutputReader(inFromEngine, outToEngine);

        Rules rules = Brandub.newBrandub7();
        Game g = new Game(rules, this, new TimeSpec(0, 30000, 5, 1000));
        r.mGame = g;

        ExternalEnginePlayer dummyPlayer = new ExternalEnginePlayer();
        dummyPlayer.setGame(g);
        dummyPlayer.setupTestEngine(this, r.mInputStream, r.mOutputStream);

        ExternalEngineHost h = dummyPlayer.getExternalEngineHost();

        r.start();

        h.setGame(g);

        h.move(g.getCurrentState());
        h.error(1);
        h.analyzePosition(5, 30, g.getCurrentState());
        h.side(true);
        h.position(g.getCurrentState());
        h.clockUpdate();
        h.playForCurrentSide(g);

        List<MoveRecord> moves = new ArrayList<>();
        moves.add(new MoveRecord(Coord.get(0, 0), Coord.get(0, 4)));
        h.notifyMovesMade(moves);

        h.finish();
        h.goodbye();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        r.mRunning = false;
        assert r.didTestPass();
    }

    private class EngineOutputReader extends Thread {

        public Game mGame;

        public PipedInputStream mInputStream;
        public PipedOutputStream mOutputStream;

        public EngineOutputReader(PipedInputStream inFromEngine, PipedOutputStream outToEngine) {
            mInputStream = inFromEngine;
            mOutputStream = outToEngine;
        }

        public boolean mRunning = true;

        private boolean mRulesResponse = false;
        private boolean mMoveResponse = false;
        private boolean mErrorResponse = false;
        private boolean mAnalyzeResponse = false;
        private boolean mSideResponse = false;
        private boolean mPositionResponse = false;
        private boolean mClockResponse = false;
        private boolean mPlayResponse = false;
        private boolean mOpponentMoveResponse = false;
        private boolean mFinishResponse = false;
        private boolean mGoodbyeResponse = false;

        public boolean didTestPass() {
            return mRulesResponse &&
                    mMoveResponse &&
                    mErrorResponse &&
                    mAnalyzeResponse &&
                    mSideResponse &&
                    mPositionResponse &&
                    mClockResponse &&
                    mPlayResponse &&
                    mOpponentMoveResponse &&
                    mFinishResponse &&
                    mGoodbyeResponse;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];

            while(mRunning) {
                try {
                    int count = mInputStream.read(buffer);
                    if(count == -1) {
                        mRunning = false;
                        break;
                    }

                    String string = new String(buffer);
                    String[] commands = string.split("\n");

                    for(String command : commands) {
                        //System.out.println("Received command: " + command);
                        if(command.startsWith("rules dim:7 name:Brandub surf:n atkf:y ks:w nj:n cj:n cenh: cenhe: start:/3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/")) {
                            mRulesResponse = true;
                        }
                        else if(command.startsWith("move /3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/")) {
                            mMoveResponse = true;
                        }
                        else if(command.startsWith("error 1")) mErrorResponse = true;
                        else if(command.startsWith("analyze 5 30")) mAnalyzeResponse = true;
                        else if(command.startsWith("side attackers")) mSideResponse = true;
                        else if(command.startsWith("position /3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/")) {
                            mPositionResponse = true;
                        }
                        else if(command.startsWith("clock 30000* 30000* 30 5 5")) {
                            mClockResponse = true;
                        }
                        else if(command.startsWith("play attackers")) mPlayResponse = true;
                        else if(command.startsWith("opponent-move a1-a5 /3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/")) mOpponentMoveResponse = true;
                        else if(command.startsWith("finish 0")) mFinishResponse = true;
                        else if(command.startsWith("goodbye")) mGoodbyeResponse = true;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
    public void timeUpdate(Side side) {

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
