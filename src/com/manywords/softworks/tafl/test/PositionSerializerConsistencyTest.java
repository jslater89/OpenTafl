package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.notation.MoveSerializer;
import com.manywords.softworks.tafl.notation.PositionSerializer;
import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.sun.xml.internal.ws.dump.LoggingDumpTube;

class PositionSerializerConsistencyTest extends TaflTest {

    @Override
    public void run() {
        Rules rules = Berserk.newCommanderCornerCaptureKingTest();
        char[][] boardArray = rules.getBoard().getBoardArray();
        String positionRecord = PositionSerializer.getPositionRecord(rules.getBoard());

        char[][] newBoardArray = PositionSerializer.loadPositionRecord(positionRecord);
        String serializedPosition = PositionSerializer.getPositionRecord(newBoardArray);

        assert positionRecord.equals(serializedPosition);
    }

}
