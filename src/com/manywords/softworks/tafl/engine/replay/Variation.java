package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 7/31/16.
 */
public class Variation {
    private ReplayGameState mRoot;
    private List<ReplayGameState> mVariationElements;
    private MoveAddress mAddress;

    public Variation(MoveAddress address, ReplayGameState nextState) {
        mRoot = nextState;
        mVariationElements = new ArrayList<>();
        mVariationElements.add(nextState);
        mAddress = address;
    }

    public ReplayGameState getRoot() {
        return mRoot;
    }

    public void addState(ReplayGameState state) {
        mVariationElements.add(state);
    }

    public ReplayGameState findVariationState(MoveAddress moveAddress) {
        MoveAddress.Element e = moveAddress.getRootElement();
        ReplayGameState replayState = null;
        for(ReplayGameState rgs : mVariationElements) {
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
        for(ReplayGameState rgs : mVariationElements) {
            rgs.changeAddressPrefix(oldAddress, newAddress);
        }
    }

    public ReplayGameState getDirectChild(MoveAddress.Element e) {
        for(ReplayGameState rgs : mVariationElements) {
            if(rgs.getMoveAddress().getLastElement().equals(e)) {
                return rgs;
            }
        }

        return null;
    }

    // TODO: this isn't returning the right value
    public MoveAddress getNextChildAddress(ReplayGame game, ReplayGameState state) {
        if(mVariationElements.size() == 1) {
            return mAddress.firstChild(game, state);
        }
        else {
            return mVariationElements.get(mVariationElements.size() - 2).getMoveAddress().increment(game, state);
        }
    }

    public void dumpTree() {
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Vtion: " + mAddress);
        for(ReplayGameState rgs : mVariationElements) {
            rgs.dumpTree();
        }
    }

    public MoveAddress getAddress() {
        return mAddress;
    }

    public List<ReplayGameState> getStates() {
        return mVariationElements;
    }

    public void removeState(ReplayGameState canonicalChild) {
        int index = mVariationElements.indexOf(canonicalChild);
        if(index != -1) {
            for(int i = index; i < mVariationElements.size(); i++) {
                mVariationElements.remove(i);
            }
        }
    }
}
