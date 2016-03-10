package com.manywords.softworks.tafl.ui.player.external;

import java.io.*;

/**
 * Created by jay on 3/10/16.
 */
public class ExternalEngineHost {
    public Process mExternalEngine;
    public BufferedInputStream mInboundPipe;
    public BufferedOutputStream mOutboundPipe;
    public CommunicationThread mCommThread;
    public CommunicationThread.CommunicationThreadCallback mCommCallback = new CommCallback();

    public ExternalEngineHost(File iniFile) {
        File directory = new File(".");
        String[] command = {
                "./linux-debug.sh",
                "--engine"
        };
        ProcessBuilder b = new ProcessBuilder();
        b.directory(directory);
        b.command(command);

        try {
            mExternalEngine = b.start();
            mInboundPipe = new BufferedInputStream(mExternalEngine.getInputStream());
            mOutboundPipe = new BufferedOutputStream(mExternalEngine.getOutputStream());

            mCommThread = new CommunicationThread(mOutboundPipe, mInboundPipe, mCommCallback);
            mCommThread.start();

        } catch (IOException e) {
            System.out.println("Failed to start: " + e);
            System.exit(0);
        }
    }

    private class CommCallback implements CommunicationThread.CommunicationThreadCallback {

        @Override
        public void onCommandReceived(byte[] command) {
            String strCommand = new String(command);

            System.out.println("Host received: " + strCommand);

            if(strCommand.equals("Hello")) {
                mCommThread.sendCommand("Hello".getBytes());
            }
        }
    }
}
