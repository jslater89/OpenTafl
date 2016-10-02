package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;
import com.manywords.softworks.tafl.engine.ai.GameTreeState;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.test.TaflTest;

public class AIIncompleteContinuationTest extends TaflTest {

    @Override
    public void statusText(String text) {
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, text);
    }

    @Override
    public void run() {
        AiWorkspace.resetTranspositionTable();

        Rules inflatedRules = RulesSerializer.loadRulesRecord("dim:7 name:Brandub surf:n atkf:y ks:w cenh: cenhe: start:/7/3t3/7/t1T1t1t/7/2tTK2/4T2/");
        Game game = new Game(inflatedRules, null);
        GameState state = game.getCurrentState();

        AiWorkspace workspace = new AiWorkspace(this, game, game.getCurrentState(), 10);
        workspace.chatty = true;
        workspace.explore(10); // Warm up JIT, just in case.

        workspace = new AiWorkspace(this, game, game.getCurrentState(), 10);
        workspace.chatty = true;
        workspace.explore(10);

        GameTreeState root = workspace.getTreeRoot();

        for(GameTreeNode child : root.getBranches()) {
            int pathSize = GameTreeState.getPathStartingWithNode(child).size();
            OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Child " + child.getEnteringMove() + " (" + pathSize + "): " + child.getValue() + (child.valueFromTransposition() ? "T" : ""));
            assert child.getValue() != Evaluator.NO_VALUE;
            assert child.getValue() != Evaluator.INTENTIONALLY_UNVALUED;
            assert !child.valueFromTransposition() && GameTreeState.getPathStartingWithNode(child).size() > 1;
        }
    }
}
