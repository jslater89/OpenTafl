package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.test.TaflTest;

/**
 * Created by jay on 8/21/16.
 */
public class MoveRecordRotationMirrorTest extends TaflTest {

    @Override
    public void run() {
        MoveRecord r = new MoveRecord(Coord.get(3, 3), Coord.get(0, 3));

        assert MoveRecord.getMirrors(7, r).size() == 1;
        assert MoveRecord.getRotations(7, r).size() == 3;

        r = new MoveRecord(Coord.get(2,8), Coord.get(2,0));

        assert MoveRecord.getMirrors(11, r).size() == 2;
        assert MoveRecord.getRotations(11, r).size() == 3;

    }
}
