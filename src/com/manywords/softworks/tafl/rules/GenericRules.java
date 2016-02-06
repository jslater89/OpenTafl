package com.manywords.softworks.tafl.rules;

/**
 * Created by jay on 2/6/16.
 */
public class GenericRules extends Rules {

    public GenericRules(Board board, Side attackers, Side defenders) {
        super(board, attackers, defenders);
        mBoard = board;
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

    @Override
    public void setupSpaceGroups(int boardSize) {
        mBoardSize = boardSize;
        setDefaultSpaceGroups();
    }

    @Override
    public boolean isKingArmed() {
        return false;
    }

    @Override
    public boolean isKingStrong() {
        return false;
    }

    @Override
    public int getKingJumpMode() {
        return 0;
    }

    @Override
    public int getKnightJumpMode() {
        return 0;
    }

    @Override
    public int getCommanderJumpMode() {
        return 0;
    }

    @Override
    public int getMercenaryJumpMode() {
        return 0;
    }

    @Override
    public boolean canSideJump(Side side) {
        return false;
    }

    @Override
    public int howManyAttackers() {
        return 0;
    }

    @Override
    public int howManyDefenders() {
        return 0;
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
        return 0;
    }

    @Override
    public boolean allowFlankingShieldwallCapturesOnly() {
        return false;
    }

    @Override
    public boolean allowShieldFortEscapes() {
        return false;
    }

    @Override
    public int getEscapeType() {
        return 0;
    }

    @Override
    public int getBerserkMode() {
        return 0;
    }

    @Override
    public Board getBoard() {
        return mBoard;
    }

    @Override
    public Side getAttackers() {
        return null;
    }

    @Override
    public Side getDefenders() {
        return null;
    }

    @Override
    public Side getStartingSide() {
        return null;
    }

    @Override
    public boolean isSurroundingFatal() {
        return false;
    }
}
