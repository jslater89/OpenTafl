package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.engine.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ServerGame wraps a Game object and its associated things, and synchronizes access to them.
 */
public class ServerGame {
    public static ServerGame getDummyGame(NetworkServer server, String attackerName, String defenderName, Game game) {
        ServerGame g = new ServerGame();
        g.mGame = game;
        g.mAttackerClient = DummyServerClient.get(server, attackerName);
        g.mDefenderClient = DummyServerClient.get(server, defenderName);

        return g;
    }

    public ServerGame() {
        uuid = UUID.randomUUID();
    }

    private Game mGame;

    public final UUID uuid;

    private ServerClient mAttackerClient;
    private ServerClient mDefenderClient;
    private List<ServerClient> mSpectators = new ArrayList<>();

    public ServerClient getAttackerClient() {
        return mAttackerClient;
    }

    public ServerClient getDefenderClient() {
        return mDefenderClient;
    }

    public List<ServerClient> getSpectators() {
        return mSpectators;
    }

    public boolean isPassworded() {
        return false;
    }

    public Game getGame() {
        return mGame;
    }
}
