package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.UiCallback;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.brandub.Brandub;

class AICertainKingCaptureTest extends TaflTest implements UiCallback {

    @Override
    public void gameStateAdvanced() {
        // TODO Auto-generated method stub

    }

    @Override
    public void victoryForSide(Side side) {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        Rules rules = Brandub.newAiCertainKingCaptureTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        //RawTerminal.renderGameState(state);
        state = game.getCurrentState();
        AiWorkspace workspace = new AiWorkspace(game, state, 5);
        workspace.explore(3);
        MoveRecord nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().checkVictory() == GameState.ATTACKER_WIN;
    }

}
