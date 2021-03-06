package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.TaflmanMoveCache;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.notation.TaflmanCodes;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.rules.brandub.Magpie;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.test.TaflTest;

public class RulesSerializerConsistencyTest extends TaflTest {

    @Override
    public void run() {
        try {
            Rules rules = Copenhagen.newLargeEdgeFortTest();
            String rules1 = rules.getOTRString(false);

            rules = RulesSerializer.loadRulesRecord(rules1);
            String rules2 = rules.getOTRString(false);

            //System.out.println(rules1);
            //System.out.println(rules2);
            assert RulesSerializer.rulesEqual(rules1, rules2);

            rules = Brandub.newBrandub7();
            rules1 = rules.getOTRString(false);

            rules = RulesSerializer.loadRulesRecord(rules1);
            rules2 = rules.getOTRString(false);

            assert RulesSerializer.rulesEqual(rules1, rules2);

            rules = Magpie.newMagpie7();
            rules1 = rules.getOTRString(false);

            rules = RulesSerializer.loadRulesRecord(rules1);
            rules2 = rules.getOTRString(false);

            assert RulesSerializer.rulesEqual(rules1, rules2);
            assert rules.getTaflmanSpeedLimit(Taflman.ALL_TAFLMAN_TYPES[TaflmanCodes.K]) == 1;
            assert rules.getTaflmanSpeedLimit(Taflman.ALL_TAFLMAN_TYPES[TaflmanCodes.t]) == -1;

            rules = Berserk.newBerserk11();
            rules1 = rules.getOTRString(false);
            rules1 += " ks:m";

            rules = RulesSerializer.loadRulesRecord(rules1);
            rules2 = rules.getOTRString(false);

            assert RulesSerializer.rulesEqual(rules1, rules2);

            Rules r = RulesSerializer.loadRulesRecord("dim:7 name:Brandub surf:n atkf:y ks:w nj:n cj:n cenh: cenhe: start:/3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/");
            Rules r2 = Brandub.newBrandub7();

            Game g = new Game(r, null);
            Game g2 = new Game(r2, null);

            GameState s = g.getCurrentState();
            GameState s2 = g2.getCurrentState();

            TaflmanMoveCache.invalidate();

            assert s.getCurrentSide().isAttackingSide() == s2.getCurrentSide().isAttackingSide();

            for (char taflman : s.getCurrentSide().getTaflmen()) {
                Coord space = Taflman.getCurrentSpace(s, taflman);

                char taflman2 = s2.getPieceAt(space.x, space.y);
                Coord space2 = Taflman.getCurrentSpace(s2, taflman2);

                Log.println(Log.Level.VERBOSE, "Considering taflmen at " + space + "/" + space2);

                assert taflman2 != Taflman.EMPTY;

                assert Taflman.getPackedSide(taflman2) == Taflman.getPackedSide(taflman);
                assert Taflman.getPackedType(taflman2) == Taflman.getPackedType(taflman);

                assert Taflman.getAllowableDestinations(s, taflman).equals(Taflman.getAllowableDestinations(s2, taflman2));
            }

            String mercenaryRulesRecord = "dim:7 name:Brandub surf:n atkf:y ks:w nj:n cj:n mj:r cenh: cenhe: linc:y start:/3t3/3m3/3T3/ttTKTtt/3T3/2Tm3/3t3/";
            rules = RulesSerializer.loadRulesRecord(mercenaryRulesRecord);
            rules2 = rules.getOTRString(false);

            assert rules.getMercenaryJumpMode() == Taflman.JUMP_RESTRICTED;
            assert rules.allowLinnaeanCaptures();

            //println(mercenaryRulesRecord);
            //println(rules2);
            assert rules2.equals(mercenaryRulesRecord);

            String guardTestRules = "dim:7 name:Guard_Test atkf:y sw:s linc:y starti:/3tT2/1tG3t/3tt2/t1tKG1t/3tt2/1TTG3/1tt4/";
            rules = RulesSerializer.loadRulesRecord(guardTestRules);
            rules2 = rules.getOTRString(true);

            //println(guardTestRules);
            //println(rules2);
            assert rules2.equals(guardTestRules);
        }
        catch(NotationParseException e) {
            assert false;
        }
    }

}
