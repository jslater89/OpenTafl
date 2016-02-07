package com.manywords.softworks.tafl.rules;

/**
 * Created by jay on 2/6/16.
 */
public class GenericRules extends Rules {

    public GenericRules(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
        mBoard = board;
        mAttackers = attackers;
        mDefenders = defenders;

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

    /*
    ----------- THIS CODE IS FOR LOADING RULES -----------
     */

    private int mBoardSize;
    private int mEscapeType = CORNERS;
    private boolean mSurroundingFatal = true;
    private boolean mAttackersFirst = true;
    private boolean mKingArmed = true;
    private boolean mKingStrong = true;
    private int mKingJumpMode = Taflman.JUMP_NONE;
    private int mCommanderJumpMode = Taflman.JUMP_STANDARD;
    private int mKnightJumpMode = Taflman.JUMP_CAPTURE;
    private int mShieldwallMode = NO_SHIELDWALL;
    private boolean mShieldwallFlankingRequired = true;
    private boolean mEdgeFortEscape = false;
    private int mBerserkMode = BERSERK_NONE;

    public void setEscapeType(int escapeType) {
        mEscapeType = escapeType;
    }

    public void setSurroundingFatal(boolean surroundingFatal) {
        mSurroundingFatal = surroundingFatal;
    }

    public void setAttackersFirst(boolean attackersFirst) {
        mAttackersFirst = attackersFirst;
    }

    public void setKingArmed(boolean kingArmed) {
        mKingArmed = kingArmed;
    }

    public void setKingStrong(boolean kingStrong) {
        mKingStrong = kingStrong;
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

    public void setCenterParameters(boolean[] passable, boolean[] stoppable, boolean[] hostile, boolean[] hostileEmpty) {
        if(passable != null) centerPassableFor = passable;
        if(stoppable != null) centerStoppableFor = stoppable;
        if(hostile != null) centerHostileTo = hostile;
        if(hostileEmpty != null) emptyCenterHostileTo = hostileEmpty;
    }

    public void setCornerParameters(boolean[] passable, boolean[] stoppable, boolean[] hostile) {
        if(passable != null) cornerPassableFor = passable;
        if(stoppable != null) cornerStoppableFor = stoppable;
        if(hostile != null) cornerHostileTo = hostile;
    }

    public void setAttackerFortParameters(boolean[] passable, boolean[] stoppable, boolean[] hostile) {
        if(passable != null) attackerFortPassableFor = passable;
        if(stoppable != null) attackerFortStoppableFor = stoppable;
        if(hostile != null) attackerFortHostileTo = hostile;
    }

    public void setDefenderFortParameters(boolean[] passable, boolean[] stoppable, boolean[] hostile) {
        if(passable != null) defenderFortPassableFor = passable;
        if(stoppable != null) defenderFortStoppableFor = stoppable;
        if(hostile != null) defenderFortHostileTo = hostile;
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

    @Override
    public void setupSpaceGroups(int boardSize) {
        mBoardSize = boardSize;
        setDefaultSpaceGroups();
    }

    @Override
    public boolean isKingArmed() {
        return mKingArmed;
    }

    @Override
    public boolean isKingStrong() {
        return mKingStrong;
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
    public int getMercenaryJumpMode() {
        return Taflman.JUMP_NONE;
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
    public boolean isSpaceHostileToSide(Board board, Coord space, Side side) {
        return false;
    }

    @Override
    public boolean canTaflmanMoveThrough(Board board, char piece, Coord space) {
        return false;
    }

    @Override
    public boolean canTaflmanStopOn(Board board, char piece, Coord space) {
        return false;
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
    public boolean allowShieldFortEscapes() {
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
}
