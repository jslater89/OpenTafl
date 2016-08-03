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

        //rg.dumpHistory();

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

        //parent.dumpTree();

        child = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a"));
        assert child != null;
        assert child.getMoveAddress().equals(MoveAddress.parseAddress("1a.2.1a"));
        assert child.getParent() == parent;

        // Try a double-variation off of a previous child
        // Try tacking a canonical move onto the end of an existing variation
        parent = rg.getStateByAddress(MoveAddress.parseAddress("1a.1.1a"));
        parent.makeVariation(new MoveRecord(Coord.get(0, 4), Coord.get(3, 4)));

        child = rg.getStateByAddress(MoveAddress.parseAddress("1a.1.1a.1.1a"));
        assert child != null;
        assert child.getParent() == parent;

        //  Make a third variation!
        parent = rg.getStateByAddress(MoveAddress.parseAddress("1a"));
        parent.makeVariation(new MoveRecord(Coord.get(4, 4), Coord.get(3, 4)));

        ReplayGameState firstVariation = rg.getStateByAddress(MoveAddress.parseAddress("1a.1.1a"));
        ReplayGameState thirdVariation = rg.getStateByAddress(MoveAddress.parseAddress("1a.3.1a"));
        assert thirdVariation != null;
        assert thirdVariation.getParent() == parent;

        // Delete variation 1a.2, and make sure that the variation formerly known as 3 is now known as 2.
        parent.deleteVariation(MoveAddress.parseAddress("1a.2"));

        assert thirdVariation.getMoveAddress().equals(MoveAddress.parseAddress("1a.2.1a"));

        // Now for the major push.
        // 3,5 to 2,5 so far
        parent = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a"));

        // 1a.2.1b, 1a.2.1a.1.1a, 1a.2.1a.2.1a
        ReplayGameState state;
        state = parent.makeVariation(new MoveRecord(Coord.get(0, 3), Coord.get(1, 3)));
        assert state.getMoveAddress().equals(MoveAddress.parseAddress("1a.2.1b"));

        state = parent.makeVariation(new MoveRecord(Coord.get(0, 4), Coord.get(1, 4)));
        assert state.getMoveAddress().equals(MoveAddress.parseAddress("1a.2.1a.1.1a"));

        state = parent.makeVariation(new MoveRecord(Coord.get(10, 3), Coord.get(9, 3)));
        assert state.getMoveAddress().equals(MoveAddress.parseAddress("1a.2.1a.2.1a"));

        state = parent.makeVariation(new MoveRecord(Coord.get(0, 6), Coord.get(1, 6)));
        assert state.getMoveAddress().equals(MoveAddress.parseAddress("1a.2.1a.3.1a"));

        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1b"))) != null;
        //System.out.println(state.getEnteringMove());
        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a.1.1a"))) != null;
        //System.out.println(state.getEnteringMove());
        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a.2.1a"))) != null;
        //System.out.println(state.getEnteringMove());
        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a.3.1a"))) != null;
        //System.out.println(state.getEnteringMove());

        //System.out.println();

        parent.deleteVariation(MoveAddress.parseAddress("1a.2.1a.1."));

        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1b"))) != null;
        //System.out.println(state.getEnteringMove());
        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a.1.1a"))) != null;
        //System.out.println(state.getEnteringMove());
        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a.2.1a"))) != null;
        //System.out.println(state.getEnteringMove());
        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a.3.1a"))) == null;

        //System.out.println();

        // Delete a canonical child, relocating the first variation to canonical child-dom
        parent.deleteVariation(MoveAddress.parseAddress("1a.2.1b"));

        state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1b"));
        assert state != null;

        //rg.dumpHistory();

        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a"))) != null;
        //System.out.println(state.getEnteringMove());
        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a.1.1a"))) != null;
        //System.out.println(state.getEnteringMove());
        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a.2.1a"))) == null;
        assert (state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a.3.1a"))) == null;

        state = rg.getStateByAddress(MoveAddress.parseAddress("1a.2.1a"));

        // Alrighty, let's:
        // 1. whack everything
        // 2. build a new tree
        // 3. hurt it

        state = rg.getStateByAddress(MoveAddress.parseAddress("1a"));

        // Delete the two remaining variations
        state.deleteVariation(MoveAddress.parseAddress("1a.1"));
        state.deleteVariation(MoveAddress.parseAddress("1a.1"));

        // No-op: variation doesn't exist
        // TODO: return false, probably
        state.deleteVariation(MoveAddress.parseAddress("1a.3"));

        assert state.getVariations().size() == 0;

        /*
        Tree to build:
        1a.1.1a. 7,5 -> 7,2
        1a.1.1b. 10,3 -> 8,3
            1a.1.1b.1.1b. 9,5 -> 9,2
            1a.1.1b.1.2a. 5,3 -> 8,3
            1a.1.1b.1.2b. 5,1 -> 8,1
        1a.1.2a. 5,3 -> 7,3
        1a.1.2b. 6,0 -> 6,3
        1a.1.3a. 7,2 -> 6,2
        1a.1.3b. 8,3 -> 6,3

        1a.2.1a. 5,3 -> 5,2
        1a.2.1b. 4,0 -> 4,2
        1a.2.2a. 3,5 -> 3,2

        1a.3.1a. 5,7 -> 6,7
        1a.3.1b. 9,5 -> 9,1
        1a.3.2a. 5,6 -> 5,8
        1a.3.2b. 0,7 -> 5,7
        1a.3.3a. 4,6 -> 4,7
        1a.3.3b. 1,5 -> 1,1
        1a.3.4a. 5,5 -> 5,8
        1a.3.4b. 5,9 -> 10,9
            1a.3.4a.1.1b 5,9 -> 0,9
            1a.3.4a.1.2a 5,8 -> 10,8
            1a.3.4a.1.2b 3,10 -> 3,9
            1a.3.4a.1.3a 10,8 -> 10,10
        1a.3.5a. 5,8 -> 0,8
        1a.3.5b. 3,10 -> 3,9
        1a.3.6a. 0,8 -> 0,10
         */
    }
}
