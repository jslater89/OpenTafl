package com.manywords.softworks.tafl.rules;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.collections.TaflmanCoordMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jay on 4/13/17.
 */
public class VariantEditorBoardImpl extends Board {
    private List<Side.TaflmanHolder> mAttackers;
    private List<Side.TaflmanHolder> mDefenders;

    private int mSize;

    private Rules mRules;

    public VariantEditorBoardImpl(int boardSize) {
        mSize = boardSize;
    }

    @Override
    public void setupTaflmen(Side attackers, Side defenders) {
        mAttackers = attackers.getStartingTaflmen();
        mDefenders = defenders.getStartingTaflmen();
    }

    @Override
    public int getBoardDimension() {
        return mSize;
    }

    @Override
    public char[][] getBoardArray() {
        char[][] board = new char[getBoardDimension()][getBoardDimension()];

        for(int y = 0; y < getBoardDimension(); y++) {
            for(int x = 0; x < getBoardDimension(); x++) {
                board[y][x] = getOccupier(x, y);
            }
        }

        return board;
    }

    private List<Side.TaflmanHolder> getAllTaflmen() {
        List<Side.TaflmanHolder> allTaflmen = new ArrayList<>();
        allTaflmen.addAll(mAttackers);
        allTaflmen.addAll(mDefenders);

        return allTaflmen;
    }

    @Override
    public char getOccupier(int x, int y) {
        for(Side.TaflmanHolder t : getAllTaflmen()) {
            if(t.coord != null && t.coord.x == x && t.coord.y == y) return t.packed;
        }

        return Taflman.EMPTY;
    }

    @Override
    public char getOccupier(Coord space) {
        return getOccupier(space.x, space.y);
    }

    @Override
    public Coord findTaflmanSpace(char taflman) {
        return null;
    }

    @Override
    public TaflmanCoordMap getCachedTaflmanLocations() {
        return null;
        //return new VariantEditorTaflmanCoordMap(mAttackers, mDefenders, mSize);
    }

    @Override
    public List<Character> getTaflmenWithMask(char mask, char value) {
        return new ArrayList<>();
    }

    @Override
    public void setOccupier(Coord space, char taflman) {
        Side.TaflmanHolder toRemove = null;
        for(Side.TaflmanHolder t : getAllTaflmen()) {
            if(space.equals(t.coord)) {
                toRemove = t;
                break;
            }
        }

        mAttackers.remove(toRemove);
        mDefenders.remove(toRemove);

        Side.TaflmanHolder h = new Side.TaflmanHolder(taflman, space);
        if(Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS) {
            mAttackers.add(h);
        }
        else {
            mDefenders.add(h);
        }
    }

    @Override
    public void unsetOccupier(char taflman) {

    }

    @Override
    public boolean isEdgeSpace(Coord space) {
        if ((space.x == 0) || (space.x == getBoardDimension() - 1) ||
                ((space.y == 0 || space.y == getBoardDimension() - 1))) {
            return true;
        }

        return false;
    }

    @Override
    public List<Coord> getCenterAndAdjacentSpaces() {
        Set<Coord> spaces = new HashSet<>(5);
        for(Coord space : getRules().getCenterSpaces()) {
            spaces.add(space);
            spaces.addAll(getAdjacentSpaces(space));
        }

        return new ArrayList<>(spaces);
    }

    @Override
    public SpaceType getSpaceTypeFor(Coord space) {
        return getRules().getSpaceTypeFor(space);
    }

    @Override
    public List<Coord> getAdjacentSpaces(Coord space) {
        return Coord.getAdjacentSpaces(getBoardDimension(), space);
    }

    @Override
    public List<Character> getAdjacentNeighbors(Coord space) {
        List<Coord> spaces = getAdjacentSpaces(space);
        List<Character> neighbors = new ArrayList<Character>(4);

        for (Coord adjacent : spaces) {
            char occupier = getOccupier(adjacent);
            if (occupier != Taflman.EMPTY) neighbors.add(occupier);
        }

        return neighbors;
    }


    @Override
    public List<Coord> getAdjacentImpassableSpaces(char taflman) {
        return null;
    }

    @Override
    public List<Coord> getDiagonalSpaces(Coord space) {
        return Coord.getDiagonalSpaces(getBoardDimension(), space);
    }

    @Override
    public List<Character> getDiagonalNeighbors(Coord space) {
        List<Coord> spaces = getDiagonalSpaces(space);
        List<Character> neighbors = new ArrayList<Character>(4);

        for (Coord diagonal : spaces) {
            char occupier = getOccupier(diagonal);
            if (occupier != 0) neighbors.add(occupier);
        }

        return neighbors;
    }

    @Override
    public Rules getRules() {
        return mRules;
    }

    @Override
    public void setRules(Rules rules) {
        mRules = rules;
    }

    @Override
    public GameState getState() {
        return null;
    }

    @Override
    public void setState(GameState state) {

    }

    @Override
    public boolean isSpaceHostileTo(Coord space, char taflman) {
        return false;
    }

    @Override
    public boolean isSideEncircled(Side side) {
        return false;
    }

    @Override
    public List<ShieldwallPosition> detectShieldwallPositionsForSide(Side surrounders, Side surrounded) {
        return null;
    }

    @Override
    public List<Coord> getTopEdge() {
        return Coord.getTopEdge(getBoardDimension());
    }

    @Override
    public List<Coord> getBottomEdge() {
        return Coord.getBottomEdge(getBoardDimension());
    }

    @Override
    public List<Coord> getLeftEdge() {
        return Coord.getLeftEdge(getBoardDimension());
    }

    @Override
    public List<Coord> getRightEdge() {
        return Coord.getRightEdge(getBoardDimension());
    }

    @Override
    public Board deepCopy() {
        // Trololol
        return this;
    }
}
