package com.manywords.softworks.tafl.test.network;


import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.pregame.CreateGamePacket;
import com.manywords.softworks.tafl.network.packet.pregame.JoinGamePacket;
import com.manywords.softworks.tafl.network.packet.utility.ErrorPacket;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jay on 7/14/16.
 */
public class LoadServerGameTest extends ServerTest {
    @Override
    public void run() {
        GameSerializer.GameContainer container = null;
        try {
            container = GameSerializer.loadGameRecordFile(new File("saved-games/replays/Fish-Nasa-2015-Fetlar.otg"));
        }
        catch(NotationParseException e) {
            assert false;
        }

        List<DetailedMoveRecord> moves = new ArrayList<>(10);
        long startZobrist = container.game.getCurrentState().mZobristHash;
        assert startZobrist != 0;

        for(int i = 0; i < 10; i++) {
            moves.add(container.moves.get(i));
            moves.get(i).setTimeRemaining(new TimeSpec(12345, 67890, 3, 3000));
        }

        setupServer();

        mPlayer1.sendCreateGameMessage(new CreateGamePacket(UUID.randomUUID(), true, "none", container.game.getRules().getOTRString(false), new TimeSpec(300000, 30000, 3, 1000), true, true));
        mPlayer1.sendHistoryByMoveRecords(moves, 11);
        mPlayer1.sendClockUpdate(new TimeSpec(200000, 30000, 2, 0), new TimeSpec(100000, 12000, 2, 0));

        sleep(500);

        mPlayer2.requestGameUpdate();

        sleep(500);

        GameInformation info = mPlayer2.lastGameUpdate.get(0);
        assert info.loaded;

        mPlayer2.sendJoinGameMessage(info, new JoinGamePacket(UUID.fromString(info.uuid), false, "none"));

        sleep(500);

        assert mPlayer1.lastHistory != null && mPlayer1.lastHistory.size() == 10;
        assert mPlayer2.lastHistory != null && mPlayer2.lastHistory.size() == 10;

        assert mPlayer1.lastHistory.get(0).getTimeRemaining().clockEquals(new TimeSpec(12345, 67890, 3, 3000));
        assert mPlayer2.lastHistory.get(0).getTimeRemaining().settingEquals(new TimeSpec(12345, 67890, 3, 3000));

        assert mPlayer1.game.getCurrentState().mZobristHash != startZobrist;
        assert mPlayer1.game.getCurrentState().mZobristHash == mPlayer2.game.getCurrentState().mZobristHash;

        assert mPlayer1.lastDefenderTime.mainTime <= 101000; // increments
        assert mPlayer2.lastAttackerTime.mainTime <= 201000;

        mPlayer1.sendLeaveGameMessage();
        mPlayer2.sendLeaveGameMessage();

        sleep(500);

        assert mPlayer1.state == mPlayer2.state && mPlayer2.state == ClientServerConnection.State.LOGGED_IN;

        mPlayer1.sendCreateGameMessage(new CreateGamePacket(UUID.randomUUID(), true, "none", container.game.getRules().getOTRString(false), new TimeSpec(0, 0, 0, 0), true, true));
        moves.clear();
        moves.add(new DetailedMoveRecord(11, Coord.get(3, 3), Coord.get(2, 2), Taflman.encode((char) 0, Taflman.TYPE_TAFLMAN, Taflman.SIDE_DEFENDERS)));
        mPlayer1.sendHistoryByMoveRecords(moves, 11);

        sleep(500);

        assert mPlayer1.lastError.equals(ErrorPacket.BAD_SAVE);

        stopServer();
    }
}
