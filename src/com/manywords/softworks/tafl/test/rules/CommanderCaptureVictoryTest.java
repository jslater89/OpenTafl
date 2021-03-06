package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.test.TaflTest;

public class CommanderCaptureVictoryTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Berserk.newCommanderCaptureKingTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        // Move the knight out of the way
        state.makeMove(new MoveRecord(Coord.get(4,4), Coord.get(0,4)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        // Move a commander to sandwich the king on the throne: no capture.
        state.makeMove(new MoveRecord(Coord.get(5,3), Coord.get(5,4)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert state.getPieceAt(5, 5) != Taflman.EMPTY;

        // Move the king off of the throne and capture him there.
        state.makeMove(new MoveRecord(Coord.get(5,5), Coord.get(3,5)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(6,5), Coord.get(4,5)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert state.getPieceAt(3, 5) == Taflman.EMPTY;
    }

}
