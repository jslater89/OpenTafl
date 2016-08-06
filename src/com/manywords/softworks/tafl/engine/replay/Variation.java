package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.OpenTafl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 7/31/16.
 */
public class Variation {
    private ReplayGameState mParent;
    private ReplayGameState mRoot;
    private List<ReplayGameState> mVariationStates;
    private MoveAddress mAddress;

    public Variation(ReplayGameState parent, MoveAddress address, ReplayGameState nextState) {
        mParent = parent;
        mRoot = nextState;
        mVariationStates = new ArrayList<>();
        mVariationStates.add(nextState);
        mAddress = address;
    }

    public ReplayGameState getRoot() {
        return mRoot;
    }

    public void addState(ReplayGameState newRoot) {
        mVariationStates.add(newRoot);
    }

    public ReplayGameState findVariationState(MoveAddress moveAddress) {
        if(moveAddress.getElements().size() == 0) {
            return getRoot();
        }

        MoveAddress.Element e = moveAddress.getRootElement();
        ReplayGameState replayState = null;
        for(ReplayGameState rgs : mVariationStates) {
            if(rgs.getMoveAddress().getLastElement().equals(e)) {
                replayState = rgs;
            }
        }

        if(replayState != null && moveAddress.getNonRootElements().size() > 0) {
            return replayState.findVariationState(new MoveAddress(moveAddress.getNonRootElements()));
        }
        else {
            return replayState;
        }
    }

    public void changeAddressPrefix(MoveAddress oldAddress, MoveAddress newAddress) {
        mAddress = mAddress.changePrefix(oldAddress, newAddress);

        // RGS changes addresses for each child already
        mRoot.changeAddressPrefix(oldAddress, newAddress);
    }

    public ReplayGameState getDirectChild(MoveAddress.Element e) {
        for(ReplayGameState rgs : mVariationStates) {
            if(rgs.getMoveAddress().getLastElement().equals(e)) {
                return rgs;
            }
        }

        return null;
    }

    // TODO: this isn't returning the right value
    public MoveAddress getNextChildAddress(ReplayGame game, ReplayGameState state) {
        if(mVariationStates.size() == 1) {
            return mAddress.firstChild(game, state);
        }
        else {
            return mVariationStates.get(mVariationStates.size() - 2).getMoveAddress().increment(game, state);
        }
    }

    public void dumpTree() {
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Vtion: " + mAddress);
        for(ReplayGameState rgs : mVariationStates) {
            rgs.dumpTree();
        }
    }

    public MoveAddress getAddress() {
        return mAddress;
    }

    public List<ReplayGameState> getStates() {
        return mVariationStates;
    }

    public void removeState(ReplayGameState canonicalChild) {
        int index = mVariationStates.indexOf(canonicalChild);
        if(index != -1) {
            int initialSize = mVariationStates.size();
            for(int i = index; i < initialSize; i++) {
                mVariationStates.remove(index);
            }
        }
    }

    @Override
    public String toString() {
        return "Variation: " + mAddress;
    }
}
