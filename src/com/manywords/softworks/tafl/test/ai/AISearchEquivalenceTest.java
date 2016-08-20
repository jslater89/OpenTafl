package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.command.player.external.engine.ExternalEngineClient;
import com.manywords.softworks.tafl.command.player.external.engine.ExternalEngineHost;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;
import com.manywords.softworks.tafl.engine.ai.GameTreeState;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.test.mechanics.ExternalEngineHostTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by jay on 8/19/16.
 */
public class AISearchEquivalenceTest extends TaflTest {
    @Override
    public void statusText(String text) {
        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, text);
    }

    @Override
    public void run() {
        Rules r = RulesSerializer.loadRulesRecord("rules dim:7 name:Brandub surf:n spd:1 atkf:y ks:w nj:n cj:n cenh: cenhe: start:/3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/");
        MoveRecord move;
        short bestValue;

        // 1. NO FEATURES, JUST THE MOVE -------------------------------------------------------------------------------
        Game g = new Game(r, null);

        GameState state = g.getCurrentState();
        state.makeMove(new MoveRecord(Coord.get(3, 0), Coord.get(4, 0)));
        state = g.getCurrentState();

        AiWorkspace workspaceNoOptimizations = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceNoOptimizations.chatty = true;
        workspaceNoOptimizations.setMaxDepth(3);

        workspaceNoOptimizations.allowIterativeDeepening(false);
        workspaceNoOptimizations.allowContinuation(false);
        workspaceNoOptimizations.allowHorizon(false);
        workspaceNoOptimizations.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_OFF);
        workspaceNoOptimizations.allowKillerMoves(false);
        workspaceNoOptimizations.allowMoveOrdering(false);

        workspaceNoOptimizations.explore(5);

        workspaceNoOptimizations.printSearchStats();
        workspaceNoOptimizations.getTreeRoot().printTree("T1: ");

        move = workspaceNoOptimizations.getTreeRoot().getBestChild().getEnteringMove();
        bestValue = workspaceNoOptimizations.getTreeRoot().getBestChild().getValue();
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "1. move: " + move + " value: " + bestValue);

        //7. MOVE ORDERING ---------------------------------------------------------------------------------------------
        AiWorkspace workspaceOrdering = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceOrdering.chatty = true;
        workspaceOrdering.setMaxDepth(3);

        workspaceOrdering.allowIterativeDeepening(false);
        workspaceOrdering.allowContinuation(false);
        workspaceOrdering.allowHorizon(false);
        workspaceOrdering.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_OFF);
        workspaceOrdering.allowKillerMoves(false);
        workspaceOrdering.allowMoveOrdering(true);

        workspaceOrdering.explore(5);

        workspaceOrdering.printSearchStats();
        workspaceOrdering.getTreeRoot().printTree("T2: ");

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "7. move: " + workspaceOrdering.getTreeRoot().getBestChild().getEnteringMove() + " value: " + workspaceOrdering.getTreeRoot().getBestChild().getValue());

        List<List<MoveRecord>> noOptimizationSequences = workspaceNoOptimizations.getAllEnteringSequences();
        List<List<MoveRecord>> orderingSequences = workspaceOrdering.getAllEnteringSequences();

        for(List<MoveRecord> path : orderingSequences) {
            //System.out.println(n.getDepth() + "(" + n.getClass().getSimpleName() + "): " + n.getEnteringMoveSequence() + " " + n.getValue());

            if(noOptimizationSequences.contains(path)) {
                System.out.println("Overlap! Path: " + path);
                GameTreeNode n1 = workspaceOrdering.getTreeRoot().getChildForPath(path);
                GameTreeNode n2 = workspaceNoOptimizations.getTreeRoot().getChildForPath(path);

                assert n1.getEnteringMoveSequence().equals(n2.getEnteringMoveSequence());

                System.out.println(n1.getDepth() + "(" + n1.getClass().getSimpleName() + "): " + n1.getEnteringMoveSequence() + " " + n1.getValue());
                System.out.println(n2.getDepth() + "(" + n2.getClass().getSimpleName() + "): " + n2.getEnteringMoveSequence() + " " + n2.getValue());

                FishyEvaluator.debug = true;
                System.out.println(AiWorkspace.evaluator.evaluate((GameTreeState) n1, ((GameTreeState) n1).mCurrentMaxDepth, n1.getDepth()));
                System.out.println(FishyEvaluator.debugString);

                System.out.println(AiWorkspace.evaluator.evaluate((GameTreeState) n2, ((GameTreeState) n1).mCurrentMaxDepth, n1.getDepth()));
                System.out.println(FishyEvaluator.debugString);
                FishyEvaluator.debug = false;

                assert n1.getValue() == n2.getValue();
            }
        }


        if(!workspaceOrdering.getTreeRoot().getBestChild().getEnteringMove().equals(move)) {
            assert workspaceOrdering.getTreeRoot().getBestChild().getValue() == bestValue;
            System.out.println("warn: different states, same value");
        }

        System.exit(0);


        // 2. EXTERNAL ENGINE MOVE -------------------------------------------------------------------------------------
        ExternalEngineClient c = new ExternalEngineClient();
        c.setDebugMode(true);
        c.start();

        c.setAiFeatures(5, false, false, false, false, false, false);
        c.setThinkTime(5);

        c.mCommCallback.onCommandReceived("rules dim:7 name:Brandub surf:n atkf:y ks:w nj:n cj:n cenh: cenhe: start:/3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/".getBytes(Charset.forName("US-ASCII")));
        c.mCommCallback.onCommandReceived("opponent-move d1-e1 /4t2/3t3/3T3/ttTKTtt/3T3/3t3/3t3/".getBytes(Charset.forName("US-ASCII")));
        c.mCommCallback.onCommandReceived("play defenders".getBytes(Charset.forName("US-ASCII")));

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "2. move: " + c.mWorkspace.getTreeRoot().getBestChild().getEnteringMove() + " value: " + c.mWorkspace.getTreeRoot().getBestChild().getValue());
        assert c.mWorkspace.getTreeRoot().getBestChild().getEnteringMove().equals(move);

        //3. CONTINUATION+HORIZON SEARCHES -----------------------------------------------------------------------------
        AiWorkspace workspaceContinuations = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceContinuations.chatty = true;
        workspaceContinuations.setMaxDepth(5);

        workspaceContinuations.allowIterativeDeepening(false);
        workspaceContinuations.allowContinuation(true);
        workspaceContinuations.allowHorizon(true);
        workspaceContinuations.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_OFF);
        workspaceContinuations.allowKillerMoves(false);
        workspaceContinuations.allowMoveOrdering(false);

        workspaceContinuations.explore(10);

        workspaceContinuations.printSearchStats();

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "3. move: " + workspaceContinuations.getTreeRoot().getBestChild().getEnteringMove() + " value: " + workspaceContinuations.getTreeRoot().getBestChild().getValue());
        assert workspaceContinuations.getTreeRoot().getBestChild().getEnteringMove().equals(move);

        //4. TRANSPOSITION SEARCH (FIXED DEPTH) ------------------------------------------------------------------------
        AiWorkspace workspaceTranspositionFixed = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceTranspositionFixed.chatty = true;
        workspaceTranspositionFixed.setMaxDepth(5);

        workspaceTranspositionFixed.allowIterativeDeepening(false);
        workspaceTranspositionFixed.allowContinuation(false);
        workspaceTranspositionFixed.allowHorizon(false);
        workspaceTranspositionFixed.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_EXACT_ONLY);
        workspaceTranspositionFixed.allowKillerMoves(false);
        workspaceTranspositionFixed.allowMoveOrdering(false);

        workspaceTranspositionFixed.explore(10);

        workspaceTranspositionFixed.printSearchStats();

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "4. move: " + workspaceTranspositionFixed.getTreeRoot().getBestChild().getEnteringMove() + " value: " + workspaceTranspositionFixed.getTreeRoot().getBestChild().getValue());
        assert workspaceTranspositionFixed.getTreeRoot().getBestChild().getEnteringMove().equals(move);

        //5. TRANSPOSITION SEARCH (ANY DEPTH) --------------------------------------------------------------------------
        AiWorkspace workspaceTranspositionAny = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceTranspositionAny.chatty = true;
        workspaceTranspositionAny.setMaxDepth(5);

        workspaceTranspositionAny.allowIterativeDeepening(false);
        workspaceTranspositionAny.allowContinuation(false);
        workspaceTranspositionAny.allowHorizon(false);
        workspaceTranspositionAny.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_ON);
        workspaceTranspositionAny.allowKillerMoves(false);
        workspaceTranspositionAny.allowMoveOrdering(false);

        workspaceTranspositionAny.explore(10);

        workspaceTranspositionAny.printSearchStats();

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "5. move: " + workspaceTranspositionAny.getTreeRoot().getBestChild().getEnteringMove() + " value: " + workspaceTranspositionAny.getTreeRoot().getBestChild().getValue());
        assert workspaceTranspositionAny.getTreeRoot().getBestChild().getEnteringMove().equals(move);

        //6. ALL BUT ORDERING ------------------------------------------------------------------------------------------
        AiWorkspace workspaceAllButOrdering = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceAllButOrdering.chatty = true;
        workspaceAllButOrdering.setMaxDepth(5);

        workspaceAllButOrdering.allowIterativeDeepening(true);
        workspaceAllButOrdering.allowContinuation(true);
        workspaceAllButOrdering.allowHorizon(true);
        workspaceAllButOrdering.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_ON);
        workspaceAllButOrdering.allowKillerMoves(true);
        workspaceAllButOrdering.allowMoveOrdering(false);

        workspaceAllButOrdering.explore(20);

        workspaceAllButOrdering.printSearchStats();

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "6. move: " + workspaceAllButOrdering.getTreeRoot().getBestChild().getEnteringMove() + " value: " + workspaceAllButOrdering.getTreeRoot().getBestChild().getValue());
        assert workspaceAllButOrdering.getTreeRoot().getBestChild().getEnteringMove().equals(move);

        //8. WORKSPACE EVERYTHING --------------------------------------------------------------------------------------
        AiWorkspace workspaceEverything = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceEverything.chatty = true;
        workspaceEverything.setMaxDepth(5);

        workspaceEverything.allowIterativeDeepening(true);
        workspaceEverything.allowContinuation(true);
        workspaceEverything.allowHorizon(true);
        workspaceEverything.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_ON);
        workspaceEverything.allowKillerMoves(true);
        workspaceEverything.allowMoveOrdering(true);

        workspaceEverything.explore(20);

        workspaceEverything.printSearchStats();

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "8. move: " + workspaceEverything.getTreeRoot().getBestChild().getEnteringMove() + " value: " + workspaceEverything.getTreeRoot().getBestChild().getValue());

        if(!workspaceEverything.getTreeRoot().getBestChild().getEnteringMove().equals(move)) {
            assert workspaceEverything.getTreeRoot().getBestChild().getValue() == bestValue;
            System.out.println("warn: different states, same value");
        }
    }
}
