package com.manywords.softworks.tafl.rules;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.collections.TaflmanCoordMap;

import java.util.List;

public abstract class Board {
    /**
     * Set up taflmen in their starting positions.
     */
    public abstract void setupTaflmen(Side attackers, Side defenders);

    public abstract int getIndex(Coord c);
    public abstract int getIndex(int x, int y);

    /**
     * Get the board's edge length.
     *
     * @return
     */
    public abstract int getBoardDimension();

    public abstract char[][] getBoardArray();

    public abstract char getOccupier(int x, int y);

    public abstract char getOccupier(Coord space);

    public abstract Coord findTaflmanSpace(char taflman);

    public abstract TaflmanCoordMap getCachedTaflmanLocations();

    public abstract List<Character> getTaflmenWithMask(char mask, char value);

    /**
     * Set the occupier of a certain coordinate to the given, non-empty taflman.
     */
    public abstract void setOccupier(Coord space, char taflman);

    /**
     * Remove a taflman from the game.
     */
    public abstract void unsetOccupier(char taflman);

    /**
     * Get the space group to which this space belongs,
     * or null.
     *
     * @return
     */
    public abstract SpaceType getSpaceTypeFor(Coord space);

    /**
     * Is this space an edge?
     *
     * @return
     */
    public abstract boolean isEdgeSpace(Coord space);

    public abstract List<Coord> getCenterAndAdjacentSpaces();

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

    public abstract List<ShieldwallPosition> detectShieldwallPositionsForSide(Side surrounders, Side surrounded);

    public abstract List<Coord> getTopEdge();

    public abstract List<Coord> getBottomEdge();

    public abstract List<Coord> getLeftEdge();

    public abstract List<Coord> getRightEdge();

    /**
     * Get a copy of this board for manipulation.
     *
     * @return
     */
    public abstract Board deepCopy();
}
