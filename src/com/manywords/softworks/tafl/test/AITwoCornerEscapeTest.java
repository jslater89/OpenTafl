package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.UiCallback;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.ui.RawTerminal;

class AITwoCornerEscapeTest extends TaflTest implements UiCallback {

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
        Rules rules = Brandub.newAiTwoCornerEscapeTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        state = game.getCurrentState();
        AiWorkspace workspace = new AiWorkspace(game, state, 5);
        workspace.explore(4);
        MoveRecord nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(game, state, 5);
        workspace.explore(4);
        nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(game, state, 5);
        workspace.explore(4);
        nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(game, state, 5);
        workspace.explore(4);
        nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(game, state, 5);
        workspace.explore(4);
        nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(game, state, 5);
        workspace.explore(4);
        nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        assert game.getCurrentState().checkVictory() == GameState.DEFENDER_WIN;
    }

}
