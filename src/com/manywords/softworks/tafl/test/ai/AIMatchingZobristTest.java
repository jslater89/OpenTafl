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

public class AIMatchingZobristTest extends TaflTest implements UiCallback {

    @Override
    public void run() {
        AiWorkspace.resetTranspositionTable();

        Rules rules = Brandub.newAiMoveRepetitionTest();
        Game game = new Game(rules, null);
        GameState state;


        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);
        AiWorkspace workspace = new AiWorkspace(this, game, state, 5);
        //workspace.chatty = true;
        workspace.explore(1);
        MoveRecord nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        long zobrist = workspace.getTreeRoot().getBestChild().getZobrist();
        long zobrist2 = workspace.getTreeRoot().updateZobristHash(workspace.getTreeRoot().getZobrist(), workspace.getTreeRoot().getBoard(), nextMove, true);

        state.makeMove(nextMove);

        state = game.getCurrentState();
        assert zobrist == zobrist2;
        assert zobrist == state.mZobristHash;
    }

}
