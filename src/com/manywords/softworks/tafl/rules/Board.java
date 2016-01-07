package com.manywords.softworks.tafl.rules;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.collections.TaflmanCoordMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class Board {
    /**
     * Set up taflmen in their starting positions.
     */
    public abstract void setupTaflmen(Side attackers, Side defenders);

    /**
     * Get the board's backing array.
     *
     * @return
     */
    public abstract char[] getBoard();
    public abstract int getIndex(Coord c);
    public abstract int getIndex(int x, int y);

    /**
     * Get the board's edge length.
     *
     * @return
     */
    public abstract int getBoardDimension();

    public abstract char getOccupier(int x, int y);

    public abstract char getOccupier(Coord space);

    public abstract Coord findTaflmanSpace(char taflman);

    public abstract TaflmanCoordMap getCachedTaflmanLocations();

    public abstract List<Character> getTaflmenWithMask(char mask, char value);

    /**
     * Set the occupier of a certain coordinate to the given taflman.
     */
    public abstract void setOccupier(Coord space, char taflman);

    /**
     * Get the square at the center of the board.
     *
     * @return
     */
    public abstract Coord getCenterSpace();

    /**
     * Get the squares or groups of squares at the
     * corners.
     *
     * @return
     */
    public abstract List<Coord> getCorners();

    /**
     * Get the space group to which this space belongs,
     * or null.
     *
     * @return
     */
    public abstract SpaceGroup getSpaceGroupFor(Coord space);

    /**
     * Is this space an edge?
     *
     * @return
     */
    public abstract boolean isEdgeSpace(Coord space);

    /**
     * Get the up-to-four squares immediately adjacent
     * to this one.
     *
     * @return
     */
    public abstract List<Coord> getAdjacentSpaces(Coord space);

    public abstract List<Character> getAdjacentNeighbors(Coord space);

    /**
     * Get a list of impassable spaces adjacent to the given taflman,
     * or an empty list.
     *
     * @param taflman
     * @return
     */
    public abstract List<Coord> getAdjacentImpassableSpaces(char taflman);

    /**
     * Get diagonally-adjacent spaces to this space.
     *
     * @param space
     * @return
     */
    public abstract List<Coord> getDiagonalSpaces(Coord space);

    public abstract List<Character> getDiagonalNeighbors(Coord space);

    /**
     * Get the rules object representing this game and its starting position.
     *
     * @return
     */
    public abstract Rules getRules();

    public abstract void setRules(Rules rules);

    public abstract GameState getState();

    public abstract void setState(GameState state);

    //public abstract Coord getSpaceAt(int x, int y);

    public abstract boolean isSpaceHostileTo(Coord space, char taflman);

    public abstract boolean isSideEncircled(Side side);

    public abstract List<ShieldwallPosition> detectShieldwallPositionsForSide(Side side);

    public abstract List<Coord> getTopEdge();

    public abstract List<Coord> getBottomEdge();

    public abstract List<Coord> getLeftEdge();

    public abstract List<Coord> getRightEdge();

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

    public static Map<String, String> getChessNotationFromCoords(int x, int y) {
        String file = "" + (char) (((int) 'a') + x);
        String rank = "" + (y + 1);

        Map<String, String> spaceString = new HashMap<String, String>();
        spaceString.put("rank", rank);
        spaceString.put("file", file);

        return spaceString;
    }

    public static Map<String, Integer> getCoordsFromChessNotation(String chess) {
        Map<String, Integer> coord = new HashMap<String, Integer>();

        int file = (int) chess.toCharArray()[0] - (int) 'a';
        if (file < 0 || file > 16) {
            throw new IllegalArgumentException("No support for chess notations with more than 17 ranks");
        }

        String fileString = chess.substring(1);
        int rank = (Integer.parseInt(fileString) - 1);

        coord.put("y", rank);
        coord.put("x", file);

        return coord;
    }

    /**
     * Get a copy of this board for manipulation.
     *
     * @return
     */
    public abstract Board deepCopy();
}
