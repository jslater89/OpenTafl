package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.replay.MoveAddress;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.engine.replay.ReplayGameState;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.test.TaflTest;

import java.io.File;

/**
 * Created by jay on 7/31/16.
 */
public class ReplayGameTest extends TaflTest {
    @Override
    public void run() {
        GameSerializer.GameContainer container = GameSerializer.loadGameRecordFile(new File("saved-games/replays/Fish-Nasa-2015-Fetlar.otg"));
        ReplayGame rg = new ReplayGame(container.game, container.moves);

        MoveAddress lastMoveAddress = null;
        for(GameState state : rg.getGame().getHistory()) {
            assert state instanceof ReplayGameState;
            ReplayGameState rgs = ((ReplayGameState) state);

            if(lastMoveAddress == null) {
                lastMoveAddress = rgs.getMoveAddress();
            }
            else {
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, lastMoveAddress.increment(rg, rgs) + "==" + rgs.getMoveAddress());
                assert lastMoveAddress.increment(rg, rgs).equals(rgs.getMoveAddress());
                lastMoveAddress = rgs.getMoveAddress();
            }
        }

        // Try finding a state by address
        ReplayGameState parent = rg.getStateByAddress(MoveAddress.parseAddress("4b"));
        assert parent != null;

        // Try creating a new variation
        parent = rg.getStateByAddress(MoveAddress.parseAddress("1a"));
        parent.makeVariation(new MoveRecord(Coord.get(5, 3), Coord.get(5, 2)));

        ReplayGameState child = rg.getStateByAddress(MoveAddress.parseAddress("1a.1.1a"));
        assert child != null;
        assert child.getParent() == parent;

        // Try tacking a canonical move onto the end of an existing variation
        parent = child;
        parent.makeVariation(new MoveRecord(Coord.get(4, 0), Coord.get(4, 3)));

        child = rg.getStateByAddress(MoveAddress.parseAddress("1a.1.1b"));
        assert child != null;
        assert child.getParent() == parent;

        // Try redoing the same move, which should return null (so ReplayGame can handle changing the state to that
        // state)
        ReplayGameState result = parent.makeVariation(new MoveRecord(Coord.get(4, 0), Coord.get(4, 3)));
        assert result == null;

        // Try a variation off of 1a again
        parent = rg.getStateByAddress(MoveAddress.parseAddress("1a"));
        parent.makeVariation(new MoveRecord(Coord.get(3, 5), Coord.get(2, 5)));

        child = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a"));
        assert child != null;
        assert child.getParent() == parent;

        // Try a double-variation off of a previous child
        // Try tacking a canonical move onto the end of an existing variation
        parent = rg.getStateByAddress(MoveAddress.parseAddress("1a.1.1a"));
        parent.makeVariation(new MoveRecord(Coord.get(0, 4), Coord.get(3, 4)));

        child = rg.getStateByAddress(MoveAddress.parseAddress("1a.1.1a.1.1a"));
        assert child != null;
        assert child.getParent() == parent;

    }
}
