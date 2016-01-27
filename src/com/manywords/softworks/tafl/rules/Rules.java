package com.manywords.softworks.tafl.rules;

public abstract class Rules {
    /**
     * Can the king participate in captures?
     *
     * @return
     */
    public abstract boolean isKingArmed();

    /**
     * Does the king take two or four men to
     * capture?
     *
     * @return
     */
    public abstract boolean isKingStrong();

    /**
     * Does the king jump? If so, how?
     */
    public abstract int getKingJumpMode();

    public abstract int getKnightJumpMode();

    public abstract int getCommanderJumpMode();

    public abstract int getMercenaryJumpMode();

    public abstract boolean canSideJump(Side side);

    public abstract int howManyAttackers();

    public abstract int howManyDefenders();

    /**
     * Is the a square or group of squares currently
     * hostile to a side?
     *
     * @return
     */
    public abstract boolean isSpaceHostileToSide(Board board, Coord space, Side side);

    /**
     * Can the given taflman move through the given square
     * or group of squares?
     *
     * @param piece
     * @param space
     * @return
     */
    public abstract boolean canTaflmanMoveThrough(Board board, char piece, Coord space);

    /**
     * Can the given taflman stop on the given square or
     * group of squares?
     *
     * @param piece
     * @param space
     * @return
     */
    public abstract boolean canTaflmanStopOn(Board board, char piece, Coord space);

    /**
     * Allow shield wall captures against corner squares, if corners
     * are hostile.
     */
    public static final int STRONG_SHIELDWALL = 2;

    /**
     * Allow shield wall captures, but require captured
     * pieces to be completely surrounded without use of
     * corners.
     */
    public static final int WEAK_SHIELDWALL = 1;
    public static final int NO_SHIELDWALL = 0;

    /**
     * Whether to allow shield wall captures. See STRONG_SHIELDWALL
     * and WEAK_SHIELDWALL.
     *
     * @return
     */
    public abstract int allowShieldWallCaptures();

    public abstract boolean allowFlankingShieldwallCapturesOnly();

    /**
     * Whether to allow shield fort escapes. If white has surrounded its king
     * at the edge of the board with one extra space inside the cordon, white
     * wins.
     *
     * @return
     */
    public abstract boolean allowShieldFortEscapes();

    public static final int CORNERS = 1;
    public static final int EDGES = 0;

    /**
     * The usual victory condition.
     *
     * @return
     */
    public abstract int getEscapeType();

    public static final int BERSERK_NONE = 0;
    public static final int BERSERK_CAPTURE_ONLY = 1;
    public static final int BERSERK_ANY_MOVE = 2;

    public abstract int getBerserkMode();

    /**
     * Get the board for this ruleset.
     *
     * @return
     */
    public abstract Board getBoard();

    public abstract Side getAttackers();

    public abstract Side getDefenders();

    public abstract Side getStartingSide();

    /**
     * Rulesets with jumping or side-switching pieces can
     * set this to false, but otherwise, once white is surrounded,
     * black always wins by one condition or another.
     *
     * @return
     */
    public abstract boolean isSurroundingFatal();

    public String getOTRString() {
        return "";
    }
}
