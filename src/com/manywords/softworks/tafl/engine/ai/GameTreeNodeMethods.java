package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.util.ArrayList;
import java.util.List;

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

    public static void printChildEvaluations(GameTreeNode n) {
        for(GameTreeNode child : n.getBranches()) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, child.getEnteringMove() + " " + child.getDepth() + "d " + child.getValue());
        }
    }

    public static List<List<MoveRecord>> getAllEnteringSequences(GameTreeNode thisNode) {
        List<GameTreeNode> branches = thisNode.getBranches();

        if(branches.size() == 0) {
            List<MoveRecord> enteringSequence = thisNode.getEnteringMoveSequence();
            List<List<MoveRecord>> enteringSequences = new ArrayList<>();
            enteringSequences.add(enteringSequence);
            return enteringSequences;
        }
        else {
            List<List<MoveRecord>> enteringSequences = new ArrayList<>();
            for(GameTreeNode n : branches) {
                List<List<MoveRecord>> childEnteringSequences = getAllEnteringSequences(n);

                enteringSequences.addAll(childEnteringSequences);
            }

            return enteringSequences;
        }
    }

    public static GameTreeNode getChildForPath(GameTreeNode root, List<MoveRecord> path) {
        if(path.size() == 0) return root;

        MoveRecord nextMove = path.remove(0);

        for(GameTreeNode branch : root.getBranches()) {
            if(nextMove.equals(branch.getEnteringMove())) return getChildForPath(branch, path);
        }

        return null;
    }

    public static GameTreeNode getBestChild(GameTreeNode root) {
        if (root.getDepth() > 0 && root.getVictory() != GameTreeState.GOOD_MOVE) return null;

        GameTreeNode bestMove = null;
        for (GameTreeNode child : root.getBranches()) {
            if (bestMove == null) {
                bestMove = child;
                continue;
            }
            else if (root.isMaximizingNode()) {
                // Attackers maximize
                if (child.getValue() == bestMove.getValue()) {
                    //if(Math.random() > 0.5) bestMove = child;
                }
                else if (child.getValue() > bestMove.getValue()) {
                    bestMove = child;
                }
            }
            else {
                // Defenders minimize
                if (child.getValue() == bestMove.getValue()) {
                    //if(Math.random() > 0.5) bestMove = child;
                }
                else if (child.getValue() < bestMove.getValue()) {
                    bestMove = child;
                }
            }
        }

        if(bestMove != null && bestMove.getValue() == Evaluator.NO_VALUE) {
            System.out.println(bestMove);
            List<GameTreeNode> path = new ArrayList<>();

            GameTreeNode parent = bestMove.getParentNode();
            while(parent != null) {
                path.add(parent);
                parent = parent.getParentNode();
            }

            System.out.println(path);

            System.out.println(bestMove.getDepth());
            System.out.println(bestMove.getBranches().size());
            System.out.println(bestMove.getValue());

            throw new IllegalStateException();
        }

        return bestMove;
    }
}
