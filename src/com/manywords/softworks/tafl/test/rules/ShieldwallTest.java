package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

public class ShieldwallTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Copenhagen.newShieldwallTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());

        //RawTerminal.renderGameState(state);
        //println "Attacker shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getAttackers())
        //println "Defender shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getDefenders())

        state.makeMove(new MoveRecord(Coord.get(2,8), Coord.get(2,7)));
        state = game.getCurrentState();

        //RawTerminal.renderGameState(state);
        //println("Attacker shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getAttackers()));
        //println("Defender shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getDefenders()));

        state.makeMove(new MoveRecord(Coord.get(5,2), Coord.get(5,1)));
        state = game.getCurrentState();

        //RawTerminal.renderGameState(state);
        //println "Attacker shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getAttackers())
        //println "Defender shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getDefenders())

        state.makeMove(new MoveRecord(Coord.get(2,1), Coord.get(1,1)));
        state = game.getCurrentState();

//        RawTerminal.renderGameState(state);
//        System.out.println("Attacker shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getAttackers()));
//        System.out.println("Defender shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getDefenders()));
        assert 2 == state.getBoard().detectShieldwallPositionsForSide(state.getAttackers(), state.getDefenders()).size();
        assert 2 == state.getBoard().detectShieldwallPositionsForSide(state.getDefenders(), state.getAttackers()).size();

        assert state.checkVictory() == GameState.DEFENDER_WIN;
        assert state.getPieceAt(5, 0) == Taflman.EMPTY;
    }

}
