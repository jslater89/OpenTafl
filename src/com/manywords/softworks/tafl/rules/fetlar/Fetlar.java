package com.manywords.softworks.tafl.rules.fetlar;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.fetlar.eleven.Fetlar11Attackers;
import com.manywords.softworks.tafl.rules.fetlar.eleven.Fetlar11Board;
import com.manywords.softworks.tafl.rules.fetlar.eleven.Fetlar11Defenders;
import com.manywords.softworks.tafl.rules.fetlar.eleven.test.FetlarTestAttackers;
import com.manywords.softworks.tafl.rules.fetlar.eleven.test.FetlarTestDefenders;

import java.util.ArrayList;
import java.util.List;

public class Fetlar extends Rules {
    public static Fetlar newFetlar11() {
        Fetlar11Board board = new Fetlar11Board();
        Fetlar11Attackers attackers = new Fetlar11Attackers(board);
        Fetlar11Defenders defenders = new Fetlar11Defenders(board);

        Fetlar rules = new Fetlar(board, attackers, defenders);
        return rules;
    }

    public static Fetlar newFetlarTest() {
        Fetlar11Board board = new Fetlar11Board();
        FetlarTestAttackers attackers = new FetlarTestAttackers(board);
        FetlarTestDefenders defenders = new FetlarTestDefenders(board);

        Fetlar rules = new Fetlar(board, attackers, defenders);
        return rules;
    }

    public Fetlar(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
        mStartingBoard = board;
        mStartingBoard.setRules(this);
        mStartingAttackers = attackers;
        mStartingDefenders = defenders;
    }

    private Board mStartingBoard;
    private Side mStartingAttackers;
    private Side mStartingDefenders;

    @Override
    public void setupSpaceGroups(int boardSize) {
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
    }

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
    public boolean isSpaceHostileToSide(Board board, Coord space, Side side) {
        SpaceGroup spaces = board.getSpaceGroupFor(space);

        // Center is hostile to attackers always.
        // Center is hostile to defenders only if not
        // occupied.
        if (spaces == SpaceGroup.THRONE) {
            if (side.isAttackingSide()) {
                return true;
            } else if (board.getOccupier(space) == Taflman.EMPTY) {
                return true;
            }
        }

        // Corners are hostile to everyone all the time.
        if (spaces == SpaceGroup.CORNER) {
            return true;
        }

        // Otherwise, we're good.
        return false;
    }

    @Override
    public boolean canTaflmanMoveThrough(Board board, char piece, Coord space) {
        // This is a little bit redundant, with canTaflmanStopOn, but probably
        // useful for logic purposes.
        SpaceGroup spaces = board.getSpaceGroupFor(space);
        if (spaces == SpaceGroup.CORNER && !Taflman.isKing(piece)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canTaflmanStopOn(Board board, char piece, Coord space) {
        SpaceGroup spaces = board.getSpaceGroupFor(space);

        // Any piece can move through any non-special square
        if (spaces == null) return true;

        // Only the king can stop on the throne.
        if (spaces == SpaceGroup.THRONE && !Taflman.isKing(piece)) {
            return false;
        }

        // Only the king can stop on corners.
        if (spaces == SpaceGroup.CORNER && !Taflman.isKing(piece)) {
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
        return Rules.BERSERK_NONE;
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
        return true;
    }
}
