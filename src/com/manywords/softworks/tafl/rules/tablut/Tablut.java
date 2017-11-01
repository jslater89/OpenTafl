package com.manywords.softworks.tafl.rules.tablut;

import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.tablut.nine.Tablut9Attackers;
import com.manywords.softworks.tafl.rules.tablut.nine.Tablut9Board;
import com.manywords.softworks.tafl.rules.tablut.nine.Tablut9Defenders;
import com.manywords.softworks.tafl.rules.tablut.nine.test.CenterKingCaptureAttackers;
import com.manywords.softworks.tafl.rules.tablut.nine.test.CenterKingCaptureDefenders;

import java.util.ArrayList;


public class Tablut extends Rules {
    public static Tablut newTablut9() {
        Tablut9Board board = new Tablut9Board();
        Tablut9Attackers attackers = new Tablut9Attackers(board);
        Tablut9Defenders defenders = new Tablut9Defenders(board);

        Tablut rules = new Tablut(board, attackers, defenders);
        return rules;
    }

    public static Tablut newCenterKingCaptureTest() {
        Tablut9Board board = new Tablut9Board();
        Tablut9Attackers attackers = new CenterKingCaptureAttackers(board);
        Tablut9Defenders defenders = new CenterKingCaptureDefenders(board);

        Tablut rules = new Tablut(board, attackers, defenders);
        return rules;
    }

    public Tablut(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
        mStartingBoard = board;
        mStartingBoard.setRules(this);
        mStartingAttackers = attackers;
        mStartingDefenders = defenders;
    }

    @Override
    public String getName() {
        return "Tablut";
    }

    private Board mStartingBoard;
    private Side mStartingAttackers;
    private Side mStartingDefenders;

    @Override
    public void setupSpaceGroups(int boardSize) {
        setDefaultSpaceGroups();

        // No corners
        setCornerSpaces(new ArrayList<Coord>());

        // Empty center hostile to everybody
        emptyCenterHostileTo = RulesSerializer.getTaflmanTypeListForString("tcnkTCNK");

        // Occupied center hostile to nobody
        centerHostileTo = RulesSerializer.getTaflmanTypeListForString("");

        // Nobody can stop on the center
        centerStoppableFor = RulesSerializer.getTaflmanTypeListForString("");

        // Everyone can move through the center
        centerPassableFor = RulesSerializer.getTaflmanTypeListForString("tcnkTCNK");
    }

    @Override
    public int getKingArmedMode() {
        return KING_ARMED;
    }

    @Override
    public int getKingStrengthMode() {
        // Tablut-style king strength (duh)
        return KING_STRONG_CENTER;
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
        return Taflman.JUMP_STANDARD;
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
    public int getSpeedLimitMode() {
        return SPEED_LIMITS_NONE;
    }

    @Override
    public int getTaflmanSpeedLimit(char taflman) {
        return -1;
    }

    @Override
    public boolean isSurroundingFatal() {
        // Surrounded pieces are doomed
        return true;
    }

    @Override
    public boolean isSpaceHostileToSide(Board board, Coord space, Side side) {
        SpaceType type = board.getSpaceTypeFor(space);

        if(type == SpaceType.CENTER) {
            if(board.getOccupier(space) == Taflman.EMPTY) {
                // Always hostile when empty
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canTaflmanMoveThrough(Board board, char piece, Coord space) {
        // Everyone can move through everything
        return true;
    }

    @Override
    public boolean canTaflmanStopOn(Board board, char piece, Coord space) {
        SpaceType type = board.getSpaceTypeFor(space);

        if(type == SpaceType.CENTER) return false;
        else return true;
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
    public boolean allowEdgeFortEscapes() {
        return false;
    }

    @Override
    public boolean allowLinnaeanCaptures() {
        return true;
    }

    @Override
    public int getEscapeType() {
        return Rules.EDGES;
    }

    @Override
    public int getBerserkMode() {
        return Rules.BERSERK_NONE;
    }

    @Override
    public int threefoldRepetitionResult() {
        return Rules.THIRD_REPETITION_DRAWS;
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
