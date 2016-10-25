package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeState;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.test.TaflTest;

public class AIMoveConsiderationCrashTest extends TaflTest {

    @Override
    public void run() {
        AiWorkspace.resetTranspositionTable();

        Rules rules = null;
        try {
            rules = RulesSerializer.loadRulesRecord("dim:5 name:Copenhagen atkf:n tfr:w cor:a1, cen: sw:s efe:y start:/1tTTt/tKt2/t2T1/5/1T3/");
        }
        catch(NotationParseException e) {
            assert false;
        }
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        state.makeMove(new MoveRecord(Coord.get(3, 2), Coord.get(2, 2)));
        state = game.getCurrentState();

        AiWorkspace w = new AiWorkspace(this, game, state, 5);

        GameTreeState root = new GameTreeState(w, new GameState(state));

        GameTreeState next = root.considerMove(Coord.get(4,0), Coord.get(4, 3));
        next = next.considerMove(Coord.get(2, 0), Coord.get(2, 1));
        next = next.considerMove(Coord.get(0, 2), Coord.get(1, 2));
        next = next.considerMove(Coord.get(3, 0), Coord.get(2, 0));

        assert next.getLastMoveResult() == GameState.GOOD_MOVE;
        assert next.getPieceAt(1, 0) == Taflman.EMPTY;
    }

}
