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
