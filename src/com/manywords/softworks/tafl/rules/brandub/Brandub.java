package com.manywords.softworks.tafl.rules.brandub;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.brandub.seven.Brandub7Attackers;
import com.manywords.softworks.tafl.rules.brandub.seven.Brandub7Board;
import com.manywords.softworks.tafl.rules.brandub.seven.Brandub7Defenders;
import com.manywords.softworks.tafl.rules.brandub.seven.test.*;


public class Brandub extends Rules {
    public static Brandub newBrandub7() {
        Brandub7Board board = new Brandub7Board();
        Brandub7Attackers attackers = new Brandub7Attackers(board);
        Brandub7Defenders defenders = new Brandub7Defenders(board);

        Brandub rules = new Brandub(board, attackers, defenders);
        return rules;
    }

    public static Brandub newAiMoveRepetitionTest() {
        Brandub7Board board = new Brandub7Board();
        AIMoveRepetitionTestAttackers attackers = new AIMoveRepetitionTestAttackers(board);
        AIMoveRepetitionTestDefenders defenders = new AIMoveRepetitionTestDefenders(board);

        Brandub rules = new Brandub(board, attackers, defenders);
        return rules;
    }

    public static Brandub newAiTwoCornerEscapeTest() {
        Brandub7Board board = new Brandub7Board();
        AITwoCornerEscapeTestAttackers attackers = new AITwoCornerEscapeTestAttackers(board);
        AITwoCornerEscapeTestDefenders defenders = new AITwoCornerEscapeTestDefenders(board);

        Brandub rules = new Brandub(board, attackers, defenders);
        return rules;
    }

    public static Brandub newAiCertainKingCaptureTest() {
        Brandub7Board board = new Brandub7Board();
        AICertainKingCaptureTestAttackers attackers = new AICertainKingCaptureTestAttackers(board);
        AICertainKingCaptureTestDefenders defenders = new AICertainKingCaptureTestDefenders(board);

        Brandub rules = new Brandub(board, attackers, defenders);
        return rules;
    }

    public Brandub(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
        mStartingBoard = board;
        mStartingBoard.setRules(this);
        mStartingAttackers = attackers;
        mStartingDefenders = defenders;
    }

    @Override
    public String getName() {
        return "Brandub";
    }

    private Board mStartingBoard;
    private Side mStartingAttackers;
    private Side mStartingDefenders;

    @Override
    public void setupSpaceGroups(int boardSize) {
        setDefaultSpaceGroups();

        centerHostileTo = new boolean[TAFLMAN_TYPE_COUNT];
        emptyCenterHostileTo = new boolean[TAFLMAN_TYPE_COUNT];
    }

    @Override
    public boolean isKingArmed() {
        // King captures
        return true;
    }

    @Override
    public boolean isKingStrong() {
        // King is captured as a regular piece.
        return false;
    }

    @Override
    public int getKingJumpMode() {
        return Taflman.JUMP_NONE;
    }

    @Override
    public int getKnightJumpMode() {
        return Taflman.JUMP_NONE;
    }

    @Override
    public int getCommanderJumpMode() {
        return Taflman.JUMP_NONE;
    }

    @Override
    public int getMercenaryJumpMode() {
        return Taflman.JUMP_NONE;
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
    public boolean isSurroundingFatal() {
        return false;
    }

    @Override
    public boolean isSpaceHostileToSide(Board board, Coord space, Side side) {
        // Corners are always hostile, center is not.
        if (board.getSpaceTypeFor(space) == SpaceType.CORNER) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canTaflmanMoveThrough(Board board, char piece, Coord space) {
        // Only the king can move through corners

        SpaceType spaces = board.getSpaceTypeFor(space);
        if (spaces == SpaceType.CORNER && !Taflman.isKing(piece)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canTaflmanStopOn(Board board, char piece, Coord space) {
        //Only the king can stop on corners or the center square.
        SpaceType spaces = board.getSpaceTypeFor(space);
        if ((spaces == SpaceType.CORNER || spaces == SpaceType.CENTER)
                && !Taflman.isKing(piece)) {
            return false;
        }
        return true;
    }

    @Override
    public int allowShieldWallCaptures() {
        // No shieldwalls.
        return Rules.NO_SHIELDWALL;
    }

    @Override
    public boolean allowFlankingShieldwallCapturesOnly() {
        return true;
    }

    @Override
    public boolean allowEdgeFortEscapes() {
        // No edge escape captures.
        return false;
    }

    @Override
    public int getEscapeType() {
        // Corner escapes only.
        return Rules.CORNERS;
    }

    @Override
    public int getBerserkMode() {
        return Rules.BERSERK_NONE;
    }

    @Override
    public int threefoldRepetitionResult() {
        return Rules.DRAW;
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
        return getAttackers();
    }
}
