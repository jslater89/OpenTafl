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
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.test.mechanics.ExternalEngineHostTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        //Rules r = RulesSerializer.loadRulesRecord("rules dim:5 name:MiniTafl surf:n spd:1 atkf:y ks:w nj:n cj:n cens:tcnkTCNK cenh: cenhe: start:/1t3/t1T2/2K2/5/3t1/");
        Rules r = RulesSerializer.loadRulesRecord("rules dim:7 name:Brandub_Test surf:n atkf:n ks:w nj:n cj:n cens:tcnkTCNK cenh: cenhe: start:/4t2/3t3/3T3/ttTKTtt/3T3/3t3/3t3/");
        MoveRecord move = null;
        short bestValue = Evaluator.NO_VALUE;
        List<MoveRecord> equivalentMoves = new ArrayList<>();
        List<MoveRecord> localEquivalentMoves = new ArrayList<>();

        // 0. MINIMAX STRAIGHT UP --------------------------------------------------------------------------------------
        Game g = new Game(r, null);
        GameState state = g.getCurrentState();

        //RawTerminal.renderGameState(state);

        AiWorkspace workspaceStraightMinimax = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceStraightMinimax.chatty = true;
        workspaceStraightMinimax.setMaxDepth(3);

        workspaceStraightMinimax.allowCutoffs(false);
        workspaceStraightMinimax.allowIterativeDeepening(false);
        workspaceStraightMinimax.allowContinuation(false);
        workspaceStraightMinimax.allowHorizon(false);
        workspaceStraightMinimax.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_OFF);
        workspaceStraightMinimax.allowKillerMoves(false);
        workspaceStraightMinimax.allowMoveOrdering(false);

        workspaceStraightMinimax.explore(5);

        workspaceStraightMinimax.printSearchStats();
        //workspaceStraightMinimax.getTreeRoot().printTree("T1: ");

        move = workspaceStraightMinimax.getTreeRoot().getBestChild().getEnteringMove();
        bestValue = workspaceStraightMinimax.getTreeRoot().getBestChild().getValue();

        for(GameTreeNode n : workspaceStraightMinimax.getTreeRoot().getBranches()) {
            if(n.getValue() == bestValue) equivalentMoves.add(n.getEnteringMove());
        }
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "0. Straight minimax move: " + move + " value: " + bestValue);
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "0. " + equivalentMoves.size() + " equivalent moves (including best): " + equivalentMoves);

        // 1. NO FEATURES, JUST THE MOVE -------------------------------------------------------------------------------
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
        //workspaceNoOptimizations.getTreeRoot().printTree("T1: ");

        localEquivalentMoves.clear();
        for(GameTreeNode n : workspaceNoOptimizations.getTreeRoot().getBranches()) {
            if(n.getValue() == workspaceNoOptimizations.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "1. Alpha-beta move: " + workspaceNoOptimizations.getTreeRoot().getBestChild().getEnteringMove() + " value: " + workspaceNoOptimizations.getTreeRoot().getBestChild().getValue());
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "1. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        localEquivalentMoves.retainAll(equivalentMoves);
        assert localEquivalentMoves.size() > 0;
        if(!equivalentMoves.contains(workspaceNoOptimizations.getTreeRoot().getBestChild().getEnteringMove())) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "warn: best move not in equivalent moves");
        }

        //2. MOVE ORDERING ---------------------------------------------------------------------------------------------
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
        //workspaceOrdering.getTreeRoot().printTree("T2: ");

        localEquivalentMoves.clear();
        for(GameTreeNode n : workspaceOrdering.getTreeRoot().getBranches()) {
            if(n.getValue() == workspaceOrdering.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "2. Move-ordering alpha-beta move: " + workspaceOrdering.getTreeRoot().getBestChild().getEnteringMove() + " value: " + workspaceOrdering.getTreeRoot().getBestChild().getValue());
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "2. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        localEquivalentMoves.retainAll(equivalentMoves);
        assert localEquivalentMoves.size() > 0;
        if(!equivalentMoves.contains(workspaceOrdering.getTreeRoot().getBestChild().getEnteringMove())) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "warn: best move not in equivalent moves");
        }

        //3. BENCHMARK DEPTH-5 ALPHA-BETA ONLY SEARCH ------------------------------------------------------------------
        AiWorkspace workspaceAlphabetaBenchmark = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceAlphabetaBenchmark.chatty = true;
        workspaceAlphabetaBenchmark.setMaxDepth(5);

        workspaceAlphabetaBenchmark.allowIterativeDeepening(false);
        workspaceAlphabetaBenchmark.allowContinuation(false);
        workspaceAlphabetaBenchmark.allowHorizon(false);
        workspaceAlphabetaBenchmark.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_OFF);
        workspaceAlphabetaBenchmark.allowKillerMoves(false);
        workspaceAlphabetaBenchmark.allowMoveOrdering(false);

        workspaceAlphabetaBenchmark.explore(5);

        workspaceAlphabetaBenchmark.printSearchStats();
        //workspaceAlphabetaBenchmark.getTreeRoot().printTree("T1: ");

        move = workspaceAlphabetaBenchmark.getTreeRoot().getBestChild().getEnteringMove();
        bestValue = workspaceAlphabetaBenchmark.getTreeRoot().getBestChild().getValue();

        equivalentMoves.clear();
        for(GameTreeNode n : workspaceAlphabetaBenchmark.getTreeRoot().getBranches()) {
            if(n.getValue() == bestValue) equivalentMoves.add(n.getEnteringMove());
        }

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "3. Alpha-beta benchmark move: " + move + " value: " + bestValue);
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "3. " + equivalentMoves.size() + " equivalent moves (including best): " + equivalentMoves);

        //4. EXTERNAL ENGINE MOVE --------------------------------------------------------------------------------------
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

        AiWorkspace tempWorkspace = c.mWorkspace;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        GameTreeNode bestChild = tempWorkspace.getTreeRoot().getBestChild();
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "4. External engine move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "4. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        localEquivalentMoves.retainAll(equivalentMoves);
        assert localEquivalentMoves.size() > 0;
        if(!equivalentMoves.contains(bestChild.getEnteringMove())) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "warn: best move not in equivalent moves");
        }

        //5. CONTINUATION+HORIZON SEARCHES -----------------------------------------------------------------------------
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

        tempWorkspace = workspaceContinuations;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        bestChild = tempWorkspace.getTreeRoot().getBestChild();
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "5. Continuation+horizon move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "5. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        localEquivalentMoves.retainAll(equivalentMoves);
        assert localEquivalentMoves.size() > 0;
        if(!equivalentMoves.contains(bestChild.getEnteringMove())) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "warn: best move not in equivalent moves");
        }

        //6. TRANSPOSITION SEARCH (FIXED DEPTH) ------------------------------------------------------------------------
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

        tempWorkspace = workspaceTranspositionFixed;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        bestChild = tempWorkspace.getTreeRoot().getBestChild();
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "6. Exact transposition move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "6. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        localEquivalentMoves.retainAll(equivalentMoves);
        assert localEquivalentMoves.size() > 0;
        if(!equivalentMoves.contains(bestChild.getEnteringMove())) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "warn: best move not in equivalent moves");
        }

        //7. TRANSPOSITION SEARCH (ANY DEPTH) --------------------------------------------------------------------------
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

        tempWorkspace = workspaceTranspositionAny;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        bestChild = tempWorkspace.getTreeRoot().getBestChild();
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "7. Any transposition move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "7. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        localEquivalentMoves.retainAll(equivalentMoves);
        assert localEquivalentMoves.size() > 0;
        if(!equivalentMoves.contains(bestChild.getEnteringMove())) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "warn: best move not in equivalent moves");
        }

        //8. ALL BUT ORDERING ------------------------------------------------------------------------------------------
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

        tempWorkspace = workspaceAllButOrdering;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        bestChild = tempWorkspace.getTreeRoot().getBestChild();
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "8. All but ordering move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "8. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        localEquivalentMoves.retainAll(equivalentMoves);
        assert localEquivalentMoves.size() > 0;
        if(!equivalentMoves.contains(bestChild.getEnteringMove())) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "warn: best move not in equivalent moves");
        }

        //9. WORKSPACE EVERYTHING --------------------------------------------------------------------------------------
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

        tempWorkspace = workspaceEverything;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        bestChild = tempWorkspace.getTreeRoot().getBestChild();
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "9. All features move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "9. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        localEquivalentMoves.retainAll(equivalentMoves);
        assert localEquivalentMoves.size() > 0;
        if(!equivalentMoves.contains(bestChild.getEnteringMove())) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "warn: best move not in equivalent moves");
        }
    }
}
