package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.test.TaflTest;

public class CommanderCornerCaptureVictoryTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Berserk.newCommanderCornerCaptureKingTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(1, 1), state.getSpaceAt(1, 0));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(2, 1), state.getSpaceAt(2, 0));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert state.getPieceAt(1, 0) == Taflman.EMPTY;
    }
}
