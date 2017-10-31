package com.manywords.softworks.tafl.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    private static boolean initialized = false;
    private static Coord[][] coords;
    private static final int minDimension = 5;
    private static final int maxDimension = 19;
    // Map sizes to a list of lists of adjacent coords, indexed by coord index
    private static Map<Integer, List<List<Coord>>> adjacentCoords;
    // Map sizes to a list of lists of diagonal coords, indexed by coord index
    private static Map<Integer, List<List<Coord>>> diagonalCoords;
    // Map sizes to a list of lists of lists of rank/file coord, indexed by coord index
    private static Map<Integer, List<List<List<Coord>>>> rankAndFileCoords;
    private static Map<Integer, List<Coord>> topEdge;
    private static Map<Integer, List<Coord>> bottomEdge;
    private static Map<Integer, List<Coord>> leftEdge;
    private static Map<Integer, List<Coord>> rightEdge;
    private static Map<Integer, List<Coord>> allEdges;

    public Coord offset(int dimension, int offsetX, int offsetY) {
        int newX = x + offsetX;
        if(newX < 0) newX = 0;
        if(newX >= dimension) newX = dimension - 1;

        int newY = y + offsetY;
        if(newY < 0) newY = 0;
        if(newY >= dimension) newY = dimension - 1;

        return Coord.get(newX, newY);
    }

    public static void initialize() {
        if(initialized) return;
        initialized = true;

        coords = new Coord[maxDimension][maxDimension];

        for (int y = 0; y < maxDimension; y++) {
            for (int x = 0; x < maxDimension; x++) {
                coords[y][x] = new Coord(x, y);
            }
        }


        leftEdge = new HashMap<>();
        rightEdge = new HashMap<>();
        topEdge = new HashMap<>();
        bottomEdge = new HashMap<>();
        allEdges = new HashMap<>();

        setupEdges();

        diagonalCoords = new HashMap<>(maxDimension * maxDimension);
        adjacentCoords = new HashMap<>(maxDimension * maxDimension);
        rankAndFileCoords = new HashMap<>(maxDimension * maxDimension);

        setupAdjacentCoords();
    }

    private static void setupEdges() {
        for(int dimension = minDimension; dimension <= maxDimension; dimension+=2) {
            ArrayList<Coord> edge = new ArrayList<>(dimension);
            for (int i = 0; i < dimension; i++) {
                edge.add(Coord.get(i, 0));
            }
            topEdge.put(dimension, edge);

            edge = new ArrayList<>(dimension);
            for (int i = 0; i < dimension; i++) {
                edge.add(Coord.get(i, dimension - 1));
            }
            bottomEdge.put(dimension, edge);

            edge = new ArrayList<>(dimension);
            for (int i = 0; i < dimension; i++) {
                edge.add(Coord.get(0, i));
            }
            leftEdge.put(dimension, edge);


            edge = new ArrayList<>(dimension);
            for (int i = 0; i < dimension; i++) {
                edge.add(Coord.get(dimension - 1, i));
            }
            rightEdge.put(dimension, edge);

            List<Coord> edges = new ArrayList<>();
            edges.addAll(topEdge.get(dimension));
            edges.addAll(bottomEdge.get(dimension));
            edges.addAll(leftEdge.get(dimension));
            edges.addAll(rightEdge.get(dimension));
            allEdges.put(dimension, edges);
        }
    }

    private static void setupAdjacentCoords() {
        for(int dimension = minDimension; dimension <= maxDimension; dimension+=2) {
            List<List<Coord>> diagonals = new ArrayList<>();
            List<List<Coord>> adjacents = new ArrayList<>();
            List<List<List<Coord>>> rankAndFiles = new ArrayList<>();

            for (int y = 0; y < dimension; y++) {
                for (int x = 0; x < dimension; x++) {

                    Coord space = coords[y][x];

                    List<Coord> diagonal = new ArrayList<Coord>(4);
                    // Top row: one of (down right), (down left), (down left, down right)
                    if (space.y == 0) {
                        if (space.x == 0) {
                            diagonal.add(Coord.get(space.x + 1, space.y + 1));
                        }
                        else if (space.x == dimension - 1) {
                            diagonal.add(Coord.get(space.x - 1, space.y + 1));
                        }
                        else {
                            diagonal.add(Coord.get(space.x + 1, space.y + 1));
                            diagonal.add(Coord.get(space.x - 1, space.y + 1));
                        }
                    }
                    // Top row: one of (up right), (up left), (up left, up right)
                    else if (space.y == dimension - 1) {
                        if (space.x == 0) {
                            diagonal.add(Coord.get(space.x + 1, space.y - 1));
                        }
                        else if (space.x == dimension - 1) {
                            diagonal.add(Coord.get(space.x - 1, space.y - 1));
                        }
                        else {
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
                        }
                        else if (space.x == dimension - 1) {
                            diagonal.add(Coord.get(space.x - 1, space.y - 1));
                            diagonal.add(Coord.get(space.x - 1, space.y + 1));
                        }
                        else {
                            diagonal.add(Coord.get(space.x + 1, space.y - 1));
                            diagonal.add(Coord.get(space.x + 1, space.y + 1));
                            diagonal.add(Coord.get(space.x - 1, space.y - 1));
                            diagonal.add(Coord.get(space.x - 1, space.y + 1));
                        }
                    }

                    diagonals.add(diagonal);

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

                    adjacents.add(adjacent);

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

                    rankAndFiles.add(rankAndFileCoords);
                }
            }

            adjacentCoords.put(dimension, adjacents);
            diagonalCoords.put(dimension, diagonals);
            rankAndFileCoords.put(dimension, rankAndFiles);
        }
    }

    public static Coord getCoordForIndex(int dimension, int i) {
        if(i % dimension == 3449 || i / dimension == 3449) {
            System.out.println(i);
            System.out.println(dimension);
        }
        return Coord.get(i % dimension, i / dimension);
    }

    public static int getIndex(int dimension, Coord c) {
        return c.y * dimension + c.x;
    }

    public static List<Coord> getAdjacentSpaces(int dimension, Coord c) {
        return adjacentCoords.get(dimension).get(getIndex(dimension, c));
    }
    public static List<Coord> getDiagonalSpaces(int dimension, Coord c) {
        return diagonalCoords.get(dimension).get(getIndex(dimension, c));
    }

    public static Coord getCoordAcrossFrom(int dimension, Coord root, Coord opposite) {
        int xDiff = Math.abs(root.x - opposite.x);
        int yDiff = Math.abs(root.y - opposite.y);

        if(xDiff == 0 && yDiff == 0) return root;

        if(yDiff == 0) {
            int newX = -1;
            if(xDiff == 1) newX = root.x + 1;
            else if(xDiff == -1) newX = root.x - 1;

            if(newX < 0 || newX >= dimension) return null;
            else return Coord.get(newX, root.y);
        }
        else if(xDiff == 0) {
            int newY = -1;
            if(yDiff == 1) newY = root.y + 1;
            else if(yDiff == -1) newY = root.y - 1;

            if(newY < 0 || newY >= dimension) return null;
            else return Coord.get(root.x, newY);
        }
        return null;
    }

    public static List<List<Coord>> getRankAndFileCoords(int dimension, Coord c) {
        if(c == null) throw new IllegalStateException();
        return rankAndFileCoords.get(dimension).get(getIndex(dimension, c));
    }

    public static List<Coord> getTopEdge(int dimension) {
        return topEdge.get(dimension);
    }

    public static List<Coord> getBottomEdge(int dimension) {
        return bottomEdge.get(dimension);
    }

    public static List<Coord> getLeftEdge(int dimension) {
        return leftEdge.get(dimension);
    }

    public static List<Coord> getRightEdge(int dimension) {
        return rightEdge.get(dimension);
    }

    public static List<Coord> getEdgesFlat(int dimension) {
        return allEdges.get(dimension);
    }

    public static List<Coord> getInterveningSpaces(int dimension, Coord start, Coord finish) {
        List<Coord> interveningSpaces = new ArrayList<>(maxDimension);

        int xDiff = Math.abs(start.x - finish.x);
        int yDiff = Math.abs(start.y - finish.y);

        if(xDiff == yDiff && xDiff == 0) {
            return interveningSpaces;
        }
        if(yDiff == 0) {
            List<List<Coord>> rankAndFileCoords = getRankAndFileCoords(dimension, start);

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
            List<List<Coord>> rankAndFileCoords = getRankAndFileCoords(dimension, start);

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

    public static boolean validateChessNotation(String chess, int boardDimension) {
        if (!Pattern.matches("[a-s][1-9][0-9]?", chess)) return false;

        int file = (int) chess.toCharArray()[0] - (int) 'a';
        if (file < 0 || file >= boardDimension) return false;

        String fileString = chess.substring(1);
        int rank = (Integer.parseInt(fileString) - 1);
        if (rank < 0 || rank >= boardDimension) return false;

        return true;
    }

    public static Map<String, String> getChessNotation(Coord space) {
        return getChessNotationFromCoords(space.x, space.y);
    }

    public static String getChessString(Coord space) {
        Map<String, String> map = getChessNotation(space);
        return map.get("file") + map.get("rank");
    }

    public static Map<String, String> getChessNotationFromCoords(int x, int y) {
        String file = "" + (char) (((int) 'a') + x);
        String rank = "" + (y + 1);

        Map<String, String> spaceString = new HashMap<String, String>();
        spaceString.put("rank", rank);
        spaceString.put("file", file);

        return spaceString;
    }

    public static Map<String, Integer> getCoordMapFromChessNotation(String chess) {
        Map<String, Integer> coord = new HashMap<String, Integer>();

        int file = (int) chess.toCharArray()[0] - (int) 'a';
        if (file < 0 || file > 18) {
            throw new IllegalArgumentException("No support for chess notations with more than 19 ranks");
        }

        String fileString = chess.substring(1);
        int rank = (Integer.parseInt(fileString) - 1);

        coord.put("y", rank);
        coord.put("x", file);

        return coord;
    }

    public static Coord get(String chess) {
        Map<String, Integer> coordMap = getCoordMapFromChessNotation(chess);

        return get(coordMap.get("x"), coordMap.get("y"));
    }

    public String toString() {
        Map<String, String> chessNotation = getChessNotation(this);
        return chessNotation.get("file") + chessNotation.get("rank");
    }

    public static Coord get(int x, int y) {
        return coords[y][x];
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
