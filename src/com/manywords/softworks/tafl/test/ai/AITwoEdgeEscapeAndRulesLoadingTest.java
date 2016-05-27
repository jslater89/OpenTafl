package com.manywords.softworks.tafl.test.ai;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

public class AITwoEdgeEscapeAndRulesLoadingTest extends TaflTest implements UiCallback {

    @Override
    public void gameStarting() {

    }

    @Override
    public void modeChanging(Mode mode, Object gameObject) {

    }

    @Override
    public void awaitingMove(Player currentPlayer, boolean isAttackingSide) {

    }

    @Override
    public void timeUpdate(Side side) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {
        System.out.println(text);
    }

    @Override
    public void modalStatus(String title, String text) {

    }

    @Override
    public void gameStateAdvanced() {

    }

    @Override
    public void victoryForSide(Side side) {

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
        AiWorkspace.resetTranspositionTable();
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
            //workspace.chatty = true;
            workspace.explore(1);
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
