package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.test.TaflTest;

public class KingHammerAnvilTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Fetlar.newFetlarTest();
        String rulesString = rules.getOTRString(false);
        rulesString += " ka:h";
        try {
            rules = RulesSerializer.loadRulesRecord(rulesString);
        }
        catch(NotationParseException e) {
            assert false;
        }

        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());
        state.makeMove(new MoveRecord(Coord.get(4, 4), Coord.get(0, 4)));

        state = game.getCurrentState();
        assert state.getPieceAt(0, 3) != Taflman.EMPTY;

        state.setCurrentSide(state.getDefenders());
        state.makeMove(new MoveRecord(Coord.get(0, 2), Coord.get(1, 2)));

        state = game.getCurrentState();

        state.setCurrentSide(state.getDefenders());
        state.makeMove(new MoveRecord(Coord.get(1, 2), Coord.get(0, 2)));

        state = game.getCurrentState();
        assert state.getPieceAt(0, 3) == Taflman.EMPTY;

        rules = Fetlar.newFetlarTest();
        rulesString = rules.getOTRString(false);
        rulesString += " ka:a";
        try {
            rules = RulesSerializer.loadRulesRecord(rulesString);
        }
        catch(NotationParseException e) {
            assert false;
        }

        game = new Game(rules, null);

        state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());
        state.makeMove(new MoveRecord(Coord.get(0,2), Coord.get(1,2)));

        state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());
        state.makeMove(new MoveRecord(Coord.get(4,4), Coord.get(0,4)));

        state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());
        state.makeMove(new MoveRecord(Coord.get(1,2), Coord.get(0,2)));

        state = game.getCurrentState();
        assert state.getPieceAt(0, 3) != Taflman.EMPTY;

        state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());
        state.makeMove(new MoveRecord(Coord.get(0,4), Coord.get(4,4)));

        state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());
        state.makeMove(new MoveRecord(Coord.get(4,4), Coord.get(0,4)));

        state = game.getCurrentState();
        assert state.getPieceAt(0, 3) == Taflman.EMPTY;
    }

}
