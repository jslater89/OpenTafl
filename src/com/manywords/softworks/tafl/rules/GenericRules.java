package com.manywords.softworks.tafl.rules;

import com.manywords.softworks.tafl.engine.Utilities;
import com.manywords.softworks.tafl.notation.TaflmanCodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jay on 2/6/16.
 */
public class GenericRules extends Rules {

    public GenericRules(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
        mBoard = board;
        mAttackers = attackers;
        mDefenders = defenders;

        for(int i = 0; i < mSpeedLimits.length; i++) {
            mSpeedLimits[i] = -1;
        }

        for(Side.TaflmanHolder holder : attackers.getStartingTaflmen()) {
            char taflman = holder.packed;

            if(Taflman.getPackedType(taflman) == Taflman.TYPE_COMMANDER) {
                mAttackerCommanders = true;
            }
            if(Taflman.getPackedType(taflman) == Taflman.TYPE_KNIGHT) {
                mAttackerKnights = true;
            }
        }

        for(Side.TaflmanHolder holder : defenders.getStartingTaflmen()) {
            char taflman = holder.packed;

            if(Taflman.getPackedType(taflman) == Taflman.TYPE_COMMANDER) {
                mDefenderCommanders = true;
            }
            if(Taflman.getPackedType(taflman) == Taflman.TYPE_KNIGHT) {
                mDefenderKnights = true;
            }
        }
    }

    @Override
    public String getName() {
        return (mName.equals("") ? "Unknown Tafl" : mName);
    }

    /*
    ----------- THIS CODE IS FOR LOADING RULES -----------
     */

    private String mName = "";
    private int mBoardSize;
    private int mEscapeType = CORNERS;
    private boolean mSurroundingFatal = true;
    private boolean mAttackersFirst = true;
    private int mThreefoldResult = THIRD_REPETITION_DRAWS;
    private int mKingArmedMode = Rules.KING_ARMED;
    private int mKingMode = Rules.KING_STRONG;
    private int mKingJumpMode = Taflman.JUMP_NONE;
    private int mCommanderJumpMode = Taflman.JUMP_STANDARD;
    private int mKnightJumpMode = Taflman.JUMP_CAPTURE;
    private int mShieldwallMode = NO_SHIELDWALL;
    private boolean mShieldwallFlankingRequired = true;
    private boolean mEdgeFortEscape = false;
    private int mBerserkMode = BERSERK_NONE;
    private int mSpeedLimitMode = SPEED_LIMITS_NONE;
    private int[] mSpeedLimits = new int[TAFLMAN_TYPE_COUNT];

    public void setName(String name) { mName = name; }

    public void setEscapeType(int escapeType) {
        mEscapeType = escapeType;
    }

    public void setSurroundingFatal(boolean surroundingFatal) {
        mSurroundingFatal = surroundingFatal;
    }

    public void setAttackersFirst(boolean attackersFirst) {
        mAttackersFirst = attackersFirst;
    }

    public void setThreefoldResult(int threefoldResult) { mThreefoldResult = threefoldResult; }

    public void setKingArmed(int kingArmed) {
        mKingArmedMode = kingArmed;
    }

    public void setKingStrength(int kingStrong) {
        mKingMode = kingStrong;
    }

    public void setKingJumpMode(int kingJumpMode) {
        mKingJumpMode = kingJumpMode;
        reevaluateJumps();
    }

    public void setCommanderJumpMode(int commanderJumpMode) {
        mCommanderJumpMode = commanderJumpMode;
        reevaluateJumps();
    }

    public void setKnightJumpMode(int knightJumpMode) {
        mKnightJumpMode = knightJumpMode;
        reevaluateJumps();
    }

    private void reevaluateJumps() {
        mDefendersJump = false;

        if(mDefenderCommanders && mCommanderJumpMode != Taflman.JUMP_NONE) {
            mDefendersJump = true;
        }
        else if(mDefenderKnights && mKnightJumpMode != Taflman.JUMP_NONE) {
            mDefendersJump = true;
        }
        else if(mKingJumpMode != Taflman.JUMP_NONE) {
            mDefendersJump = true;
        }

        mAttackersJump = false;

        if(mAttackerCommanders && mCommanderJumpMode != Taflman.JUMP_NONE) {
            mAttackersJump = true;
        }
        else if(mAttackerKnights && mKnightJumpMode != Taflman.JUMP_NONE) {
            mAttackersJump = true;
        }
    }

    public void setShieldwallMode(int shieldwallMode) {
        mShieldwallMode = shieldwallMode;
    }

    public void setShieldwallFlankingRequired(boolean shieldwallFlankingRequired) {
        mShieldwallFlankingRequired = shieldwallFlankingRequired;
    }

    public void setEdgeFortEscape(boolean edgeFortEscape) {
        mEdgeFortEscape = edgeFortEscape;
    }

    public void setBerserkMode(int berserkMode) {
        mBerserkMode = berserkMode;
    }

    public void setSpeedLimits(int mode, int[] speeds) {
        mSpeedLimitMode = mode;
        mSpeedLimits = speeds;
    }

    public void setCenterParameters(boolean[] passable, boolean[] stoppable, boolean[] hostile, boolean[] hostileEmpty, boolean[] reenterable) {
        if(passable != null) centerPassableFor = passable;
        if(stoppable != null) centerStoppableFor = stoppable;
        if(hostile != null) centerHostileTo = hostile;
        if(hostileEmpty != null) emptyCenterHostileTo = hostileEmpty;
        if(reenterable != null) centerReenterableFor = reenterable;
    }

    public void setCornerParameters(boolean[] passable, boolean[] stoppable, boolean[] hostile, boolean[] reenterable) {
        if(passable != null) cornerPassableFor = passable;
        if(stoppable != null) cornerStoppableFor = stoppable;
        if(hostile != null) cornerHostileTo = hostile;
        if(reenterable != null) cornerReenterableFor = reenterable;
    }

    public void setAttackerFortParameters(boolean[] passable, boolean[] stoppable, boolean[] hostile, boolean[] reenterable) {
        if(passable != null) attackerFortPassableFor = passable;
        if(stoppable != null) attackerFortStoppableFor = stoppable;
        if(hostile != null) attackerFortHostileTo = hostile;
        if(reenterable != null) attackerFortReenterableFor = reenterable;
    }

    public void setDefenderFortParameters(boolean[] passable, boolean[] stoppable, boolean[] hostile, boolean[] reenterable) {
        if(passable != null) defenderFortPassableFor = passable;
        if(stoppable != null) defenderFortStoppableFor = stoppable;
        if(hostile != null) defenderFortHostileTo = hostile;
        if(reenterable != null) defenderFortReenterableFor = reenterable;
    }

    /*
    ----------- THIS CODE IS FOR RULING GAMES -----------
     */

    private Board mBoard;
    private Side mAttackers;
    private Side mDefenders;

    private boolean mAttackersJump;
    private boolean mDefendersJump;

    private boolean mAttackerCommanders;
    private boolean mDefenderCommanders;
    private boolean mAttackerKnights;
    private boolean mDefenderKnights;

    public void setBoard(Board b) {
        mBoard = b;
    }

    @Override
    public void setupSpaceGroups(int boardSize) {
        mBoardSize = boardSize;
        setDefaultSpaceGroups();
    }

    @Override
    public int getKingArmedMode() {
        return mKingArmedMode;
    }

    @Override
    public int getKingStrengthMode() {
        return mKingMode;
    }

    @Override
    public int getKingJumpMode() {
        return mKingJumpMode;
    }

    @Override
    public int getKnightJumpMode() {
        return mKnightJumpMode;
    }

    @Override
    public int getCommanderJumpMode() {
        return mCommanderJumpMode;
    }

    @Override
    public boolean canSideJump(Side side) {
        if(side.isAttackingSide()) return mAttackersJump;
        else return mDefendersJump;
    }

    @Override
    public int howManyAttackers() {
        return mAttackers.getStartingTaflmen().size();
    }

    @Override
    public int howManyDefenders() {
        return mDefenders.getStartingTaflmen().size();
    }

    @Override
    public int getSpeedLimitMode() {
        return mSpeedLimitMode;
    }

    @Override
    public int getTaflmanSpeedLimit(char taflman) {
        return mSpeedLimits[TaflmanCodes.getIndexForTaflmanChar(taflman)];
    }

    @Override
    public boolean isSpaceHostileToSide(Board board, Coord space, Side side) {
        SpaceType type = board.getSpaceTypeFor(space);

        boolean[] hostilityArray = new boolean[TAFLMAN_TYPE_COUNT];
        switch(type) {
            case CENTER:
                char centerOccupier = board.getOccupier(space);

                hostilityArray = (centerOccupier == Taflman.EMPTY ? emptyCenterHostileTo : centerHostileTo);
                break;
            case CORNER:
                hostilityArray = cornerHostileTo;
                break;
            case ATTACKER_FORT:
                hostilityArray = attackerFortHostileTo;
                break;
            case DEFENDER_FORT:
                hostilityArray = defenderFortHostileTo;
                break;
        }

        if(side.isAttackingSide()) {
            return hostilityArray[TaflmanCodes.t];
        }
        else {
            return hostilityArray[TaflmanCodes.T];
        }
    }

    @Override
    public boolean canTaflmanMoveThrough(Board board, char piece, Coord space) {
        int index = getTaflmanTypeIndexFor(piece);
        SpaceType type = board.getSpaceTypeFor(space);

        boolean[] passabilityArray = new boolean[TAFLMAN_TYPE_COUNT];
        for(int i = 0; i < passabilityArray.length; i++) passabilityArray[i] = true;

        boolean[] reentryArray = new boolean[TAFLMAN_TYPE_COUNT];
        for(int i = 0; i < reentryArray.length; i++) reentryArray[i] = true;

        switch(type) {
            case CENTER:
                passabilityArray = centerPassableFor;
                reentryArray = centerReenterableFor;
                break;
            case CORNER:
                passabilityArray = cornerPassableFor;
                reentryArray = cornerReenterableFor;
                break;
            case ATTACKER_FORT:
                passabilityArray = attackerFortPassableFor;
                reentryArray = attackerFortReenterableFor;
                break;
            case DEFENDER_FORT:
                passabilityArray = defenderFortPassableFor;
                reentryArray = defenderFortReenterableFor;
                break;
        }

        // If we can't reenter the destination type, we can't move through
        // it if we aren't on the same type.
        if(!reentryArray[index]) {
            SpaceType startType = board.getSpaceTypeFor(board.findTaflmanSpace(piece));
            if(startType != type) return false;
        }

        return passabilityArray[index];
    }

    @Override
    public boolean canTaflmanStopOn(Board board, char piece, Coord space) {
        int index = getTaflmanTypeIndexFor(piece);
        SpaceType type = board.getSpaceTypeFor(space);

        boolean[] stoppabilityArray = new boolean[TAFLMAN_TYPE_COUNT];
        for(int i = 0; i < stoppabilityArray.length; i++) stoppabilityArray[i] = true;

        boolean[] reentryArray = new boolean[TAFLMAN_TYPE_COUNT];
        for(int i = 0; i < reentryArray.length; i++) reentryArray[i] = true;

        switch(type) {
            case CENTER:
                stoppabilityArray = centerStoppableFor;
                reentryArray = centerReenterableFor;
                break;
            case CORNER:
                stoppabilityArray = cornerStoppableFor;
                reentryArray = cornerReenterableFor;
                break;
            case ATTACKER_FORT:
                stoppabilityArray = attackerFortStoppableFor;
                reentryArray = attackerFortReenterableFor;
                break;
            case DEFENDER_FORT:
                stoppabilityArray = defenderFortStoppableFor;
                reentryArray = defenderFortReenterableFor;
                break;
        }

        // If there are any non-reenterable spaces, we need to see if we're starting on the same
        // kind of space. If not, we can't make this move.
        if(!reentryArray[index]) {
            SpaceType startType = board.getSpaceTypeFor(board.findTaflmanSpace(piece));
            if(startType != type) return false;
        }

        return stoppabilityArray[index];
    }


    private int getTaflmanTypeIndexFor(char taflman) {
        if(Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS) {
            switch(Taflman.getPackedType(taflman)) {
                case Taflman.TYPE_KING:
                    return TaflmanCodes.k;
                case Taflman.TYPE_KNIGHT:
                    return TaflmanCodes.n;
                case Taflman.TYPE_COMMANDER:
                    return TaflmanCodes.c;
                default:
                    return TaflmanCodes.t;
            }
        }
        else {
            switch(Taflman.getPackedType(taflman)) {
                case Taflman.TYPE_KING:
                    return TaflmanCodes.K;
                case Taflman.TYPE_KNIGHT:
                    return TaflmanCodes.N;
                case Taflman.TYPE_COMMANDER:
                    return TaflmanCodes.C;
                default:
                    return TaflmanCodes.T;
            }
        }
    }

    @Override
    public int allowShieldWallCaptures() {
        return mShieldwallMode;
    }

    @Override
    public boolean allowFlankingShieldwallCapturesOnly() {
        return mShieldwallFlankingRequired;
    }

    @Override
    public boolean allowEdgeFortEscapes() {
        return mEdgeFortEscape;
    }

    @Override
    public int getEscapeType() {
        return mEscapeType;
    }

    @Override
    public int getBerserkMode() {
        return mBerserkMode;
    }

    @Override
    public int threefoldRepetitionResult() {
        return mThreefoldResult;
    }

    @Override
    public Board getBoard() {
        return mBoard;
    }

    @Override
    public Side getAttackers() {
        return mAttackers;
    }

    @Override
    public Side getDefenders() {
        return mDefenders;
    }

    @Override
    public Side getStartingSide() {
        if(mAttackersFirst) return mAttackers;
        else return mDefenders;
    }

    @Override
    public boolean isSurroundingFatal() {
        return mSurroundingFatal;
    }

    public void copyNonDimensionalRules(Rules from) {
        mEscapeType = from.getEscapeType();
        mSurroundingFatal = from.isSurroundingFatal();
        mAttackersFirst = from.getStartingSide().isAttackingSide();
        mThreefoldResult = from.threefoldRepetitionResult();
        mKingArmedMode = from.getKingArmedMode();
        mKingMode = from.getKingStrengthMode();
        mKingJumpMode = from.getKingJumpMode();
        mCommanderJumpMode = from.getCommanderJumpMode();
        mKnightJumpMode = from.getKnightJumpMode();
        mShieldwallMode = from.allowShieldWallCaptures();
        mShieldwallFlankingRequired = from.allowFlankingShieldwallCapturesOnly();
        mEdgeFortEscape = from.allowEdgeFortEscapes();
        mBerserkMode = from.getBerserkMode();
        mSpeedLimitMode = from.getSpeedLimitMode();
        mSpeedLimits = new int[TAFLMAN_TYPE_COUNT];
        Utilities.fillArray(mSpeedLimits, -1);

        if(mSpeedLimitMode == SPEED_LIMITS_IDENTICAL) {
            char taflman = Taflman.TYPE_TAFLMAN | Taflman.SIDE_DEFENDERS;
            Utilities.fillArray(mSpeedLimits, from.getTaflmanSpeedLimit(taflman));
        }
        else if(mSpeedLimitMode == SPEED_LIMITS_BY_SIDE) {
            char taflman = Taflman.TYPE_TAFLMAN | Taflman.SIDE_ATTACKERS;
            Arrays.fill(mSpeedLimits, 0, TAFLMAN_TYPE_COUNT / 2, from.getTaflmanSpeedLimit(taflman));

            taflman = Taflman.TYPE_TAFLMAN | Taflman.SIDE_DEFENDERS;
            Arrays.fill(mSpeedLimits, TAFLMAN_TYPE_COUNT / 2, mSpeedLimits.length, from.getTaflmanSpeedLimit(taflman));
        }
        else if(mSpeedLimitMode == SPEED_LIMITS_BY_TYPE) {
            char taflman = Taflman.TYPE_TAFLMAN | Taflman.SIDE_ATTACKERS;
            mSpeedLimits[TaflmanCodes.t] = from.getTaflmanSpeedLimit(taflman);

            taflman = Taflman.TYPE_COMMANDER | Taflman.SIDE_ATTACKERS;
            mSpeedLimits[TaflmanCodes.c] = from.getTaflmanSpeedLimit(taflman);

            taflman = Taflman.TYPE_KNIGHT | Taflman.SIDE_ATTACKERS;
            mSpeedLimits[TaflmanCodes.n] = from.getTaflmanSpeedLimit(taflman);

            taflman = Taflman.TYPE_KING | Taflman.SIDE_ATTACKERS;
            mSpeedLimits[TaflmanCodes.k] = from.getTaflmanSpeedLimit(taflman);

            taflman = Taflman.TYPE_TAFLMAN | Taflman.SIDE_DEFENDERS;
            mSpeedLimits[TaflmanCodes.T] = from.getTaflmanSpeedLimit(taflman);

            taflman = Taflman.TYPE_COMMANDER | Taflman.SIDE_DEFENDERS;
            mSpeedLimits[TaflmanCodes.C] = from.getTaflmanSpeedLimit(taflman);

            taflman = Taflman.TYPE_KNIGHT | Taflman.SIDE_DEFENDERS;
            mSpeedLimits[TaflmanCodes.N] = from.getTaflmanSpeedLimit(taflman);

            taflman = Taflman.TYPE_KING | Taflman.SIDE_DEFENDERS;
            mSpeedLimits[TaflmanCodes.K] = from.getTaflmanSpeedLimit(taflman);
        }

        System.arraycopy(from.centerPassableFor, 0, centerPassableFor, 0, centerPassableFor.length);
        System.arraycopy(from.centerStoppableFor, 0, centerStoppableFor, 0, centerStoppableFor.length);
        System.arraycopy(from.centerHostileTo, 0, centerHostileTo, 0, centerHostileTo.length);
        System.arraycopy(from.emptyCenterHostileTo, 0, emptyCenterHostileTo, 0, emptyCenterHostileTo.length);
        System.arraycopy(from.centerPassableFor, 0, centerPassableFor, 0, centerPassableFor.length);

        System.arraycopy(from.cornerPassableFor, 0, cornerPassableFor, 0, cornerPassableFor.length);
        System.arraycopy(from.cornerStoppableFor, 0, cornerStoppableFor, 0, cornerStoppableFor.length);
        System.arraycopy(from.cornerHostileTo, 0, cornerHostileTo, 0, cornerHostileTo.length);
        System.arraycopy(from.cornerReenterableFor, 0, cornerReenterableFor, 0, cornerReenterableFor.length);

        System.arraycopy(from.attackerFortPassableFor, 0, attackerFortPassableFor, 0, attackerFortPassableFor.length);
        System.arraycopy(from.attackerFortStoppableFor, 0, attackerFortStoppableFor, 0, attackerFortStoppableFor.length);
        System.arraycopy(from.attackerFortHostileTo, 0, attackerFortHostileTo, 0, attackerFortHostileTo.length);
        System.arraycopy(from.attackerFortReenterableFor, 0, attackerFortReenterableFor, 0, attackerFortReenterableFor.length);

        System.arraycopy(from.defenderFortPassableFor, 0, defenderFortPassableFor, 0, defenderFortPassableFor.length);
        System.arraycopy(from.defenderFortStoppableFor, 0, defenderFortStoppableFor, 0, defenderFortStoppableFor.length);
        System.arraycopy(from.defenderFortHostileTo, 0, defenderFortHostileTo, 0, defenderFortHostileTo.length);
        System.arraycopy(from.defenderFortReenterableFor, 0, defenderFortReenterableFor, 0, defenderFortReenterableFor.length);
    }

    public void copyDimensionalRules(Rules from) {
        mBoardSize = from.boardSize;

        List<Coord> centerSpaces = new ArrayList<>(from.getCenterSpaces());
        List<Coord> cornerSpaces = new ArrayList<>(from.getCornerSpaces());
        List<Coord> attackerForts = new ArrayList<>(from.getAttackerForts());
        List<Coord> defenderForts = new ArrayList<>(from.getDefenderForts());

        setCenterSpaces(new ArrayList<>());
        setCornerSpaces(new ArrayList<>());
        setAttackerForts(new ArrayList<>());
        setDefenderForts(new ArrayList<>());

        setCenterSpaces(centerSpaces);
        setCornerSpaces(cornerSpaces);
        setAttackerForts(attackerForts);
        setDefenderForts(defenderForts);
    }
}
