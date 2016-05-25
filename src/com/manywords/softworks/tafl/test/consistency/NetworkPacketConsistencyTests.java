package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.network.NetworkDummyDataGenerator;
import com.manywords.softworks.tafl.network.packet.GameListPacket;
import com.manywords.softworks.tafl.network.packet.LobbyChatPacket;
import com.manywords.softworks.tafl.network.packet.LoginPacket;
import com.manywords.softworks.tafl.test.TaflTest;

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

        LoginPacket lp = new LoginPacket("Fish Breath", "saltytest", "hashypassword");
        first = lp.toString();

        lp = LoginPacket.parse(first);
        second = lp.toString();

        assert first.equals(second);
    }
}
