package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.tablut.FotevikenTablut;
import com.manywords.softworks.tafl.test.TaflTest;

import java.util.List;

public class RestrictedFortReentryTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = FotevikenTablut.newFotevikenTablut9();
        runTest(rules);

        String rulesString = rules.getOTRString();
        try {
            Rules loadedRules = RulesSerializer.loadRulesRecord(rulesString);

            runTest(loadedRules);
        }
        catch(NotationParseException e) {
            assert false;
        }
    }

    private void runTest(Rules rules) {
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        char taflman = state.getPieceAt(1, 4);
        List<Coord> dests = Taflman.getAllowableDestinations(state, taflman);

        assert dests.contains(Coord.get(1, 3));
        assert dests.contains(Coord.get(1, 5));

        state.makeMove(new MoveRecord(Coord.get(1,4), Coord.get(1,2)));
        state = game.getCurrentState();

        state.makeMove(new MoveRecord(Coord.get(4,3), Coord.get(1,3)));
        state = game.getCurrentState();

        taflman = state.getPieceAt(1,3);
        dests = Taflman.getAllowableDestinations(state, taflman);
        List<Coord> moves = Taflman.getAllowableMoves(state, taflman);
        assert !dests.contains(Coord.get(1, 4));
        assert !moves.contains(Coord.get(1, 4));

        taflman = state.getPieceAt(0,4);
        dests = Taflman.getAllowableDestinations(state, taflman);
        moves = Taflman.getAllowableMoves(state, taflman);
        assert dests.contains(Coord.get(1, 4));
        assert moves.contains(Coord.get(1, 4));

        state.makeMove(new MoveRecord(Coord.get(0,4), Coord.get(1,4)));
        state = game.getCurrentState();

        taflman = state.getPieceAt(1,4);
        dests = Taflman.getAllowableDestinations(state, taflman);
        moves = Taflman.getAllowableMoves(state, taflman);
        assert dests.contains(Coord.get(0, 4));
        assert moves.contains(Coord.get(0, 4));
        assert dests.contains(Coord.get(1, 5));
    }

}
