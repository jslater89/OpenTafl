package com.manywords.softworks.tafl.engine.replay;

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
        // TODO: these will have to search because of possible berserker turns
        int index = e.moveIndex + (e.rootIndex - 1) * 2;

        ReplayGameState replayState = mVariationElements.get(index);

        System.out.println("Variation " + mAddress);
        System.out.println("Searching for " + moveAddress);

        if(moveAddress.getNonRootElements().size() > 0) {
            return replayState.findVariationState(new MoveAddress(moveAddress.getNonRootElements()));
        }
        else {
            return replayState;
        }
    }

    public void changeAddress(MoveAddress oldAddress, MoveAddress newAddress) {
        mAddress = newAddress;
        for(ReplayGameState rgs : mVariationElements) {
            rgs.changeAddressPrefix(oldAddress, newAddress);
        }
    }

    public ReplayGameState getDirectChild(MoveAddress.Element e) {
        // TODO: these will have to search because of possible berserker turns
        int index = e.moveIndex + (e.rootIndex - 1) * 2;

        return mVariationElements.get(index);
    }

    // TODO: this isn't returning the right value
    public MoveAddress getNextChildAddress(ReplayGame game, ReplayGameState state) {
        if(mVariationElements.size() == 1) {
            return mAddress.firstChild();
        }
        else {
            return mVariationElements.get(mVariationElements.size() - 2).getMoveAddress().increment(game, state);
        }
    }
}
