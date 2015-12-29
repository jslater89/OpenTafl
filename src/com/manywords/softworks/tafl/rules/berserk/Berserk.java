package com.manywords.softworks.tafl.rules.berserk;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.berserk.eleven.Berserk11Attackers;
import com.manywords.softworks.tafl.rules.berserk.eleven.Berserk11Board;
import com.manywords.softworks.tafl.rules.berserk.eleven.Berserk11Defenders;
import com.manywords.softworks.tafl.rules.berserk.eleven.test.*;

public class Berserk extends Rules {
    public static Berserk newBerserk11() {
        Berserk11Board board = new Berserk11Board();
        Berserk11Attackers attackers = new Berserk11Attackers(board);
        Berserk11Defenders defenders = new Berserk11Defenders(board);

        Berserk rules = new Berserk(board, attackers, defenders);
        return rules;
    }

    public static Berserk newCommanderCaptureKingTest() {
        Berserk11Board board = new Berserk11Board();
        CommanderCaptureKingTestAttackers attackers = new CommanderCaptureKingTestAttackers(board);
        CommanderCaptureKingTestDefenders defenders = new CommanderCaptureKingTestDefenders(board);

        Berserk rules = new Berserk(board, attackers, defenders);
        return rules;
    }

    public static Berserk newCommanderCornerCaptureKingTest() {
        Berserk11Board board = new Berserk11Board();
        CommanderCornerCaptureKingTestAttackers attackers = new CommanderCornerCaptureKingTestAttackers(board);
        CommanderCornerCaptureKingTestDefenders defenders = new CommanderCornerCaptureKingTestDefenders(board);

        Berserk rules = new Berserk(board, attackers, defenders);
        return rules;
    }

    public static Berserk newJumpCaptureBerserkerTest() {
        Berserk11Board board = new Berserk11Board();
        JumpCaptureTestAttackers attackers = new JumpCaptureTestAttackers(board);
        JumpCaptureTestDefenders defenders = new JumpCaptureTestDefenders(board);

        Berserk rules = new Berserk(board, attackers, defenders);
        return rules;
    }

    public Berserk(Board board, Side attackers, Side defenders) {
        mStartingBoard = board;
        mStartingBoard.setRules(this);
        mStartingAttackers = attackers;
        mStartingDefenders = defenders;
    }

    private Board mStartingBoard;
    private Side mStartingAttackers;
    private Side mStartingDefenders;

    @Override
    public boolean isKingArmed() {
        // King takes part in captures
        return true;
    }

    @Override
    public boolean isKingStrong() {
        // King must be surrounded on four sides
        return true;
    }

    @Override
    public int getKingJumpMode() {
        return Taflman.JUMP_RESTRICTED;
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
    public int getMercenaryJumpMode() {
        return Taflman.JUMP_NONE;
    }

    @Override
    public boolean canSideJump(Side side) {
        return true;
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
    public boolean isSpaceHostileToSide(Board board, Coord space, Side side) {
        // Center is hostile to attackers always.
        if (board.getSpaceGroupFor(space) == SpaceGroup.THRONE && side.isAttackingSide()) {
            return true;
        }

        // Center is hostile to defenders only if not
        // occupied.
        if (board.getSpaceGroupFor(space) == SpaceGroup.THRONE && !side.isAttackingSide()) {
            if (board.getOccupier(space) == 0) {
                return true;
            }
        }

        // Corners are hostile to everyone all the time.
        if (board.getSpaceGroupFor(space) == SpaceGroup.CORNER) {
            return true;
        }

        // Otherwise, we're good.
        return false;
    }

    @Override
    public boolean canTaflmanMoveThrough(Board board, char piece, Coord space) {
        // This is a little bit redundant, with canTaflmanStopOn, but probably
        // useful for logic purposes.
        SpaceGroup group = board.getSpaceGroupFor(space);
        if (group == SpaceGroup.CORNER && !Taflman.isKing(piece)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canTaflmanStopOn(Board board, char piece, Coord space) {
        SpaceGroup group = board.getSpaceGroupFor(space);

        // Any piece can move through any non-special square
        if (group == SpaceGroup.NONE) return true;

        // Only the king can stop on the throne.
        if (group == SpaceGroup.THRONE && !Taflman.isKing(piece)) {
            return false;
        }

        // Only the king can stop on corners.
        if (group == SpaceGroup.CORNER && !Taflman.isKing(piece)) {
            return false;
        }

        // No other spaces are restricted.
        return true;
    }

    @Override
    public int allowShieldWallCaptures() {
        return Rules.NO_SHIELDWALL;
    }

    @Override
    public boolean allowFlankingShieldwallCapturesOnly() {
        return true;
    }

    @Override
    public boolean allowShieldFortEscapes() {
        return false;
    }

    @Override
    public int getEscapeType() {
        // Escape only at the corners.
        return Rules.CORNERS;
    }

    @Override
    public int getBerserkMode() {
        return Rules.BERSERK_CAPTURE_ONLY;
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
        return mStartingDefenders;
    }

    @Override
    public boolean isSurroundingFatal() {
        // Surrounded pieces can't escape.
        return false;
    }
}
