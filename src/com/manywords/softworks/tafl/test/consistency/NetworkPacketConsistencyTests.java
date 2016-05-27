package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.network.NetworkDummyDataGenerator;
import com.manywords.softworks.tafl.network.packet.pregame.*;
import com.manywords.softworks.tafl.rules.brandub.Brandub;
import com.manywords.softworks.tafl.test.TaflTest;

import java.util.UUID;

/**
 * Created by jay on 5/25/16.
 */
public class NetworkPacketConsistencyTests extends TaflTest {
    @Override
    public void run() {
        GameListPacket gl = new GameListPacket(NetworkDummyDataGenerator.generateDebugClientGames(10));
        String first = gl.toString();

        gl = GameListPacket.parse(first);
        String second = gl.toString();

        assert first.equals(second);

        LobbyChatPacket lc = new LobbyChatPacket("Fish Breath", "This is a chat message.");
        first = lc.toString();

        lc = LobbyChatPacket.parse(first);
        second = lc.toString();

        assert first.equals(second);

        LoginPacket lp = new LoginPacket("Fish Breath", "hashypassword");
        first = lp.toString();

        lp = LoginPacket.parse(first);
        second = lp.toString();

        assert first.equals(second);

        CreateGamePacket cgp = new CreateGamePacket(UUID.randomUUID(), true, "hashypasswordy", Brandub.newBrandub7().getOTRString());
        first = cgp.toString();

        cgp = CreateGamePacket.parse(first);
        second = cgp.toString();

        assert first.equals(second);

        CancelGamePacket canp = new CancelGamePacket(UUID.randomUUID());
        first = canp.toString();

        canp = CancelGamePacket.parse(first);
        second = canp.toString();

        assert first.equals(second);
    }
}
