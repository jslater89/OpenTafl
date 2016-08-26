package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.engine.collections.RepetitionHashTable;
import com.manywords.softworks.tafl.test.TaflTest;

/**
 * Created by jay on 8/22/16.
 */
public class RepetitionHashTableTest extends TaflTest {
    @Override
    public void run() {
        RepetitionHashTable h = new RepetitionHashTable();
        h.increment(1);
        h.increment(RepetitionHashTable.ARRAY_SIZE);
        h.increment(RepetitionHashTable.ARRAY_SIZE);
        h.increment(RepetitionHashTable.ARRAY_SIZE);

        assert h.getRepetitionCount(1) == 1;
        assert h.getRepetitionCount(RepetitionHashTable.ARRAY_SIZE) == 3;

        h.decrement(1);
        h.decrement(RepetitionHashTable.ARRAY_SIZE);
        h.decrement(RepetitionHashTable.ARRAY_SIZE);

        assert h.getRepetitionCount(1) == 0;
        assert h.getRepetitionCount(RepetitionHashTable.ARRAY_SIZE) == 1;

        h.decrement(RepetitionHashTable.ARRAY_SIZE);
        assert h.getRepetitionCount(RepetitionHashTable.ARRAY_SIZE) == 0;
    }
}
