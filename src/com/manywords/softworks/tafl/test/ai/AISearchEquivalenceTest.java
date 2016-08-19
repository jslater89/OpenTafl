package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.RawTerminal;

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
        //Rules r = RulesSerializer.loadRulesRecord("dim:7 name:Brandub_Test surf:n atkf:y ks:w nj:n cj:n cenh: cenhe: start:/3t3/1t1t3/3T3/4tt1/2T1K2/3TT1t/2t4/");
        Rules r = Brandub.newBrandub7();
        Game g = new Game(r, null);
        MoveRecord move;

        GameState state = g.getCurrentState();
        //state.makeMove(new MoveRecord(Coord.get(3, 0), Coord.get(4, 0)));
        state = g.getCurrentState();
        RawTerminal.renderGameState(state);

        AiWorkspace workspaceNoOptimizations = new AiWorkspace(this, g, g.getCurrentState(), 5);
        workspaceNoOptimizations.chatty = true;
        workspaceNoOptimizations.setMaxDepth(5);

        workspaceNoOptimizations.allowIterativeDeepening(false);
        workspaceNoOptimizations.allowContinuation(false);
        workspaceNoOptimizations.allowHorizon(false);
        workspaceNoOptimizations.allowTranspositionTable(false);
        workspaceNoOptimizations.allowKillerMoves(false);
        workspaceNoOptimizations.allowMoveOrdering(false);

        // Allow plenty of extra time to get to depth 5, so it's definitely > (depth4 * 15)
        workspaceNoOptimizations.explore(5);

        workspaceNoOptimizations.printSearchStats();

        move = workspaceNoOptimizations.getTreeRoot().getBestChild().getEnteringMove();

        System.out.println(move);
        System.exit(0);
    }
}
