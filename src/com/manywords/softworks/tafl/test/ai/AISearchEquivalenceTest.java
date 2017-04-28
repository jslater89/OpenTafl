package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.test.TaflTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 8/19/16.
 */
public class AISearchEquivalenceTest extends TaflTest {
    @Override
    public void statusText(String text) {
        Log.println(Log.Level.CHATTY, text);
    }

    @Override
    public void run() {
        //Rules r = RulesSerializer.loadRulesRecord("rules dim:5 name:MiniTafl surf:n spd:1 atkf:y ks:w nj:n cj:n cens:tcnkTCNK cenh: cenhe: start:/1t3/t1T2/2K2/5/3t1/");
        Rules r = null;
        try {
            r = RulesSerializer.loadRulesRecord("rules dim:7 name:Brandub_Test surf:n atkf:n ks:w nj:n cj:n cens:tcnkTCNK cenh: cenhe: start:/4t2/3t3/3T3/ttTKTtt/3T3/3t3/3t3/");
        }
        catch(NotationParseException e) {
            assert false;
        }

        MoveRecord move = null;
        short bestValue = Evaluator.NO_VALUE;
        List<MoveRecord> equivalentMoves = new ArrayList<>();
        List<MoveRecord> localEquivalentMoves = new ArrayList<>();

        AiWorkspace tempWorkspace;
        GameTreeNode bestChild;

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
        workspaceStraightMinimax.allowHistoryTable(false);

        workspaceStraightMinimax.explore(5);

        workspaceStraightMinimax.printSearchStats();
        //workspaceStraightMinimax.getTreeRoot().printTree("T1: ");

        move = workspaceStraightMinimax.getTreeRoot().getBestChild().getEnteringMove();
        bestValue = workspaceStraightMinimax.getTreeRoot().getBestChild().getValue();

        for(GameTreeNode n : workspaceStraightMinimax.getTreeRoot().getBranches()) {
            if(n.getValue() == bestValue) equivalentMoves.add(n.getEnteringMove());
        }
        Log.println(Log.Level.NORMAL, "0. Straight minimax move: " + move + " value: " + bestValue);
        Log.println(Log.Level.NORMAL, "0. " + equivalentMoves.size() + " equivalent moves (including best): " + equivalentMoves);

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
        workspaceNoOptimizations.allowHistoryTable(false);

        workspaceNoOptimizations.explore(5);

        workspaceNoOptimizations.printSearchStats();
        //workspaceNoOptimizations.getTreeRoot().printTree("T1: ");

        localEquivalentMoves.clear();
        for(GameTreeNode n : workspaceNoOptimizations.getTreeRoot().getBranches()) {
            if(n.getValue() == workspaceNoOptimizations.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        Log.println(Log.Level.NORMAL, "1. Alpha-beta move: " + workspaceNoOptimizations.getTreeRoot().getBestChild().getEnteringMove() + " value: " + workspaceNoOptimizations.getTreeRoot().getBestChild().getValue());
        Log.println(Log.Level.NORMAL, "1. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        localEquivalentMoves.retainAll(equivalentMoves);
        assert localEquivalentMoves.size() > 0;
        if(!isMoveEquivalent(workspaceNoOptimizations.getTreeRoot().getBestChild().getEnteringMove(), equivalentMoves)) {
            Log.println(Log.Level.NORMAL, "warn: best move not in equivalent moves");
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
        workspaceOrdering.allowHistoryTable(false);

        workspaceOrdering.explore(5);

        workspaceOrdering.printSearchStats();
        //workspaceOrdering.getTreeRoot().printTree("T2: ");

        localEquivalentMoves.clear();
        for(GameTreeNode n : workspaceOrdering.getTreeRoot().getBranches()) {
            if(n.getValue() == workspaceOrdering.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        Log.println(Log.Level.NORMAL, "2. Move-ordering alpha-beta move: " + workspaceOrdering.getTreeRoot().getBestChild().getEnteringMove() + " value: " + workspaceOrdering.getTreeRoot().getBestChild().getValue());
        Log.println(Log.Level.NORMAL, "2. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        localEquivalentMoves.retainAll(equivalentMoves);
        assert localEquivalentMoves.size() > 0;
        if(!isMoveEquivalent(workspaceOrdering.getTreeRoot().getBestChild().getEnteringMove(), equivalentMoves)) {
            Log.println(Log.Level.NORMAL, "warn: best move not in equivalent moves");
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

        workspaceAlphabetaBenchmark.explore(10);

        workspaceAlphabetaBenchmark.printSearchStats();
        //workspaceAlphabetaBenchmark.getTreeRoot().printTree("T1: ");

        move = workspaceAlphabetaBenchmark.getTreeRoot().getBestChild().getEnteringMove();
        bestValue = workspaceAlphabetaBenchmark.getTreeRoot().getBestChild().getValue();

        equivalentMoves.clear();
        for(GameTreeNode n : workspaceAlphabetaBenchmark.getTreeRoot().getBranches()) {
            if(n.getValue() == bestValue) equivalentMoves.add(n.getEnteringMove());
        }

        Log.println(Log.Level.NORMAL, "3. Alpha-beta benchmark move: " + move + " value: " + bestValue);
        Log.println(Log.Level.NORMAL, "3. " + equivalentMoves.size() + " equivalent moves (including best): " + equivalentMoves);

        //4. HISTORY TABLE SEARCH --------------------------------------------------------------------------------------
        AiWorkspace workspaceHistoryOrdering = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceHistoryOrdering.chatty = true;
        workspaceHistoryOrdering.setMaxDepth(5);

        workspaceHistoryOrdering.allowIterativeDeepening(false);
        workspaceHistoryOrdering.allowContinuation(false);
        workspaceHistoryOrdering.allowHorizon(false);
        workspaceHistoryOrdering.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_OFF);
        workspaceHistoryOrdering.allowKillerMoves(false);
        workspaceHistoryOrdering.allowMoveOrdering(true);
        workspaceHistoryOrdering.allowHistoryTable(true);

        workspaceHistoryOrdering.explore(10);

        workspaceHistoryOrdering.printSearchStats();

        tempWorkspace = workspaceHistoryOrdering;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        bestChild = tempWorkspace.getTreeRoot().getBestChild();
        Log.println(Log.Level.NORMAL, "4. History table move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        Log.println(Log.Level.NORMAL, "4. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        assert isMoveEquivalent(bestChild.getEnteringMove(), equivalentMoves);

        //5. CONTINUATION+HORIZON SEARCHES -----------------------------------------------------------------------------
        // The whole point of continuation/horizon searches is that they may reveal something different
//        AiWorkspace workspaceContinuations = new AiWorkspace(this, g, g.getCurrentState(), 5);
//        workspaceContinuations.chatty = true;
//        workspaceContinuations.setMaxDepth(5);
//
//        workspaceContinuations.allowIterativeDeepening(false);
//        workspaceContinuations.allowContinuation(true);
//        workspaceContinuations.allowHorizon(true);
//        workspaceContinuations.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_OFF);
//        workspaceContinuations.allowKillerMoves(false);
//        workspaceContinuations.allowMoveOrdering(false);
//
//        workspaceContinuations.explore(10);
//
//        workspaceContinuations.printSearchStats();
//
//        tempWorkspace = workspaceContinuations;
//        localEquivalentMoves.clear();
//        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
//            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
//        }
//
//        bestChild = tempWorkspace.getTreeRoot().getBestChild();
//        OpenTafl.println(OpenTafl.Level.NORMAL, "5. Continuation+horizon move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
//        OpenTafl.println(OpenTafl.Level.NORMAL, "5. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
//        assert isMoveEquivalent(bestChild.getEnteringMove(), equivalentMoves);

        //6. TRANSPOSITION SEARCH (FIXED DEPTH) ------------------------------------------------------------------------
        AiWorkspace workspaceTranspositionFixed = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceTranspositionFixed.chatty = true;
        workspaceTranspositionFixed.setMaxDepth(5);

        workspaceTranspositionFixed.allowIterativeDeepening(false);
        workspaceTranspositionFixed.allowContinuation(false);
        workspaceTranspositionFixed.allowHorizon(false);
        workspaceTranspositionFixed.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_EXACT_ONLY);
        workspaceTranspositionFixed.allowKillerMoves(false);
        workspaceTranspositionFixed.allowMoveOrdering(true);

        workspaceTranspositionFixed.explore(10);

        workspaceTranspositionFixed.printSearchStats();

        tempWorkspace = workspaceTranspositionFixed;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        bestChild = tempWorkspace.getTreeRoot().getBestChild();
        Log.println(Log.Level.NORMAL, "6. Exact transposition move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        Log.println(Log.Level.NORMAL, "6. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        assert isMoveEquivalent(bestChild.getEnteringMove(), equivalentMoves);

        //7. TRANSPOSITION SEARCH (ANY DEPTH) --------------------------------------------------------------------------
        AiWorkspace workspaceTranspositionAny = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceTranspositionAny.chatty = true;
        workspaceTranspositionAny.setMaxDepth(5);

        workspaceTranspositionAny.allowIterativeDeepening(false);
        workspaceTranspositionAny.allowContinuation(false);
        workspaceTranspositionAny.allowHorizon(false);
        workspaceTranspositionAny.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_ON);
        workspaceTranspositionAny.allowKillerMoves(false);
        workspaceTranspositionAny.allowMoveOrdering(true);

        workspaceTranspositionAny.explore(10);

        workspaceTranspositionAny.printSearchStats();

        tempWorkspace = workspaceTranspositionAny;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        bestChild = tempWorkspace.getTreeRoot().getBestChild();
        Log.println(Log.Level.NORMAL, "7. Any transposition move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        Log.println(Log.Level.NORMAL, "7. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        assert isMoveEquivalent(bestChild.getEnteringMove(), equivalentMoves);

        //8. ALL BUT ORDERING ------------------------------------------------------------------------------------------
        AiWorkspace workspaceAllButOrdering = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceAllButOrdering.chatty = true;
        workspaceAllButOrdering.setMaxDepth(5);

        workspaceAllButOrdering.allowIterativeDeepening(true);
        workspaceAllButOrdering.allowContinuation(false);
        workspaceAllButOrdering.allowHorizon(false);
        workspaceAllButOrdering.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_ON);
        workspaceAllButOrdering.allowKillerMoves(true);
        workspaceAllButOrdering.allowMoveOrdering(false);

        workspaceAllButOrdering.explore(10);

        workspaceAllButOrdering.printSearchStats();

        tempWorkspace = workspaceAllButOrdering;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        bestChild = tempWorkspace.getTreeRoot().getBestChild();
        Log.println(Log.Level.NORMAL, "8. All but ordering move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        Log.println(Log.Level.NORMAL, "8. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        assert isMoveEquivalent(bestChild.getEnteringMove(), equivalentMoves);

        //9. WORKSPACE EVERYTHING --------------------------------------------------------------------------------------
        AiWorkspace workspaceEverything = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceEverything.chatty = true;
        workspaceEverything.setMaxDepth(5);

        workspaceEverything.allowIterativeDeepening(true);
        workspaceEverything.allowContinuation(false);
        workspaceEverything.allowHorizon(false);
        workspaceEverything.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_ON);
        workspaceEverything.allowKillerMoves(true);
        workspaceEverything.allowMoveOrdering(true);

        workspaceEverything.explore(10);

        workspaceEverything.printSearchStats();

        tempWorkspace = workspaceEverything;
        localEquivalentMoves.clear();
        for(GameTreeNode n : tempWorkspace.getTreeRoot().getBranches()) {
            if(n.getValue() == tempWorkspace.getTreeRoot().getBestChild().getValue()) localEquivalentMoves.add(n.getEnteringMove());
        }

        bestChild = tempWorkspace.getTreeRoot().getBestChild();
        Log.println(Log.Level.NORMAL, "9. All features move: " + bestChild.getEnteringMove() + " value: " + bestChild.getValue());
        Log.println(Log.Level.NORMAL, "9. " + localEquivalentMoves.size() + " local equivalent moves: " + localEquivalentMoves);
        assert isMoveEquivalent(bestChild.getEnteringMove(), equivalentMoves);
    }

    private boolean isMoveEquivalent(MoveRecord m, List<MoveRecord> testAgainst) {
        for(MoveRecord test : testAgainst) {
            if(MoveRecord.isRotationOrMirror(7, m, test)) return true;
        }

        return false;
    }
}
