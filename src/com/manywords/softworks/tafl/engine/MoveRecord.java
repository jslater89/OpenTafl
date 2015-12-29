package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoveRecord {
    public final Coord mStart;
    public final Coord mEnd;

    public final List<Coord> captures;

    public MoveRecord(Coord start, Coord end) {
        mStart = start;
        mEnd = end;

        this.captures = new ArrayList<Coord>();
    }

    public MoveRecord(Coord start, Coord end, List<Coord> captures) {
        mStart = start;
        mEnd = end;

        this.captures = captures;
    }

    public String toString() {
        Map<String, String> start = Board.getChessNotation(mStart);
        Map<String, String> end = Board.getChessNotation(mEnd);
        String move = start.get("file") + start.get("rank") + " " + end.get("file") + end.get("rank");

        if (captures != null) {
            for (Coord capture : captures) {
                move += " x" + Board.getChessNotation(capture).get("file") + Board.getChessNotation(capture).get("rank");
            }
        }

        return move;
    }

    public boolean equals(Object o) {
        return (o instanceof MoveRecord)
                && this.mStart.equals(((MoveRecord) o).mStart)
                && this.mEnd.equals(((MoveRecord) o).mEnd)
                && this.captures.equals(((MoveRecord) o).captures);
    }
}
