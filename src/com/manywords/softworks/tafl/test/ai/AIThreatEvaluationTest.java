package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.test.TaflTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jay on 11/20/16.
 */
public class AIThreatEvaluationTest extends TaflTest {
    @Override
    public void run() {
        String otnrString = "dim:7 name:Brandub surf:n atkf:y ks:w cenh: cenhe: start:/3t3/3t3/3T3/tTTKt1t/3T3/3t3/2T1t2/";

        Rules r = null;
        try {
            r = RulesSerializer.loadRulesRecord(otnrString);
        }
        catch (NotationParseException e) {
            assert false;
        }

        if(r == null) assert false;

        Game g = new Game(r, this);
        FishyEvaluator evaluator = new FishyEvaluator();
        evaluator.initialize(r);

        List<Character> allTaflmen = new ArrayList<Character>(g.getCurrentState().getAttackers().getTaflmen());
        allTaflmen.addAll(g.getCurrentState().getDefenders().getTaflmen());

        int boardSize = r.boardSize;

        Set<Coord> defenderMoves = new HashSet<>(boardSize * boardSize);
        Set<Coord> attackerMoves = new HashSet<>(boardSize * boardSize);

        for(char taflman: allTaflmen) {
            if(Taflman.getPackedSide(taflman) > 0)
                attackerMoves.addAll(Taflman.getAllowableDestinations(g.getCurrentState(), taflman));
            else
                defenderMoves.addAll(Taflman.getAllowableDestinations(g.getCurrentState(), taflman));
        }

        assert !evaluator.isTaflmanThreatened(Coord.get(3, 3), Taflman.SIDE_DEFENDERS, g.getCurrentState().getBoard(), defenderMoves, attackerMoves);
    }
}
