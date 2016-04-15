package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.player.Player;

class ThreefoldVictoryTest extends TaflTest implements UiCallback {

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
        Rules rules = Berserk.newBerserk11();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        // Target: start position

        // First time at the position
        //RawTerminal.renderGameState(state);
        state.moveTaflman(state.getPieceAt(5, 3), state.getSpaceAt(4, 3));
        state = game.getCurrentState();

        state.moveTaflman(state.getPieceAt(5, 1), state.getSpaceAt(4, 1));
        state = game.getCurrentState();


        state.moveTaflman(state.getPieceAt(4, 3), state.getSpaceAt(5, 3));
        state = game.getCurrentState();

        state.moveTaflman(state.getPieceAt(4, 1), state.getSpaceAt(5, 1));
        state = game.getCurrentState();
        // Second time at the position
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(5, 3), state.getSpaceAt(4, 3));
        state = game.getCurrentState();

        state.moveTaflman(state.getPieceAt(5, 1), state.getSpaceAt(4, 1));
        state = game.getCurrentState();

        state.moveTaflman(state.getPieceAt(4, 3), state.getSpaceAt(5, 3));
        state = game.getCurrentState();

        state.moveTaflman(state.getPieceAt(4, 1), state.getSpaceAt(5, 1));
        state = game.getCurrentState();
        // Third time at the position
        //RawTerminal.renderGameState(state);

        // In berserk, the player forcing the repetition loses.
        assert state.checkVictory() == GameState.ATTACKER_WIN;
    }

}
