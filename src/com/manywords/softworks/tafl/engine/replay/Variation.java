package com.manywords.softworks.tafl.engine.replay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 7/31/16.
 */
public class Variation {
    private ReplayGameState mRoot;
    private List<ReplayGameState> mVariationElements;

    public Variation(ReplayGameState nextState) {
        mVariationElements = new ArrayList<>();
        mVariationElements.add(nextState);
    }

    public ReplayGameState findVariationState(MoveAddress moveAddress) {
        MoveAddress.Element e = moveAddress.getRootElement();
        int index = e.moveIndex + (e.rootIndex - 1) * 2;

        ReplayGameState replayState = mVariationElements.get(index);

        if(moveAddress.getNonRootElements().size() > 0) {
            return replayState.findVariationState(new MoveAddress(moveAddress.getNonRootElements()));
        }
        else {
            return replayState;
        }
    }
}
