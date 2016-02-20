package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.player.Player;

import java.util.List;

class RestrictedSpaceTest extends TaflTest implements UiCallback {

    @Override
    public void gameStarting() {

    }

    @Override
    public void awaitingMove(Player currentPlayer, boolean isAttackingSide) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {

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
        Rules rules = Fetlar.newFetlarTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        char ofInterest = state.getPieceAt(5, 6);
        List<Coord> allowableDestinations = Taflman.getAllowableDestinations(state, ofInterest);
        List<Coord> allowableMoves = Taflman.getAllowableMoves(state, ofInterest);

        assert allowableMoves.contains(state.getSpaceAt(5, 5));
        assert !allowableDestinations.contains(state.getSpaceAt(5, 5));
        assert allowableDestinations.contains(state.getSpaceAt(5, 4));

        assert state.moveTaflman(ofInterest, state.getSpaceAt(5, 5)).getLastMoveResult() == GameState.ILLEGAL_MOVE;
        assert state.moveTaflman(ofInterest, state.getSpaceAt(5, 4)).getLastMoveResult() == GameState.GOOD_MOVE;
    }

}
