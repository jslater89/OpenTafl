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

package com.manywords.softworks.tafl.engine;

/**
 * Created by jay on 2/8/16.
 */

import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.notation.MoveSerializer;
import com.manywords.softworks.tafl.notation.TaflmanCodes;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.List;

public class DetailedMoveRecord extends MoveRecord {
    private static final byte MOVE_TYPE_MASK = 16 + 32;
    private static final byte JUMP = 16; //bit 5
    private static final byte BERSERK = 32; //bit 6

    private static final byte TYPE_MASK = 7; //bit 1-3
    private static final byte TAFLMAN = 1;
    private static final byte COMMANDER = 2;
    private static final byte KNIGHT = 1 + 2;
    private static final byte KING = 4;

    private static final byte SIDE_MASK = 8; // bit 4
    private static final byte ATTACKERS = 8;
    public final byte flags;

    // bits 1-3: TYPE_MASK
    // bit 4: SIDE_MASK
    private static final char LOCATION_MASK = 16 + 32 + 64 + 128 + 256 + 512 + 1024 + 2048 + 4096; // bits 5-13
    public final char[] captureArray;
    public final int dimension;

    private String mComment = "";
    private TimeSpec mTimeRemaining;

    public DetailedMoveRecord(MoveRecord move) {
        super(move.start, move.end);
        flags = 0;
        captureArray = new char[0];
        dimension = 0;
    }

    public DetailedMoveRecord(int dimension, Coord start, Coord end, char mover) {
        super(start, end);
        this.dimension = dimension;
        flags = moveRecordFlagFor(mover);

        this.captureArray = new char[0];
    }

    public DetailedMoveRecord(int dimension, Coord start, Coord end, char mover, List<Coord> captures, List<Character> capturedTaflmen) {
        super(start, end, captures);
        this.dimension = dimension;
        flags = moveRecordFlagFor(mover);
        this.captureArray = buildCaptureArray(dimension, captures, capturedTaflmen);
    }

    public DetailedMoveRecord(int dimension, Coord start, Coord end, char mover, List<Coord> captures, List<Character> capturedTaflmen, boolean wasJump, boolean wasBerserk) {
        super(start, end, captures);
        this.dimension = dimension;
        byte flags = 0;
        flags |= moveRecordFlagFor(mover);
        if(wasJump) flags |= JUMP;
        if(wasBerserk) flags |= BERSERK;
        this.flags = flags;

        this.captureArray = buildCaptureArray(dimension, captures, capturedTaflmen);
    }

    @Override
    public boolean isDetailed() {
        return true;
    }

    public void setTimeRemaining(TimeSpec remaining) {
        mTimeRemaining = remaining;
    }

    public TimeSpec getTimeRemaining() {
        return mTimeRemaining;
    }

    public void setComment(String comment) {
        TimeSpec ts = GameClock.getTimeSpecForGameNotationString(comment);
        if(ts != null) {
            mComment = comment.replaceFirst(GameClock.TIME_SPEC_REGEX, "");
            mTimeRemaining = ts;
        }
        else {
            mComment = comment;
        }
    }

    public String getComment() {
        return mComment;
    }

    public boolean wasJump() {
        return (flags & JUMP) == JUMP;
    }

    public boolean wasBerserk() {
        return (flags & BERSERK) == BERSERK;
    }

    private char[] buildCaptureArray(int dimension, List<Coord> captures, List<Character> capturedTaflmen) {
        if(capturedTaflmen.size() != captures.size()) throw new IllegalArgumentException("captureArray and capturedTaflmen differ");

        char[] captureArray = new char[captures.size()];
        for(int i = 0; i < captureArray.length; i++) {
            char index = (char) Coord.getIndex(dimension, captures.get(i));
            index = (char)(index << 4);
            byte taflmanFlag = moveRecordFlagFor(capturedTaflmen.get(i));

            captureArray[i] = (char)(index | taflmanFlag);
        }

        return captureArray;
    }

    private byte moveRecordFlagFor(char mover) {
        byte flag = taflmanTypeToMoveRecordType(mover);
        flag |= (Taflman.getPackedSide(mover) == Taflman.SIDE_ATTACKERS ? ATTACKERS : 0);
        return flag;
    }

    public String toString() {
        return MoveSerializer.getMoveRecord(this);
    }

    public static byte taflmanTypeToMoveRecordType(char mover) {
        byte typeFlag = TAFLMAN;
        switch(Taflman.getPackedType(mover)) {
            case Taflman.TYPE_COMMANDER:
                typeFlag = COMMANDER;
                break;
            case Taflman.TYPE_KNIGHT:
                typeFlag = KNIGHT;
                break;
            case Taflman.TYPE_KING:
                typeFlag = KING;
                break;
        }

        return typeFlag;
    }

    public static String getCaptureString(int dimension, char captureEntry, boolean first) {
        String captureRecord = "";

        if(!first) captureRecord += "/";

        char taflmanChar = getTaflmanCharForFlag((byte) captureEntry);
        int index = (captureEntry & LOCATION_MASK) >> 4;
        Coord location = Coord.getCoordForIndex(dimension, index);

        captureRecord += (taflmanChar != 0 ? taflmanChar : "") + Coord.getChessString(location);

        return captureRecord;
    }

    public static char getTaflmanCharForFlag(byte flags) {
        char taflmanChar;
        byte taflmanType = (byte)(flags & TYPE_MASK);
        byte side = (byte)(flags & SIDE_MASK);
        if(taflmanType == COMMANDER) taflmanChar = TaflmanCodes.inverse[TaflmanCodes.c];
        else if(taflmanType == KNIGHT) taflmanChar = TaflmanCodes.inverse[TaflmanCodes.n];
        else if(taflmanType == KING) taflmanChar = TaflmanCodes.inverse[TaflmanCodes.k];
        else taflmanChar = 0;

        if(taflmanChar != 0) {
            if (side == ATTACKERS) taflmanChar = Character.toLowerCase(taflmanChar);
            else taflmanChar = Character.toUpperCase(taflmanChar);
        }

        return taflmanChar;
    }
}

