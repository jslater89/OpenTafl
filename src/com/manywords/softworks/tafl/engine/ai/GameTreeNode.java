package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.engine.MoveRecord;

import java.util.List;

public interface GameTreeNode {
    public abstract boolean isMaximizingNode();

    public abstract void setAlpha(short alpha);

    public abstract void setBeta(short beta);

    public abstract void setValue(short value);

    public abstract short getAlpha();

    public abstract short getBeta();

    public abstract short getValue();

    public abstract int getVictory();

    public abstract GameTreeNode explore(int currentMaxDepth, int overallMaxDepth, short alpha, short beta, AiThreadPool threadPool, boolean continuation);

    public abstract short evaluate();

    public abstract MoveRecord getRootMove();

    public abstract MoveRecord getEnteringMove();

    public abstract List<MoveRecord> getEnteringMoveSequence();

    public abstract GameTreeNode getChildForPath(List<MoveRecord> moves);

    public abstract GameTreeNode getParentNode();

    public abstract GameTreeState getRootNode();

    public abstract boolean valueFromTransposition();

    public abstract void replaceChild(GameTreeNode oldNode, GameTreeNode newNode);

    public abstract void setParent(GameTreeNode newParent);

    public abstract int countChildren(int toDepth);

    public abstract int getDepth();

    public abstract List<GameTreeNode> getBranches();

    public abstract GameTreeNode getBestChild();

    public abstract List<List<MoveRecord>> getAllEnteringSequences();

    public abstract long getZobrist();

    public abstract void revalueParent(int depthOfObservation);

    public abstract void printChildEvaluations();
}
