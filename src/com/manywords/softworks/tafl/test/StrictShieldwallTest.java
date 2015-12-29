package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;

class StrictShieldwallTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Copenhagen.newShieldwallTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        //RawTerminal.renderGameState(state);
        //println "Attacker shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getAttackers())
        //println "Defender shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getDefenders())

        state.moveTaflman(state.getPieceAt(2, 8), state.getSpaceAt(2, 7));
        state = game.getCurrentState();

        //RawTerminal.renderGameState(state);
        //println("Attacker shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getAttackers()));
        //println("Defender shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getDefenders()));

        state.moveTaflman(state.getPieceAt(5, 2), state.getSpaceAt(5, 1));
        state = game.getCurrentState();

        //RawTerminal.renderGameState(state);
        //println "Attacker shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getAttackers())
        //println "Defender shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getDefenders())

        state.moveTaflman(state.getPieceAt(2, 1), state.getSpaceAt(1, 1));
        state = game.getCurrentState();

        //RawTerminal.renderGameState(state);
        //println "Attacker shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getAttackers())
        //println "Defender shieldwalls: " + state.getBoard().detectShieldwallPositionsForSide(state.getDefenders())
        assert 2 == state.getBoard().detectShieldwallPositionsForSide(state.getAttackers()).size();
        assert 2 == state.getBoard().detectShieldwallPositionsForSide(state.getDefenders()).size();

        assert state.checkVictory() == GameState.DEFENDER_WIN;
        assert state.getPieceAt(5, 0) == Taflman.EMPTY;
    }

}
