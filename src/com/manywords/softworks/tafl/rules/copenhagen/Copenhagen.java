package com.manywords.softworks.tafl.rules.copenhagen;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.copenhagen.eleven.Copenhagen11Attackers;
import com.manywords.softworks.tafl.rules.copenhagen.eleven.Copenhagen11Board;
import com.manywords.softworks.tafl.rules.copenhagen.eleven.Copenhagen11Defenders;
import com.manywords.softworks.tafl.rules.copenhagen.eleven.test.*;
import com.manywords.softworks.tafl.rules.seabattle.nine.SeaBattle9Board;

public class Copenhagen extends Rules {
    private String mName = "Copenhagen";

    public static Copenhagen newCopenhagen11() {
        Copenhagen11Board board = new Copenhagen11Board();
        Copenhagen11Attackers attackers = new Copenhagen11Attackers(board);
        Copenhagen11Defenders defenders = new Copenhagen11Defenders(board);

        Copenhagen rules = new Copenhagen(board, attackers, defenders);
        rules.mStrictShieldwallRule = true;
        return rules;
    }

    public static Copenhagen newCopenhagen11RelaxedShieldwall() {
        Copenhagen11Board board = new Copenhagen11Board();
        Copenhagen11Attackers attackers = new Copenhagen11Attackers(board);
        Copenhagen11Defenders defenders = new Copenhagen11Defenders(board);

        Copenhagen rules = new Copenhagen(board, attackers, defenders);
        rules.mName = "Copenhagen (relaxed shieldwall)";
        rules.mStrictShieldwallRule = false;
        return rules;
    }

    public static Copenhagen newShieldwallTest() {
        Board board = new SeaBattle9Board();
        ShieldwallTestAttackers attackers = new ShieldwallTestAttackers(board);
        ShieldwallTestDefenders defenders = new ShieldwallTestDefenders(board);

        Copenhagen rules = new Copenhagen(board, attackers, defenders);
        rules.mStrictShieldwallRule = false;
        return rules;
    }

    public static Copenhagen newStrictShieldwallTest() {
        Board board = new SeaBattle9Board();
        ShieldwallTestAttackers attackers = new ShieldwallTestAttackers(board);
        ShieldwallTestDefenders defenders = new ShieldwallTestDefenders(board);

        Copenhagen rules = new Copenhagen(board, attackers, defenders);
        rules.mStrictShieldwallRule = true;
        return rules;
    }

    public static Copenhagen newLargeEdgeFortTest() {
        Board board = new SeaBattle9Board();
        EdgeFortTestAttackers attackers = new EdgeFortTestAttackers(board);
        EdgeFortTestDefenders defenders = new EdgeFortTestDefenders(board);

        Copenhagen rules = new Copenhagen(board, attackers, defenders);
        rules.mStrictShieldwallRule = false;
        return rules;

    }

    public static Copenhagen newLargeEdgeFortFailedTest() {
        Board board = new SeaBattle9Board();
        EdgeFortFailedTestAttackers attackers = new EdgeFortFailedTestAttackers(board);
        EdgeFortFailedTestDefenders defenders = new EdgeFortFailedTestDefenders(board);

        Copenhagen rules = new Copenhagen(board, attackers, defenders);
        rules.mStrictShieldwallRule = false;
        return rules;
    }

    public Copenhagen(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
        mStartingBoard = board;
        mStartingBoard.setRules(this);
        mStartingAttackers = attackers;
        mStartingDefenders = defenders;
    }

    @Override
    public String getName() {
        return mName;
    }

    private Board mStartingBoard;
    private Side mStartingAttackers;
    private Side mStartingDefenders;
    private boolean mStrictShieldwallRule = false;

    @Override
    public void setupSpaceGroups(int boardSize) {
        setDefaultSpaceGroups();
    }

    @Override
    public boolean isKingArmed() {
        // King takes part in captures
        return true;
    }

    @Override
    public int getKingStrengthMode() {
        // King must be surrounded on four sides
        return KING_STRONG;
    }

    @Override
    public int getKingJumpMode() {
        return Taflman.JUMP_NONE;
    }

    @Override
    public int getKnightJumpMode() {
        return Taflman.JUMP_CAPTURE;
    }

    @Override
    public int getCommanderJumpMode() {
        return Taflman.JUMP_STANDARD;
    }

    @Override
    public boolean canSideJump(Side side) {
        return false;
    }

    @Override
    public int howManyAttackers() {
        return mStartingAttackers.getStartingTaflmen().size();
    }

    @Override
    public int howManyDefenders() {
        return mStartingDefenders.getStartingTaflmen().size();
    }

    @Override
    public int getSpeedLimitMode() {
        return SPEED_LIMITS_NONE;
    }

    @Override
    public int getTaflmanSpeedLimit(char taflman) {
        return -1;
    }

    @Override
    public boolean isSpaceHostileToSide(Board board, Coord space, Side side) {

        SpaceType spaces = board.getSpaceTypeFor(space);
        // Center is hostile to defenders only if not
        // occupied, and attackers always.
        if (spaces == SpaceType.CENTER) {
            if (side.isAttackingSide()) {
                return true;
            } else if (board.getOccupier(space) == Taflman.EMPTY) {
                return true;
            }

        }

        // Corners are hostile to everyone all the time.
        if (spaces == SpaceType.CORNER) {
            return true;
        }

        // Otherwise, we're good.
        return false;
    }

    @Override
    public boolean canTaflmanMoveThrough(Board board, char piece, Coord space) {
        // This is a little bit redundant, with canTaflmanStopOn, but probably
        // useful for logic purposes.
        SpaceType spaces = board.getSpaceTypeFor(space);
        if (spaces == SpaceType.CORNER && !Taflman.isKing(piece)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canTaflmanStopOn(Board board, char piece, Coord space) {
        SpaceType spaces = board.getSpaceTypeFor(space);

        // Any piece can move through any non-special square
        if (spaces == SpaceType.NONE) return true;

        // Only the king can stop on the throne.
        if (spaces == SpaceType.CENTER && !Taflman.isKing(piece)) {
            return false;
        }

        // Only the king can stop on corners.
        if (spaces == SpaceType.CORNER && !Taflman.isKing(piece)) {
            return false;
        }

        // No other spaces are restricted.
        return true;
    }

    @Override
    public int allowShieldWallCaptures() {
        return Rules.STRONG_SHIELDWALL;
    }

    @Override
    public boolean allowFlankingShieldwallCapturesOnly() {
        return mStrictShieldwallRule;
    }

    @Override
    public boolean allowEdgeFortEscapes() {
        return true;
    }

    @Override
    public int getEscapeType() {
        // Escape only at the corners.
        return Rules.CORNERS;
    }

    @Override
    public int getBerserkMode() {
        return Rules.BERSERK_NONE;
    }

    @Override
    public int threefoldRepetitionResult() {
        return Rules.THIRD_REPETITION_WINS;
    }

    @Override
    public Board getBoard() {
        return mStartingBoard;
    }

    @Override
    public Side getAttackers() {
        return mStartingAttackers;
    }

    @Override
    public Side getDefenders() {
        return mStartingDefenders;
    }

    @Override
    public Side getStartingSide() {
        return mStartingAttackers;
    }

    @Override
    public boolean isSurroundingFatal() {
        // Surrounded pieces can't escape.
        return true;
    }
}
