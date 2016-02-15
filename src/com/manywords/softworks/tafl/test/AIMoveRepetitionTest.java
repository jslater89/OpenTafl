package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.brandub.Brandub;

import java.util.ArrayList;
import java.util.List;

class AIMoveRepetitionTest extends TaflTest implements UiCallback {

    @Override
    public void gameStateAdvanced() {
        // TODO Auto-generated method stub

    }

    @Override
    public void victoryForSide(Side side) {
        // TODO Auto-generated method stub

    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
    }

    @Override
    public void run() {
        //TODO: implement

        Rules rules = Brandub.newAiMoveRepetitionTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();


        state = game.getCurrentState();
        AiWorkspace workspace = new AiWorkspace(game, state, 5);
        workspace.explore(3);
        MoveRecord nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(game, state, 5);
        workspace.explore(3);
        nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(game, state, 5);
        workspace.explore(3);
        nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(game, state, 5);
        workspace.explore(3);
        nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();

        boolean repeatedStates = false;
        List<Long> knownZobrists = new ArrayList<Long>();
        for(GameState historicalState : game.getHistory()) {
            if(knownZobrists.contains(historicalState.mZobristHash)) {
                repeatedStates = true;
                break;
            }
            else {
                knownZobrists.add(historicalState.mZobristHash);
            }
        }

        assert repeatedStates == false;
    }

}
