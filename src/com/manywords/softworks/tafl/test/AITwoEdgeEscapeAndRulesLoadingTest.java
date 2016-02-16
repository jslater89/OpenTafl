package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.player.Player;

class AITwoEdgeEscapeAndRulesLoadingTest extends TaflTest implements UiCallback {

    @Override
    public void gameStarting() {

    }

    @Override
    public void awaitingMove(Player currentPlayer, boolean isAttackingSide) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {

    }

    @Override
    public void gameStateAdvanced() {
        // TODO Auto-generated method stub

    }

    @Override
    public void victoryForSide(Side side) {
        // TODO Auto-generated method stub

    }

    @Override
    public void gameFinished() {

    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
    }

    @Override
    public boolean inGame() {
        return false;
    }

    @Override
    public void run() {
        Rules rules = SeaBattle.newAiTwoEdgeEscapeTest();
        String rulesString = RulesSerializer.getRulesRecord(rules);
        Rules inflatedRules = RulesSerializer.loadRulesRecord(rulesString);
        Game game = new Game(inflatedRules, null);
        GameState state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());

        int victory = GameState.GOOD_MOVE;
        int value = 0;

        for(int i = 0; i < 2; i++) {
            //RawTerminal.renderGameState(state);
            AiWorkspace workspace = new AiWorkspace(this, game, state, 5);
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
            if(victory != GameState.GOOD_MOVE) {
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
