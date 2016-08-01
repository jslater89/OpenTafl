package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 7/30/16.
 */
public class ReplayGameState extends GameState {
    private ReplayGame mReplayGame;
    private ReplayGameState mParent;
    private Variation mEnclosingVariation;
    private MoveAddress mMoveAddress;
    private ReplayGameState mCanonicalChild;
    private List<Variation> mVariations = new ArrayList<>();

    public ReplayGameState(ReplayGame replayGame, GameState copyState) {
        super(copyState);
        mReplayGame = replayGame;
    }

    public ReplayGameState(int error) {
        super(error);
    }

    public void setParent(ReplayGameState state) {
        mParent = state;
        mParent.mCanonicalChild = this;

        mMoveAddress = mParent.getMoveAddress().increment(mReplayGame, this);
    }

    public void setVariationParent(ReplayGameState state, Variation parentVariation) {
        mParent = state;
        mMoveAddress = mParent.getMoveAddress().nextVariation();
        mEnclosingVariation = parentVariation;
    }

    public MoveAddress getMoveAddress() {
        return mMoveAddress;
    }

    public ReplayGameState getParent() {
        return mParent;
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

    private ReplayGameState moveTaflmanVariation(char taflman, Coord destination) {
        GameState state = super.moveTaflman(taflman, destination);
        ReplayGameState replayState = new ReplayGameState(mReplayGame, state);

        // Don't record this move
        mGame.advanceState(
                this,
                replayState,
                replayState.getBerserkingTaflman() == Taflman.EMPTY,
                replayState.getBerserkingTaflman(),
                false);

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

    public ReplayGameState findVariationState(MoveAddress moveAddress) {
        MoveAddress.Element e = moveAddress.getRootElement();
        int index = e.rootIndex - 1;

        return mVariations.get(index).findVariationState(new MoveAddress(moveAddress.getNonRootElements()));
    }

    /**
     * Adds a variation to the history tree. If this state has no canonical child, the variation becomes the canonical
     * child. If this state does have a canonical child, the variation becomes the root of a new variation off of this
     * state. If the variation already exists, it is not re-added. Callers of this method should change the current
     * state as desired.
     * @param move The move to enter the variation.
     * @return A game state containing either the next state or an error, or null, if this variation already exists.
     */
    public ReplayGameState makeVariation(MoveRecord move) {
        if(getPieceAt(move.start.x, move.start.y) == Taflman.EMPTY) return new ReplayGameState(ILLEGAL_MOVE);

        if(mCanonicalChild != null && mCanonicalChild.getEnteringMove().equals(move)) {
            return null;
        }

        for(Variation v : mVariations) {
            if(v.getRoot().getEnteringMove().equals(move)) {
                return null;
            }
        }

        ReplayGameState nextState = (ReplayGameState) moveTaflmanVariation(getPieceAt(move.start.x, move.start.y), move.end);

        if(nextState.getLastMoveResult() == GOOD_MOVE) {
            nextState.mLastMoveResult = nextState.checkVictory();
        }

        if(nextState.getLastMoveResult() >= GOOD_MOVE) {
            if(mCanonicalChild == null) {
                nextState.setParent(this);
                nextState.mEnclosingVariation = mEnclosingVariation;
                mEnclosingVariation.addState(nextState);
            }
            else {
                Variation v = new Variation(nextState);
                mVariations.add(v);
                nextState.setVariationParent(this, v);
            }
        }

        return nextState;
    }
}
