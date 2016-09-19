/*
Under section 3(b)(ii) of the Free-As-In-Beer License, this file
is exempted from all terms and conditions of the license. It is
released instead under the Apache license 2.0:

Copyright 2015 Jay Slater

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.manywords.softworks.tafl.notation;

import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jay on 2/13/16.
 */
public class MoveSerializer {
    public static String getMoveRecord(DetailedMoveRecord record) {
        String startString = Coord.getChessString(record.start);
        String endString = Coord.getChessString(record.end);

        String moveSeparator = "-";
        if(record.wasJump() && record.wasBerserk()) moveSeparator = "^=";
        else if(record.wasJump()) moveSeparator = "^";
        else if(record.wasBerserk()) moveSeparator = "=";

        char moverChar = DetailedMoveRecord.getTaflmanCharForFlag(record.flags);

        String move = (moverChar != 0 ? moverChar : "") + startString + moveSeparator + endString;

        if(record.captureArray.length > 0) {
            move += "x";

            for(int i = 0; i < record.captureArray.length; i++) {
                move += DetailedMoveRecord.getCaptureString(record.dimension, record.captureArray[i], i == 0);
            }
        }

        return move;
    }

    public static DetailedMoveRecord loadMoveRecord(int dimension, String record) {
        final int MOVER_TYPE = 1;
        final int START_SPACE = 2;
        final int MOVE_TYPE = 3;
        final int END_SPACE = 4;
        final int[] CAPTURE_TYPES = {7, 10, 13, 16};
        final int[] CAPTURE_SPACES = {8, 11, 14, 17};
        final int STATUS_CODE = 18;
        Pattern p = Pattern.compile(getMoveRegex());
        Matcher m = p.matcher(record);
        if(!m.matches()) throw new IllegalArgumentException("Bad move record: " + record);

        String[] groups = new String[19];

        for(int i = 0; i < groups.length; i++) {
            groups[i] = m.group(i);
        }

        char moverCode = 0;
        if(groups[MOVER_TYPE] != null && groups[MOVER_TYPE].length() > 0) {
            moverCode = groups[MOVER_TYPE].charAt(0);
        }
        boolean moverAttacker = TaflmanCodes.isCodeAttackingSide(moverCode);
        char moverType = TaflmanCodes.getTaflmanTypeForCode(moverCode);
        char mover = (char)((moverAttacker ? Taflman.SIDE_ATTACKERS : Taflman.SIDE_DEFENDERS) | moverType);

        boolean wasJump = groups[MOVE_TYPE].contains("^");
        boolean wasBerserk = groups[MOVE_TYPE].contains("=");
        Coord start = Coord.get(groups[START_SPACE]);
        Coord end = Coord.get(groups[END_SPACE]);

        List<Character> capturedTaflmen = new ArrayList<Character>();
        List<Coord> capturedSpaces = new ArrayList<Coord>();
        for(int i = 0; i < CAPTURE_SPACES.length; i++) {
            if(groups[CAPTURE_SPACES[i]] == null) break;

            char capturedCode = 0;
            if(groups[CAPTURE_TYPES[i]] != null && groups[CAPTURE_TYPES[i]].length() > 0) {
                capturedCode = groups[CAPTURE_TYPES[i]].charAt(0);
            }
            boolean capturedAttacker = TaflmanCodes.isCodeAttackingSide(capturedCode);
            char capturedType = TaflmanCodes.getTaflmanTypeForCode(capturedCode);
            char captured = (char)((capturedAttacker ? Taflman.SIDE_ATTACKERS : Taflman.SIDE_DEFENDERS) | capturedType);
            Coord capturedSpace = Coord.get(groups[CAPTURE_SPACES[i]]);

            capturedTaflmen.add(captured);
            capturedSpaces.add(capturedSpace);
        }

        String statusCode = groups[STATUS_CODE];

        DetailedMoveRecord move = new DetailedMoveRecord(dimension, start, end, mover, capturedSpaces, capturedTaflmen, wasJump, wasBerserk);
        return move;
    }

    public static String getMoveRegex() {
        // This monstrosity is the fully-assembled version of the regex.
        //String moveRegex = "([tcnkTCNK]?)([a-s][0-9]{1,2})([\\^\\=\\-]{1,2})([a-s][0-9]{1,2})((x([tcnkTCNK]?)([a-s][0-9]{1,2}))(/([tcnkTCNK]?)([a-s][0-9]{1,2}))?(/([tcnkTCNK]?)([a-s][0-9]{1,2}))?(/([tcnkTCNK]?)(([a-s][0-9]{1,2}))?)?([\\+|\\-]{1,3})?";

        // Matches a taflman type character.
        final String taflmanTypePattern = "([tcnkTCNK]?)";

        // Matches a position string (e.g. e4), capped at 19x19 for our purposes
        final String positionPattern = "([a-s][0-9]{1,2})";

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
