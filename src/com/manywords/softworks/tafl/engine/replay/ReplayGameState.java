package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
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

    public void setMoveAddress(MoveAddress address) {
        mMoveAddress = address;
    }

    @Override
    protected GameState moveTaflman(char taflman, Coord destination) {
        GameState state = super.moveTaflman(taflman, destination);
        ReplayGameState replayState = new ReplayGameState(mReplayGame, state);

        mGame.advanceState(
                this,
                replayState,
                replayState.getBerserkingTaflman() == Taflman.EMPTY,
                replayState.getBerserkingTaflman(),
                true);

        replayState.setParent(this);

        return replayState;
    }

    @Override
    public int makeMove(MoveRecord nextMove) {
        if(getPieceAt(nextMove.start.x, nextMove.start.y) == Taflman.EMPTY) return ILLEGAL_MOVE;

        GameState nextState = moveTaflman(getPieceAt(nextMove.start.x, nextMove.start.y), nextMove.end);
        if(nextState.getLastMoveResult() == GOOD_MOVE) {
            nextState.mLastMoveResult = nextState.checkVictory();
        }

        return nextState.getLastMoveResult();
    }
}
