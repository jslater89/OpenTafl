package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.test.TaflTest;

public class MercenarySideSwitchTest extends TaflTest{
    @Override
    public void run() {
        String mercenaryRulesRecord = "dim:7 name:Brandub surf:n atkf:n ks:w nj:n cj:n mj:r cenh: cenhe: start:/3t3/3m3/3T3/ttTKTtt/3T3/2Tm3/3t3/";
        Rules rules = null;
        try {
            rules = RulesSerializer.loadRulesRecord(mercenaryRulesRecord);
        }
        catch (NotationParseException e) {
            assert false;
        }

        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(4, 3), Coord.get(4, 5)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        char taflman = state.getPieceAt(3,5);
        assert Taflman.getPackedSide(taflman) == Taflman.SIDE_DEFENDERS;

        mercenaryRulesRecord = "dim:7 name:Brandub surf:n atkf:y ks:w nj:n cj:n mj:r cenh: cenhe: start:/3t3/3m3/3T3/ttTKTtt/3T3/2Mm3/3t3/";

        try {
            rules = RulesSerializer.loadRulesRecord(mercenaryRulesRecord);
        }
        catch (NotationParseException e) {
            assert false;
        }

        game = new Game(rules, null);
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get("b4"), Coord.get("b6")));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        taflman = state.getPieceAt(2,5);
        assert Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS;
    }
}
