package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.engine.MoveRecord;

import java.util.List;

public interface GameTreeNode {
    MoveRecord getRootMove();
    MoveRecord getEnteringMove();
    List<MoveRecord> getEnteringMoveSequence();
    GameTreeNode getChildForPath(List<MoveRecord> moves);
    GameTreeNode getParentNode();
    GameTreeState getRootNode();
    GameTreeNode getBestChild();
    List<List<MoveRecord>> getAllEnteringSequences();
}
