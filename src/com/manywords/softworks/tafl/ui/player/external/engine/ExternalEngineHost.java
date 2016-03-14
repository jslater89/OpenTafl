package com.manywords.softworks.tafl.ui.player.external.engine;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.ui.player.Player;
import org.ini4j.Wini;

import java.io.*;
import java.nio.charset.Charset;
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

    public void setGame(Game g) {
        setRules(g.getGameRules());
        mGame = g;
    }

    public String setRules(Rules rules) {
        String command = "rules ";
        command += rules.getOTRString();
        //System.out.println("Host view of rules: " + rules.getOTRString());
        command += "\n";

        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
        return command;
    }

    public String start(Game game) {
        mGame = game;
        side(mPlayer.isAttackingSide());
        mCommThread.sendCommand("start\n".getBytes(Charset.forName("US-ASCII")));
        return "start\n";
    }

    public String setPosition(GameState state) {
        String command = "position ";
        command += state.getOTNPositionString();

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
        return command;
    }

    public String side(boolean isAttackingSide) {
        String command = "side ";
        command += (isAttackingSide ? "attackers" : "defenders");

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
        return command;
    }

    public String clockUpdate(GameClock.ClockEntry attackerClock, GameClock.ClockEntry defenderClock) {
        String command = "clock ";
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
        }

        if(defenderClock.getMainTime() > 0) {
            defenderMillis = defenderClock.getMainTime();
        }
        else {
            defenderMillis = defenderClock.getOvertimeTime();
        }

        overtimeSeconds = (int)(attackerClock.getClock().getOvertimeTime() / 1000);
        attackerOvertimes = attackerClock.getOvertimeCount();
        defenderOvertimes = defenderClock.getOvertimeCount();

        command += attackerMillis + " " + defenderMillis + " " + overtimeSeconds + " " + attackerOvertimes + " " + defenderOvertimes;

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
        return command;
    }

    public String analyze(int moves, int seconds) {
        String command = "analyze " + moves + " " + seconds;

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
        return command;
    }

    public String playForCurrentSide(Game game) {
        String command = "play ";
        command += (game.getCurrentSide().isAttackingSide() ? "attackers" : "defenders");

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
        return command;
    }

    public String error(int code) {
        String command = "error " + code;

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
        return command;
    }

    public String notifyMovesMade(List<MoveRecord> moves) {
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
        return command;
    }

    public String finish(int code) {
        String command = "finish " + code;

        command += "\n";
        mCommThread.sendCommand(command.getBytes(Charset.forName("US-ASCII")));
        return command;
    }

    public String quit() {
        mCommThread.sendCommand("quit\n".getBytes(Charset.forName("US-ASCII")));
        return "quit\n";
    }

    public void handleMoveCommand(String command) {
        command = command.replace("move ", "");
        MoveRecord move = MoveRecord.getMoveRecordFromSimpleString(command);

        mPlayer.onMoveDecided(move);
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
            }
        }
    }
}
