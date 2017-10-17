package com.manywords.softworks.tafl.command.player.external.engine;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.MoveSerializer;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.command.player.ExternalEnginePlayer;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 3/10/16.
 */
public class ExternalEngineHost {
    private boolean mConnected;

    private Process mExternalEngine;
    private InputStream mInboundPipe;
    private OutputStream mOutboundPipe;
    private CommunicationThread mCommThread;
    private CommunicationThread.CommunicationThreadCallback mCommCallback = new CommCallback();
    private Game mGame;
    private ExternalEnginePlayer mPlayer;
    private boolean mSimpleNotation = true;

    public ExternalEngineHost(ExternalEnginePlayer player, TaflTest host, PipedInputStream connectToOutput, PipedOutputStream connectToInput) {
        mPlayer = player;
        try {
            mInboundPipe = new BufferedInputStream(new PipedInputStream(connectToInput));
            mOutboundPipe = new BufferedOutputStream(new PipedOutputStream(connectToOutput));
        }
        catch(IOException e) {
            Log.println(Log.Level.CRITICAL, "Could not connect input streams");
            Log.stackTrace(Log.Level.CRITICAL, e);
            System.exit(-1);
        }

        mCommThread = new CommunicationThread(null, mOutboundPipe, mInboundPipe, mCommCallback);
        mCommThread.start();
    }

    public ExternalEngineHost(ExternalEnginePlayer player, EngineSpec spec) {
        mPlayer = player;

        File directory = spec.directory;
        File absoluteDirectory = directory.getAbsoluteFile();

        String[] commandLine = new String[spec.arguments.length + 1];
        commandLine[0] = spec.command;
        System.arraycopy(spec.arguments, 0, commandLine, 1, spec.arguments.length);

        ProcessBuilder b = new ProcessBuilder();
        b.directory(absoluteDirectory);
        b.command(commandLine);
        Log.println(Log.Level.VERBOSE, b.command());

        try {
            mExternalEngine = b.start();
            mInboundPipe = new BufferedInputStream(mExternalEngine.getInputStream());
            mOutboundPipe = new BufferedOutputStream(mExternalEngine.getOutputStream());

            mCommThread = new CommunicationThread(mExternalEngine, mOutboundPipe, mInboundPipe, mCommCallback);
            mCommThread.start();

        } catch (IOException e) {
            Log.println(Log.Level.NORMAL, "Failed to start: " + e);
            Log.stackTrace(Log.Level.NORMAL, e);
            System.exit(1);
        }
    }

    public void moveResult(int result) {
        if(result == GameState.GOOD_MOVE) {
            move(mGame.getCurrentState());
        }
        else if(result == GameState.ILLEGAL_SIDE) {
            error(1);
        }
        else if(result == GameState.ILLEGAL_MOVE) {
            error(2);
        }
        else if(result == GameState.ILLEGAL_SIDE_BERSERKER) {
            error(3);
        }
        else if(result == GameState.ILLEGAL_MOVE_BERSERKER) {
            error(4);
        }
    }

    public void setGame(Game g) {
        rules(g.getRules());
        mGame = g;
    }

    public boolean ready() {
        return mConnected;
    }

    public void analyzePosition(GameState state) {
        analyzePosition(5, 30, state);
    }

    public void analyzePosition(int moves, int seconds, GameState state) {
        // This is a passable but not perfectly ideal way to check for rules equality.
        if(!mGame.getRules().getOTRString(false).equals(state.mGame.getRules().getOTRString(false))) {
            rules(state.mGame.getRules());
        }

        position(state);
        side(state.getCurrentSide().isAttackingSide());

        analyze(moves, seconds);
    }

    public void rules(Rules rules) {
        String command = "rules ";
        command += rules.getOTRString(false);
        //System.out.println("Host view of rules: " + rules.getOTRString());
        command += "\n";

        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void position(GameState state) {
        String command = "position ";
        command += state.getOTNPositionString();

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void move(GameState state) {
        String command = "move ";
        command += state.getOTNPositionString();

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void side(boolean isAttackingSide) {
        String command = "side ";
        command += (isAttackingSide ? "attackers" : "defenders");

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void clockUpdate() {
        if(mGame == null || mGame.getClock() == null) return;

        GameClock.ClockEntry attackerClock = mGame.getClock().getClockEntry(mGame.getCurrentState().getAttackers());
        GameClock.ClockEntry defenderClock = mGame.getClock().getClockEntry(mGame.getCurrentState().getDefenders());
        String command = "clock ";
        boolean attackerOvertime = false;
        boolean defenderOvertime = false;
        long attackerMillis = -1;
        long defenderMillis = -1;
        int overtimeSeconds = 0;
        int attackerOvertimes = 0;
        int defenderOvertimes = 0;

        if(attackerClock.getMainTime() > 0) {
            attackerMillis = attackerClock.getMainTime();
        }
        else {
            attackerMillis = attackerClock.getOvertimeTime();
            attackerOvertime = true;
        }

        if(defenderClock.getMainTime() > 0) {
            defenderMillis = defenderClock.getMainTime();
        }
        else {
            defenderMillis = defenderClock.getOvertimeTime();
            defenderOvertime = true;
        }

        overtimeSeconds = (int)(attackerClock.getClock().getOvertimeTime() / 1000);
        attackerOvertimes = attackerClock.getOvertimeCount();
        defenderOvertimes = defenderClock.getOvertimeCount();

        command += attackerMillis + (attackerOvertime ? "* " : " ")
                + defenderMillis + (defenderOvertime ? "* " : " ")
                + overtimeSeconds + " "
                + attackerOvertimes + " "
                + defenderOvertimes + " ";

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void analyze(int moves, int seconds) {
        clockUpdate();
        String command = "analyze " + moves + " " + seconds;

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void playForCurrentSide(Game game) {
        clockUpdate();

        String command = "play ";
        command += (game.getCurrentSide().isAttackingSide() ? "attackers" : "defenders");

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void error(int code) {
        String command = "error " + code;

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void notifyMovesMade(List<MoveRecord> moves) {
        String command = "opponent-move ";
        int index = 0;
        for(MoveRecord move : moves) {
            if(index++ > 0) command += "|";

            if(mSimpleNotation) {
                command += move.start + "-" + move.end;
            }
            else {
                command += move.toString();
            }
        }

        command += " " + mGame.getCurrentState().getOTNPositionString();

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void dumpEvaluation(int child) {
        mCommThread.sendCommand(("dump " + child).getBytes(Charset.forName("US-ASCII")));
    }

    public void stopEnginePlay() {
        finish();
    }

    public void terminateEngine() {
        goodbye();
    }

    public void finish() {
        int code = 0;

        if(mGame.getCurrentState().checkVictory() == GameState.DRAW) {
            code = 1;
        }
        else if(mGame.getCurrentState().checkVictory() == GameState.ATTACKER_WIN) {
            code = 2;
        }
        else if(mGame.getCurrentState().checkVictory() == GameState.DEFENDER_WIN) {
            code = 3;
        }

        String command = "finish " + code;

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void goodbye() {
        mCommThread.sendCommand("goodbye\n".getBytes(Charset.forName("US-ASCII")));
    }

    public void handleMoveCommand(String command) {
        command = command.replace("move ", "");

        try {
            MoveRecord move = MoveSerializer.loadMoveRecord(mGame.getRules().boardSize, command);
            mPlayer.onMoveDecided(move);
        }
        catch(NotationParseException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    public void handleStatusCommand(String command) {
        command = command.replace("status ", "");
        mPlayer.statusText(command);
    }

    public void handleAnalysisCommand(String command) {
        command = command.replace("analysis ", "");
        String[] commandParts = command.split(" ");

        if(commandParts.length % 2 != 1) {
            error(5);
            mPlayer.statusText("Engine returned improperly-formatted analysis result");
        }
        else {
            DecimalFormat f = new DecimalFormat("#.##");
            for(int i = 1; i < commandParts.length; i += 2) {
                int moveIndex = i;
                int analysisIndex = i + 1;

                String moveString = commandParts[moveIndex];
                String analysisString = commandParts[analysisIndex];

                double analysis = Double.parseDouble(analysisString);
                String[] moveArray = moveString.split("\\|");
                List<MoveRecord> moves = new ArrayList<>(moveArray.length);

                for(String move : moveArray) {
                    try {
                        MoveRecord record = MoveSerializer.loadMoveRecord(mGame.getRules().boardSize, move);
                        moves.add(record);
                    }
                    catch(NotationParseException e) {
                        Log.println(Log.Level.NORMAL, "Failed to parse analysis move record: " + e.toString());
                    }
                }

                mPlayer.statusText(moves.get(0).toString() + ": " + f.format(analysis));
            }
        }
    }

    private void handleSimpleMovesCommand(String command) {
        mSimpleNotation = command.contains("on");
    }

    private void handleErrorCommand(String command) {
        if(command.startsWith("error 0")) {
            command = command.replace("error 0", "");
            mPlayer.modalStatus("Engine error!", command);
        }
        if(command.startsWith("error -1")) {
            command = command.replace("error -1", "");
            mPlayer.modalStatus("Engine error!", command);
            mPlayer.resign();
        }
    }

    private void handleDumpCommand(String command) {
        command = command.replaceFirst("dump", "");
        command = command.replaceAll("XXXXX", "\n");
        Log.println(Log.Level.VERBOSE, command);
    }

    private class CommCallback implements CommunicationThread.CommunicationThreadCallback {

        @Override
        public void onCommandReceived(byte[] command) {
            String strCommand = new String(command);
            String[] commands = strCommand.split("\n");

            for(String cmd : commands) {
                if(!cmd.startsWith("dump")) {
                    Log.println(Log.Level.VERBOSE, "Host received: " + cmd);
                }
                else {
                    Log.println(Log.Level.VERBOSE, "Host received dump command");
                }

                if (cmd.startsWith("hello")) {
                    mConnected = true;
                }
                else if(cmd.startsWith("move")) {
                    handleMoveCommand(cmd);
                }
                else if(cmd.startsWith("rules")) {
                    if(mGame != null) {
                        rules(mGame.getRules());
                    }
                }
                else if(cmd.startsWith("side")) {
                    side(mPlayer.isAttackingSide());
                }
                else if(cmd.startsWith("position")) {
                    position(mGame.getCurrentState());
                }
                else if(cmd.startsWith("clock")) {
                    clockUpdate();
                }
                else if(cmd.startsWith("status")) {
                    handleStatusCommand(cmd);
                }
                else if(cmd.startsWith("analysis")) {
                    handleAnalysisCommand(cmd);
                }
                else if(cmd.startsWith("simple-moves")) {
                    handleSimpleMovesCommand(cmd);
                }
                else if(cmd.startsWith("error")) {
                    handleErrorCommand(cmd);
                }
                else if(cmd.startsWith("dump")) {
                    handleDumpCommand(cmd);
                }
            }
        }
    }
}
