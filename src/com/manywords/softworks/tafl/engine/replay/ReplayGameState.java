package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.List;

/**
 * Created by jay on 7/30/16.
 */
public class ReplayGameState extends GameState {
    private ReplayGameState mParent;
    private List<ReplayGameState> mVariations;

    public ReplayGameState(GameState copyState) {
        super(copyState);
    }

    @Override
    protected GameState moveTaflman(char taflman, Coord destination) {
        GameState state = super.moveTaflman(taflman, destination);
        ReplayGameState replayState = new ReplayGameState(state);

        mGame.advanceState(mGame.getHistory().get(
                mGame.getHistory().size() - 1),
                replayState,
                replayState.getBerserkingTaflman() == Taflman.EMPTY,
                replayState.getBerserkingTaflman(),
                true);

        return replayState;
    }
}
