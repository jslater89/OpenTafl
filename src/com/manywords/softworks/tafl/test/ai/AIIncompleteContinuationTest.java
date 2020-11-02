package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.ai.alphabeta.FishyWorkspace;
import com.manywords.softworks.tafl.engine.ai.alphabeta.AlphaBetaGameTreeNode;
import com.manywords.softworks.tafl.engine.ai.alphabeta.AlphaBetaGameTreeState;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.test.TaflTest;

public class AIIncompleteContinuationTest extends TaflTest {

    @Override
    public void statusText(String text) {
        Log.println(Log.Level.VERBOSE, text);
    }

    @Override
    public void run() {
        FishyWorkspace.resetTranspositionTable();
        Rules inflatedRules = null;
        try {
            inflatedRules = RulesSerializer.loadRulesRecord("dim:7 name:Brandub surf:n atkf:y ks:w cenh: cenhe: start:/7/3t3/7/t1T1t1t/7/2tTK2/4T2/");
        }
        catch(NotationParseException e) {
            assert false;
        }
        Game game = new Game(inflatedRules, null);
        GameState state = game.getCurrentState();

        FishyWorkspace workspace = new FishyWorkspace(this, game, game.getCurrentState(), 10);
        workspace.chatty = true;
        workspace.explore(10); // Warm up JIT, just in case.

        workspace = new FishyWorkspace(this, game, game.getCurrentState(), 10);
        workspace.chatty = true;
        workspace.explore(10);

        AlphaBetaGameTreeState root = workspace.getTreeRoot();

        for(AlphaBetaGameTreeNode child : root.getBranches()) {
            int pathSize = AlphaBetaGameTreeState.getPathStartingWithNode(child).size();
            Log.println(Log.Level.VERBOSE, "Child " + child.getEnteringMove() + " (" + pathSize + "): " + child.getValue() + (child.valueFromTransposition() ? "T" : ""));
            assert child.getValue() != FishyEvaluator.NO_VALUE;
            assert child.getValue() != FishyEvaluator.INTENTIONALLY_UNVALUED;
            // TODO: figure out what this is supposed to be testing
            //assert !child.valueFromTransposition() && GameTreeState.getPathStartingWithNode(child).size() > 1;
        }
    }
}
