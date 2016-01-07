package com.manywords.softworks.tafl.rules;

import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.TaflmanCoordMap;

import java.util.*;

public abstract class BoardImpl extends Board {
    public BoardImpl() {
        Coord.initialize(getBoardDimension());
        mBoardArray = new char[getBoardDimension() * getBoardDimension()];

        for (int y = 0; y < getBoardDimension(); y++) {
            mBoardArray[y] = 0;
        }

        int center = (getBoardDimension() - 1) / 2;
        mCenterSpace = Coord.get(center, center);

        Coord topLeft = Coord.get(0, 0);
        Coord topRight = Coord.get(getBoardDimension() - 1, 0);
        Coord bottomLeft = Coord.get(0, getBoardDimension() - 1);
        Coord bottomRight = Coord.get(getBoardDimension() - 1, getBoardDimension() - 1);

        mCorners = new ArrayList<Coord>(4);
        mCorners.add(topLeft);
        mCorners.add(topRight);
        mCorners.add(bottomLeft);
        mCorners.add(bottomRight);
    }

    public BoardImpl(Board board) {
        mBoardArray = new char[getBoardDimension() * getBoardDimension()];

        System.arraycopy(board.getBoard(), 0, mBoardArray, 0, getBoardDimension() * getBoardDimension());

        mCenterSpace = board.getCenterSpace();
        mCorners = board.getCorners();
        if (board.getCachedTaflmanLocations() == null && getState() != null) {
            initializeTaflmanLocations();
        } else if(board.getCachedTaflmanLocations() != null){
            mCachedTaflmanLocations = new TaflmanCoordMap(board.getCachedTaflmanLocations());
        }
    }

    // [y][x]
    private char[] mBoardArray;
    //TODO: privatize
    public TaflmanCoordMap mCachedTaflmanLocations = null;
    private Coord mCenterSpace;
    private List<Coord> mCorners;
    private Rules mRules;
    private GameState mState;

    @Override
    public void setupTaflmen(Side attackers, Side defenders) {
        for (Side.TaflmanHolder taflman : attackers.getStartingTaflmen()) {
            mBoardArray[getIndex(taflman.coord)] = taflman.packed;
        }

        for (Side.TaflmanHolder taflman : defenders.getStartingTaflmen()) {
            mBoardArray[getIndex(taflman.coord)] = taflman.packed;
        }

        initializeTaflmanLocations();
    }

    private void initializeTaflmanLocations() {
        byte defenderCount = (byte) getState().mGame.getGameRules().howManyDefenders();
        byte attackerCount = (byte) getState().mGame.getGameRules().howManyAttackers();
        mCachedTaflmanLocations = new TaflmanCoordMap(attackerCount, defenderCount);
        for (int i = 0; i < mBoardArray.length; i++) {
            if (mBoardArray[i] != Taflman.EMPTY) {
                mCachedTaflmanLocations.put(mBoardArray[i], Coord.getCoordForIndex(i));
            }
        }
    }

    public int getIndex(Coord c) {
        return c.y * getBoardDimension() + c.x;
    }

    public int getIndex(int x, int y) {
        return y * getBoardDimension() + x;
    }

    public Coord findTaflmanSpace(char taflman) {
        return mCachedTaflmanLocations.get(taflman);
    }

    public List<Character> getTaflmenWithMask(char mask, char value) {
        List<Character> taflmen = new ArrayList<Character>();

        for(char taflman : mCachedTaflmanLocations.getTaflmen()) {
            if(taflman != Taflman.EMPTY && (taflman & mask) == value) {
                taflmen.add(taflman);
            }
        }

        return taflmen;
    }

    @Override
    public char[] getBoard() {
        return mBoardArray;
    }

    @Override
    public abstract int getBoardDimension();

    @Override
    public Coord getCenterSpace() {
        return mCenterSpace;
    }

    @Override
    public List<Coord> getCorners() {
        return mCorners;
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
    public SpaceGroup getSpaceGroupFor(Coord space) {
        if (getCenterSpace().equals(space)) {
            return SpaceGroup.THRONE;
        }
        for (Coord corner : getCorners()) {
            if (corner.equals(space)) {
                return SpaceGroup.CORNER;
            }
        }

        return SpaceGroup.NONE;
    }

    @Override
    public List<Coord> getAdjacentSpaces(Coord space) {
        return Coord.getAdjacentSpace(space);
    }

    @Override
    public List<Character> getAdjacentNeighbors(Coord space) {
        List<Coord> spaces = getAdjacentSpaces(space);
        List<Character> neighbors = new ArrayList<Character>(4);

        for (Coord adjacent : spaces) {
            char occupier = getOccupier(adjacent);
            if (occupier != 0) neighbors.add(occupier);
        }

        return neighbors;
    }

    @Override
    public List<Coord> getAdjacentImpassableSpaces(char taflman) {
        List<Coord> impassableSpaces = new ArrayList<Coord>(4);

        if (Taflman.getCurrentSpace(mState, taflman) != null) {
            for (Coord adjacent : getAdjacentSpaces(Taflman.getCurrentSpace(mState, taflman))) {
                if (!getRules().canTaflmanMoveThrough(this, taflman, adjacent)) {
                    impassableSpaces.add(adjacent);
                }
            }
        }

        return impassableSpaces;
    }

    @Override
    public List<Coord> getDiagonalSpaces(Coord space) {
        return Coord.getDiagonalSpaces(space);
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
    public void setState(GameState state) {
        mState = state;
    }

    @Override
    public GameState getState() {
        return mState;
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
    public void setOccupier(Coord space, char taflman) {
        char originalTaflman = mBoardArray[getIndex(space)];
        mBoardArray[getIndex(space)] = taflman;
        if(taflman != Taflman.EMPTY) {
            mCachedTaflmanLocations.put(taflman, space);
        }
        else {
            mCachedTaflmanLocations.remove(originalTaflman);
        }
    }

    @Override
    public char getOccupier(int x, int y) {
        return getOccupier(Coord.get(x, y));
    }

    @Override
    public char getOccupier(Coord coord) {
        return mBoardArray[getIndex(coord)];
    }

    @Override
    public TaflmanCoordMap getCachedTaflmanLocations() {
        return mCachedTaflmanLocations;
    }

    @Override
    public boolean isSpaceHostileTo(Coord space, char taflman) {
        // If the space contains a piece from a different side, it's
        // hostile.
        char occupier = getOccupier(space);
        if (occupier != 0 && Taflman.getPackedSide(occupier) != Taflman.getPackedSide(taflman)) {

            // The exception is if the hostile piece is a king, and that king
            // is unarmed.
            if (Taflman.isKing(occupier) && !getRules().isKingArmed()) {
                return false;
            } else {
                return true;
            }
        }

        // If this space isn't naturally hostile, then this isn't a hostile space.
        return getRules().isSpaceHostileToSide(this, space, Taflman.getSide(taflman));
    }

    /**
     * Encircled in tafl means prevented from reaching the edges by the other
     * side.
     *
     * @return
     */

    @Override
    public boolean isSideEncircled(Side side) {
        // Start at the edges.
        List<Coord> edges = getEdgesFlat();
        Set<Coord> considered = new HashSet<Coord>(getBoardDimension() * getBoardDimension());
        Set<Coord> toExplore = new HashSet<Coord>(getBoardDimension() * 4);
        toExplore.addAll(edges);

        // Flow inward.
        while (toExplore.size() > 0) {
            Coord consider = null;

            // get the first element.
            for (Coord coord : toExplore) {
                consider = coord;
                break;
            }

            toExplore.remove(consider);
            considered.add(consider);
            char taflman = getOccupier(consider);

            // If this space contains a taflman, then we don't need to consider any of its neighbors.
            // All of its neighbors outside the potential encirclement will be snagged by the spaces
            // adjacent to the one that added this space in the first place.
            if (taflman != Taflman.EMPTY) {
                // If this space contains a taflman of the side in question, then the side can be
                // reached from an edge, and isn't surrounded.
                if (Taflman.getSide(taflman).isAttackingSide() == side.isAttackingSide()) return false;
            } else {
                // If this space does not contain a taflman, add all of its neighbors which aren't scheduled
                // for consideration and haven't already been considered.
                List<Coord> neighbors = getAdjacentSpaces(consider);
                for (Coord neighbor : neighbors) {
                    if (!considered.contains(neighbor) && !toExplore.contains(neighbor)) {
                        toExplore.add(neighbor);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public List<ShieldwallPosition> detectShieldwallPositionsForSide(Side side) {
        // Algorithm thought: the simplest way to find shieldwalls is to search
        // exhaustively.
        //
        // 1. For each edge space from 0 to dimension:
        //     2. Is occupied by side or corner, and no start position?
        //     3. Then mark as start position and continue.
        //     4. Is occupied by side and start position not null?
        //         5. Is positive neighbor *and* edge-adjacent space occupied by side?
        //         6. Then add space to surrounded list and continue
        //         7. Special case: if in a shieldwall and king,
        //         7. Else mark end space and stop
        //             8. Set start position to end space
        //             9. Is surrounded list size > 1?
        //            10. Then add to shieldwall positions list
        //    11. Is edge-adjacent space occupied by side?
        //    12. Then add space to surrounded list and continue.
        //    13. Else clear start space and surrounded spaces list.
        List<ShieldwallPosition> shieldwallPositions = new ArrayList<ShieldwallPosition>();

        Coord startPosition = null;
        Coord endPosition = null;
        int direction = DIRECTION_X;
        int index = 0;
        List<Coord> surroundedSpaces = new ArrayList<Coord>();
        List<Character> surroundingTaflmen = new ArrayList<Character>();

        List<List<Coord>> edges = getEdges();

        for (List<Coord> edge : edges) {
            startPosition = null;
            endPosition = null;
            if (edge.contains(Coord.get(0, 1)) || edge.contains(Coord.get(getBoardDimension() - 1, 1))) {
                direction = DIRECTION_Y;
            } else {
                direction = DIRECTION_X;
            }
            index = 0;

            for (Coord space : edge) {
                // If the space is a corner and start position is null, this is a potential
                // start position.
                if (startPosition == null && getCorners().contains(space)
                        && getRules().allowShieldWallCaptures() == Rules.STRONG_SHIELDWALL) {
                    startPosition = space;
                    continue;
                }
                // If start position is null and this space is occupied, this is a potential
                // start position.
                else if (startPosition == null && getOccupier(space) != 0 && Taflman.getSide(getOccupier(space)) == side) {
                    startPosition = space;
                    surroundingTaflmen.add(getOccupier(space));
                    continue;
                }

                // If start position isn't null, check to see if the position has ended.
                if (startPosition != null) {
                    // STRONG_SHIELDWALL means corners can cap shieldwalls.
                    if (getCorners().contains(space)
                            && getRules().allowShieldWallCaptures() == Rules.STRONG_SHIELDWALL) {
                        endPosition = space;

                        if (surroundedSpaces.size() > 1) {
                            shieldwallPositions.add(new ShieldwallPosition(surroundedSpaces, surroundingTaflmen));
                        }

                        break;
                    }

                    // If this space is occupied by a friendly, it's close to the end
                    // of the shieldwall, for our purposes.
                    // There are special cases for shieldwalls of this form, with stars being pieces:
                    // ** *
                    //  **
                    // (That is, shieldwalls surrounding two spaces, one of which contains a friendly
                    // piece.) We don't need to catch bigger arrangements like that; any larger
                    // 'imperfect shieldwall' of this sort just look like the minimum size shieldwall,
                    // and that's okay from a rules perspective.
                    if (getOccupier(space) != 0 && Taflman.getPackedSide(getOccupier(space)) == side.getSideChar()) {
                        if (getOccupier(edge.get(index + 1)) != 0
                                && Taflman.getSide(getOccupier(edge.get(index + 1))) == side
                                && getOccupier(getEdgeAdjacentSpace(direction, space)) != 0
                                && Taflman.getSide(getOccupier(getEdgeAdjacentSpace(direction, space))) == side) {
                            surroundingTaflmen.add(getOccupier(getEdgeAdjacentSpace(direction, space)));
                            surroundedSpaces.add(space);
                            continue;
                        } else if (getOccupier(space) != 0 && Taflman.isKing(getOccupier(space))
                                && getOccupier(getEdgeAdjacentSpace(direction, space)) != 0
                                && Taflman.getSide(getOccupier(getEdgeAdjacentSpace(direction, space))) == side) {
                            surroundingTaflmen.add(getOccupier(getEdgeAdjacentSpace(direction, space)));
                            surroundedSpaces.add(space);
                            continue;
                        } else {
                            endPosition = space;
                            if (surroundedSpaces.size() > 1) {
                                surroundingTaflmen.add(getOccupier(space));
                                shieldwallPositions.add(new ShieldwallPosition(surroundedSpaces, surroundingTaflmen));
                            }

                            startPosition = space;
                            endPosition = null;
                            surroundingTaflmen = new ArrayList<Character>();
                            surroundedSpaces = new ArrayList<Coord>();
                            continue;
                        }
                    }

                    if (getOccupier(getEdgeAdjacentSpace(direction, space)) != 0
                            && Taflman.getSide(getOccupier(getEdgeAdjacentSpace(direction, space))) == side) {
                        surroundingTaflmen.add(getOccupier(getEdgeAdjacentSpace(direction, space)));
                        surroundedSpaces.add(space);
                    } else {
                        startPosition = null;
                        endPosition = null;
                        surroundingTaflmen = new ArrayList<Character>();
                        surroundedSpaces = new ArrayList<Coord>();
                    }
                }

                index++;
            }
        }

        return shieldwallPositions;
    }

    public List<List<Coord>> getEdges() {
        List<List<Coord>> edges = new ArrayList<List<Coord>>(4);
        edges.add(getTopEdge());
        edges.add(getBottomEdge());
        edges.add(getLeftEdge());
        edges.add(getRightEdge());

        return edges;
    }

    public List<Coord> getEdgesFlat() {
        return Coord.getEdgesFlat();
    }

    @Override
    public List<Coord> getTopEdge() {
        return Coord.getTopEdge();
    }

    @Override
    public List<Coord> getBottomEdge() {
        return Coord.getBottomEdge();
    }

    @Override
    public List<Coord> getLeftEdge() {
        return Coord.getLeftEdge();
    }

    @Override
    public List<Coord> getRightEdge() {
        return Coord.getRightEdge();
    }

    public static final int DIRECTION_X = 0;
    public static final int DIRECTION_Y = 1;

    public Coord getEdgeAdjacentSpace(int direction, Coord space) {
        if (!isEdgeSpace(space)) {
            return null;
        }

        if (direction == DIRECTION_X) {
            if (space.y == 0) {
                return Coord.get(space.x, 1);
            } else {
                return Coord.get(space.x, getBoardDimension() - 2);
            }
        } else {
            if (space.x == 0) {
                return Coord.get(1, space.y);
            } else {
                return Coord.get(getBoardDimension() - 2, space.y);
            }
        }
    }

    @Override
    public abstract Board deepCopy();
}
