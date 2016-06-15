package com.manywords.softworks.tafl.test.network;


import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.network.client.HeadlessAIClient;
import com.manywords.softworks.tafl.network.server.DummyTestServer;
import com.manywords.softworks.tafl.network.server.GameRole;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.test.TaflTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jay on 6/15/16.
 */
public class HeadlessAITest extends TaflTest {
    private Thread mServerThread;
    private DummyTestServer mServer;
    private HeadlessAIClient mClient;
    @Override
    public void run() {
        mServerThread = new Thread(() -> {
            // Server running
            //System.out.println("Starting server");
            mServer = new DummyTestServer();
            mServer.start();
            //System.out.println("Ending server");
        });

        mServerThread.start();
        while(mServer == null) {
            //System.out.println("Waiting");
            sleep(100);
        }

        Map<String, String> args = new HashMap<>();

        args.put("--headless", "");
        args.put("--create", "");
        args.put("--server", "localhost");
        args.put("--username", "OpenTafl AI");
        args.put("--password", "aipw");
        args.put("--rules", "3");
        args.put("--clock", "0+10000/2+0");
        args.put("--engine", "engines/opentafl-debug.ini");
        mClient = HeadlessAIClient.startFromArgs(args, false);

        sleep(250);

        assert mClient.getConnection().getCurrentState() == ClientServerConnection.State.IN_PREGAME;
        assert mClient.getConnection().getGameRole() == GameRole.ATTACKER;

        mServer.prodStartGame();

        sleep(250);

        assert mClient.getConnection().getCurrentState() == ClientServerConnection.State.IN_GAME;
        mServer.prodVictory();

        sleep(250);

        assert mClient.getConnection().getCurrentState() == ClientServerConnection.State.IN_PREGAME;

        mServer.stop();
        mClient.getConnection().disconnect();
    }
}
