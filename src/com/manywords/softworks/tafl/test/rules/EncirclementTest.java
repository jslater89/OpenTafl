package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.test.TaflTest;

public class EncirclementTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = SeaBattle.newEncirclementTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        char king = state.getPieceAt(4, 6);
        //RawTerminal.renderGameStateWithReachableSpaces(state, king.getCurrentSpace(), king.getReachableSpaces());
        //println "Surrounded? ${state.getBoard().isSideEncircled(state.getDefenders())}"

        state.moveTaflman(state.getPieceAt(3, 4), state.getSpaceAt(4, 4));
        state = game.getCurrentState();
        king = state.getPieceAt(4, 6);
        //RawTerminal.renderGameStateWithReachableSpaces(state, king.getCurrentSpace(), king.getReachableSpaces());
        //println "Surrounded? ${state.getBoard().isSideEncircled(state.getDefenders())}"

        state.moveTaflman(state.getPieceAt(3, 6), state.getSpaceAt(3, 7));
        state = game.getCurrentState();
        king = state.getPieceAt(4, 6);
        //RawTerminal.renderGameStateWithReachableSpaces(state, king.getCurrentSpace(), king.getReachableSpaces());
        //println "Surrounded? ${state.getBoard().isSideEncircled(state.getDefenders())}"

        //king = state.getPieceAt(5, 8)
        //RawTerminal.renderGameStateWithAllowableMoves(state, king.getCurrentSpace(), king.getAllowableDestinations(), king.getAllowableMoves(), king.getCapturingMoves())

        state.moveTaflman(state.getPieceAt(5, 8), state.getSpaceAt(4, 8));
        state = game.getCurrentState();
        king = state.getPieceAt(4, 6);
        //RawTerminal.renderGameStateWithReachableSpaces(state, king.getCurrentSpace(), king.getReachableSpaces());
        //System.out.println("Surrounded? " + state.getBoard().isSideEncircled(state.getDefenders()));

        assert state.getBoard().isSideEncircled(state.getDefenders());
        assert state.checkVictory() == GameState.ATTACKER_WIN;
    }

}
