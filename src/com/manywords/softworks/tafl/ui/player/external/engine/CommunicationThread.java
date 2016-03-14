package com.manywords.softworks.tafl.ui.player.external.engine;

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

    public CommunicationThread(Process process, OutputStream output, InputStream input, CommunicationThreadCallback callback) {
        this.process = process;
        this.output = output;
        this.input = input;
        this.callback = callback;
    }

    public void cancel() {
        running = false;
    }

    public synchronized void sendCommand(byte[] command) {
        try {
            output.write(command);
            output.flush();
        } catch (IOException e) {
            System.out.println("Exception writing command: " + e);
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        while(running) {
            try {
                int i = input.read(buffer);
                if(i > 0) {
                    byte[] command = new byte[i];
                    System.arraycopy(buffer, 0, command, 0, i);
                    callback.onCommandReceived(command);
                }
                else {
                    System.out.println("EOF in stream");
                    if(process != null) {
                        process.waitFor();
                        System.out.println("Other process ended with: " + process.exitValue());
                    }
                    running = false;
                }
            } catch (IOException e) {
                System.out.println("Exception reading command: " + e);
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for stopped process exit");
                e.printStackTrace(System.out);
            }
        }
    }
}
