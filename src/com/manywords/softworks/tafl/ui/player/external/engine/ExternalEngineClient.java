package com.manywords.softworks.tafl.ui.player.external.engine;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.rules.Rules;

/**
 * Created by jay on 3/10/16.
 */
public class ExternalEngineClient {
    public static ExternalEngineClient instance;
    public static void run() {
        instance = new ExternalEngineClient();
        instance.start();
    }

    public CommunicationThread mCommThread;
    public CommunicationThread.CommunicationThreadCallback mCommCallback;

    public Rules mRules;
    public Game mGame;

    public void start() {
        mCommCallback = new CommCallback();
        mCommThread = new CommunicationThread(System.out, System.in, mCommCallback);
        mCommThread.start();

        mCommThread.sendCommand("hello\n".getBytes());
    }

    private class CommCallback implements CommunicationThread.CommunicationThreadCallback {
        @Override
        public void onCommandReceived(byte[] command) {
            String strCommand = new String(command);

            String received = "Client received: " + strCommand;
            mCommThread.sendCommand(received.getBytes());
        }
    }
}
