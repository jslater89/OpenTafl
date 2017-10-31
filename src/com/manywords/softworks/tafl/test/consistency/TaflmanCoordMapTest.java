package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.engine.collections.TaflmanCoordMap;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.test.TaflTest;

public class TaflmanCoordMapTest extends TaflTest {
    @Override
    public void run() {
        TaflmanCoordMap map = new TaflmanCoordMap(19, 128, 128);

        for(int i = 0; i < 128; i++) {
            char packed = Taflman.encode((char) i, Taflman.SIDE_ATTACKERS, Taflman.TYPE_TAFLMAN);
            map.put(packed, Coord.getCoordForIndex(19, i));
        }

        for(int i = 128; i < 256; i++) {
            char packed = Taflman.encode((char) i, Taflman.SIDE_DEFENDERS, Taflman.TYPE_TAFLMAN);
            map.put(packed, Coord.getCoordForIndex(19, i));
        }

        char packed = Taflman.encode((char) 129, Taflman.SIDE_DEFENDERS, Taflman.TYPE_TAFLMAN);


        //println(map);
        assert map.getCoord(packed) != null;

        for(int i = 0; i < 128; i++) {
            packed = Taflman.encode((char) i, Taflman.SIDE_ATTACKERS, Taflman.TYPE_TAFLMAN);
            assert Coord.getCoordForIndex(19, i).equals(map.getCoord(packed));
        }

        for(int i = 128; i < 256; i++) {
            packed = Taflman.encode((char) i, Taflman.SIDE_DEFENDERS, Taflman.TYPE_TAFLMAN);
            assert Coord.getCoordForIndex(19, i).equals(map.getCoord(packed));
        }
    }
}
