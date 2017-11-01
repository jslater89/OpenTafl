package com.manywords.softworks.tafl.rules;

import java.util.Set;

/**
 * A standard tafl piece.
 */
public class TaflmanImpl extends Taflman {
    public TaflmanImpl(byte id, char type, Coord startingSpace, Side side, Board board, Rules rules) {
        mId = id;
        mType = type;
        mSide = side;
        mStartingSpace = startingSpace;
        mCurrentSpace = startingSpace;
        mBoard = board;
        mRules = rules;
    }

    private byte mId;
    private char mType;
    private Coord mStartingSpace;
    private Side mSide;
    private Coord mCurrentSpace;
    private Board mBoard;
    private Rules mRules;
    private Set<Coord> mCachedReachableSpaces;

    public TaflmanImpl(char packed, Side attackers, Side defenders, Board board) {
        mId = getPackedId(packed);
        mType = getPackedType(packed);
        mSide = getPackedSide(packed) == SIDE_ATTACKERS ? attackers : defenders;
        mBoard = board;
        mRules = board.getRules();
    }

    public boolean isKing() {
        // This is a king.
        return (char) (mType & TYPE_KING) == TYPE_KING;
    }

    public boolean isKnight() {
        // This is a bog-standard tafl piece, who does jump capturing.
        return (char) (mType & TYPE_KNIGHT) == TYPE_KNIGHT;
    }

    public boolean isCommander() {
        // This is a commander, who does non-capture jumping.
        return (char) (mType & TYPE_COMMANDER) == TYPE_COMMANDER;
    }

    public boolean isMercenary() {
        // This is a mercenary, who flipflops.
        return (char) (mType & TYPE_MERCENARY) == TYPE_MERCENARY;
    }

    public Side getSide() {
        return mSide;
    }

    public Coord getStartingSpace() {
        return mStartingSpace;
    }

    public String toString() {
        return "Taflman at: " + mCurrentSpace.x + " " + mCurrentSpace.y;
    }

    public byte getImplId() {
        return mId;
    }

    public boolean equals(Object o) {
        if (!(o instanceof TaflmanImpl)) return false;
        return this.mType == ((TaflmanImpl) o).mType && this.getImplId() == ((TaflmanImpl) o).getImplId() && (this.getSide().isAttackingSide() == ((TaflmanImpl) o).getSide().isAttackingSide());
    }

    public int hashCode() {
        return 501 * (mType * 29) + (mId * 31) * (getSide().isAttackingSide() ? 1 : -1);
    }
}
