package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.brandub.Brandub;

class AITwoCornerEscapeAndRulesLoadingTest extends TaflTest implements UiCallback {

    @Override
    public void gameStateAdvanced() {
        // TODO Auto-generated method stub

    }

    @Override
    public void victoryForSide(Side side) {
        // TODO Auto-generated method stub

    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
    }

    @Override
    public void run() {
        Rules rules = Brandub.newAiTwoCornerEscapeTest();
        String rulesString = RulesSerializer.getRulesRecord(rules);
        Rules inflatedRules = RulesSerializer.loadRulesRecord(rulesString);
        Game game = new Game(inflatedRules, null);
        GameState state = game.getCurrentState();

        int victory = GameState.NO_WIN;
        int value = 0;

        for(int i = 0; i < 4; i++) {
            //RawTerminal.renderGameState(state);
            AiWorkspace workspace = new AiWorkspace(game, state, 5);
            //workspace.chatty = true;
            workspace.explore(4);
            MoveRecord nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
            value = workspace.getTreeRoot().getBestChild().getValue();
            //System.out.println("value: " + value);

            victory = state.checkVictory();
            if(victory != GameState.NO_WIN) {
                break;
            }
            else {
                state.makeMove(nextMove);
            }

            state = game.getCurrentState();
        }

        victory = state.checkVictory();
        //RawTerminal.renderGameState(state);

        assert victory == GameState.DEFENDER_WIN;
    }

}
