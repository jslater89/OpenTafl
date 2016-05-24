package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.network.DummyServerClient;
import com.manywords.softworks.tafl.rules.Rules;

/**
 * ServerGame wraps a Game object and its associated things, and synchronizes access to them.
 */
public class ServerGame {
    public static ServerGame getDummyGame(NetworkServer server, String attackerName, String defenderName, Game game) {
        ServerGame g = new ServerGame();
        g.mGame = game;
        g.attackerPlayer = DummyServerClient.get(server, attackerName);
        g.defenderPlayer = DummyServerClient.get(server, defenderName);

        return g;
    }

    private Game mGame;

    private ServerClient attackerPlayer;
    private ServerClient defenderPlayer;
}
