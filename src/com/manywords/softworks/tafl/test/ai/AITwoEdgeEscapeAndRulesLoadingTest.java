package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.alphabeta.FishyWorkspace;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.test.TaflTest;

public class AITwoEdgeEscapeAndRulesLoadingTest extends TaflTest {

    @Override
    public void statusText(String text) {
        Log.println(Log.Level.VERBOSE, text);
    }

    @Override
    public void run() {
        FishyWorkspace.resetTranspositionTable();
        Rules rules = SeaBattle.newAiTwoEdgeEscapeTest();
        String rulesString = RulesSerializer.getRulesRecord(rules, false);

        Rules inflatedRules = null;
        try {
            inflatedRules = RulesSerializer.loadRulesRecord(rulesString);
        }
        catch(NotationParseException e) {
            assert false;
        }

        Game game = new Game(inflatedRules, null);
        GameState state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());

        int victory = GameState.GOOD_MOVE;
        int value = 0;


        //RawTerminal.renderGameState(state);
        FishyWorkspace workspace = new FishyWorkspace(this, game, state, 5);
        workspace.chatty = true;
        workspace.explore(5);
        MoveRecord nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
        value = workspace.getTreeRoot().getBestChild().getValue();
        state.makeMove(nextMove);

        /*
        System.out.println("value: " + value);
        for(GameTreeNode node : workspace.getTreeRoot().getBestPath()) {
            for(GameTreeNode child : node.getBranches()) {
                System.out.print(child.getEnteringMove() + ", ");
            }
            System.out.println();
            System.out.println("Node: " + node.getEnteringMove() + " " + value);
        }
        */

        workspace.printSearchStats();


        state = game.getCurrentState();

        victory = state.checkVictory();
        //RawTerminal.renderGameState(state);

        assert victory == GameState.DEFENDER_WIN;
    }

}
