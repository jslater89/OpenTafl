package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.engine.MoveRecord;

import java.util.List;

public interface GameTreeNode {
    public abstract boolean isMaximizingNode();

    public abstract void setAlpha(short alpha);

    public abstract void setBeta(short beta);

    public abstract short getAlpha();

    public abstract short getBeta();

    public abstract short getValue();

    public abstract int getVictory();

    public abstract short explore(int currentMaxDepth, int overallMaxDepth, short alpha, short beta, AiThreadPool threadPool);

    public abstract short evaluate();

    public abstract MoveRecord getRootMove();

    public abstract MoveRecord getEnteringMove();

    public abstract List<MoveRecord> getEnteringMoveSequence();

    public abstract GameTreeNode getParentNode();

    public abstract GameTreeState getRootNode();

    public abstract void replaceChild(GameTreeNode oldNode, GameTreeNode newNode);

    public abstract int countChildren();

    public abstract int getDepth();

    public abstract List<GameTreeNode> getBranches();

    public abstract GameTreeNode getBestChild();

    public abstract long getZobrist();
}
