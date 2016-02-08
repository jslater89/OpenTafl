package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.engine.ai.GameTreeState;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public GameState(Game game, Rules startingRules) {
        mGameLength = 0;
        mBoard = startingRules.getBoard().deepCopy();
        mAttackers = startingRules.getAttackers().deepCopy(mBoard);
        mDefenders = startingRules.getDefenders().deepCopy(mBoard);
        mGame = game;

        mBoard.setState(this);
        mBoard.setupTaflmen(mAttackers, mDefenders);

        if (mBoard.getRules().getStartingSide().isAttackingSide()) {
            mCurrentSide = mAttackers;
        } else {
            mCurrentSide = mDefenders;
        }

        mZobristHash = zobristHash();
        mTaflmanMoveCache = new TaflmanMoveCache(mZobristHash, (byte) startingRules.howManyAttackers(), (byte) startingRules.howManyDefenders());
    }

    public GameState(Game game, GameState previousState, Board board, Side attackers, Side defenders, boolean updateZobrist) {
        this(game, previousState, board, attackers, defenders, updateZobrist, true);
    }

    public GameState(GameState copyState) {
        mGameLength = copyState.mGameLength;
        mBoard = copyState.getBoard().deepCopy();
        mBoard.setState(this);
        mAttackers = copyState.getAttackers().deepCopy(mBoard);
        mDefenders = copyState.getDefenders().deepCopy(mBoard);
        mZobristHash = copyState.mZobristHash;
        mGame = copyState.mGame;
        mCurrentSide = (copyState.getCurrentSide().isAttackingSide() ? mAttackers : mDefenders);
        mExitingMove = copyState.getExitingMove();
        mEnteringMove = copyState.getEnteringMove();
        mBerserkingTaflman = copyState.getBerserkingTaflman();
        mTaflmanMoveCache = new TaflmanMoveCache(mZobristHash, (byte) mGame.getGameRules().howManyAttackers(), (byte) mGame.getGameRules().howManyDefenders());
    }

    public GameState(Game game, GameState previousState, Board board, Side attackers, Side defenders, boolean updateZobrist, boolean autoChangeSides) {
        if (!((this instanceof GameTreeState) || (this instanceof GameState)) && !autoChangeSides) {
            throw new IllegalArgumentException("Only internal methods may directly call this constructor!");
        }

        mBoard = board.deepCopy();
        mBoard.setState(this);
        mAttackers = attackers.deepCopy(mBoard);
        mDefenders = defenders.deepCopy(mBoard);
        mGame = game;
        mGameLength = (char)(previousState.mGameLength + 1);
        mEnteringMove = previousState.getExitingMove();

        if (autoChangeSides) {
            if (previousState.getCurrentSide().isAttackingSide()) setCurrentSide(getDefenders());
            else setCurrentSide(getAttackers());
        }

        if(updateZobrist) {
            mZobristHash = updateZobristHash(previousState.mZobristHash, previousState.getBoard(), previousState.getExitingMove());
        }

        mTaflmanMoveCache = new TaflmanMoveCache(mZobristHash, (byte) mGame.getGameRules().howManyAttackers(), (byte) mGame.getGameRules().howManyDefenders());
    }

    public GameState(Game game, GameState previousState, Board board, Side attackers, Side defenders, char berserkingTaflman) {
        this(game, previousState, board, attackers, defenders, true, true);

        if(berserkingTaflman == Taflman.EMPTY) return;

        boolean changeSides = true;

        int x = Taflman.getCurrentSpace(this, berserkingTaflman).x;
        int y = Taflman.getCurrentSpace(this, berserkingTaflman).y;

        char taflman = getPieceAt(x, y);

        if (getBoard().getRules().getBerserkMode() == Rules.BERSERK_CAPTURE_ONLY) {
            if (Taflman.getCapturingMoves(this, taflman).size() > 0) {
                setBerserkingTaflman(taflman);
                changeSides = false;
            } else {
                setBerserkingTaflman(Taflman.EMPTY);
                changeSides = true;
            }
        } else if (getBoard().getRules().getBerserkMode() == Rules.BERSERK_ANY_MOVE) {
            if (Taflman.getAllowableMoves(this, taflman).size() > 0) {
                setBerserkingTaflman(taflman);
                changeSides = false;
            } else {
                setBerserkingTaflman(Taflman.EMPTY);
                changeSides = true;
            }
        }

        if (changeSides) {
            if (previousState.getCurrentSide().isAttackingSide()) setCurrentSide(getDefenders());
            else setCurrentSide(getAttackers());
        } else {
            if (previousState.getCurrentSide().isAttackingSide()) setCurrentSide(getAttackers());
            else setCurrentSide(getDefenders());
        }

        mZobristHash = updateZobristHash(previousState.mZobristHash, previousState.getBoard(), previousState.getExitingMove());
    }

    public Game mGame;
    public long mZobristHash;
    private Board mBoard;
    private Side mAttackers;
    private Side mDefenders;
    private Side mCurrentSide;
    private char mBerserkingTaflman;
    protected TaflmanMoveCache mTaflmanMoveCache;
    protected char mGameLength;

    /**
     * This move object is a concise representation of what we moved to where.
     */
    protected MoveRecord mExitingMove;
    protected MoveRecord mEnteringMove;

    public MoveRecord getExitingMove() {
        return mExitingMove;
    }
    public MoveRecord getEnteringMove() { return mEnteringMove; }

    public Side setCurrentSide(Side side) {
        mCurrentSide = side;
        return mCurrentSide;
    }

    public void setBerserkingTaflman(char taflman) {
        mBerserkingTaflman = taflman;
    }

    public char getBerserkingTaflman() {
        return mBerserkingTaflman;
    }

    public Side getCurrentSide() {
        return mCurrentSide;
    }

    public Board getBoard() {
        return mBoard;
    }

    public Side getAttackers() {
        return mAttackers;
    }

    public Side getDefenders() {
        return mDefenders;
    }

    public char getPieceAt(int x, int y) {
        return mBoard.getOccupier(x, y);
    }

    public Coord getSpaceAt(int x, int y) {
        return Coord.get(x, y);
    }

    public void setCachedAllowableMovesForTaflman(char taflman, List<Coord> moves) {
        if (mTaflmanMoveCache == null) return;
        mTaflmanMoveCache.setCachedAllowableMovesForTaflman(mZobristHash, taflman, moves);
    }

    public void setCachedAllowableDestinationsForTaflman(char taflman, List<Coord> moves) {
        if (mTaflmanMoveCache == null) return;
        mTaflmanMoveCache.setCachedAllowableDestinationsForTaflman(mZobristHash, taflman, moves);
    }

    public void setCachedCapturingMovesForTaflman(char taflman, List<Coord> moves) {
        if (mTaflmanMoveCache == null) return;
        mTaflmanMoveCache.setCachedCapturingMovesForTaflman(mZobristHash, taflman, moves);
    }

    public void setCachedReachableSpacesForTaflman(char taflman, List<Coord> moves) {
        if (mTaflmanMoveCache == null) return;
        mTaflmanMoveCache.setCachedReachableSpacesForTaflman(mZobristHash, taflman, moves);
    }

    public List<Coord> getCachedAllowableMovesForTaflman(char taflman) {
        if (mTaflmanMoveCache == null) return null;
        return mTaflmanMoveCache.getCachedAllowableMovesForTaflman(mZobristHash, taflman);
    }

    public List<Coord> getCachedAllowableDestinationsForTaflman(char taflman) {
        if (mTaflmanMoveCache == null) return null;
        return mTaflmanMoveCache.getCachedAllowableDestinationsForTaflman(mZobristHash, taflman);
    }

    public List<Coord> getCachedCapturingMovesForTaflman(char taflman) {
        if (mTaflmanMoveCache == null) return null;
        return mTaflmanMoveCache.getCachedCapturingMovesForTaflman(mZobristHash, taflman);
    }

    public List<Coord> getCachedReachableSpacesForTaflman(char taflman) {
        if (mTaflmanMoveCache == null) return null;
        return mTaflmanMoveCache.getCachedReachableSpacesForTaflman(mZobristHash, taflman);
    }

    public static final int GOOD_MOVE = 0;
    public static final int ILLEGAL_SIDE = -1;
    public static final int ILLEGAL_SIDE_BERSERKER = -2;
    public static final int ILLEGAL_MOVE = -3;
    public static final int ILLEGAL_MOVE_BERSERKER = -4;

    public int moveTaflman(char taflman, Coord destination) {
        if (mBerserkingTaflman != Taflman.EMPTY && Taflman.getSide(taflman).isAttackingSide() != getCurrentSide().isAttackingSide()) {
            return ILLEGAL_SIDE_BERSERKER;
        }

        if (Taflman.getSide(taflman).isAttackingSide() != getCurrentSide().isAttackingSide()) {
            return ILLEGAL_SIDE;
        }

        if (mBerserkingTaflman != Taflman.EMPTY && taflman != mBerserkingTaflman) {
            return ILLEGAL_MOVE_BERSERKER;
        }

        List<Coord> moves = Taflman.getAllowableDestinations(this, taflman);
        if (!moves.contains(destination)) {
            return ILLEGAL_MOVE;
        } else {
            Coord start = Taflman.getCurrentSpace(this, taflman);
            List<Coord> captures = Taflman.moveTo(this, taflman, destination);

            if (getBoard().getRules().allowShieldWallCaptures() > 0) {
                List<ShieldwallPosition> shieldwallPositionsAttackers = getBoard().detectShieldwallPositionsForSide(getAttackers());
                List<ShieldwallPosition> shieldwallPositionsDefenders = getBoard().detectShieldwallPositionsForSide(getDefenders());

                for (ShieldwallPosition position : shieldwallPositionsAttackers) {
                    captures.addAll(checkShieldwallPositionForCaptures(taflman, destination, position));
                }

                for (ShieldwallPosition position : shieldwallPositionsDefenders) {
                    captures.addAll(checkShieldwallPositionForCaptures(taflman, destination, position));
                }
            }

            mExitingMove = new MoveRecord(start, destination, captures);

            if (captures.size() > 0 && getBoard().getRules().getBerserkMode() > 0) {
                setBerserkingTaflman(taflman);
                mGame.advanceState(this, false, taflman, true);
            } else {
                setBerserkingTaflman(Taflman.EMPTY);
                mGame.advanceState(this, true, Taflman.EMPTY, true);
            }
            return GOOD_MOVE;
        }
    }

    private List<Coord> checkShieldwallPositionForCaptures(char potentialCapturer, Coord destination, ShieldwallPosition position) {
        List<Coord> surroundedByShieldwall = position.surroundedSpaces;
        List<Coord> captures = new ArrayList<Coord>();

        if (getBoard().getRules().allowFlankingShieldwallCapturesOnly()) {
            // Flanking captures require a movement to an edge space
            if (!getBoard().isEdgeSpace(destination)) {
                return captures;
            }
            // If this shieldwall position doesn't contain the capturer, then nothing happens.
            else if (!position.surroundingTaflmen.contains(potentialCapturer)) {
                return captures;
            }
        }

        boolean capturingShieldwall = true;
        for (Coord space : surroundedByShieldwall) {
            if (mBoard.getOccupier(space) == Taflman.EMPTY || Taflman.getSide(mBoard.getOccupier(space)) != getDefenders()) {
                capturingShieldwall = false;
            }
        }

        if (capturingShieldwall) {
            for (Coord space : surroundedByShieldwall) {
                Taflman.capturedBy(this, mBoard.getOccupier(space), potentialCapturer, destination, false);
                captures.add(space);
            }
        }

        return captures;
    }

    public static final int NO_WIN = 0;
    public static final int ATTACKER_WIN = -1;
    public static final int DEFENDER_WIN = -2;

    public int checkVictory() {
        boolean kingAlive = false;
        boolean defenderMovesAvailable = false;

        for (char taflman : getDefenders().getTaflmen()) {
            // King-related win conditions
            if (Taflman.isKing(taflman)) {
                kingAlive = true;
                if (getBoard().getRules().getEscapeType() == Rules.EDGES &&
                        getBoard().isEdgeSpace(Taflman.getCurrentSpace(this, taflman))) {
                    return DEFENDER_WIN;
                } else if (getBoard().getRules().getEscapeType() == Rules.CORNERS) {
                    for (Coord corner : mGame.getGameRules().getCornerSpaces()) {
                        if (mBoard.getOccupier(corner) == taflman) {
                            return DEFENDER_WIN;
                        }
                    }
                }
            }
        }

        defenderLoop:
        for (char taflman : getDefenders().getTaflmen()) {
            Coord space = Taflman.getCurrentSpace(this, taflman);
            if (space != null) {
                for(Coord c : getBoard().getAdjacentSpaces(space)) {
                    if(getBoard().getOccupier(c) == Taflman.EMPTY) {
                        defenderMovesAvailable = true;
                        break defenderLoop;
                    }
                }
            }
        }

        if (!kingAlive) {
            return ATTACKER_WIN;
        }

        if (!defenderMovesAvailable) {
            return ATTACKER_WIN;
        }

		/* Handle edge fort escapes */
        if (getBoard().getRules().allowShieldFortEscapes()) {
            List<ShieldwallPosition> defenderShieldwalls = getBoard().detectShieldwallPositionsForSide(getDefenders());

            for (ShieldwallPosition position : defenderShieldwalls) {
                List<Coord> shieldwallInterior = position.surroundedSpaces;
                for (Coord space : shieldwallInterior) {
                    if (mBoard.getOccupier(space) != Taflman.EMPTY
                            && Taflman.isKing(mBoard.getOccupier(space))
                            && Taflman.getAllowableDestinations(this, mBoard.getOccupier(space)).size() > 0) {
                        return DEFENDER_WIN;
                    }
                }
            }

            // Do the exhaustive edge fort check
            if (checkEdgeFortEscape()) {
                return DEFENDER_WIN;
            }
        }

        // Surrounded sides are sides that cannot reach an edge. Surrounding is sometimes, but not always, fatal.
        if (getBoard().getRules().isSurroundingFatal() && getBoard().isSideEncircled(getDefenders())) {
            return ATTACKER_WIN;
        }

        return NO_WIN;
    }

    private boolean checkEdgeFortEscape() {
        // We have an edge fort escape if three conditions hold:
        // 1. The king can reach an edge, or is on an edge
        // 2. The king has at least one available move
        // 3. No black piece can reach the king, excluding jumps.

        // Get the king.
        char king = Taflman.EMPTY;
        for (char taflman : getDefenders().getTaflmen()) {
            if (Taflman.isKing(taflman)) {
                king = taflman;
                break;
            }
        }

        boolean kingOnEdge = getBoard().isEdgeSpace(Taflman.getCurrentSpace(this, king));

        if (kingOnEdge) {
            // If the king is on an edge and his allowable destinations are
            // empty, then he's surrounded closely, and is by definition
            // not in an edge fort.
            if (Taflman.getAllowableDestinations(this, king).size() < 1) return false;
        } else {
            // If the king is not on an edge, then he must be able to
            // reach an edge. (He must also be allowed to move, but
            // if he can reach an edge from not an edge, he can clearly
            // move.)
            List<Coord> kingReachable = Taflman.getReachableSpaces(this, king, false);

            boolean edgeReachable = false;
            for (Coord space : kingReachable) {
                if (getBoard().isEdgeSpace(space)) {
                    edgeReachable = true;
                    break;
                }
            }

            if (!edgeReachable) return false;
        }

        // If no attacking pieces can reach the king, given the prior
        // two conditions, then the king is surrounded by his own pieces,
        // against an edge, with at least one move, and no black pieces
        // inside the fort.

        // TODO: this is the general case, for if jumping into edge forts is
        // allowed. If rules knobs disable that, then we can just check whether
        // the king can reach any black pieces, and save ourselves the trouble
        // of calculating reachable spaces for every piece.
        for (Coord space : Taflman.getReachableSpaces(this, king)) {
            for (char t : getBoard().getAdjacentNeighbors(space)) {
                if (Taflman.getSide(t).isAttackingSide()) return false;
            }
        }


        // If we've checked every black piece and none of them can reach the king, then
        // this is a successful edge fort.
        return true;
    }

    public GameState deepCopy() {
        return new GameState(this);
    }

    public String getOTNPositionString() {
        return getBoard().getOTNPositionString();
    }

    public long updateZobristHash(long oldZobrist, Board oldBoard, MoveRecord move) {
        long hash = oldZobrist;
        int startIndex = oldBoard.getIndex(move.mStart);
        int endIndex = oldBoard.getIndex(move.mEnd);
        int oldType = getZobristTypeIndex(oldBoard.getOccupier(move.mStart));

        hash = hash ^ mGame.mZobristConstants[startIndex][oldType];
        hash = hash ^ mGame.mZobristConstants[endIndex][oldType];

        for(Coord capturedCoord : move.captures) {
            int captureIndex = getBoard().getIndex(capturedCoord);
            oldType = getZobristTypeIndex(oldBoard.getOccupier(capturedCoord));
            hash = hash ^ mGame.mZobristConstants[captureIndex][oldType];
        }

        if(hash == 0) hash = 1;
        return hash;
    }

    public long zobristHash() {
        int boardSquares = getBoard().getBoardDimension() * getBoard().getBoardDimension();

        long hash = 0;
        for (char taflman : getBoard().getCachedTaflmanLocations().getTaflmen()) {
            int typeIndex = getZobristTypeIndex(taflman);
            int coordIndex = Coord.getIndex(getBoard().findTaflmanSpace(taflman));
            hash = hash ^ mGame.mZobristConstants[coordIndex][typeIndex];
        }

        if(hash == 0) hash = 1;
        return hash;
    }

    private int getZobristTypeIndex(char taflman) {
        int typeIndex = 0;
        int type = Taflman.getPackedType(taflman);
        switch (type) {
            case Taflman.TYPE_KING:
                typeIndex = 0;
                break;
            case Taflman.TYPE_KNIGHT:
                typeIndex = 1;
                break;
            case Taflman.TYPE_COMMANDER:
                typeIndex = 2;
                break;
            default:
                typeIndex = 3;
                break;
        }
        if (Taflman.getPackedSide(taflman) > 0) {
            typeIndex += Taflman.COUNT_TYPES;
        }

        return typeIndex;
    }

    public void makeMove(MoveRecord nextMove) {
        moveTaflman(getPieceAt(nextMove.mStart.x, nextMove.mStart.y), nextMove.mEnd);
    }
}
