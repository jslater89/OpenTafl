package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.tablut.FotevikenTablut;
import com.manywords.softworks.tafl.rules.tablut.Tablut;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.player.Player;

import java.util.List;

class RestrictedFortReentryTest extends TaflTest implements UiCallback {

    @Override
    public void gameStarting() {

    }

    @Override
    public void modeChanging(Mode mode, Object gameObject) {

    }

    @Override
    public void awaitingMove(Player currentPlayer, boolean isAttackingSide) {

    }

    @Override
    public void timeUpdate(Side side) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {

    }

    @Override
    public void modalStatus(String title, String text) {

    }

    @Override
    public void gameStateAdvanced() {
        // TODO Auto-generated method stub

    }

    @Override
    public void victoryForSide(Side side) {
        // TODO Auto-generated method stub

    }

    @Override
    public void gameFinished() {

    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
    }

    @Override
    public boolean inGame() {
        return false;
    }

    @Override
    public void run() {
        Rules rules = FotevikenTablut.newFotevikenTablut9();
        runTest(rules);

        String rulesString = rules.getOTRString();
        Rules loadedRules = RulesSerializer.loadRulesRecord(rulesString);

        runTest(loadedRules);
    }

    private void runTest(Rules rules) {
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        char taflman = state.getPieceAt(1, 4);
        List<Coord> dests = Taflman.getAllowableDestinations(state, taflman);

        assert dests.contains(Coord.get(1, 3));
        assert dests.contains(Coord.get(1, 5));

        state.moveTaflman(taflman, Coord.get(1, 2));
        state = game.getCurrentState();

        state.moveTaflman(state.getPieceAt(4,3), Coord.get(1,3));
        state = game.getCurrentState();

        taflman = state.getPieceAt(1,3);
        dests = Taflman.getAllowableDestinations(state, taflman);
        List<Coord> moves = Taflman.getAllowableMoves(state, taflman);
        assert !dests.contains(Coord.get(1, 4));
        assert !moves.contains(Coord.get(1, 4));

        taflman = state.getPieceAt(0,4);
        dests = Taflman.getAllowableDestinations(state, taflman);
        moves = Taflman.getAllowableMoves(state, taflman);
        assert dests.contains(Coord.get(1, 4));
        assert moves.contains(Coord.get(1, 4));

        state.moveTaflman(taflman, Coord.get(1, 4));
        state = game.getCurrentState();
        
        taflman = state.getPieceAt(1,4);
        dests = Taflman.getAllowableDestinations(state, taflman);
        moves = Taflman.getAllowableMoves(state, taflman);
        assert dests.contains(Coord.get(0, 4));
        assert moves.contains(Coord.get(0, 4));
        assert dests.contains(Coord.get(1, 5));
    }

}
