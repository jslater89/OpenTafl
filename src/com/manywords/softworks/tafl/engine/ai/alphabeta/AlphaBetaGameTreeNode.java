package com.manywords.softworks.tafl.engine.ai.alphabeta;

import com.manywords.softworks.tafl.engine.ai.AiThreadPool;
import com.manywords.softworks.tafl.engine.ai.GameTreeNode;

import java.util.List;

public interface AlphaBetaGameTreeNode extends GameTreeNode {
    boolean isMaximizingNode();

    void setAlpha(short alpha);

    void setBeta(short beta);

    void setValue(short value);

    short getAlpha();

    short getBeta();

    short getValue();

    int getVictory();

    AlphaBetaGameTreeNode explore(int currentMaxDepth, int overallMaxDepth, short alpha, short beta, AiThreadPool threadPool, boolean continuation);

    short evaluate();

    boolean valueFromTransposition();

    void replaceChild(AlphaBetaGameTreeNode oldNode, AlphaBetaGameTreeNode newNode);

    void setParent(AlphaBetaGameTreeNode newParent);

    int countChildren(int toDepth);

    int getDepth();

    List<AlphaBetaGameTreeNode> getBranches();

    long getZobrist();

    void revalueParent(int depthOfObservation);

    void printChildEvaluations();

    @Override
    AlphaBetaGameTreeNode getParentNode();

    @Override
    AlphaBetaGameTreeState getRootNode();

    @Override
    AlphaBetaGameTreeNode getBestChild();
}
