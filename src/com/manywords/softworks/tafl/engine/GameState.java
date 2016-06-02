package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.engine.ai.GameTreeState;
import com.manywords.softworks.tafl.notation.PositionSerializer;
import com.manywords.softworks.tafl.rules.*;

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

    public GameState(Game game, Rules startingRules, Board board, Side attackers, Side defenders) {
        mBoard = board;
        mAttackers = attackers;
        mDefenders = defenders;
        mGame = game;
        mBoard.setState(this);
        mBoard.setupTaflmen(attackers, defenders);

        if (mBoard.getRules().getStartingSide().isAttackingSide()) {
            mCurrentSide = mAttackers;
        } else {
            mCurrentSide = mDefenders;
        }

        mZobristHash = zobristHash();
        mTaflmanMoveCache = new TaflmanMoveCache(mZobristHash, (byte) startingRules.howManyAttackers(), (byte) startingRules.howManyDefenders());
    }

    public GameState(Game game, GameState previousState, Board board, Side attackers, Side defenders, boolean updateZobrist) {
        this(previousState);

        updateGameState(game, previousState, board, attackers, defenders, updateZobrist, Taflman.EMPTY);
    }

    public GameState(int moveErrorCode) {
        mLastMoveResult = moveErrorCode;
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
        mTaflmanMoveCache = new TaflmanMoveCache(mZobristHash, (byte) mGame.getRules().howManyAttackers(), (byte) mGame.getRules().howManyDefenders());
    }

    public void updateBoard(GameState previousState) {
        mBoard = previousState.getBoard().deepCopy();
        mBoard.setState(this);
    }

    public void updateGameState(Game game, GameState previousState, Board board, Side attackers, Side defenders, boolean updateZobrist, char berserkingTaflman) {
        if (!((this instanceof GameTreeState) || (this instanceof GameState))) {
            throw new IllegalArgumentException("Only internal methods may directly call this constructor!");
        }

        mBoard = board.deepCopy();
        mBoard.setState(this);
        mAttackers = attackers.deepCopy(mBoard);
        mDefenders = defenders.deepCopy(mBoard);
        mGame = game;
        mGameLength = (char)(previousState.mGameLength + 1);
        mEnteringMove = previousState.getExitingMove();

        if(updateZobrist) {
            mZobristHash = updateZobristHash(previousState.mZobristHash, previousState.getBoard(), previousState.getExitingMove());
        }

        mTaflmanMoveCache = new TaflmanMoveCache(mZobristHash, (byte) mGame.getRules().howManyAttackers(), (byte) mGame.getRules().howManyDefenders());
        boolean changeSides = true;

        if(berserkingTaflman != Taflman.EMPTY) {
            int x = Taflman.getCurrentSpace(this, berserkingTaflman).x;
            int y = Taflman.getCurrentSpace(this, berserkingTaflman).y;

            char taflman = getPieceAt(x, y);

            if (getBoard().getRules().getBerserkMode() == Rules.BERSERK_CAPTURE_ONLY) {
                if (Taflman.getCapturingMoves(this, taflman).size() > 0) {
                    setBerserkingTaflman(taflman);
                    changeSides = false;
                }
                else {
                    setBerserkingTaflman(Taflman.EMPTY);
                    changeSides = true;
                }
            }
            else if (getBoard().getRules().getBerserkMode() == Rules.BERSERK_ANY_MOVE) {
                if (Taflman.getAllowableMoves(this, taflman).size() > 0) {
                    setBerserkingTaflman(taflman);
                    changeSides = false;
                }
                else {
                    setBerserkingTaflman(Taflman.EMPTY);
                    changeSides = true;
                }
            }
        }

        if (changeSides) {
            if (previousState.getCurrentSide().isAttackingSide()) setCurrentSide(getDefenders());
            else setCurrentSide(getAttackers());
        } else {
            if (previousState.getCurrentSide().isAttackingSide()) setCurrentSide(getAttackers());
            else setCurrentSide(getDefenders());
        }
    }

    private static final int VICTORY_UNCHECKED = -50;
    public Game mGame;
    private int mLastMoveResult;
    protected int mVictory = VICTORY_UNCHECKED;
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
    public int getLastMoveResult() { return mLastMoveResult; }

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

    public void setCachedJumpsForTaflman(char taflman, List<Coord> jumps) {
        if(mTaflmanMoveCache == null) return;
        mTaflmanMoveCache.setCachedJumpsForTaflman(mZobristHash, taflman, jumps);
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

    public List<Coord> getCachedJumpsForTaflman(char taflman) {
        if(mTaflmanMoveCache == null) return null;
        return mTaflmanMoveCache.getCachedJumpsForTaflman(mZobristHash, taflman);
    }

    public List<Coord> getCachedCapturingMovesForTaflman(char taflman) {
        if (mTaflmanMoveCache == null) return null;
        return mTaflmanMoveCache.getCachedCapturingMovesForTaflman(mZobristHash, taflman);
    }

    public List<Coord> getCachedReachableSpacesForTaflman(char taflman) {
        if (mTaflmanMoveCache == null) return null;
        return mTaflmanMoveCache.getCachedReachableSpacesForTaflman(mZobristHash, taflman);
    }

    public static final int DRAW = 3;
    public static final int DEFENDER_WIN = 2;
    public static final int ATTACKER_WIN = 1;
    public static final int GOOD_MOVE = 0;
    public static final int ILLEGAL_SIDE = -1;
    public static final int ILLEGAL_SIDE_BERSERKER = -2;
    public static final int ILLEGAL_MOVE = -3;
    public static final int ILLEGAL_MOVE_BERSERKER = -4;

    public GameState moveTaflman(char taflman, Coord destination) {
        if (mBerserkingTaflman != Taflman.EMPTY && Taflman.getSide(this, taflman).isAttackingSide() != getCurrentSide().isAttackingSide()) {
            return new GameState(ILLEGAL_SIDE_BERSERKER);
        }

        if (Taflman.getSide(this, taflman).isAttackingSide() != getCurrentSide().isAttackingSide()) {
            return new GameState(ILLEGAL_SIDE);
        }

        if (mBerserkingTaflman != Taflman.EMPTY && taflman != mBerserkingTaflman) {
            return new GameState(ILLEGAL_MOVE_BERSERKER);
        }

        List<Coord> moves = Taflman.getAllowableDestinations(this, taflman);
        if (!moves.contains(destination)) {
            return new GameState(ILLEGAL_MOVE);
        }
        else {
            GameState nextState = new GameState(this);

            boolean detailed = !(this instanceof GameTreeState);
            MoveRecord move = Taflman.moveTo(nextState, taflman, destination, detailed);
            List<Coord> captures = move.captures;

            if (getBoard().getRules().allowShieldWallCaptures() > 0) {
                List<ShieldwallPosition> shieldwallPositionsAttackers = nextState.getBoard().detectShieldwallPositionsForSide(getAttackers());
                List<ShieldwallPosition> shieldwallPositionsDefenders = nextState.getBoard().detectShieldwallPositionsForSide(getDefenders());


                for (ShieldwallPosition position : shieldwallPositionsAttackers) {
                    captures.addAll(nextState.checkShieldwallPositionForCaptures(taflman, destination, position));
                }

                for (ShieldwallPosition position : shieldwallPositionsDefenders) {
                    captures.addAll(nextState.checkShieldwallPositionForCaptures(taflman, destination, position));
                }
            }

            nextState.mEnteringMove = move;
            mExitingMove = move;

            if (captures.size() > 0 && getBoard().getRules().getBerserkMode() > 0) {
                nextState.setBerserkingTaflman(taflman);
                nextState = mGame.advanceState(this, nextState, false, taflman, true);
                nextState.mLastMoveResult = nextState.checkVictory();
            } else {
                nextState.setBerserkingTaflman(Taflman.EMPTY);
                nextState = mGame.advanceState(this, nextState, true, Taflman.EMPTY, true);
                nextState.mLastMoveResult = nextState.checkVictory();
            }
            return nextState;
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
            if (mBoard.getOccupier(space) == Taflman.EMPTY || Taflman.getPackedSide(mBoard.getOccupier(space)) == Taflman.getPackedSide(potentialCapturer)) {
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

    public int countPositionOccurrences() {
        int repeats = 0;
        for (GameState state : mGame.getHistory()) {
            if (this.mZobristHash == state.mZobristHash) {
                repeats++;
            }
        }

        return repeats;
    }

    public void winByResignation(boolean isWinnerAttackingSide) {
        if(isWinnerAttackingSide) mVictory = ATTACKER_WIN;
        else mVictory = DEFENDER_WIN;
    }

    public int checkVictory() {
        if(mVictory == VICTORY_UNCHECKED) {
            mVictory = checkVictoryInternal();
        }
        return mVictory;
    }

    private int checkVictoryInternal() {
        if(getAttackers().getTaflmen().size() == 0) return DEFENDER_WIN;
        else if (getDefenders().getTaflmen().size() == 0) return ATTACKER_WIN;
        int threefoldRepetitionResult = mGame.getRules().threefoldRepetitionResult();
        // Threefold repetition cannot occur as the result of a berserk move
        if(threefoldRepetitionResult != Rules.IGNORE && mBerserkingTaflman == Taflman.EMPTY) {
            int repeats = countPositionOccurrences();

            // If this position has occurred two other times plus this one, do the threefold
            // checks.
            if(repeats >= 2) {
                if(threefoldRepetitionResult == Rules.DRAW) {
                    return DRAW;
                }
                else if (threefoldRepetitionResult == Rules.THIRD_REPETITION_LOSES) {
                    return (getCurrentSide().isAttackingSide() ? DEFENDER_WIN : ATTACKER_WIN);
                }
                else if (threefoldRepetitionResult == Rules.THIRD_REPETITION_WINS) {
                    return (getCurrentSide().isAttackingSide() ? ATTACKER_WIN : DEFENDER_WIN);
                }
            }
        }

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
                    for (Coord corner : mGame.getRules().getCornerSpaces()) {
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
        if (getBoard().getRules().allowEdgeFortEscapes()) {
            List<ShieldwallPosition> defenderShieldwalls = getBoard().detectShieldwallPositionsForSide(getDefenders());

            // A shieldwall shape is a subset of all invincible shapes, so don't bother checking them for
            // invincibility
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

        return GOOD_MOVE; // i.e. no win
    }

    private boolean checkEdgeFortEscape() {
        // We have an edge fort escape if four conditions hold:
        // 1. The king can reach an edge, or is on an edge
        // 2. The king has at least one available move
        // 3. No black piece can reach the king, excluding jumps.
        // 4. The white pieces surrounding the king cannot be captured.
        //    We can check this by looking at each one in turn, checking its
        //    horizontal and vertical neighbors, and seeing if any of them
        //    have two potentially hostile spaces on the same rank or file.
        //    (A potentially hostile space is a space which is empty, but
        //    not part of the fort. The spaces the king can reach are the
        //    fort spaces.

        // Get the king.
        char king = Taflman.EMPTY;
        for (char taflman : getDefenders().getTaflmen()) {
            if (Taflman.isKing(taflman)) {
                king = taflman;
                break;
            }
        }

        boolean kingOnEdge = getBoard().isEdgeSpace(Taflman.getCurrentSpace(this, king));
        List<Coord> fortSpaces = Taflman.getReachableSpaces(this, king, false);

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

            boolean edgeReachable = false;
            for (Coord space : fortSpaces) {
                if (getBoard().isEdgeSpace(space)) {
                    edgeReachable = true;
                    break;
                }
            }

            if (!edgeReachable) return false;
        }

        // If the king can't reach any attacking pieces, then he is surrounded
        // by friendly taflmen.
        List<Character> edgefortTaflmen = new ArrayList<>();
        for (Coord space : fortSpaces) {
            for (char t : getBoard().getAdjacentNeighbors(space)) {
                if (Taflman.getSide(this, t).isAttackingSide()) return false;
                else edgefortTaflmen.add(t);
            }
        }

        // We've established that the king is fully surrounded by friendly taflmen.
        // Now we have to check to see if they can be captured. Do that this way:
        // 1. Get the adjacent spaces for each taflman in the edge fort.
        // 2. Remove any spaces which make up the fort, and any occupied by friendly
        //    taflmen.
        // 3. If the number of remaining spaces is not 3, then it can't be captured.
        //    (Try it yourself on a piece of paper if you don't believe me.)

        for(char taflman : edgefortTaflmen) {
            List<Coord> adjacent = getBoard().getAdjacentSpaces(Taflman.getCurrentSpace(this, taflman));
            adjacent.removeAll(fortSpaces);

            int friendlySpaces = 0;
            for(Coord c : adjacent) {
                char occupier = getBoard().getOccupier(c);
                if(occupier != Taflman.EMPTY && !Taflman.getSide(this, occupier).isAttackingSide()) {
                    friendlySpaces++;
                }
            }

            if(adjacent.size() - friendlySpaces >= 3) {
                return false;
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
        return PositionSerializer.getPositionRecord(getBoard());
    }

    public long updateZobristHash(long oldZobrist, Board oldBoard, MoveRecord move) {
        long hash = oldZobrist;
        int startIndex = oldBoard.getIndex(move.start);
        int endIndex = oldBoard.getIndex(move.end);
        int oldType = getZobristTypeIndex(oldBoard.getOccupier(move.start));

        hash = hash ^ mGame.mZobristConstants[startIndex][oldType];
        hash = hash ^ mGame.mZobristConstants[endIndex][oldType];

        for(Coord capturedCoord : move.captures) {
            int captureIndex = oldBoard.getIndex(capturedCoord);
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

    public int makeMove(MoveRecord nextMove) {
        return moveTaflman(getPieceAt(nextMove.start.x, nextMove.start.y), nextMove.end).getLastMoveResult();
    }

    public void setExitingMove(DetailedMoveRecord exitingMove) {
        mExitingMove = exitingMove;
    }

    public void setEnteringMove(DetailedMoveRecord enteringMove) {
        mEnteringMove = enteringMove;
    }
}
