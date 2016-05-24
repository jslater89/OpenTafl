package com.manywords.softworks.tafl.network;

import com.manywords.softworks.tafl.network.packet.NetworkPacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;

import java.net.Socket;

/**
 * Created by jay on 5/24/16.
 */
public class DummyServerClient extends ServerClient {
    public static DummyServerClient get(NetworkServer server, String username) {
        DummyServerClient c = new DummyServerClient(server, null);
        c.onRegistered(username);
        return c;
    }

    private NetworkServer mServer;
    public DummyServerClient(NetworkServer server, Socket clientSocket) {
        super();

        mServer = server;
    }

    @Override
    public void writePacket(NetworkPacket packet) {
        // No-op
    }
}
