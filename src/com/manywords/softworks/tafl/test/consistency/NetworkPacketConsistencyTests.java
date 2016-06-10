package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.NetworkDummyDataGenerator;
import com.manywords.softworks.tafl.network.packet.ingame.ClockUpdatePacket;
import com.manywords.softworks.tafl.network.packet.ingame.VictoryPacket;
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

        LobbyChatPacket lc = new LobbyChatPacket("Fish Breath", "This is a \"chat message\".");
        first = lc.toString();

        lc = LobbyChatPacket.parse(first);
        second = lc.toString();

        assert first.equals(second);

        LoginPacket lp = new LoginPacket("Fish Breath", "hashypassword", OpenTafl.NETWORK_PROTOCOL_VERSION);
        first = lp.toString();

        lp = LoginPacket.parse(first);
        second = lp.toString();

        assert first.equals(second);

        CreateGamePacket cgp = new CreateGamePacket(UUID.randomUUID(), true, "hashypasswordy", Brandub.newBrandub7().getOTRString(), new TimeSpec(300000, 15000, 3, 0));
        first = cgp.toString();

        cgp = CreateGamePacket.parse(first);
        second = cgp.toString();

        assert first.equals(second);

        LeaveGamePacket canp = new LeaveGamePacket(UUID.randomUUID());
        first = canp.toString();

        canp = LeaveGamePacket.parse(first);
        second = canp.toString();

        assert first.equals(second);

        JoinGamePacket joinp = new JoinGamePacket(UUID.randomUUID(), true, "hashypassword");
        first = joinp.toString();

        joinp = JoinGamePacket.parse(first);
        second = joinp.toString();

        assert first.equals(second);

        VictoryPacket vic = new VictoryPacket(VictoryPacket.Victory.ATTACKER);
        first = vic.toString();

        vic = VictoryPacket.parse(first);
        second = vic.toString();

        assert first.equals(second);

        ClockUpdatePacket cloc = new ClockUpdatePacket(new TimeSpec(30000, 30000, 3, 0), new TimeSpec(47500, 12841, 2, 0));
        first = cloc.toString();

        cloc = ClockUpdatePacket.parse(first);
        second = cloc.toString();

        assert first.equals(second);

        ClientListPacket cl = ClientListPacket.parse("Fish Breath||");
        first = cl.toString();

        cl = ClientListPacket.parse(first);
        second = cl.toString();

        //System.out.println(first);
        //System.out.println(second);

        assert first.equals(second);

        SpectateGamePacket specp = new SpectateGamePacket(UUID.randomUUID(), true, "hashypassword");
        first = specp.toString();

        specp = SpectateGamePacket.parse(first);
        second = specp.toString();

        assert first.equals(second);
    }
}
