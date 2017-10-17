package com.manywords.softworks.tafl.test.ai.tactics;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.test.TaflTest;

import java.util.List;

import static com.manywords.softworks.tafl.Log.Level.VERBOSE;

/**
 * Created by jay on 8/18/16.
 */
public class AITacticsEscapeTest extends TaflTest {

    @Override
    public void statusText(String text) {
        Log.println(VERBOSE, text);
    }

    @Override
    public void run() {
        Rules r = null;
        try {
            r = RulesSerializer.loadRulesRecord("dim:7 name:Brandub_Test surf:n atkf:y ks:w nj:n cj:n cenh: cenhe: start:/3t3/1t1t3/3T3/4tt1/2T1K2/3TT1t/2t4/");
        }
        catch(NotationParseException e) {
            assert false;
        }

        Game game = new Game(r, this);

        GameState state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());

        int victory = GameState.GOOD_MOVE;
        int value = 0;

        for(int i = 0; i < 4; i++) {
            victory = state.checkVictory();
            //System.out.println("victory " + victory);
            if(victory != GameState.GOOD_MOVE) {
                break;
            }

            //RawTerminal.renderGameState(state);
            AiWorkspace workspace = new AiWorkspace(this, game, state, 25);
            workspace.chatty = true;
            workspace.explore(5);
            MoveRecord nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
            value = workspace.getTreeRoot().getBestChild().getValue();
            List<GameTreeNode> path = workspace.getTreeRoot().getBestPath();

            workspace.printSearchStats();

            state.makeMove(nextMove);
            state = game.getCurrentState();
        }

        victory = state.checkVictory();
        //System.out.println("victory " + victory);
        //RawTerminal.renderGameState(state);

        assert victory == GameState.DEFENDER_WIN;
    }
}
