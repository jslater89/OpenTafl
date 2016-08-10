package com.manywords.softworks.tafl.rules.tablut;

import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.tablut.nine.Tablut9Attackers;
import com.manywords.softworks.tafl.rules.tablut.nine.Tablut9Board;
import com.manywords.softworks.tafl.rules.tablut.nine.Tablut9Defenders;
import com.manywords.softworks.tafl.rules.tablut.nine.test.CenterKingCaptureAttackers;
import com.manywords.softworks.tafl.rules.tablut.nine.test.CenterKingCaptureDefenders;

import java.util.ArrayList;
import java.util.List;


public class FotevikenTablut extends Rules {
    public static FotevikenTablut newFotevikenTablut9() {
        Tablut9Board board = new Tablut9Board();
        Tablut9Attackers attackers = new Tablut9Attackers(board);
        Tablut9Defenders defenders = new Tablut9Defenders(board);

        FotevikenTablut rules = new FotevikenTablut(board, attackers, defenders);
        return rules;
    }

    public static FotevikenTablut newCenterKingCaptureTest() {
        Tablut9Board board = new Tablut9Board();
        Tablut9Attackers attackers = new CenterKingCaptureAttackers(board);
        Tablut9Defenders defenders = new CenterKingCaptureDefenders(board);

        FotevikenTablut rules = new FotevikenTablut(board, attackers, defenders);
        return rules;
    }

    public FotevikenTablut(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
        mStartingBoard = board;
        mStartingBoard.setRules(this);
        mStartingAttackers = attackers;
        mStartingDefenders = defenders;
    }

    @Override
    public String getName() {
        return "Foteviken Tablut";
    }

    private Board mStartingBoard;
    private Side mStartingAttackers;
    private Side mStartingDefenders;

    @Override
    public void setupSpaceGroups(int boardSize) {
        setDefaultSpaceGroups();

        // No corners
        setCornerSpaces(new ArrayList<Coord>());

        // Attacker forts
        List<Coord> forts = new ArrayList<>(12);
        forts.add(Coord.get(0, 3));
        forts.add(Coord.get(0, 4));
        forts.add(Coord.get(0, 5));
        forts.add(Coord.get(1, 4));

        forts.add(Coord.get(3, 0));
        forts.add(Coord.get(4, 0));
        forts.add(Coord.get(5, 0));
        forts.add(Coord.get(4, 1));

        forts.add(Coord.get(8, 3));
        forts.add(Coord.get(8, 4));
        forts.add(Coord.get(8, 5));
        forts.add(Coord.get(7, 4));

        forts.add(Coord.get(3, 8));
        forts.add(Coord.get(4, 8));
        forts.add(Coord.get(5, 8));
        forts.add(Coord.get(4, 7));
        setAttackerForts(forts);

        // Empty center hostile to kings only
        emptyCenterHostileTo = RulesSerializer.getTaflmanTypeListForString("K");

        // Occupied center hostile to nobody
        centerHostileTo = RulesSerializer.getTaflmanTypeListForString("");

        // Nobody can stop on the center
        centerStoppableFor = RulesSerializer.getTaflmanTypeListForString("");

        // Nobody can move through the center
        centerPassableFor = RulesSerializer.getTaflmanTypeListForString("tcnkTCNK");
        centerReenterableFor = RulesSerializer.getTaflmanTypeListForString("");

        // Attacker forts hostile to kings only
        attackerFortHostileTo = RulesSerializer.getTaflmanTypeListForString("K");

        // Nobody can re-enter attacker forts
        attackerFortPassableFor = RulesSerializer.getTaflmanTypeListForString("tcnkTCNK");
        attackerFortReenterableFor = RulesSerializer.getTaflmanTypeListForString("");
    }

    @Override
    public boolean isKingArmed() {
        return true;
    }

    @Override
    public int getKingStrengthMode() {
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
        Coord startingSpace = board.findTaflmanSpace(piece);
        SpaceType type = board.getSpaceTypeFor(space);
        SpaceType startType = board.getSpaceTypeFor(startingSpace);

        // Nobody can move through the center under any circumstances
        if(type == SpaceType.CENTER) return false;

        // Nobody can move through an attacker fort unless they're already on an attacker fort
        if(startType != SpaceType.ATTACKER_FORT && type == SpaceType.ATTACKER_FORT) return false;

        else return true;
    }

    @Override
    public boolean canTaflmanStopOn(Board board, char piece, Coord space) {
        Coord startingSpace = board.findTaflmanSpace(piece);
        SpaceType type = board.getSpaceTypeFor(space);
        SpaceType startType = board.getSpaceTypeFor(startingSpace);

        // Nobody can stop on the center
        if(type == SpaceType.CENTER) return false;

        // Nobody who isn't already in an attacker fort can stop on an attacker fort
        if(type == SpaceType.ATTACKER_FORT && startType != SpaceType.ATTACKER_FORT) return false;

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
