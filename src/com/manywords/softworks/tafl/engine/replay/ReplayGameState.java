package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.List;

/**
 * Created by jay on 7/30/16.
 */
public class ReplayGameState extends GameState {
    private ReplayGame mReplayGame;
    private ReplayGameState mParent;
    private MoveAddress mMoveAddress;
    private List<ReplayGameState> mVariations;

    public ReplayGameState(ReplayGame replayGame, GameState copyState) {
        super(copyState);
        mReplayGame = replayGame;
    }

    public void setParent(ReplayGameState state) {
        mParent = state;

        mMoveAddress = mParent.getMoveAddress().increment(mReplayGame, this);
    }

    public MoveAddress getMoveAddress() {
        return mMoveAddress;
    }

    @Override
    protected GameState moveTaflman(char taflman, Coord destination) {
        GameState state = super.moveTaflman(taflman, destination);
        ReplayGameState replayState = new ReplayGameState(mReplayGame, state);

        mGame.advanceState(mGame.getHistory().get(
                mGame.getHistory().size() - 1),
                replayState,
                replayState.getBerserkingTaflman() == Taflman.EMPTY,
                replayState.getBerserkingTaflman(),
                true);

        replayState.setParent(this);

        return replayState;
    }
}
