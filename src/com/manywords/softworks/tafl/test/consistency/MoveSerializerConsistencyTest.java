package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.notation.MoveSerializer;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.test.TaflTest;

public class MoveSerializerConsistencyTest extends TaflTest {

    @Override
    public void run() {
        String move = "Ne6^=e8xce7/ne9/Kf8/d8";
        DetailedMoveRecord moveRecord = null;
        try {
             moveRecord = MoveSerializer.loadMoveRecord(9, move);
        }
        catch(NotationParseException e) {
            assert false;
        }
        String serializedMove = MoveSerializer.getMoveRecord(moveRecord);

        //System.out.println(move);
        //System.out.println(serializedMove);

        assert move.equals(serializedMove);
    }

}
