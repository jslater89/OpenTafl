package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.player.Player;

import java.util.List;

class AITwoCornerEscapeAndRulesLoadingTest extends TaflTest implements UiCallback {

    @Override
    public void gameStarting() {

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
        AiWorkspace.resetTranspositionTable();

        Rules rules = Brandub.newAiTwoCornerEscapeTest();
        String rulesString = RulesSerializer.getRulesRecord(rules);
        Rules inflatedRules = RulesSerializer.loadRulesRecord(rulesString);
        Game game = new Game(inflatedRules, null);
        GameState state = game.getCurrentState();

        int victory = GameState.GOOD_MOVE;
        int value = 0;

        for(int i = 0; i < 4; i++) {
            victory = state.checkVictory();
            //System.out.println("victory " + victory);
            if(victory != GameState.GOOD_MOVE) {
                break;
            }

            //RawTerminal.renderGameState(state);
            AiWorkspace workspace = new AiWorkspace(this, game, state, 5);
            //workspace.chatty = true;
            workspace.explore(1);
            MoveRecord nextMove = workspace.getTreeRoot().getBestChild().getEnteringMove();
            value = workspace.getTreeRoot().getBestChild().getValue();
            List<GameTreeNode> path = workspace.getTreeRoot().getBestPath();

            state.makeMove(nextMove);
            state = game.getCurrentState();
        }

        victory = state.checkVictory();
        //System.out.println("victory " + victory);
        //RawTerminal.renderGameState(state);

        assert victory == GameState.DEFENDER_WIN;
    }

}
