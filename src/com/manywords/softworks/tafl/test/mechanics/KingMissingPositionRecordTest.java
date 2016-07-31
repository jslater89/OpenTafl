package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.PositionSerializer;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

/**
 * Created by jay on 5/31/16.
 */
public class KingMissingPositionRecordTest extends TaflTest {
    @Override
    public void run() {
        Rules rules = SeaBattle.newSeaBattle9();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        RawTerminal display = new RawTerminal();

        state.makeMove(new MoveRecord(Coord.get(4,5), Coord.get(1,5)));
        state = game.getCurrentState();
        state.makeMove(new MoveRecord(Coord.get(3,4), Coord.get(3,1)));
        state = game.getCurrentState();
        //display.renderGameState(state);

        //8,5 and 3,8
        state.makeMove(new MoveRecord(Coord.get(3,8), Coord.get(3,2)));
        state = game.getCurrentState();
        state.makeMove(new MoveRecord(Coord.get(8,5), Coord.get(2,5)));
        state = game.getCurrentState();

        assert PositionSerializer.getPositionRecord(state.getBoard()).contains("K");

        rules = RulesSerializer.loadRulesRecord("dim:11 name:Fetlar atkf:y nj:n cj:n start:/4tttt3/5t5/3t7/t4T4t/t3TTT3t/tt1TTKTT1tt/t1T3T3t/t4T4t/4Tt5/11/3ttttt3/");
        game = new Game(rules, null);
        state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());

        state.makeMove(new MoveRecord(Coord.get(3, 10), Coord.get(3, 8)));

        assert PositionSerializer.getPositionRecord(state.getBoard()).contains("K");

        state = game.getCurrentState();

        //System.out.println(PositionSerializer.getPositionRecord(state.getBoard()));
        assert PositionSerializer.getPositionRecord(state.getBoard()).contains("K");
    }
}
