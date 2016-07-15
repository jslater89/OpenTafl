package com.manywords.softworks.tafl.network.client;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.command.player.NetworkClientPlayer;
import com.manywords.softworks.tafl.command.player.Player;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.packet.ClientInformation;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.ingame.GameChatPacket;
import com.manywords.softworks.tafl.network.packet.ingame.VictoryPacket;
import com.manywords.softworks.tafl.network.packet.pregame.LobbyChatPacket;
import com.manywords.softworks.tafl.network.packet.utility.SuccessPacket;
import com.manywords.softworks.tafl.rules.Rules;

import java.util.List;

/**
 * Created by jay on 6/2/16.
 */
public class TestClientServerConnection extends ClientServerConnection {
    public TestClientServerConnection(String hostname, int port) {
        super(hostname, port);

        setTestCallback(new TestClientServerCallback());
    }

    @Override
    public NetworkClientPlayer getNetworkPlayer() {
        dummyPlayer = super.getNetworkPlayer();

        dummyPlayer.setCallback(new Player.PlayerCallback() {
            @Override
            public void onMoveDecided(Player player, MoveRecord record) {
                lastMove = record;
            }

            @Override
            public void notifyResignation(Player player) {

            }
        });

        return dummyPlayer;
    }

    public Game game;
    public NetworkClientPlayer dummyPlayer = new NetworkClientPlayer(this);
    public State state = State.DISCONNECTED;
    public LobbyChatPacket lastLobbyChat;
    public GameChatPacket lastGameChat;
    public List<ClientInformation> lastClientUpdate;
    public List<GameInformation> lastGameUpdate;
    public List<MoveRecord> lastHistory;
    public MoveRecord lastMove;
    public boolean gameEnded = false;
    public VictoryPacket.Victory victory;
    public String lastError = "";

    class TestClientServerCallback implements ClientServerConnection.ClientServerCallback {
        @Override
        public void onStateChanged(State newState) {
            state = newState;
        }

        @Override
        public void onChatMessageReceived(ChatType type, String sender, String message) {
            if(type == ChatType.LOBBY) {
                lastLobbyChat = new LobbyChatPacket(sender, message);
            }
            else if(type == ChatType.GAME) {
                lastGameChat = new GameChatPacket(sender, message);
            }
        }

        @Override
        public void onSuccessReceived(String message) {
            if(message.equals(SuccessPacket.LOGGED_IN)) setState(State.LOGGED_IN);
        }

        @Override
        public void onErrorReceived(String message) {
            lastError = message;
        }

        @Override
        public void onGameListReceived(List<GameInformation> games) {
            lastGameUpdate = games;
        }

        @Override
        public void onClientListReceived(List<ClientInformation> clients) {
            lastClientUpdate = clients;
        }

        @Override
        public void onDisconnect(boolean planned) {

        }

        @Override
        public Game getGame() {
            return game;
        }

        @Override
        public void onStartGame(Rules r, List<MoveRecord> history) {
            gameEnded = false;
            game = new Game(r, null);

            if(lastHistory != null) {
                for (MoveRecord m : lastHistory) {
                    int result = game.getCurrentState().makeMove(m);
                    OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Move result: " + result);
                }
            }
        }

        @Override
        public void onHistoryReceived(List<MoveRecord> moves) {
            lastHistory = moves;

            OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Moves: " + moves);
            if(game != null) {
                for (MoveRecord m : moves) {
                    int result = game.getCurrentState().makeMove(m);
                    OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Move result: " + result);
                }
            }
        }

        @Override
        public void onServerMoveReceived(MoveRecord move) {
            // This comes from the internal callback through the dummy player
        }

        @Override
        public void onClockUpdateReceived(TimeSpec attackerClock, TimeSpec defenderClock) {

        }

        @Override
        public void onVictory(VictoryPacket.Victory victory) {
            gameEnded = true;
            TestClientServerConnection.this.victory = victory;
        }
    }
}
