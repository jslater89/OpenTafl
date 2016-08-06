package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.RawTerminal;

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
        mExitingMove = null;
        mDetailedExitingMove = null;
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
        mMoveAddress = parentVariation.getNextChildAddress(mReplayGame, this);
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

        if(state.getLastMoveResult() < GOOD_MOVE) {
            return new ReplayGameState(state.getLastMoveResult());
        }
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
        if(state.getLastMoveResult() < GOOD_MOVE) return new ReplayGameState(state.getLastMoveResult());
        ReplayGameState replayState = new ReplayGameState(mReplayGame, state);

        // Don't record this move
        mGame.advanceState(
                this,
                replayState,
                replayState.getBerserkingTaflman() == Taflman.EMPTY,
                replayState.getBerserkingTaflman(),
                false);

        if(mCanonicalChild != null) {
            mExitingMove = mCanonicalChild.getEnteringMove();
            mDetailedExitingMove = (DetailedMoveRecord) mExitingMove;
        }

        return replayState;
    }

    @Override
    public int makeMove(MoveRecord nextMove) {
        if(getPieceAt(nextMove.start.x, nextMove.start.y) == Taflman.EMPTY) return ILLEGAL_MOVE;

        GameState nextState = moveTaflman(getPieceAt(nextMove.start.x, nextMove.start.y), nextMove.end);
        if(nextState.getLastMoveResult() == GOOD_MOVE) {
            nextState.mLastMoveResult = nextState.checkVictory();
        }

        if(nextMove.isDetailed()) {
            // Preserve comments
            ((DetailedMoveRecord) getExitingMove()).setComment(((DetailedMoveRecord) nextMove).getComment());
        }

        return nextState.getLastMoveResult();
    }

    public ReplayGameState findVariationState(MoveAddress moveAddress) {
        MoveAddress.Element e = moveAddress.getRootElement();
        int index = e.rootIndex - 1;

        if(mCanonicalChild.getMoveAddress().equals(moveAddress)) return mCanonicalChild;
        else if(mVariations.size() <= index) return null;
        else return mVariations.get(index).findVariationState(new MoveAddress(moveAddress.getNonRootElements()));
    }

    /**
     * Adds a variation to the history tree. If this state has no canonical child, the variation becomes the canonical
     * child. If this state does have a canonical child, the variation becomes the root of a new variation off of this
     * state. If the variation already exists, it is not re-added. Callers of this method should change the current
     * state as desired.
     * @param move The move to enter the variation.
     * @return A game state containing either the next state or an error
     */
    public ReplayGameState makeVariation(MoveRecord move) {
        if(getPieceAt(move.start.x, move.start.y) == Taflman.EMPTY) return new ReplayGameState(ILLEGAL_MOVE);

        if(mCanonicalChild != null && mCanonicalChild.getEnteringMove().equals(move)) {
            return mCanonicalChild;
        }

        for(Variation v : mVariations) {
            if(v.getRoot().getEnteringMove().equals(move)) {
                return v.getRoot();
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
                Variation v = new Variation(this, mMoveAddress.nextVariation(mVariations.size() + 1), nextState);
                mVariations.add(v);
                nextState.setVariationParent(this, v);
            }
        }

        return nextState;
    }

    /**
     * Deletes a variation from the history tree, including all of its children.
     * @param moveAddress
     */
    public boolean deleteVariation(MoveAddress moveAddress) {
        ReplayGameState state = mReplayGame.getStateByAddress(moveAddress);
        Variation v = mReplayGame.getVariationByAddress(moveAddress);

        // Since getVariation does some magic to help us find variations by multiple names (i.e. 1a.1., 1a.1.1, 1a.1.1a),
        // we have to make sure we aren't referencing a state or, if we are referencing a state, that it's the root of
        // the variation.
        if(v != null && (state == null || v.getRoot().equals(state))) {
            return deleteVariationInternal(v.getAddress());
        }
        else if(state != null) {
            return deleteCanonicalChild(state.getMoveAddress());
        }

        return false;
    }

    private boolean deleteCanonicalChild(MoveAddress moveAddress) {
        // e.g. 1a.1.1b and this is 1a.1.1a. Non-root filtering means (1a.1.)1b.
        MoveAddress childAddress = moveAddress.changePrefix(new MoveAddress(mMoveAddress.getAllRootElements()), new MoveAddress());
        List<MoveAddress.Element> childElements = childAddress.getElements();

        if(childElements.size() == 1) {
            // This is us, our canonical child, or a canonical child from somewhere in our tree.
            if(moveAddress.equals(mMoveAddress)) {
                if(mParent != null) {
                    return mParent.deleteCanonicalChild(moveAddress);
                }
                else {
                    throw new IllegalStateException("Tried to delete canonical child for state with no parent");
                }
            }
            else if(mCanonicalChild != null && mCanonicalChild.getMoveAddress().equals(moveAddress)) {
                //System.out.println("Old child: " + mCanonicalChild.getMoveAddress() + " " + mCanonicalChild.getEnteringMove());
                if(mEnclosingVariation != null) {
                    mEnclosingVariation.removeState(mCanonicalChild);
                }
                mCanonicalChild = null; // This is the easy part.

                if(mVariations.size() > 0) {
                    // Keep the old variation around; we'll want the move list.
                    Variation toRelocate = mVariations.get(0);
                    deleteVariationInternal(toRelocate.getAddress());

                    for(ReplayGameState rgs : toRelocate.getStates()) {
                        rgs.mEnclosingVariation = null;
                    }

                    mCanonicalChild = toRelocate.getRoot();
                    setExitingMove((DetailedMoveRecord) mCanonicalChild.getEnteringMove());

                    // At this point, this variation subtree is now correctly relocated, but incorrectly
                    // addressed.
                    mCanonicalChild.changeParent(this);
                }
                //System.out.println("New child: " + mCanonicalChild.getMoveAddress() + " " + mCanonicalChild.getEnteringMove());
                return true;
            }
            else if (mCanonicalChild != null) {
                // If it isn't our canonical child, it may be our child's child, &c. TODO: test this
                return mCanonicalChild.deleteCanonicalChild(moveAddress);
            }
            else {
                return false;
            }
        }
        else if(childElements.size() > 2) {
            // e.g 5a.1.1a.1.1b is our target, and this is 5a.1.1a. Filter becomes 1a.1.1b, so variation element is 1 and target is 2. // TODO: test this
            MoveAddress.Element variationElement = childElements.get(1);
            MoveAddress.Element nextStateElement = childElements.get(2);

            ReplayGameState variationState = mVariations.get(variationElement.rootIndex - 1).getDirectChild(nextStateElement);
            if(variationState != null) {
                return variationState.deleteCanonicalChild(moveAddress);
            }
            else {
                return false;
            }
        }
        else {
            throw new IllegalArgumentException("Argument to deleteCanonicalChild does not address a canonical child: " + moveAddress);
        }
    }

    private void changeParent(ReplayGameState newParent) {
        // Keep this around. We'll need it to re-prefix things.
        MoveAddress oldAddress = mMoveAddress;

        // This changes my move address and my parent's canonical child.
        setParent(newParent);

        mEnclosingVariation = mParent.mEnclosingVariation;
        mEnclosingVariation.addState(this);

        // Variations are as simple as re-prefixing. This method recurses
        // to take care of the rest.
        for(Variation v : mVariations) {
            v.changeAddressPrefix(oldAddress, mMoveAddress);
        }

        // This is slight overkill, but it does take care of everything and is the simplest.
        if(mCanonicalChild != null) {
            mCanonicalChild.changeParent(this);
        }
    }

    private boolean deleteVariationInternal(MoveAddress moveAddress) {
        // Remove this address from the front of the move address.
        MoveAddress variationAddress = moveAddress.changePrefix(mMoveAddress, new MoveAddress());
        List<MoveAddress.Element> variationElements = variationAddress.getElements();

        if(variationElements.size() == 1) {
            // Hooray! a variation!
            int index = variationElements.get(0).rootIndex - 1;

            // No such variation exists
            if(index > mVariations.size()) {
                return false;
            }

            MoveAddress m = mVariations.remove(index).getAddress();

            // Each variation now has index i+2 (because they're one-indexed, not zero-indexed).
            // Its address is our address, plus a variation number, plus the rest of the address. For each one,
            // the prefix is our address plus i+2. Change that prefix to our address plus i+1.
            List<MoveAddress.Element> thisElements = mMoveAddress.getElements();

            for(int i = index; i < mVariations.size(); i++) {
                // Allocate inside the loop to avoid any trickiness with reuse
                MoveAddress.Element oldVariation = new MoveAddress.Element(i+2, -1);
                MoveAddress.Element newVariation = new MoveAddress.Element(i+1, -1);

                mVariations.get(i).changeAddressPrefix(new MoveAddress(thisElements, oldVariation), new MoveAddress(thisElements, newVariation));
            }

            return true;
        }
        else if(variationElements.size() > 1) {
            // Get the next replay state addressed by the move address and call this on that
            MoveAddress.Element variationElement = variationElements.get(0);
            MoveAddress.Element nextStateElement = variationElements.get(1);

            ReplayGameState variationState = mVariations.get(variationElement.rootIndex - 1).getDirectChild(nextStateElement);
            if(variationState != null) {
                return variationState.deleteVariationInternal(moveAddress);
            }
            else {
                return false;
            }
        }
        else {
            throw new IllegalArgumentException("Argument to deleteVariation does not address a variation: " + moveAddress);
        }
    }

    public void changeAddressPrefix(MoveAddress oldPrefix, MoveAddress newPrefix) {
        MoveAddress oldAddress = mMoveAddress;
        mMoveAddress = mMoveAddress.changePrefix(oldPrefix, newPrefix);

        if(mCanonicalChild != null) {
            mCanonicalChild.changeAddressPrefix(oldPrefix, newPrefix);
        }

        List<MoveAddress.Element> oldElements = oldAddress.getElements();
        List<MoveAddress.Element> thisElements = mMoveAddress.getElements();
        for(int i = 0; i < mVariations.size(); i++) {
            Variation v = mVariations.get(i);
            MoveAddress.Element variation = new MoveAddress.Element(i+1, -1);

            v.changeAddressPrefix(new MoveAddress(oldElements, variation), new MoveAddress(thisElements, variation));
        }
    }

    public void dumpTree() {
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "State: " + mMoveAddress + " " + getEnteringMove());
        for(int i = 0; i < mVariations.size(); i++) {
            mVariations.get(i).dumpTree();
        }
    }

    public ReplayGameState getCanonicalChild() {
        return mCanonicalChild;
    }

    @Override
    public String toString() {
        return (getMoveAddress() != null ? getMoveAddress().toString() : "null address") + " " + getEnteringMove() + " " + getExitingMove();
    }

    public List<Variation> getVariations() {
        return mVariations;
    }
}
