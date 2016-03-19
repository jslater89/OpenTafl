package com.manywords.softworks.tafl.ui.player.external.engine;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.MoveSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.player.Player;
import org.ini4j.Wini;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 3/10/16.
 */
public class ExternalEngineHost {
    private Process mExternalEngine;
    private InputStream mInboundPipe;
    private OutputStream mOutboundPipe;
    private CommunicationThread mCommThread;
    private CommunicationThread.CommunicationThreadCallback mCommCallback = new CommCallback();
    private Game mGame;
    private Player mPlayer;
    private boolean mSimpleNotation = true;

    public static boolean validateEngineFile(File iniFile) {
        try {
            Wini ini = new Wini(iniFile);
            String dir = ini.get("engine", "directory", String.class);
            String filename = ini.get("engine", "filename", String.class);
            String command = ini.get("engine", "command", String.class);
            String args = ini.get("engine", "arguments", String.class);

            if(dir == null || dir.equals("") || command == null || command.equals("") || filename == null || filename.equals("")) {
                System.out.println("Missing elements");
                return false;
            }

            File engineFileDir = new File("engines");
            File engineDir = new File(engineFileDir, dir);
            File engineFile = new File(engineDir, filename);
            if(!engineFile.exists()) {
                System.out.println("File does not exist: " + engineFile);
                System.out.println(engineFile.getAbsolutePath());
                return false;
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public ExternalEngineHost(Player player, TaflTest host, PipedInputStream connectToOutput, PipedOutputStream connectToInput) {
        mPlayer = player;
        try {
            mInboundPipe = new BufferedInputStream(new PipedInputStream(connectToInput));
            mOutboundPipe = new BufferedOutputStream(new PipedOutputStream(connectToOutput));
        }
        catch(IOException e) {
            System.out.println("Could not connect input streams");
            e.printStackTrace(System.out);
            System.exit(-1);
        }

        mCommThread = new CommunicationThread(null, mOutboundPipe, mInboundPipe, mCommCallback);
        mCommThread.start();
    }

    public ExternalEngineHost(Player player, File iniFile) {
        mPlayer = player;

        String dirName;
        String fileName;
        String command;
        String args;
        try {
            if(!validateEngineFile(iniFile)) {
                throw new IllegalStateException("Missing engine file for " + (player.isAttackingSide() ? "attacker" : "defender") + ": " + iniFile);
            }
            Wini ini = new Wini(iniFile);
            dirName = ini.get("engine", "directory", String.class);
            fileName = ini.get("engine", "filename", String.class);
            command = ini.get("engine", "command", String.class);
            args = ini.get("engine", "arguments", String.class);
            if(args == null) args = "";
        } catch (IOException e) {
            e.printStackTrace(System.out);
            throw new IllegalStateException("Missing engine file for " + (player.isAttackingSide() ? "attacker" : "defender"));
        }

        File engineFileDir = new File("engines");
        File directory = new File(engineFileDir, dirName);
        File absoluteDirectory = directory.getAbsoluteFile();

        String[] argArray = args.split(" ");
        String[] commandLine = new String[argArray.length + 1];
        commandLine[0] = command;
        System.arraycopy(argArray, 0, commandLine, 1, argArray.length);

        ProcessBuilder b = new ProcessBuilder();
        b.directory(absoluteDirectory);
        b.command(commandLine);
        System.out.println(b.command());

        try {
            mExternalEngine = b.start();
            mInboundPipe = new BufferedInputStream(mExternalEngine.getInputStream());
            mOutboundPipe = new BufferedOutputStream(mExternalEngine.getOutputStream());

            mCommThread = new CommunicationThread(mExternalEngine, mOutboundPipe, mInboundPipe, mCommCallback);
            mCommThread.start();

        } catch (IOException e) {
            System.out.println("Failed to start: " + e);
            e.printStackTrace(System.out);
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
        rules(g.getGameRules());
        mGame = g;
    }

    public void analyzePosition(GameState state) {
        analyzePosition(5, 30, state);
    }

    public void analyzePosition(int moves, int seconds, GameState state) {
        // This is a passable but not perfectly ideal way to check for rules equality.
        if(!mGame.getGameRules().getOTRString().equals(state.mGame.getGameRules().getOTRString())) {
            rules(state.mGame.getGameRules());
        }

        position(state);
        side(state.getCurrentSide().isAttackingSide());

        analyze(moves, seconds);
    }

    public void rules(Rules rules) {
        String command = "rules ";
        command += rules.getOTRString();
        //System.out.println("Host view of rules: " + rules.getOTRString());
        command += "\n";

        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
    }

    public void start(Game game) {
        mGame = game;
        side(mPlayer.isAttackingSide());
        mCommThread.sendCommand("start\n".getBytes(Charset.forName("US-ASCII")));
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
        MoveRecord move = MoveSerializer.loadMoveRecord(command);

        mPlayer.onMoveDecided(move);
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
                    MoveRecord record = MoveSerializer.loadMoveRecord(move);
                    moves.add(record);
                }

                mPlayer.statusText(moves.get(0).toString() + ": " + f.format(analysis));
            }
        }
    }

    private void handleSimpleMovesCommand(String command) {
        mSimpleNotation = command.contains("on");
    }

    private class CommCallback implements CommunicationThread.CommunicationThreadCallback {

        @Override
        public void onCommandReceived(byte[] command) {
            String strCommand = new String(command);
            String[] commands = strCommand.split("\n");

            for(String cmd : commands) {
                System.out.println("Host received: " + cmd);

                if (cmd.startsWith("hello")) {
                    mCommThread.sendCommand("hello\n".getBytes());
                }
                else if(cmd.startsWith("move")) {
                    handleMoveCommand(cmd);
                }
                else if(cmd.startsWith("rules")) {
                    if(mGame != null) {
                        rules(mGame.getGameRules());
                    }
                }
                else if(cmd.startsWith("side")) {
                    side(mPlayer.isAttackingSide());
                }
                else if(cmd.startsWith("position")) {
                    position(mGame.getCurrentState());
                }
                else if(cmd.startsWith("clock")) {
                    GameClock.ClockEntry attackerClock = mGame.getClock().getClockEntry(mGame.getCurrentState().getAttackers());
                    GameClock.ClockEntry defenderClock = mGame.getClock().getClockEntry(mGame.getCurrentState().getDefenders());
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
            }
        }
    }
}
