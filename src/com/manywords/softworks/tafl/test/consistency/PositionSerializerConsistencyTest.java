package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.PositionSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.test.TaflTest;

public class PositionSerializerConsistencyTest extends TaflTest {

    @Override
    public void run() {
        try {
            Rules rules = Berserk.newCommanderCornerCaptureKingTest();
            char[][] boardArray = rules.getBoard().getBoardArray();
            String positionRecord = PositionSerializer.getPositionRecord(rules.getBoard());

            assert PositionSerializer.testInversion(positionRecord);

            char[][] newBoardArray = PositionSerializer.loadPositionRecord(positionRecord);
            String serializedPosition = PositionSerializer.getPositionRecord(newBoardArray);

            assert positionRecord.equals(serializedPosition);
        }
        catch(NotationParseException e) { assert false; }
    }

}
