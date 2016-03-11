package com.manywords.softworks.tafl.ui.player.external.engine;

import java.io.*;

/**
 * Created by jay on 3/10/16.
 */
public class CommunicationThread extends Thread {
    public interface CommunicationThreadCallback {
        public void onCommandReceived(byte[] command);
    }

    private OutputStream output;
    private InputStream input;
    private boolean running = true;
    private CommunicationThreadCallback callback;

    public CommunicationThread(OutputStream output, InputStream input, CommunicationThreadCallback callback) {
        this.output = output;
        this.input = input;
        this.callback = callback;
    }

    public void cancel() {
        running = false;
    }

    public void sendCommand(byte[] command) {
        new Thread(() -> {
            try {
                output.write(command);
                output.flush();
            } catch (IOException e) {
                System.out.println("Exception writing command: " + e);
            }
        }).start();
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        while(running) {
            try {
                int i = input.read(buffer);
                byte[] command = new byte[i];
                System.arraycopy(buffer, 0, command, 0, i);
                callback.onCommandReceived(command);
            } catch (IOException e) {
                System.out.println("Exception reading command: " + e);
            }
        }
    }
}
