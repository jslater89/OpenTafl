package com.manywords.softworks.tafl.rules.tawlbwrdd;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.tawlbwrdd.eleven.Tawlbwrdd11Attackers;
import com.manywords.softworks.tafl.rules.tawlbwrdd.eleven.Tawlbwrdd11Board;
import com.manywords.softworks.tafl.rules.tawlbwrdd.eleven.Tawlbwrdd11Defenders;

import java.util.ArrayList;

public class Tawlbwrdd extends Rules {
    public static Tawlbwrdd newTawlbwrdd11() {
        Tawlbwrdd11Board board = new Tawlbwrdd11Board();
        Tawlbwrdd11Attackers attackers = new Tawlbwrdd11Attackers(board);
        Tawlbwrdd11Defenders defenders = new Tawlbwrdd11Defenders(board);

        Tawlbwrdd rules = new Tawlbwrdd(board, attackers, defenders);
        return rules;
    }

    public Tawlbwrdd(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
        mStartingBoard = board;
        mStartingBoard.setRules(this);
        mStartingAttackers = attackers;
        mStartingDefenders = defenders;
    }

    @Override
    public String getName() {
        return "Tawlbwrdd";
    }

    private Board mStartingBoard;
    private Side mStartingAttackers;
    private Side mStartingDefenders;

    @Override
    public void setupSpaceGroups(int boardSize) {
        setDefaultSpaceGroups();

        setCornerSpaces(new ArrayList<Coord>());
        setCenterSpaces(new ArrayList<Coord>());
    }

    @Override
    public boolean isKingArmed() {
        // King takes part in captures
        return true;
    }

    @Override
    public boolean isKingStrong() {
        // King must be surrounded on four sides
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
    public boolean isSpaceHostileToSide(Board board, Coord space, Side side) {
        return false;
    }

    @Override
    public boolean canTaflmanMoveThrough(Board board, char piece, Coord space) {
        return true;
    }

    @Override
    public boolean canTaflmanStopOn(Board board, char piece, Coord space) {
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
        return Rules.EDGES;
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
        return mStartingAttackers;
    }

    @Override
    public boolean isSurroundingFatal() {
        // Surrounded pieces can't escape.
        return true;
    }
}
