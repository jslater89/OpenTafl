package com.manywords.softworks.tafl.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Coord {
    public final byte x;
    public final byte y;

    private Coord(byte x, byte y) {
        this.x = x;
        this.y = y;
    }

    private Coord(int x, int y) {
        this((byte) x, (byte) y);
    }

    private static final int EAST = 0;
    private static final int WEST = 1;
    private static final int NORTH = 2;
    private static final int SOUTH = 3;

    private static Coord[][] mCoords;
    private static int mDimension;
    private static Map<Coord, List<Coord>> mAdjacentCoords;
    private static Map<Coord, List<Coord>> mDiagonalCoords;
    private static Map<Coord, List<List<Coord>>> mRankAndFileCoords;
    private static List<Coord> mTopEdge;
    private static List<Coord> mBottomEdge;
    private static List<Coord> mLeftEdge;
    private static List<Coord> mRightEdge;
    private static List<Coord> mAllEdges;

    public static void initialize(int dimension) {
        if(dimension == 0) {
            throw new IllegalStateException("Cannot initialize Coord to dimension 0");
        }

        if(mDimension == dimension) {
            return;
        }
        mDimension = dimension;
        mCoords = new Coord[dimension][dimension];

        for (int y = 0; y < dimension; y++) {
            for (int x = 0; x < dimension; x++) {
                mCoords[y][x] = new Coord(x, y);
            }
        }


        mLeftEdge = new ArrayList<Coord>(dimension);
        mRightEdge = new ArrayList<Coord>(dimension);
        mTopEdge = new ArrayList<Coord>(dimension);
        mBottomEdge = new ArrayList<Coord>(dimension);
        mAllEdges = new ArrayList<Coord>(dimension);

        ArrayList<Coord> edge = new ArrayList<Coord>(dimension);
        for (int i = 0; i < dimension; i++) {
            edge.add(Coord.get(i, 0));
        }
        mTopEdge = edge;

        edge = new ArrayList<Coord>(dimension);
        for (int i = 0; i < dimension; i++) {
            edge.add(Coord.get(i, dimension - 1));
        }
        mBottomEdge = edge;

        edge = new ArrayList<Coord>(dimension);
        for (int i = 0; i < dimension; i++) {
            edge.add(Coord.get(0, i));
        }
        mLeftEdge = edge;


        edge = new ArrayList<Coord>(dimension);
        for (int i = 0; i < dimension; i++) {
                edge.add(Coord.get(dimension - 1, i));
        }
        mRightEdge = edge;

        mAllEdges = new ArrayList<Coord>(dimension * 4);
        mAllEdges.addAll(mTopEdge);
        mAllEdges.addAll(mBottomEdge);
        mAllEdges.addAll(mLeftEdge);
        mAllEdges.addAll(mRightEdge);

        mDiagonalCoords = new HashMap<Coord, List<Coord>>(dimension * dimension);
        mAdjacentCoords = new HashMap<Coord, List<Coord>>(dimension * dimension);
        mRankAndFileCoords = new HashMap<Coord, List<List<Coord>>>(dimension * dimension);

        for(int y = 0; y < dimension; y++) {
            for(int x = 0; x < dimension; x++) {
                Coord space = mCoords[y][x];

                List<Coord> diagonal = new ArrayList<Coord>(4);
                // Top row: one of (down right), (down left), (down left, down right)
                if (space.y == 0) {
                    if (space.x == 0) {
                        diagonal.add(Coord.get(space.x + 1, space.y + 1));
                    } else if (space.x == dimension - 1) {
                        diagonal.add(Coord.get(space.x - 1, space.y + 1));
                    } else {
                        diagonal.add(Coord.get(space.x + 1, space.y + 1));
                        diagonal.add(Coord.get(space.x - 1, space.y + 1));
                    }
                }
                // Top row: one of (up right), (up left), (up left, up right)
                else if (space.y == dimension - 1) {
                    if (space.x == 0) {
                        diagonal.add(Coord.get(space.x + 1, space.y - 1));
                    } else if (space.x == dimension - 1) {
                        diagonal.add(Coord.get(space.x - 1, space.y - 1));
                    } else {
                        diagonal.add(Coord.get(space.x + 1, space.y - 1));
                        diagonal.add(Coord.get(space.x - 1, space.y - 1));
                    }
                }
                // Between the top and bottom rows.
                else {
                    // Left edge.
                    if (space.x == 0) {
                        diagonal.add(Coord.get(space.x + 1, space.y - 1));
                        diagonal.add(Coord.get(space.x + 1, space.y + 1));
                    } else if (space.x == dimension - 1) {
                        diagonal.add(Coord.get(space.x - 1, space.y - 1));
                        diagonal.add(Coord.get(space.x - 1, space.y + 1));
                    } else {
                        diagonal.add(Coord.get(space.x + 1, space.y - 1));
                        diagonal.add(Coord.get(space.x + 1, space.y + 1));
                        diagonal.add(Coord.get(space.x - 1, space.y - 1));
                        diagonal.add(Coord.get(space.x - 1, space.y + 1));
                    }
                }

                mDiagonalCoords.put(space, diagonal);

                List<Coord> adjacent = new ArrayList<Coord>(4);
                if (space.x > 0) {
                    adjacent.add(Coord.get(space.x - 1, space.y));
                }
                if (space.x < (dimension - 1)) {
                    adjacent.add(Coord.get(space.x + 1, space.y));
                }
                if (space.y > 0) {
                    adjacent.add(Coord.get(space.x, space.y - 1));
                }
                if (space.y < (dimension - 1)) {
                    adjacent.add(Coord.get(space.x, space.y + 1));
                }

                mAdjacentCoords.put(space, adjacent);

                List<Coord> eastCoords = new ArrayList<Coord>(dimension / 2);
                for (int i = (x + 1); i < dimension; i++) {
                    eastCoords.add(Coord.get(i, space.y));
                }

                List<Coord> westCoords = new ArrayList<Coord>(dimension / 2);
                for (int i = (x - 1); i >= 0; i--) {
                    westCoords.add(Coord.get(i, space.y));
                }

                List<Coord> southCoords = new ArrayList<Coord>(dimension / 2);
                for (int i = (y + 1); i < dimension; i++) {
                    southCoords.add(Coord.get(space.x, i));
                }

                List<Coord> northCoords = new ArrayList<Coord>(dimension / 2);
                for (int i = (y - 1); i >= 0; i--) {
                    northCoords.add(Coord.get(space.x, i));
                }

                List<List<Coord>> rankAndFileCoords = new ArrayList<List<Coord>>(4);
                rankAndFileCoords.add(eastCoords);
                rankAndFileCoords.add(westCoords);
                rankAndFileCoords.add(northCoords);
                rankAndFileCoords.add(southCoords);

                mRankAndFileCoords.put(space, rankAndFileCoords);
            }
        }
    }

    public static Coord getCoordForIndex(int i) {
        return Coord.get(i % mDimension, i / mDimension);
    }

    public static int getIndex(Coord c) {
        return c.y * mDimension + c.x;
    }

    public static List<Coord> getAdjacentSpaces(Coord c) {
        return mAdjacentCoords.get(c);
    }
    public static List<Coord> getDiagonalSpaces(Coord c) {
        return mDiagonalCoords.get(c);
    }

    public static List<List<Coord>> getRankAndFileCoords(Coord c) {
        return mRankAndFileCoords.get(c);
    }

    public static List<Coord> getTopEdge() {
        return mTopEdge;
    }

    public static List<Coord> getBottomEdge() {
        return mBottomEdge;
    }

    public static List<Coord> getLeftEdge() {
        return mLeftEdge;
    }

    public static List<Coord> getRightEdge() {
        return mRightEdge;
    }

    public static List<Coord> getEdgesFlat() {
        return mAllEdges;
    }

    public static List<Coord> getInterveningSpaces(Coord start, Coord finish) {
        List<Coord> interveningSpaces = new ArrayList<>(mDimension);

        int xDiff = Math.abs(start.x - finish.x);
        int yDiff = Math.abs(start.y - finish.y);

        if(xDiff == yDiff && xDiff == 0) {
            return interveningSpaces;
        }
        if(yDiff == 0) {
            List<List<Coord>> rankAndFileCoords = getRankAndFileCoords(start);

            if(finish.x > start.x) {
                for(Coord c : rankAndFileCoords.get(EAST)) {
                    if(!c.equals(finish)) interveningSpaces.add(c);
                    else break;
                }
            }
            else {
                for(Coord c : rankAndFileCoords.get(WEST)) {
                    if(!c.equals(finish)) interveningSpaces.add(c);
                    else break;
                }
            }
        }
        else if(xDiff == 0) {
            List<List<Coord>> rankAndFileCoords = getRankAndFileCoords(start);

            if(finish.y > start.y) {
                for(Coord c : rankAndFileCoords.get(SOUTH)) {
                    if(!c.equals(finish)) interveningSpaces.add(c);
                    else break;
                }
            }
            else {
                for(Coord c : rankAndFileCoords.get(NORTH)) {
                    if(!c.equals(finish)) interveningSpaces.add(c);
                    else break;
                }
            }
        }

        return interveningSpaces;
    }

    public String toString() {
        Map<String, String> chessNotation = Board.getChessNotation(this);
        return chessNotation.get("file") + chessNotation.get("rank");
    }

    public static Coord get(int x, int y) {
        return mCoords[y][x];
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Coord && this.x == ((Coord) other).x && this.y == ((Coord) other).y;
    }

    @Override
    public int hashCode() {
        return (this.x * 32 - this.x) + this.y;
    }
}
