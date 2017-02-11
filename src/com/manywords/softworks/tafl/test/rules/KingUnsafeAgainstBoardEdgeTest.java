package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.test.TaflTest;

public class KingUnsafeAgainstBoardEdgeTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Berserk.newCommanderCornerCaptureKingTest();

        String rulesString = rules.getOTRString(false);
        rulesString += " ks:m";
        try {
            rules = RulesSerializer.loadRulesRecord(rulesString);
        }
        catch(NotationParseException e) {
            assert false;
        }

        assert rules.getKingStrengthMode() == Rules.KING_MIDDLEWEIGHT;

        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(1,1), Coord.get(1,0)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(2,1), Coord.get(1,1)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(4,4), Coord.get(5,4)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(4,0), Coord.get(2,0)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert state.getPieceAt(1, 0) == Taflman.EMPTY;
    }
}
