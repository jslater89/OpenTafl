package com.manywords.softworks.tafl.engine.ai.alphabeta;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 2/19/16.
 */
public class AlphaBetaGameTreeNodeMethods {
    public static void revalueParent(AlphaBetaGameTreeNode n, int depthOfObservation) {
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

    public static void printChildEvaluations(AlphaBetaGameTreeNode n) {
        for(AlphaBetaGameTreeNode child : n.getBranches()) {
            Log.println(Log.Level.NORMAL, child.getEnteringMove() + " " + child.getDepth() + "d " + child.getValue());
        }
    }

    public static List<List<MoveRecord>> getAllEnteringSequences(AlphaBetaGameTreeNode thisNode) {
        List<AlphaBetaGameTreeNode> branches = thisNode.getBranches();

        if(branches.size() == 0) {
            List<MoveRecord> enteringSequence = thisNode.getEnteringMoveSequence();
            List<List<MoveRecord>> enteringSequences = new ArrayList<>();
            enteringSequences.add(enteringSequence);
            return enteringSequences;
        }
        else {
            List<List<MoveRecord>> enteringSequences = new ArrayList<>();
            for(AlphaBetaGameTreeNode n : branches) {
                List<List<MoveRecord>> childEnteringSequences = getAllEnteringSequences(n);

                enteringSequences.addAll(childEnteringSequences);
            }

            return enteringSequences;
        }
    }

    public static AlphaBetaGameTreeNode getChildForPath(AlphaBetaGameTreeNode root, List<MoveRecord> path) {
        if(path.size() == 0) return root;

        MoveRecord nextMove = path.remove(0);

        for(AlphaBetaGameTreeNode branch : root.getBranches()) {
            if(nextMove.equals(branch.getEnteringMove())) return getChildForPath(branch, path);
        }

        return null;
    }

    public static AlphaBetaGameTreeNode getBestChild(AlphaBetaGameTreeNode root) {
        if (root.getDepth() > 0 && root.getVictory() != AlphaBetaGameTreeState.GOOD_MOVE) return null;

        AlphaBetaGameTreeNode bestMove = null;
        for (AlphaBetaGameTreeNode child : root.getBranches()) {
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

        if(bestMove != null && bestMove.getValue() == FishyEvaluator.NO_VALUE) {
            Log.println(Log.Level.CRITICAL, "Chose an unvalued node as the best move!");
            Log.println(Log.Level.CRITICAL, "Entering path: " + root.getEnteringMoveSequence());
            Log.println(Log.Level.CRITICAL, "Best move: " + bestMove.getEnteringMove());
            Log.println(Log.Level.CRITICAL, "Best move has children? " + bestMove.getBranches());
            Log.println(Log.Level.CRITICAL, "Reported evaluation: " + bestMove.getValue());
            Log.println(Log.Level.CRITICAL, "Best move is a TT hit? " + FishyWorkspace.transpositionTable.getData(bestMove.getZobrist()));
            AlphaBetaGameTreeState state = AlphaBetaGameTreeState.getStateForNode(root.getRootNode(), root);
            Log.println(Log.Level.CRITICAL, "Actual evaluation: " + new FishyEvaluator().evaluate(state, state.mCurrentMaxDepth, state.getDepth()));
            RawTerminal.renderGameState(state);
            Log.println(Log.Level.CRITICAL, state.getPasteableRulesString());
            throw new IllegalStateException();
        }

        return bestMove;
    }
}
