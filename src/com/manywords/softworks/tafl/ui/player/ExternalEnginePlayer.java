package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineHost;

import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jay on 3/10/16.
 */
public class ExternalEnginePlayer extends Player {
    private PlayerCallback mCallback;
    private ExternalEngineHost mHost;
    private MoveRecord mMyLastMove;

    boolean mAttackerExpired = true;
    boolean mDefenderExpired = true;
    private int mAttackerOvertimes = -1;
    private int mDefenderOvertimes = -1;

    public void setupEngine(File iniFile) {
        mHost = new ExternalEngineHost(this, iniFile);
        mMyLastMove = null;
        if(getGame() == null) throw new IllegalStateException("ExternalEnginePlayer game is null when setting up engine!");
        mHost.setGame(getGame());
    }

    public void setupTestEngine(TaflTest host, PipedInputStream connectToOutput, PipedOutputStream connectToInput) {
        mHost = new ExternalEngineHost(this, host, connectToOutput, connectToInput);
        if(getGame() == null) throw new IllegalStateException("ExternalEnginePlayer game is null when setting up engine!");
        mHost.setGame(getGame());
    }

    public ExternalEngineHost getExternalEngineHost() {
        return mHost;
    }

    @Override
    public void setupPlayer() {
        super.setupPlayer();
        mAttackerOvertimes = -1;
        mDefenderOvertimes = -1;
        setupEngine(this.isAttackingSide() ? TerminalSettings.attackerEngineFile : TerminalSettings.defenderEngineFile);
    }

    public ExternalEngineHost setupAnalysisEngine() {
        super.setupPlayer();
        mAttackerOvertimes = -1;
        mDefenderOvertimes = -1;
        setupEngine(TerminalSettings.analysisEngineFile);

        return mHost;
    }

    @Override
    public void setGame(Game game) {
        super.setGame(game);
        if(getGame().getClock() != null) {
            mAttackerOvertimes = mDefenderOvertimes = getGame().getClock().getOvertimeCount();
        }
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int thinkTime) {
        // Support berserk...
        List<MoveRecord> movesSinceMyLastMove = new ArrayList<>(1);

        int historySize = game.getHistory().size();
        for(int i = historySize - 1; i >= 0; i--) {
            GameState s = game.getHistory().get(i);
            // We only want to check the start and end space, since the incoming move may not
            // record captures.
            if(!s.getExitingMove().softEquals(mMyLastMove)) movesSinceMyLastMove.add(s.getExitingMove());
            else break;
        }

        if(movesSinceMyLastMove.size() > 0) {
            Collections.reverse(movesSinceMyLastMove);
            mHost.notifyMovesMade(movesSinceMyLastMove);
        }
        mHost.playForCurrentSide(game);
    }

    @Override
    public void moveResult(int moveResult) {
        mHost.moveResult(moveResult);
    }

    @Override
    public void opponentMove(MoveRecord move) {
        // EE host has a hacky fix for this already
    }

    public void resign() {
        mCallback.notifyResignation(this);
    }

    @Override
    public void stop() {
        mHost.stopEngine();
    }

    @Override
    public void timeUpdate() {
        boolean sendUpdate = false;
        GameClock clock = getGame().getClock();
        GameClock.ClockEntry attackerClock = clock.getClockEntry(getGame().getCurrentState().getAttackers());
        GameClock.ClockEntry defenderClock = clock.getClockEntry(getGame().getCurrentState().getDefenders());

        if(mAttackerOvertimes == -1) {
            mAttackerOvertimes = attackerClock.getOvertimeCount();
        }
        if(mDefenderOvertimes == -1) {
            mDefenderOvertimes = defenderClock.getOvertimeCount();
        }

        if(!mAttackerExpired && attackerClock.mainTimeExpired()) {
            mAttackerExpired = true;
            sendUpdate = true;
        }
        if(!mDefenderExpired && defenderClock.mainTimeExpired()) {
            mDefenderExpired = true;
            sendUpdate = true;
        }

        if(attackerClock.getOvertimeCount() < mAttackerOvertimes) {
            mAttackerOvertimes = attackerClock.getOvertimeCount();
            sendUpdate = true;
        }
        if(defenderClock.getOvertimeCount() < mDefenderOvertimes) {
            mDefenderOvertimes = defenderClock.getOvertimeCount();
            sendUpdate = true;
        }

        if(sendUpdate) {
            new Thread(() -> {
                mHost.clockUpdate();
            }).start();
        }
    }

    @Override
    public void onMoveDecided(MoveRecord record) {
        mCallback.onMoveDecided(this, record);
        mMyLastMove = record;
    }

    @Override
    public void setCallback(PlayerCallback callback) {
        mCallback = callback;
    }

    @Override
    public Type getType() {
        return null;
    }
}
