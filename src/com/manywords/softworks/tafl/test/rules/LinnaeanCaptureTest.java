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

public class LinnaeanCaptureTest extends TaflTest{
    @Override
    public void run() {
        String mercenaryRulesRecord = "dim:7 name:Brandub surf:n atkf:y ks:w nj:n cj:n mj:r cenh: cenhe: linc:y start:/3t3/3m3/3t3/tttKttt/3T3/2m4/3t3/";
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

        state.makeMove(new MoveRecord(Coord.get(2, 5), Coord.get(3, 5)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert state.getPieceAt(3,4) == Taflman.EMPTY;
    }
}
