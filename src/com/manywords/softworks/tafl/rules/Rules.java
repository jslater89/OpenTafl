package com.manywords.softworks.tafl.rules;

import com.manywords.softworks.tafl.notation.RulesSerializer;

import java.util.*;

public abstract class Rules {
    public static final int TAFLMAN_TYPE_COUNT = Taflman.COUNT_TYPES * 2;

    public final int boardSize;
    private Set<Coord> mCenterSpaces = new LinkedHashSet<Coord>();
    private Set<Coord> mCornerSpaces = new LinkedHashSet<Coord>();
    private Set<Coord> mAttackerForts = new LinkedHashSet<Coord>();
    private Set<Coord> mDefenderForts = new LinkedHashSet<Coord>();

    /*
        These arrays are provided for auto-generated rules; hardcoded rules
        need not use them for movement/hostility detection, but should
        set them for serialization.
     */
    public boolean[] centerPassableFor = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] centerStoppableFor = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] centerHostileTo = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] emptyCenterHostileTo = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] centerReenterableFor = new boolean[TAFLMAN_TYPE_COUNT];

    public boolean[] cornerPassableFor = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] cornerStoppableFor = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] cornerHostileTo = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] cornerReenterableFor = new boolean[TAFLMAN_TYPE_COUNT];

    public boolean[] attackerFortPassableFor = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] attackerFortStoppableFor = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] attackerFortHostileTo = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] attackerFortReenterableFor = new boolean[TAFLMAN_TYPE_COUNT];

    public boolean[] defenderFortPassableFor = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] defenderFortStoppableFor = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] defenderFortHostileTo = new boolean[TAFLMAN_TYPE_COUNT];
    public boolean[] defenderFortReenterableFor = new boolean[TAFLMAN_TYPE_COUNT];

    public Rules (Board board, Side attackers, Side defenders) {
        board.setRules(this);
        board.setupTaflmen(attackers, defenders);
        boardSize = board.getBoardDimension();
        setupSpaceGroups(boardSize);
    }

    /**
     * Sets the default (i.e. Fetlar-style) space groups.
     */
    public void setDefaultSpaceGroups() {
        int center = (boardSize - 1) / 2;

        List<Coord> centerSpace = new ArrayList<Coord>(1);
        centerSpace.add(Coord.get(center, center));

        setCenterSpaces(centerSpace);

        Coord c1 = Coord.get(0, 0);
        Coord c2 = Coord.get(boardSize - 1, 0);
        Coord c3 = Coord.get(0, boardSize - 1);
        Coord c4 = Coord.get(boardSize - 1, boardSize - 1);

        List<Coord> corners = new ArrayList<Coord>(4);
        corners.add(c1);
        corners.add(c2);
        corners.add(c3);
        corners.add(c4);

        setCornerSpaces(corners);

        centerPassableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("cenp"));
        centerStoppableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("cens"));
        centerHostileTo = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("cenh"));
        emptyCenterHostileTo = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("cenhe"));
        centerReenterableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("cenre"));

        cornerPassableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("corp"));
        cornerStoppableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("cors"));
        cornerHostileTo = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("corh"));
        cornerReenterableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("corre"));

        attackerFortPassableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("aforp"));
        attackerFortStoppableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("afors"));
        attackerFortHostileTo = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("aforh"));
        attackerFortReenterableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("aforre"));

        defenderFortPassableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("dforp"));
        defenderFortStoppableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("dfors"));
        defenderFortHostileTo = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("dforh"));
        defenderFortReenterableFor = RulesSerializer.getTaflmanTypeListForString(RulesSerializer.defaults.get("dforre"));
    }

    /**
     * Set the spaces considered as center spaces.
     * @param c
     */
    public void setCenterSpaces(List<Coord> c) {
        mCenterSpaces.clear();
        mCenterSpaces.addAll(c);
    }

    /**
     * Set the spaces considered as corner spaces.
     * @param c
     */
    public void setCornerSpaces(List<Coord> c) {
        mCornerSpaces.clear();
        mCornerSpaces.addAll(c);
    }

    /**
     * Set the spaces considered as attacker fortresses.
     * @param c
     */
    public void setAttackerForts(List<Coord> c) {
        mAttackerForts.clear();
        mAttackerForts.addAll(c);
    }

    /**
     * Set the spaces considered as defender fortresses.
     * @param c
     */
    public void setDefenderForts(List<Coord> c) {
        mDefenderForts.clear();
        mDefenderForts.addAll(c);
    }

    /**
     * Return whether the given space is a center space.
     * @param c
     * @return
     */
    public boolean isCenterSpace(Coord c) {
        return mCenterSpaces.contains(c);
    }

    /**
     * Return whether the given space is a corner space.
     * @param c
     * @return
     */
    public boolean isCornerSpace(Coord c) {
        return mCornerSpaces.contains(c);
    }

    /**
     * Return whether the given space is an attacker fortress.
     * @param c
     * @return
     */
    public boolean isAttackerFort(Coord c) {
        return mAttackerForts.contains(c);
    }

    /**
     * Return whether the given space is a defender fortress.
     * @param c
     * @return
     */
    public boolean isDefenderFort(Coord c) {
        return mDefenderForts.contains(c);
    }

    public Set<Coord> getCenterSpaces() {
        return mCenterSpaces;
    }

    public Set<Coord> getCornerSpaces() {
        return mCornerSpaces;
    }

    public Set<Coord> getAttackerForts() {
        return mAttackerForts;
    }

    public Set<Coord> getDefenderForts() {
        return mDefenderForts;
    }

    public abstract String getName();

    /**
     * Set up the centers, corners, and
     */
    public abstract void setupSpaceGroups(int boardSize);

    /**
     * Can the king participate in captures?
     *
     * @return
     */
    public abstract boolean isKingArmed();

    /**
     * The king is strong, requiring four men to capture at all times.
     */
    public static final int KING_STRONG = 0;

    /**
     * The king is strong when on or adjacent to the throne, and weak
     * elsewhere.
     */
    public static final int KING_STRONG_CENTER = 1;

    /**
     * The king is weak, requiring only two men to capture.
     */
    public static final int KING_WEAK = 2;

    /**
     * Does the king take two or four men to
     * capture?
     *
     * @return
     */
    public abstract int getKingStrengthMode();

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
    public abstract boolean allowEdgeFortEscapes();

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
     * No result.
     */
    public static final int IGNORE = 0;
    /**
     * The game is drawn immediately on threefold repetition.
     */
    public static final int DRAW = 1;
    /**
     * The player who moves to make the third repetition loses.
     * In the most common example case, where one piece is
     * blocking the movement of one other piece, the player
     * who makes the move into the third board state is the
     * player who is forced to block the movement.
     */
    public static final int THIRD_REPETITION_LOSES = 2;
    /**
     * The player who moves to make the third repetition wins.
     * In the most common example case, where one piece is
     * blocking the movement of one other piece, the player
     * who makes the move into the third board state is the
     * player who is forced to block the movement.
     */
    public static final int THIRD_REPETITION_WINS = 3;

    /**
     * What happens on a threefold repetition? A threefold repetition
     * is defined as a return to the same board state for the third
     * time.
     * @return
     */
    public abstract int threefoldRepetitionResult();

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
        return RulesSerializer.getRulesRecord(this);
    }

    public String toString() {
        int dim = getBoard().getBoardDimension();
        return getName() + " " + dim + "x" + dim;
    }
}
