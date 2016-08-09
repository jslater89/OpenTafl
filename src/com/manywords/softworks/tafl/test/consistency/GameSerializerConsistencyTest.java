package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.XorshiftRandom;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.engine.replay.ReplayGameState;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameSerializerConsistencyTest extends TaflTest {

    @Override
    public void run() {
        //1. REGULAR GAME TESTING
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

        assert record1.equals(record2);

        record1 = GameSerializer.getGameRecord(g, false);
        g = makeMoves(GameSerializer.loadGameRecord(record1));
        record2 = GameSerializer.getGameRecord(g, false);

        //System.out.println(rules1);
        //System.out.println(rules2);
        assert record1.equals(record2);

        // 2. REPLAY GAME TESTING
        rules = Copenhagen.newCopenhagen11();
        g = new Game(rules, null);

        // Build a game
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

        ReplayGame rg = ReplayGame.copyGameToReplay(g);

        // Make some variations
        for(int i = 0; i < 10; i++) {
            ReplayGameState s = (ReplayGameState) rg.getGame().getHistory().get(r.nextInt(rg.getGame().getHistory().size()));
            rg.setCurrentState(s);

            char taflman = Taflman.EMPTY;
            Coord destination = null;
            List<Coord> destinations;
            while(destination == null) {
                List<Character> taflmen = rg.getCurrentState().getCurrentSide().getTaflmen();
                taflman = taflmen.get(r.nextInt(taflmen.size()));
                destinations = Taflman.getAllowableDestinations(rg.getCurrentState(), taflman);
                if(destinations.size() > 0) {
                    destination = destinations.get(r.nextInt(destinations.size()));
                }
            }

            MoveRecord m = new MoveRecord(Taflman.getCurrentSpace(rg.getCurrentState(), taflman), destination);
            rg.makeVariation(m);
        }

        record1 = GameSerializer.getReplayGameRecord(rg, true);

        rg = getReplay(GameSerializer.loadGameRecord(record1));

        record2 = GameSerializer.getReplayGameRecord(rg, true);

        assert record1.equals(record2);
    }

    private ReplayGame getReplay(GameSerializer.GameContainer c) {
        ReplayGame g = new ReplayGame(c.game, c.moves, c.variations);
        return g;
    }

    private Game makeMoves(GameSerializer.GameContainer c) {
        Game g = c.game;
        for(MoveRecord move : c.moves) {
            g.getCurrentState().makeMove(move);
        }

        return g;
    }
}
