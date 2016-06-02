package com.manywords.softworks.tafl.test.network;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.network.client.TestClientServerConnection;
import com.manywords.softworks.tafl.network.packet.ClientInformation;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.ingame.VictoryPacket;
import com.manywords.softworks.tafl.network.packet.pregame.CreateGamePacket;
import com.manywords.softworks.tafl.network.packet.pregame.JoinGamePacket;
import com.manywords.softworks.tafl.network.server.GameRole;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.test.TaflTest;

import java.util.UUID;

/**
 * Created by jay on 6/2/16.
 */
public class ServerTest extends TaflTest {
    private Thread mServerThread;
    private NetworkServer mServer;

    private TestClientServerConnection mPlayer1;
    private TestClientServerConnection mPlayer2;

    private static final String player1Username = "asdf";
    private static final String player1Password = PasswordHasher.hashPassword(player1Username, "asdf");

    private static final String player2Username = "fdsa";
    private static final String player2Password = PasswordHasher.hashPassword(player2Username, "fdsa");
    @Override
    public void run() {

        mServerThread = new Thread(() -> {
            // Server running
            //System.out.println("Starting server");
            mServer = new NetworkServer(4, false);
            mServer.start();
            //System.out.println("Ending server");
        });

        mServerThread.start();

        while(mServer == null) {
            sleep(100);
        }

        mPlayer1 = new TestClientServerConnection("localhost", 11541);
        mPlayer2 = new TestClientServerConnection("localhost", 11541);

        mPlayer1.connect(player1Username, player1Password);
        mPlayer2.connect(player2Username, player2Password);

        sleep(500);

        assert mPlayer1.state == ClientServerConnection.State.LOGGED_IN;
        assert mPlayer2.state == ClientServerConnection.State.LOGGED_IN;

        mPlayer1.sendChatMessage(ClientServerConnection.ChatType.LOBBY, "player1", "chatmessage");
        mPlayer2.requestGameUpdate();

        sleep(500);

        assert mPlayer1.lastLobbyChat.message.equals("chatmessage");
        assert mPlayer2.lastLobbyChat.message.equals("chatmessage");

        int knownPlayerCount = 0;
        for(ClientInformation c : mPlayer2.lastClientUpdate) {
            if(c.username.equals(mPlayer1.getUsername())) knownPlayerCount++;
            else if(c.username.equals(mPlayer2.getUsername())) knownPlayerCount++;
        }

        assert knownPlayerCount == 2;

        mPlayer1.sendCreateGameMessage(new CreateGamePacket(UUID.randomUUID(), true, "none", Brandub.newBrandub7().getOTRString(), new TimeSpec(0, 0, 0, 0)));

        sleep(100);

        mPlayer2.requestGameUpdate();

        sleep(500);

        assert mPlayer2.lastGameUpdate.size() == 1;
        assert mPlayer2.lastGameUpdate.get(0).attackerUsername.equals(player1Username);
        assert !mPlayer2.lastGameUpdate.get(0).clockSetting.isEnabled();

        GameInformation info = mPlayer2.lastGameUpdate.get(0);
        mPlayer2.sendJoinGameMessage(info, new JoinGamePacket(UUID.fromString(info.uuid), false, "none"));

        sleep(1000);

        mPlayer1.getNetworkPlayer();
        mPlayer2.getNetworkPlayer();

        assert mPlayer1.getGameRole() == GameRole.ATTACKER;
        mPlayer1.sendMoveDecidedMessage(new MoveRecord(Coord.get(3, 0), Coord.get(2, 0)));

        sleep(500);

        assert mPlayer2.lastMove.softEquals(new MoveRecord(Coord.get(3, 0), Coord.get(2, 0)));
        mPlayer2.sendChatMessage(ClientServerConnection.ChatType.GAME, "player2", "chatmessage");

        sleep(500);

        assert mPlayer1.lastGameChat.message.equals("chatmessage");
        assert mPlayer2.lastGameChat.message.equals("chatmessage");

        mPlayer2.sendLeaveGameMessage();

        sleep(500);

        assert mPlayer1.gameEnded;
        assert mPlayer1.victory == VictoryPacket.Victory.ATTACKER;
        mPlayer1.sendLeaveGameMessage();

        sleep(500);

        mPlayer2.requestGameUpdate();

        sleep(500);

        assert mPlayer2.lastGameUpdate.size() == 0;

        mPlayer1.disconnect();
        mPlayer2.disconnect();

        mServer.stop();

        sleep(1000);

        assert mPlayer1.state == ClientServerConnection.State.DISCONNECTED;
        assert mPlayer2.state == ClientServerConnection.State.DISCONNECTED;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
