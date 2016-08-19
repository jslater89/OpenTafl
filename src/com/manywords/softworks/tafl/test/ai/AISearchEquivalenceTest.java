package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.command.player.external.engine.ExternalEngineClient;
import com.manywords.softworks.tafl.command.player.external.engine.ExternalEngineHost;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.test.mechanics.ExternalEngineHostTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.nio.charset.Charset;

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
        Rules r = RulesSerializer.loadRulesRecord("rules dim:7 name:Brandub surf:n atkf:y ks:w nj:n cj:n cenh: cenhe: start:/3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/");

        // 1. NO FEATURES, JUST THE MOVE -------------------------------------------------------------------------------
        Game g = new Game(r, null);
        MoveRecord move;

        GameState state = g.getCurrentState();
        state.makeMove(new MoveRecord(Coord.get(3, 0), Coord.get(4, 0)));
        state = g.getCurrentState();

        AiWorkspace workspaceNoOptimizations = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceNoOptimizations.chatty = true;
        workspaceNoOptimizations.setMaxDepth(5);

        workspaceNoOptimizations.allowIterativeDeepening(false);
        workspaceNoOptimizations.allowContinuation(false);
        workspaceNoOptimizations.allowHorizon(false);
        workspaceNoOptimizations.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_OFF);
        workspaceNoOptimizations.allowKillerMoves(false);
        workspaceNoOptimizations.allowMoveOrdering(false);

        workspaceNoOptimizations.explore(5);

        workspaceNoOptimizations.printSearchStats();

        move = workspaceNoOptimizations.getTreeRoot().getBestChild().getEnteringMove();
        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "1. move: " + move);

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

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "2. move: " + c.mWorkspace.getTreeRoot().getBestChild().getEnteringMove());
        assert c.mWorkspace.getTreeRoot().getBestChild().getEnteringMove().equals(move);

        //3. CONTINUATION+HORIZON SEARCHES -----------------------------------------------------------------------------
        AiWorkspace workspaceContinuationHorizon = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceContinuationHorizon.chatty = true;
        workspaceContinuationHorizon.setMaxDepth(5);

        workspaceContinuationHorizon.allowIterativeDeepening(false);
        workspaceContinuationHorizon.allowContinuation(true);
        workspaceContinuationHorizon.allowHorizon(true);
        workspaceContinuationHorizon.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_OFF);
        workspaceContinuationHorizon.allowKillerMoves(false);
        workspaceContinuationHorizon.allowMoveOrdering(false);

        workspaceContinuationHorizon.explore(10);

        workspaceContinuationHorizon.printSearchStats();

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "3. move: " + workspaceContinuationHorizon.getTreeRoot().getBestChild().getEnteringMove());
        assert workspaceContinuationHorizon.getTreeRoot().getBestChild().getEnteringMove().equals(move);

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

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "4. move: " + workspaceTranspositionFixed.getTreeRoot().getBestChild().getEnteringMove());
        assert workspaceTranspositionFixed.getTreeRoot().getBestChild().getEnteringMove().equals(move);

        //5. TRANSPOSITION SEARCH (ANY DEPTH) ------------------------------------------------------------------------
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

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "5. move: " + workspaceTranspositionAny.getTreeRoot().getBestChild().getEnteringMove());
        assert workspaceTranspositionAny.getTreeRoot().getBestChild().getEnteringMove().equals(move);

        //6. ITERATIVE DEEPENING + MOVE ORDERING -----------------------------------------------------------------------
        AiWorkspace workspaceDeepeningOrdering = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceDeepeningOrdering.chatty = true;
        workspaceDeepeningOrdering.setMaxDepth(5);

        workspaceDeepeningOrdering.allowIterativeDeepening(false);
        workspaceDeepeningOrdering.allowContinuation(false);
        workspaceDeepeningOrdering.allowHorizon(false);
        workspaceDeepeningOrdering.allowTranspositionTable(AiWorkspace.TRANSPOSITION_TABLE_OFF);
        workspaceDeepeningOrdering.allowKillerMoves(false);
        workspaceDeepeningOrdering.allowMoveOrdering(true);

        workspaceDeepeningOrdering.explore(30);

        workspaceDeepeningOrdering.printSearchStats();

        OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "6. move: " + workspaceDeepeningOrdering.getTreeRoot().getBestChild().getEnteringMove());
        assert workspaceDeepeningOrdering.getTreeRoot().getBestChild().getEnteringMove().equals(move);

        System.exit(0);
    }
}
