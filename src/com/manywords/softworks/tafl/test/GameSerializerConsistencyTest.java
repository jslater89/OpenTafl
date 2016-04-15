package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.XorshiftRandom;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class GameSerializerConsistencyTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Copenhagen.newCopenhagen11();
        Game g = new Game(rules, null);

        Random r = new XorshiftRandom(3131990);
        for(int i = 0; i < 10; i++) {
            char taflman = Taflman.EMPTY;
            Coord destination = null;
            List<Coord> destinations = new ArrayList<>();
            while(destination == null) {
                List<Character> taflmen = g.getCurrentState().getCurrentSide().getTaflmen();
                taflman = taflmen.get(r.nextInt(taflmen.size()));
                destinations = Taflman.getAllowableDestinations(g.getCurrentState(), taflman);
                if(destinations.size() > 0) {
                    destination = destinations.get(r.nextInt(destinations.size()));
                }
            }

            MoveRecord m = new MoveRecord(Taflman.getCurrentSpace(g.getCurrentState(), taflman), destination);
            g.getCurrentState().makeMove(m);
        }

        String record1 = GameSerializer.getGameRecord(g, true);
        //RawTerminal.renderGameState(g.getCurrentState());
        //System.out.println(record1);


        g = makeMoves(GameSerializer.loadGameRecord(record1));
        String record2 = GameSerializer.getGameRecord(g, true);
        //RawTerminal.renderGameState(g.getCurrentState());
        //System.out.println(record2);

        assert RulesSerializer.rulesEqual(record1, record2);

        record1 = GameSerializer.getGameRecord(g, false);
        g = makeMoves(GameSerializer.loadGameRecord(record1));

        record2 = GameSerializer.getGameRecord(g, false);

        //System.out.println(rules1);
        //System.out.println(rules2);
        assert RulesSerializer.rulesEqual(record1, record2);
    }

    private Game makeMoves(GameSerializer.GameContainer c) {
        Game g = c.game;
        for(MoveRecord move : c.moves) {
            g.getCurrentState().makeMove(move);
        }

        return g;
    }
}
