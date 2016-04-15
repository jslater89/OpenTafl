package com.manywords.softworks.tafl.rules.seabattle;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.seabattle.nine.SeaBattle9Attackers;
import com.manywords.softworks.tafl.rules.seabattle.nine.SeaBattle9Board;
import com.manywords.softworks.tafl.rules.seabattle.nine.SeaBattle9Defenders;
import com.manywords.softworks.tafl.rules.seabattle.nine.test.*;

import java.util.ArrayList;


public class SeaBattle extends Rules {
    public static SeaBattle newSeaBattle9() {
        SeaBattle9Board board = new SeaBattle9Board();
        SeaBattle9Attackers attackers = new SeaBattle9Attackers(board);
        SeaBattle9Defenders defenders = new SeaBattle9Defenders(board);

        SeaBattle rules = new SeaBattle(board, attackers, defenders);
        return rules;
    }

    public static SeaBattle newStrongKingTest() {
        SeaBattle9Board board = new SeaBattle9Board();
        SimpleVictoryTestAttackers attackers = new SimpleVictoryTestAttackers(board);
        SimpleVictoryTestDefenders defenders = new SimpleVictoryTestDefenders(board);

        SeaBattle rules = new SeaBattle(board, attackers, defenders);
        return rules;
    }

    public static SeaBattle newEncirclementTest() {
        SeaBattle9Board board = new SeaBattle9Board();
        EncirclementTestAttackers attackers = new EncirclementTestAttackers(board);
        EncirclementTestDefenders defenders = new EncirclementTestDefenders(board);

        SeaBattle rules = new SeaBattle(board, attackers, defenders);
        return rules;
    }

    public static SeaBattle newAiTwoEdgeEscapeTest() {
        SeaBattle9Board board = new SeaBattle9Board();
        AITwoEdgeEscapeTestAttackers attackers = new AITwoEdgeEscapeTestAttackers(board);
        AITwoEdgeEscapeTestDefenders defenders = new AITwoEdgeEscapeTestDefenders(board);

        SeaBattle rules = new SeaBattle(board, attackers, defenders);
        return rules;
    }

    public SeaBattle(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
        mStartingBoard = board;
        mStartingBoard.setRules(this);
        mStartingAttackers = attackers;
        mStartingDefenders = defenders;
    }

    @Override
    public String getName() {
        return "Sea Battle";
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
        // King does not capture
        return false;
    }

    @Override
    public int getKingStrengthMode() {
        // King must be surrounded to capture
        return KING_STRONG;
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
        // Surrounded pieces are doomed
        return true;
    }

    @Override
    public boolean isSpaceHostileToSide(Board board, Coord space, Side side) {
        // Sea Battles has no special spaces
        return false;
    }

    @Override
    public boolean canTaflmanMoveThrough(Board board, char piece, Coord space) {
        // Sea Battles has no special spaces
        return true;
    }

    @Override
    public boolean canTaflmanStopOn(Board board, char piece, Coord space) {
        // Sea Battles has no special spaces
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
    public boolean allowEdgeFortEscapes() {
        // Sea Battles is an edge-escape ruleset
        return false;
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
