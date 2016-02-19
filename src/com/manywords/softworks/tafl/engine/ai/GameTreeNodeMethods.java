package com.manywords.softworks.tafl.engine.ai;

/**
 * Created by jay on 2/19/16.
 */
// TODO: refactor GameTreeState and MinimalGameTreeNode shared code to here, with an arg of type GameTreeNode
public class GameTreeNodeMethods {
    public static void revalueParent(GameTreeNode n, int depthOfObservation) {
        if(n.getParentNode() != null) {
            if (n.getParentNode().isMaximizingNode()) {
                n.getParentNode().setValue((short) Math.max(n.getParentNode().getValue(), n.getValue()));
            }
            else {
                n.getParentNode().setValue((short) Math.min(n.getParentNode().getValue(), n.getValue()));
            }

            n.getParentNode().revalueParent(depthOfObservation);
        }
    }
}
