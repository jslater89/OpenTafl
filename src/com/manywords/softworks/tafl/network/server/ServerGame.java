package com.manywords.softworks.tafl.network.server;

import com.manywords.softworks.tafl.command.CommandEngine;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;
import com.manywords.softworks.tafl.command.player.NetworkServerPlayer;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.UiCallback;

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

    public final UUID uuid;

    private NetworkServer mServer;

    private Game mGame;
    private CommandEngine mCommandEngine;
    private ServerUiCallback mUiCallback = new ServerUiCallback();

    private ServerClient mAttackerClient;
    private NetworkServerPlayer mAttackerPlayer;
    private ServerClient mDefenderClient;
    private NetworkServerPlayer mDefenderPlayer;
    private List<ServerClient> mSpectators = new ArrayList<>();

    private String mBase64HashedPassword = "";

    public ServerGame() {
        uuid = UUID.randomUUID();
    }

    public ServerGame(NetworkServer server, UUID uuid) {
        this.uuid = uuid;
        mServer = server;
    }

    public void setRules(Rules r) {
        mGame = new Game(r, mUiCallback);
        mAttackerPlayer = new NetworkServerPlayer();
        mDefenderPlayer = new NetworkServerPlayer();
        mCommandEngine = new CommandEngine(mGame, mUiCallback, mAttackerPlayer, mDefenderPlayer);
    }

    public void onGameStart() {

    }

    public void setPassword(String password) {
        mBase64HashedPassword = PasswordHasher.hashPassword("", password);
    }

    public void setAttackerClient(ServerClient attackerClient) {
        if(attackerClient == null) {
            mAttackerClient.setGame(null, ServerClient.GameRole.OUT_OF_GAME);
        }
        else {
            attackerClient.setGame(this, ServerClient.GameRole.ATTACKER);
            mAttackerPlayer.setClient(attackerClient);
        }

        mAttackerClient = attackerClient;
    }

    public void setDefenderClient(ServerClient defenderClient) {
        if(defenderClient == null) {
            mDefenderClient.setGame(null, ServerClient.GameRole.OUT_OF_GAME);
        }
        else {
            defenderClient.setGame(this, ServerClient.GameRole.DEFENDER);
            mDefenderPlayer.setClient(defenderClient);
        }

        mDefenderClient = defenderClient;
    }

    public void addSpectator(ServerClient client) {
        mSpectators.add(client);
        client.setGame(this, ServerClient.GameRole.KIBBITZER);
    }

    public void removeSpectator(ServerClient client) {
        mSpectators.remove(client);
        client.setGame(null, ServerClient.GameRole.OUT_OF_GAME);
    }

    private void removePlayer(ServerClient client) {
        if(client.equals(mAttackerClient)) {
            setAttackerClient(null);
        }
        else if(client.equals(mDefenderClient)) {
            setDefenderClient(null);
        }
        else {
            removeSpectator(client);
        }

        if(mAttackerClient == null && mDefenderClient == null) {
            mServer.removeGame(this);
        }
    }

    public void cancel(ServerClient c) {
        removePlayer(c);
    }

    public void shutdown() {
        if(mAttackerClient != null) mAttackerClient.setGame(null, ServerClient.GameRole.OUT_OF_GAME);
        if(mDefenderClient != null) mDefenderClient.setGame(null, ServerClient.GameRole.OUT_OF_GAME);

        for(ServerClient c : mSpectators) {
            c.setGame(null, ServerClient.GameRole.OUT_OF_GAME);
        }
    }

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
        return !mBase64HashedPassword.isEmpty();
    }

    public Game getGame() {
        return mGame;
    }

    private class ServerUiCallback implements UiCallback {

        @Override
        public void gameStarting() {

        }

        @Override
        public void modeChanging(Mode mode, Object gameObject) {

        }

        @Override
        public void awaitingMove(Player player, boolean isAttackingSide) {

        }

        @Override
        public void timeUpdate(Side side) {

        }

        @Override
        public void moveResult(CommandResult result, MoveRecord move) {

        }

        @Override
        public void statusText(String text) {

        }

        @Override
        public void modalStatus(String title, String text) {

        }

        @Override
        public void gameStateAdvanced() {

        }

        @Override
        public void victoryForSide(Side side) {

        }

        @Override
        public void gameFinished() {

        }

        @Override
        public MoveRecord waitForHumanMoveInput() {
            return null;
        }

        @Override
        public boolean inGame() {
            return false;
        }
    }
}
