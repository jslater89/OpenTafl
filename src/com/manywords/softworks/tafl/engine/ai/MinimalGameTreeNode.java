package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinimalGameTreeNode implements GameTreeNode {
    public GameTreeNode mParent;
    public final int mDepth;
    public final MoveRecord mEnteringMove;
    public short mAlpha;
    public short mBeta;
    public short mValue;
    public final List<GameTreeNode> mBranches;
    public final boolean mCurrentSideAttackers;
    public final long mZobrist;
    public final int mVictory;
    public final int mGameLength;

    public MinimalGameTreeNode(GameTreeNode root, int depth, int maxDepth, MoveRecord enteringMove, short alpha, short beta, short evaluation, List<GameTreeNode> branches, boolean currentSideAttackers, long zobrist, int victory, int gameLength) {
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

        // This is a leaf
        if (evaluation != Evaluator.NO_VALUE) {
            mValue = evaluation;
        } else { // This is a branch
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
    public short explore(int currentMaxDepth, int overallMaxDepth, short alpha, short beta, AiThreadPool threadPool) {
        // First, we have to get a full game tree state for this node:
        GameTreeState thisState = GameTreeState.getStateForMinimalNode(getRootNode(), this);
        thisState.explore(currentMaxDepth, overallMaxDepth, alpha, beta, threadPool);
        return 0;
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
        GameTreeNode parent = getParentNode();

        while (parent != null && parent.getEnteringMove() != null) {
            moves.add(parent.getEnteringMove());
            parent = parent.getParentNode();
        }

        Collections.reverse(moves);

        return moves;
    }

    @Override
    public GameTreeState getRootNode() {
        if (mParent == null) throw new IllegalStateException("MinimalGameTreeNode has no parent!");

        GameTreeNode state = mParent;
        while (true) {
            if (state.getParentNode() == null) return (GameTreeState) state;
            else state = state.getParentNode();
        }
    }

    @Override
    public GameTreeNode getParentNode() {
        return mParent;
    }

    public GameTreeNode getBestChild() {
        if (this.mVictory != GameTreeState.GOOD_MOVE) return null;

        GameTreeNode bestMove = null;
        for (GameTreeNode child : getBranches()) {
            if(getRootNode().mGame.historyContainsHash(child.getZobrist())) {
                // Don't make moves that repeat board states.
                // We have to have this here, in addition to the exploration
                // function, in case of transposition table hits, which don't
                // keep track of move repetitions.
                continue;
            }
            else if (bestMove == null) {
                bestMove = child;
                continue;
            }
            else if (mCurrentSideAttackers) {
                // Attackers maximize
                if (child.getValue() > bestMove.getValue()) {
                    bestMove = child;
                }
            } else {
                // Defenders minimize
                if (child.getValue() < bestMove.getValue()) {
                    bestMove = child;
                }
            }
        }

        return bestMove;
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

    public void replaceChild(GameTreeNode oldNode, GameTreeNode newNode) {
        mBranches.set(mBranches.indexOf(oldNode), newNode);
    }

    public void setParent(GameTreeNode newParent) {
        mParent = newParent;
    }

    public int countChildren(int depth) {
        if(getDepth() == depth) return 1;

        int total = 0;
        for (GameTreeNode node : mBranches) {
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
        GameTreeNodeMethods.revalueParent(this, depthOfObservation);
    }

    @Override
    public List<GameTreeNode> getBranches() {
        return mBranches;
    }

}
