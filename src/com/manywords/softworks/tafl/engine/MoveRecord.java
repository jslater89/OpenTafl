package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;

import java.util.*;

public class MoveRecord {
    public final Coord start;
    public final Coord end;

    public final List<Coord> captures;

    public static MoveRecord getMoveRecordFromSimpleString(String simpleMoveRecord) {
        String[] coordStrings = simpleMoveRecord.split("-");
        Coord start = Board.getCoordFromChessNotation(coordStrings[0]);
        Coord end = Board.getCoordFromChessNotation(coordStrings[1]);

        return new MoveRecord(start, end);
    }

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

    public String toSimpleString() {
        Map<String, String> start = Board.getChessNotation(this.start);
        Map<String, String> end = Board.getChessNotation(this.end);
        return start.get("file") + start.get("rank") + "-" + end.get("file") + end.get("rank");
    }

    public boolean isDetailed() { return false; }

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

    public boolean softEquals(Object o) {
        return (o instanceof MoveRecord)
                && this.start.equals(((MoveRecord) o).start)
                && this.end.equals(((MoveRecord) o).end);
    }

    public static boolean isRotationOrMirror(int dimension, MoveRecord m1, MoveRecord m2) {
        // If the move is the same, it's obviously a rotation or mirror of itself.
        if(m1.softEquals(m2)) return true;

        List<MoveRecord> rotations = getRotations(dimension, m1);
        Set<MoveRecord> rotationsAndMirrors = new HashSet<>();

        for(MoveRecord rotation : rotations) {
            rotationsAndMirrors.add(rotation);
            rotationsAndMirrors.addAll(getMirrors(dimension, rotation));
        }

        return rotationsAndMirrors.contains(m2);
    }

    public static List<MoveRecord> getMirrors(int dimension, MoveRecord m) {
        List<MoveRecord> mirrors = new ArrayList<>(2); // At most two mirrors

        int axis = dimension / 2;
        // Around horizontal axis first
        if(m.start.y != axis || m.end.y != axis ) {
            int startDistance = m.start.y - axis;
            int endDistance = m.end.y - axis;

            mirrors.add(new MoveRecord(Coord.get(m.start.x, axis - startDistance), Coord.get(m.end.x, axis - endDistance)));
        }

        // Same thing around the vertical axis
        if(m.start.x != axis || m.end.x != axis ) {
            int startDistance = m.start.x - axis;
            int endDistance = m.end.x - axis;

            mirrors.add(new MoveRecord(Coord.get(axis - startDistance, m.start.y), Coord.get(axis - endDistance, m.end.y)));
        }

        return mirrors;
    }

    public static List<MoveRecord> getRotations(int dimension, MoveRecord m) {
        List<MoveRecord> rotations = new ArrayList<>(3); // Always three there are

        int axis = dimension / 2;

        int xStart = m.start.x;
        int yStart = m.start.y;
        int xEnd = m.end.x;
        int yEnd = m.end.y;

        int xTemp, yTemp;
        for(int i = 0; i < 3; i++) {
            xTemp = xStart - axis;
            yTemp = yStart - axis;

            xStart = yTemp + axis;
            yStart = -xTemp + axis;

            xTemp = xEnd - axis;
            yTemp = yEnd - axis;

            xEnd = yTemp + axis;
            yEnd = -xTemp + axis;

            rotations.add(new MoveRecord(Coord.get(xStart, yStart), Coord.get(xEnd, yEnd)));
        }
        return rotations;
    }
}
