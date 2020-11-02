package com.manywords.softworks.tafl.engine.ai.alphabeta;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiThreadPool;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinimalAlphaBetaGameTreeNode implements AlphaBetaGameTreeNode {
    public AlphaBetaGameTreeNode mParent;
    public final int mDepth;
    public final MoveRecord mEnteringMove;
    public short mAlpha;
    public short mBeta;
    public short mValue;
    public final List<AlphaBetaGameTreeNode> mBranches;
    public final boolean mCurrentSideAttackers;
    public final long mZobrist;
    public final int mVictory;
    public final int mGameLength;
    public final boolean mValueFromTransposition;

    public MinimalAlphaBetaGameTreeNode(AlphaBetaGameTreeNode root, int depth, int maxDepth, MoveRecord enteringMove, short alpha, short beta, short evaluation, boolean valueFromTransposition, List<AlphaBetaGameTreeNode> branches, boolean currentSideAttackers, long zobrist, int victory, int gameLength) {
        mParent = root;
        mDepth = depth;
        mEnteringMove = enteringMove;
        mBranches = branches;
        mCurrentSideAttackers = currentSideAttackers;
        mZobrist = zobrist;
        mAlpha = alpha;
        mBeta = beta;
        mVictory = victory;
        mGameLength = gameLength;
        mValueFromTransposition = valueFromTransposition;

        // This is a leaf
        if (evaluation != FishyEvaluator.NO_VALUE) {
            mValue = evaluation;
        } else { // This is a branch
            if(root instanceof AlphaBetaGameTreeState) {
                RawTerminal.renderGameState((AlphaBetaGameTreeState) root);
            }
            throw new IllegalStateException("MinimalGameTreeNode created for unvalued state");
        }
    }

    public boolean isMaximizingNode() {
        return mCurrentSideAttackers;
    }

    @Override
    public void setAlpha(short alpha) {
        mAlpha = alpha;
    }

    @Override
    public void setBeta(short beta) {
        mBeta = beta;
    }

    @Override
    public void setValue(short value) {
        mValue = value;
    }

    public short getAlpha() {
        return mAlpha;
    }

    public short getBeta() {
        return mBeta;
    }

    public short evaluate() {
        return mValue;
    }

    @Override
    public AlphaBetaGameTreeNode explore(int currentMaxDepth, int overallMaxDepth, short alpha, short beta, AiThreadPool threadPool, boolean continuation) {
        // First, we have to get a full game tree state for this node:
        AlphaBetaGameTreeState thisState = AlphaBetaGameTreeState.getStateForNode(getRootNode(), this);
        return thisState.explore(currentMaxDepth, overallMaxDepth, alpha, beta, threadPool, continuation);
    }

    @Override
    public MoveRecord getRootMove() {
        MoveRecord move = mParent.getEnteringMove();

        if (move == null) return this.getEnteringMove();
        else return mParent.getRootMove();
    }

    @Override
    public MoveRecord getEnteringMove() {
        return mEnteringMove;
    }

    @Override
    public List<MoveRecord> getEnteringMoveSequence() {
        List<MoveRecord> moves = new ArrayList<MoveRecord>(mDepth);

        moves.add(getEnteringMove());
        AlphaBetaGameTreeNode parent = getParentNode();

        while (parent != null && parent.getEnteringMove() != null) {
            moves.add(parent.getEnteringMove());
            parent = parent.getParentNode();
        }

        Collections.reverse(moves);

        return moves;
    }

    @Override
    public AlphaBetaGameTreeNode getChildForPath(List<MoveRecord> moves) {
        return AlphaBetaGameTreeNodeMethods.getChildForPath(this, moves);
    }

    @Override
    public AlphaBetaGameTreeState getRootNode() {
        if (mParent == null) throw new IllegalStateException("MinimalGameTreeNode has no parent!");

        AlphaBetaGameTreeNode state = mParent;
        while (true) {
            if (state.getParentNode() == null) return (AlphaBetaGameTreeState) state;
            else state = state.getParentNode();
        }
    }

    @Override
    public boolean valueFromTransposition() {
        return mValueFromTransposition;
    }

    @Override
    public AlphaBetaGameTreeNode getParentNode() {
        return mParent;
    }

    public AlphaBetaGameTreeNode getBestChild() {
        return AlphaBetaGameTreeNodeMethods.getBestChild(this);
    }

    @Override
    public List<List<MoveRecord>> getAllEnteringSequences() {
        return AlphaBetaGameTreeNodeMethods.getAllEnteringSequences(this);
    }

    public int getDepth() {
        return mDepth;
    }

    public short getValue() {
        return mValue;
    }

    public int getVictory() {
        return mVictory;
    }

    public void replaceChild(AlphaBetaGameTreeNode oldNode, AlphaBetaGameTreeNode newNode) {
        mBranches.set(mBranches.indexOf(oldNode), newNode);
    }

    public void setParent(AlphaBetaGameTreeNode newParent) {
        mParent = newParent;
    }

    public int countChildren(int depth) {
        if(getDepth() == depth) return 1;

        int total = 0;
        for (AlphaBetaGameTreeNode node : mBranches) {
            total += node.countChildren(depth);
        }

        if (total == 0) {
            return 1;
        } else {
            return total;
        }
    }

    @Override
    public long getZobrist() {
        return mZobrist;
    }

    @Override
    public void revalueParent(int depthOfObservation) {
        AlphaBetaGameTreeNodeMethods.revalueParent(this, depthOfObservation);
    }

    @Override
    public List<AlphaBetaGameTreeNode> getBranches() {
        return mBranches;
    }

    @Override
    public void printChildEvaluations() {
        AlphaBetaGameTreeNodeMethods.printChildEvaluations(this);
    }
}
