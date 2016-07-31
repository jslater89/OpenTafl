package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.engine.replay.MoveAddress;
import com.manywords.softworks.tafl.test.TaflTest;

/**
 * Created by jay on 7/31/16.
 */
public class MoveAddressTest extends TaflTest {
    @Override
    public void run() {
        // The first move of the 29th turn in the game.
        String stringAddress = "29.";
        MoveAddress moveAddress = MoveAddress.parseAddress(stringAddress);

        assert moveAddress.toString().equals(stringAddress);

        // The first move of the 29th turn in the game.
        stringAddress = "29a.";
        moveAddress = MoveAddress.parseAddress(stringAddress);

        assert moveAddress.toString().equals(stringAddress);

        // The second move of the 29th turn in the game.
        stringAddress = "29b.";
        moveAddress = MoveAddress.parseAddress(stringAddress);

        assert moveAddress.toString().equals(stringAddress);

        // The first move of the first variation off of the first move of the 29th turn in the game.
        stringAddress = "29a.1.1.";
        moveAddress = MoveAddress.parseAddress(stringAddress);

        assert moveAddress.toString().equals(stringAddress);

        assert moveAddress.toString().equals(stringAddress);

        // The first move of the first variation off of the first move of the second turn of the third variation off of the first move of the 29th turn in the game.
        stringAddress = "29a.3.2a.1.1.";
        moveAddress = MoveAddress.parseAddress(stringAddress);

        assert moveAddress.toString().equals(stringAddress);

        // Same.
        stringAddress = "29a.3.2a.1.1a.";
        moveAddress = MoveAddress.parseAddress(stringAddress);

        assert moveAddress.toString().equals(stringAddress);

        // The third move (berserk) of the third turn of the third variation off of the second move of the fourth turn of the first variation off of the fourth move (berserk) of the 29th turn in the game.
        stringAddress = "29d.1.4b.3.3c.";
        moveAddress = MoveAddress.parseAddress(stringAddress);

        assert moveAddress.toString().equals(stringAddress);

        moveAddress = MoveAddress.parseAddress("29.");

        //29.1.1a
        moveAddress = moveAddress.addVariation();
        assert moveAddress.toString().equals("29.1.1a.");

        //29.1.1b, c, d
        moveAddress = moveAddress.increment(false);
        moveAddress = moveAddress.increment(false);
        moveAddress = moveAddress.increment(false);
        assert moveAddress.toString().equals("29.1.1d.");

        //29.1.2a
        moveAddress = moveAddress.increment(true);
        assert moveAddress.toString().equals("29.1.2a.");

        //29.2.1a
        moveAddress = moveAddress.addSibling();
        assert moveAddress.toString().equals("29.2.1a.");

        //29.2.1a.1.1a
        moveAddress = moveAddress.addVariation();
        assert moveAddress.toString().equals("29.2.1a.1.1a.");

        //29.2.1a.1.1b, 2a
        moveAddress = moveAddress.increment(false);
        assert moveAddress.toString().equals("29.2.1a.1.1b.");

        moveAddress = moveAddress.increment(true);
        assert moveAddress.toString().equals("29.2.1a.1.2a.");
    }
}
