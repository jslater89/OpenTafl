package com.manywords.softworks.tafl.ui.player.external.engine;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;
import com.manywords.softworks.tafl.notation.PositionSerializer;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.player.Player;
import com.manywords.softworks.tafl.ui.player.UiWorkerThread;

import java.nio.charset.Charset;

/**
 * Created by jay on 3/10/16.
 */
public class ExternalEngineClient implements UiCallback {
    public static ExternalEngineClient instance;
    public static void run() {
        instance = new ExternalEngineClient();
        instance.start();
    }

    public CommunicationThread mCommThread;
    public CommunicationThread.CommunicationThreadCallback mCommCallback;

    private Rules mRules;
    private Game mGame;
    private GameClock.ClockEntry mTimeRemaining;
    private UiWorkerThread mAiThread;

    public void start() {
        System.setErr(System.out);
        mCommCallback = new CommCallback();
        mCommThread = new CommunicationThread(System.out, System.in, mCommCallback);
        mCommThread.start();

        TerminalSettings.loadFromFile();
        mCommThread.sendCommand("hello\n".getBytes());
    }

    private void handleRulesCommand(String command) {
        command = command.replace("rules ", "");
        mRules = RulesSerializer.loadRulesRecord(command);
        mGame = new Game(mRules, this);
    }

    private void handlePositionCommand(String command) {
        command = command.replace("position ", "");
        GameState state = PositionSerializer.loadPositionRecord(command, mGame);
        mGame.setCurrentState(state);
    }

    private void handleSideCommand(String command) {
        boolean attackers = false;
        if(command.contains("attackers")) attackers = true;
        Side s = (attackers ? mGame.getCurrentState().getAttackers() : mGame.getCurrentState().getDefenders());

        mGame.getCurrentState().setCurrentSide(s);
    }

    private void handlePlayCommand(String command) {
        AiWorkspace workspace = new AiWorkspace(this, mGame, mGame.getCurrentState(), 50);

        mAiThread = new UiWorkerThread(new UiWorkerThread.UiWorkerRunnable() {
            private boolean mRunning = true;
            @Override
            public void cancel() {
                mRunning = false;
            }

            @Override
            public void run() {
                workspace.explore(TerminalSettings.aiThinkTime);
                workspace.stopExploring();
                GameTreeNode bestMove = workspace.getTreeRoot().getBestChild();
                sendMoveCommand(bestMove.getEnteringMove());
                mGame.getCurrentState().makeMove(bestMove.getEnteringMove());
            }
        });
        mAiThread.start();
    }

    private void handleOpponentMoveCommand(String command) {
        command = command.replace("opponent-move ", "");
        String[] commandParts = command.split(" ");
        String[] moves = commandParts[0].split("\\|");

        for(String move : moves) {
            mGame.getCurrentState().makeMove(MoveRecord.getMoveRecordFromSimpleString(move));
        }
    }

    private void sendMoveCommand(MoveRecord move) {
        String command = "move ";
        command += move.toSimpleString();

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    private class CommCallback implements CommunicationThread.CommunicationThreadCallback {
        @Override
        public void onCommandReceived(byte[] command) {
            String strCommand = new String(command);
            String[] commands = strCommand.split("\n");

            for(String cmd : commands) {
                System.out.println("Client received: " + cmd);
                if (cmd.startsWith("rules")) {
                    handleRulesCommand(cmd);
                    //System.out.println("Client view of rules: " + mRules.getOTRString());
                }
                else if (cmd.startsWith("play")) {
                    handlePlayCommand(cmd);
                }
                else if (cmd.startsWith("opponent-move")) {
                    handleOpponentMoveCommand(cmd);
                }
                else if (cmd.startsWith("side")) {
                    handleSideCommand(cmd);
                }
                else if (cmd.startsWith("position")) {
                    handlePositionCommand(cmd);
                }
            }
        }
    }


    // Below are UI callback methods, which aren't really necessary for us, but it's good to
    // not pass a null object in.
    @Override
    public void gameStarting() {

    }

    @Override
    public void awaitingMove(Player player, boolean isAttackingSide) {

    }

    @Override
    public void timeUpdate(Side side) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {

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
}
