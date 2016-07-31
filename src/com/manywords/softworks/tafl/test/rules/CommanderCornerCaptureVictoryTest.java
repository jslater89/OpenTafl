package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
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

        state.makeMove(new MoveRecord(Coord.get(1,1), Coord.get(1,0)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(2,1), Coord.get(2,0)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert state.getPieceAt(1, 0) == Taflman.EMPTY;
    }
}
