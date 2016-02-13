package com.manywords.softworks.tafl.notation;

import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;

/**
 * Created by jay on 2/13/16.
 */
public class MoveSerializer {
    public static String getMoveRecord(DetailedMoveRecord record) {
        String startString = Board.getChessString(record.start);
        String endString = Board.getChessString(record.end);

        String moveSeparator = "-";
        if(record.wasJump() && record.wasBerserk()) moveSeparator = "^=";
        else if(record.wasJump()) moveSeparator = "^";
        else if(record.wasBerserk()) moveSeparator = "=";

        char moverChar = DetailedMoveRecord.getTaflmanCharForFlag(record.flags);

        String move = (moverChar != 0 ? moverChar : "") + startString + moveSeparator + endString;

        if(record.captureArray.length > 0) {
            move += "x";

            for(int i = 0; i < record.captureArray.length; i++) {
                move += DetailedMoveRecord.getCaptureString(record.captureArray[i], i == 0);
            }
        }

        return move;
    }

    public static DetailedMoveRecord loadMoveRecord(String record) {
        DetailedMoveRecord move = new DetailedMoveRecord(null, null, (char) 0);
        return move;
    }

    public static final String getMoveRegex() {
        // This monstrosity is the fully-assembled version of the regex.
        //String moveRegex = "([tcnkTCNK]?)([abcdefghijklmnopqrs]{1}[1234567890]{1,2})([\\^\\=\\-]{1,2})([abcdefghijklmnopqrs]{1}[1234567890]{1,2})((x([tcnkTCNK]?)([abcdefghijklmnopqrs]{1}[1234567890]{1,2})){1}(/([tcnkTCNK]?)([abcdefghijklmnopqrs]{1}[1234567890]{1,2}))?(/([tcnkTCNK]?)([abcdefghijklmnopqrs]{1}[1234567890]{1,2}))?(/([tcnkTCNK]?)([abcdefghijklmnopqrs]{1}[1234567890]{1,2}))?)?([\\+|\\-]{1,3})?";

        // Matches a taflman type character.
        final String taflmanTypePattern = "([tcnkTCNK]?)";

        // Matches a position string (e.g. e4), capped at 19x19 for our purposes
        final String positionPattern = "([a-s][1-0]{1,2})";

        // Matches a taflman position.
        final String taflmanPositionPattern = taflmanTypePattern + positionPattern;

        // Matches a move type string (one of -, ^, =, or ^=)
        final String moveTypePattern = "([\\^\\=\\-]{1,2})";

        final String statusString = "([\\+|\\-]{1,3})";

        final String moveRegex = taflmanPositionPattern + moveTypePattern + positionPattern +
                "(" + // group all captures together
                    "(x" + taflmanPositionPattern + ")" + // at least one capture must be present for this pattern
                    "(/" + taflmanPositionPattern + ")?" + // up to three other captures
                    "(/" + taflmanPositionPattern + ")?" + // up to three other captures
                    "(/" + taflmanPositionPattern + ")?" + // up to three other captures
                ")?" + // captures are optional
                statusString + "?";


        return moveRegex;
    }
}
