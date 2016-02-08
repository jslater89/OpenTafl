package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoveRecord {
    public final Coord start;
    public final Coord end;

    public final List<Coord> captures;

    public MoveRecord(Coord start, Coord end) {
        this.start = start;
        this.end = end;

        this.captures = new ArrayList<Coord>();
    }

    public MoveRecord(Coord start, Coord end, List<Coord> captures) {
        this.start = start;
        this.end = end;

        this.captures = captures;
    }

    public String toString() {
        Map<String, String> start = Board.getChessNotation(this.start);
        Map<String, String> end = Board.getChessNotation(this.end);
        String move = start.get("file") + start.get("rank") + "-" + end.get("file") + end.get("rank");

        if (captures != null && captures.size() > 0) {
            Coord capture = captures.get(0);
            move += "x";
            move += Board.getChessNotation(capture).get("file") + Board.getChessNotation(capture).get("rank");
            for(int i = 1; i < captures.size(); i++) {
                capture = captures.get(i);
                move += "/" + Board.getChessNotation(capture).get("file") + Board.getChessNotation(capture).get("rank");
            }
        }

        return move;
    }

    public boolean equals(Object o) {
        return (o instanceof MoveRecord)
                && this.start.equals(((MoveRecord) o).start)
                && this.end.equals(((MoveRecord) o).end)
                && this.captures.equals(((MoveRecord) o).captures);
    }
}
