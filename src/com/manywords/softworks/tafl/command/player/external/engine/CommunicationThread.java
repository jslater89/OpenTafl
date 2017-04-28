package com.manywords.softworks.tafl.command.player.external.engine;

import com.manywords.softworks.tafl.Log;

import java.io.*;

/**
 * Created by jay on 3/10/16.
 */
public class CommunicationThread extends Thread {
    public interface CommunicationThreadCallback {
        public void onCommandReceived(byte[] command);
    }

    private Process process;
    private OutputStream output;
    private InputStream input;
    private boolean running = true;
    private CommunicationThreadCallback callback;

    private boolean debug = false;
    private boolean log = false;
    private File logFile;
    private FileWriter logFileWriter;

    public CommunicationThread(Process process, OutputStream output, InputStream input, CommunicationThreadCallback callback) {
        this.process = process;
        this.output = output;
        this.input = input;
        this.callback = callback;
    }

    public synchronized void setDebugMode(boolean on) {
        debug = on;
    }

    public void setLog(boolean l) {
        log = l;

        if(log) {
            logFile = new File("engine-output.log");
            try {
                logFileWriter = new FileWriter(logFile);
            } catch (IOException e) {
                Log.println(Log.Level.NORMAL, "Failed to open engine output log for writing");
                log = false;
            }
        }
        else {
            try {
                logFileWriter.flush();
                logFileWriter.close();
            } catch (IOException e) {
                Log.println(Log.Level.NORMAL, "Failed to close engine log");
            }
        }
    }

    public void cancel() {
        running = false;
        setLog(false);
    }

    public synchronized void sendCommand(byte[] command) {
        logBytes(true, command);

        if(!debug) {
            try {
                output.write(command);
                output.flush();
            } catch (IOException e) {
                Log.println(Log.Level.NORMAL, "Exception writing command: " + e);
            }
        }
        else {
            Log.print(Log.Level.CHATTY, new String(command));
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[65536];
        while(running) {
            try {
                int i = input.read(buffer);
                if(i > 0) {
                    byte[] command = new byte[i];
                    System.arraycopy(buffer, 0, command, 0, i);
                    logBytes(false, command);
                    callback.onCommandReceived(command);
                }
                else {
                    Log.println(Log.Level.NORMAL, "EOF in stream");
                    if(process != null) {
                        process.waitFor();
                        Log.println(Log.Level.CHATTY, "Other process ended with: " + process.exitValue());
                    }
                    running = false;
                }
            } catch (IOException e) {
                Log.println(Log.Level.NORMAL, "Exception reading command: " + e);
            } catch (InterruptedException e) {
                Log.println(Log.Level.CHATTY, "Interrupted while waiting for stopped process exit");
                Log.stackTrace(Log.Level.CHATTY, e);
            }
        }

        try {
            input.close();
        } catch (IOException e) {
            //best effort
        }

        try {
            output.close();
        } catch (IOException e) {
            //best effort
        }
    }

    private void logBytes(boolean sender, byte[] command) {
        if(log) {
            try {
                logFileWriter.write((sender ? "Sent: " : "Received: ") + new String(command));
                logFileWriter.flush();
            } catch (IOException e) {
                Log.println(Log.Level.NORMAL, "Failed to write to engine log: " + e);
            }
        }
    }
}
