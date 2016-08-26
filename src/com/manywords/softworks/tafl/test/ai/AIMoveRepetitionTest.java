package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

import java.util.ArrayList;
import java.util.List;

// TODO: revamp this test to show that the AI knows not to lose
public class AIMoveRepetitionTest extends TaflTest implements UiCallback {

    @Override
    public void run() {
        AiWorkspace.resetTranspositionTable();

        Rules rules = Brandub.newAiMoveRepetitionTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();


        state = game.getCurrentState();
        AiWorkspace workspace = new AiWorkspace(this, game, state, 5);
        workspace.explore(1);
        MoveRecord nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(this, game, state, 5);
        workspace.explore(1);
        nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(this, game, state, 5);
        workspace.explore(1);
        nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        state.makeMove(nextMove);

        state = game.getCurrentState();
        workspace = new AiWorkspace(this, game, state, 5);
        workspace.explore(1);
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
