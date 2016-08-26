package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

public class JumpCaptureBerserkerTest extends TaflTest {

    @Override
    public void run() {
        // This is a crazy complex one.
        Rules rules = Berserk.newJumpCaptureBerserkerTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        assert rules.getBerserkMode() == Rules.BERSERK_CAPTURE_ONLY;

        // Is a jump-capture correctly reported as a capturing move?
        char knight = state.getPieceAt(4, 4);
        assert Taflman.getAllowableDestinations(state, knight).contains(state.getSpaceAt(4, 2)) == true;
        assert Taflman.getCapturingMoves(state, knight).contains(state.getSpaceAt(4, 2)) == true;
        //RawTerminal.renderGameStateWithAllowableMoves(state, Coord.get(4,4), knight.getAllowableMoves(), knight.getAllowableDestinations(), knight.getCapturingMoves());

        // Can a jumping piece jump the king?
        char kingCommander = state.getPieceAt(6, 5);
        assert Taflman.getAllowableDestinations(state, kingCommander).contains(state.getSpaceAt(4, 5)) == false;

        // Can a jumping piece jump into a restricted space?
        char restrictedCommander = state.getPieceAt(6, 5);
        assert Taflman.getAllowableDestinations(state, restrictedCommander).contains(state.getSpaceAt(10, 10)) == false;

        // Is a jump into a capturing position correctly reported as a capturing move?
        char berserkerCommander = state.getPieceAt(0, 6);
        assert Taflman.getCapturingMoves(state, berserkerCommander).contains(state.getSpaceAt(0, 8)) == true;

        state.makeMove(new MoveRecord(Coord.get(4,4), Coord.get(4,2)));
        state = game.getCurrentState();

        // Did the knight successfully jump?
        assert Taflman.isKnight(state.getPieceAt(4, 2)) == true;

        // Did the knight's jump successfully capture a piece?
        assert state.getPieceAt(4, 3) == Taflman.EMPTY;

        // Although the knight made a capture, did the turn advance correctly?
        assert state.getCurrentSide().isAttackingSide() == true;

        state.makeMove(new MoveRecord(Coord.get(0,6), Coord.get(0,8)));
        state = game.getCurrentState();

        // Did the commander successfully jump?
        assert Taflman.isCommander(state.getPieceAt(0, 8)) == true;

        // Did the commander capture a piece against the corner after jumping?
        assert state.getPieceAt(0, 9) == Taflman.EMPTY;

        // Does the commander's capturing move list incorrectly include the piece it
        // just jumped?
        assert Taflman.getCapturingMoves(state, state.getPieceAt(0, 8)).contains(state.getSpaceAt(0, 6)) == false;

        // Does the commander's capturing move list correctly include the berserker move?
        assert Taflman.getCapturingMoves(state, state.getPieceAt(0, 8)).contains(state.getSpaceAt(2, 8)) == true;

        // Did the berserker rule correctly take effect?
        assert state.getCurrentSide().isAttackingSide() == true;

        state.makeMove(new MoveRecord(Coord.get(0,8), Coord.get(2,8)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        // Did the turn correctly advance?
        assert state.getCurrentSide().isAttackingSide() == false;

        // Did the berserker capture work correctly?
        assert state.getPieceAt(2, 9) == Taflman.EMPTY;

        state.makeMove(new MoveRecord(Coord.get(5,5), Coord.get(5,7)));
        state = game.getCurrentState();

        // Did the king capture a piece against the restricted square it jumped out of?
        assert state.getPieceAt(5, 6) == Taflman.EMPTY;

        // Is the king restricted from jumping over the piece ahead of it?
        assert Taflman.getAllowableDestinations(state, state.getPieceAt(5, 7)).contains(state.getSpaceAt(5, 9)) == false;
    }

}
