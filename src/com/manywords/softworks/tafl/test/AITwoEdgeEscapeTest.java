package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.UiCallback;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.ui.RawTerminal;

class AITwoEdgeEscapeTest extends TaflTest implements UiCallback {

    @Override
    public void gameStateAdvanced() {
        // TODO Auto-generated method stub

    }

    @Override
    public void victoryForSide(Side side) {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        Rules rules = SeaBattle.newAiTwoEdgeEscapeTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());

        int victory = GameState.NO_WIN;
        int value = 0;

        for(int i = 0; i < 2; i++) {
            //RawTerminal.renderGameState(state);
            AiWorkspace workspace = new AiWorkspace(game, state, 5);
            workspace.chatty = false;
            workspace.explore(4);
            MoveRecord nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
            value = workspace.getTreeRoot().getBestChild().getValue();

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
