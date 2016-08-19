package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.manywords.softworks.tafl.rules.Taflman.EMPTY;

public class GameTreeState extends GameState implements GameTreeNode {
    public static AiWorkspace workspace;

    public static final int DEFENDER = -1;
    public static final int ATTACKER = 1;

    public GameTreeNode mParent;
    public final int mDepth;
    public int mCurrentMaxDepth;
    public short mAlpha;
    public short mBeta;
    public short mValue = Evaluator.NO_VALUE;
    public List<GameTreeNode> mBranches = new ArrayList<GameTreeNode>();

    private boolean mContinuation = false;

    private String debugOutputString = null;

    public GameTreeState(AiWorkspace workspace, GameState copyState) {
        super(copyState);
        mGame = workspace;

        mZobristHash = copyState.mZobristHash;
        setCurrentSide((copyState.getCurrentSide().isAttackingSide() ? getAttackers() : getDefenders()));

        mEnteringMove = null;

        mAlpha = -5000;
        mBeta = 5000;
        mDepth = 0;
        mParent = null;
    }

    public GameTreeState(GameTreeState copyState) {
        super(copyState);
        mGame = workspace;
        mParent = copyState.mParent;
        mAlpha = copyState.mAlpha;
        mBeta = copyState.mBeta;
        mDepth = copyState.mDepth;
    }

    public GameTreeState considerMove(Coord start, Coord end) {
        char toMove = getBoard().getOccupier(start);
        GameState nextGameState = moveTaflman(toMove, end);
        mGame.advanceState(this, nextGameState, nextGameState.getBerserkingTaflman() == EMPTY, nextGameState.getBerserkingTaflman(), true);

        // result should be good move except in cases like berserk,
        // where most moves on a berserk turn are illegal.
        if(nextGameState.getLastMoveResult() >= GOOD_MOVE) {
            GameTreeState nextState = new GameTreeState(workspace, nextGameState, this);

            return nextState;
        }
        else {
            //System.out.println("Bad move " + result);
            return null;
        }
    }

    public GameTreeState(AiWorkspace workspace, GameState advanceFrom, GameTreeState realParent) {
        super(advanceFrom);

        mParent = realParent;
        mAlpha = mParent.getAlpha();
        mBeta = mParent.getBeta();
        mDepth = ((GameTreeState) mParent).mDepth + 1;
    }

    public short getValue() {
        return mValue;
    }

    public int getVictory() {
        return mVictory;
    }

    public List<GameTreeNode> getBestPath() {
        return getPathForChild(getBestChild());
    }

    public List<GameTreeNode> getNthPath(int i) {
        return getPathForChild(getNthChild(i));
    }

    public GameTreeNode getNthChild(int i) {
        getBranches().sort((o1, o2) -> {
            if(isMaximizingNode()) {
                return -(o1.getValue() - o2.getValue());
            }
            else {
                // low to high
                return (o1.getValue() - o2.getValue());
            }
        });

        return getBranches().get(i);
    }

    public static List<GameTreeNode> getPathForChild(GameTreeNode node) {
        List<GameTreeNode> bestPath = new ArrayList<GameTreeNode>();

        while (node != null) {
            bestPath.add(node);
            node = node.getBestChild();
        }

        return bestPath;
    }

    public GameTreeNode getBestChild() {
        GameTreeNode bestMove = null;
        for (GameTreeNode child : getBranches()) {
            if (bestMove == null) {
                bestMove = child;
                continue;
            }
            else if (isMaximizingNode()) {
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

        return bestMove;
    }

    public boolean isMaximizingNode() {
        return getCurrentSide().isAttackingSide();
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
        if (mValue != Evaluator.NO_VALUE) return mValue;
        else return workspace.evaluator.evaluate(this, mCurrentMaxDepth, mDepth);
    }

    public void replaceChild(GameTreeNode oldNode, GameTreeNode newNode) {
        mBranches.set(mBranches.indexOf(oldNode), newNode);
    }

    public void setParent(GameTreeNode newParent) {
        mParent = newParent;
    }

    public MoveRecord getEnteringMove() {
        return mEnteringMove;
    }

    public MoveRecord getRootMove() {
        MoveRecord move = mParent.getEnteringMove();

        if (move == null) return this.getEnteringMove();
        else return mParent.getRootMove();
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
        if (mParent == null) return this;

        GameTreeNode state = mParent;
        while (true) {
            if (state.getParentNode() == null) return (GameTreeState) state;
            else state = state.getParentNode();
        }
    }

    public GameTreeNode getParentNode() {
        return mParent;
    }

    public int getDepth() {
        return mDepth;
    }

    public List<GameTreeNode> getBranches() {
        return mBranches;
    }

    public short explore(int currentMaxDepth, int overallMaxDepth, short alpha, short beta, AiThreadPool threadPool, boolean continuation) {
        mContinuation = continuation;
        mCurrentMaxDepth = currentMaxDepth;

        setAlpha(alpha);
        setBeta(beta);

        int remainingDepth = mCurrentMaxDepth - mDepth;

        boolean extension = false;
        if(overallMaxDepth < currentMaxDepth) {
            extension = true;
        }

        if(extension && mDepth < overallMaxDepth) {
            if(workspace.mNoTime) return mValue;
            if(mVictory > GOOD_MOVE) return mValue;

            continuationOnChildren(currentMaxDepth, overallMaxDepth);

            return mValue;
        }

        short cachedValue = Evaluator.NO_VALUE;
        if(!extension && !continuation) {
            cachedValue = AiWorkspace.transpositionTable.getValue(getZobrist(), remainingDepth, mGameLength);
        }
        else if(mDepth > overallMaxDepth) {
            // If we're in an extension and past the point we've already explored, transposition table hits are allowed.
            // No transposition hits allowed in the first level of depthin', I guess.
            cachedValue = AiWorkspace.transpositionTable.getValue(getZobrist(), remainingDepth, mGameLength);
        }


        if (cachedValue != Evaluator.NO_VALUE && mDepth > 0) {
            mValue = cachedValue;

            if (getCurrentSide().isAttackingSide()) {
                mAlpha = (short) Math.max(mAlpha, mValue);

                //System.out.println("Depth " + mDepth + " Attacker value " + mValue + " Alpha " + mAlpha + " Beta " + mBeta);
                if (mBeta <= mAlpha && mDepth < workspace.mBetaCutoffs.length) {
                    workspace.mBetaCutoffs[mDepth]++;
                    workspace.mBetaCutoffDistances[mDepth] += 1;
                }
            } else {
                mBeta = (short) Math.min(mBeta, mValue);

                //System.out.println("Depth " + mDepth + " Defender value " + mValue + " Alpha " + mAlpha + " Beta " + mBeta);
                if (mBeta <= mAlpha && mDepth < workspace.mAlphaCutoffs.length) {
                    workspace.mAlphaCutoffs[mDepth]++;
                    workspace.mAlphaCutoffDistances[mDepth] += 1;
                }
            }

            MinimalGameTreeNode smallChild = new MinimalGameTreeNode(mParent, mDepth, currentMaxDepth, mEnteringMove, mAlpha, mBeta, mValue, mBranches, getCurrentSide().isAttackingSide(), mZobristHash, GameState.TRANSPOSITION_HIT, mGameLength);
            mParent.replaceChild(GameTreeState.this, smallChild);
        } else if (mDepth != 0 && (checkVictory() != GOOD_MOVE || mDepth >= currentMaxDepth || (workspace.mNoTime) || (!extension && workspace.mExtensionTime) || (extension && continuation && workspace.mExtensionTime))) {
            // If this is a victory, evaluate and stop exploring.
            // If we've hit the target depth, evaluate and stop exploring.
            // If we're out of time and this isn't the root node, stop exploring.
            // If we're in extension time and not in extension search, stop exploring.
            // If we're at depth 0, go explore another level, just to be safe.
            mValue = evaluate();

            // Leaf nodes we don't get to finish are always in the transposition table at depth 0.
            // This also holds for e.g. continuation search and other things.
            AiWorkspace.transpositionTable.putValue(getZobrist(), mValue, 0, mGameLength);

            // Replace small child
            MinimalGameTreeNode smallChild = new MinimalGameTreeNode(mParent, mDepth, currentMaxDepth, mEnteringMove, mAlpha, mBeta, mValue, mBranches, getCurrentSide().isAttackingSide(), mZobristHash, mVictory, mGameLength);
            mParent.replaceChild(GameTreeState.this, smallChild);
        } else {
            this.mValue = Evaluator.NO_VALUE;
            exploreChildren(currentMaxDepth, overallMaxDepth, continuation, extension);
            revalueParent(mDepth);
        }

        return mValue;
    }

    public static GameTreeState getStateForMinimalNode(GameTreeState rootNode, MinimalGameTreeNode minimalGameTreeNode) {
        GameTreeState desiredState = rootNode;
        for (MoveRecord m : minimalGameTreeNode.getEnteringMoveSequence()) {
            desiredState = desiredState.considerMove(m.start, m.end);
        }

        desiredState.mParent = minimalGameTreeNode.getParentNode();
        desiredState.mAlpha = minimalGameTreeNode.getAlpha();
        desiredState.mBeta = minimalGameTreeNode.getBeta();
        desiredState.mValue = minimalGameTreeNode.getValue();
        desiredState.mBranches = minimalGameTreeNode.getBranches();
        desiredState.mVictory = minimalGameTreeNode.getVictory();

        for(GameTreeNode node : minimalGameTreeNode.getBranches()) {
            node.setParent(desiredState);
        }
        desiredState.getParentNode().replaceChild(minimalGameTreeNode, desiredState);
        return desiredState;
    }

    private boolean treeParentsContainHash(long zobrist) {
        GameTreeNode parent = getParentNode();
        while(parent != null) {
            if(parent.getZobrist() == zobrist) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }

    public void exploreChildren(int currentMaxDepth, int overallMaxDepth, boolean continuation, boolean extension) {
        List<MoveRecord> successorMoves = new ArrayList<>();

        if(!continuation || getBranches().size() == 0) {
            successorMoves = generateSuccessorMoves(currentMaxDepth);
        }

        boolean cutoff = false;
        int cutoffType = 0;

        int distanceToFirstCutoff = 0;
        boolean savedDistanceToFirstCutoff = false;
        for (MoveRecord move : successorMoves) {
            if (cutoff) {
                break;
            }

            GameTreeState node = considerMove(move.start, move.end);
            // Node will be null in e.g. berserk tafl, where moves are legal
            // according to the movement rules, but not legal according to
            // special rules, like the berserk rule.
            if(node == null) {
                continue;
            }

            mBranches.add(node);
            node.explore(currentMaxDepth, overallMaxDepth, mAlpha, mBeta, null, mContinuation);
            distanceToFirstCutoff++;

            short evaluation = node.getValue();

            // A/B pruning
            if (evaluation != Evaluator.NO_VALUE) {
                if (mValue == Evaluator.NO_VALUE) {
                    mValue = evaluation;
                }

                cutoff = handleEvaluationResults(evaluation, distanceToFirstCutoff);

                if(cutoff) {
                    AiWorkspace.killerMoveTable.putMove(mDepth, move);
                }
            }
        }

        // If we have no legal moves, the other side wins
        if(successorMoves.size() == 0) {
            if(getCurrentSide().isAttackingSide()) {
                mVictory = DEFENDER_WIN;
            }
            else {
                mVictory = ATTACKER_WIN;
            }
            mValue = evaluate();
        }


        // TODO: try putting continuation search/extension search into the transposition table at a depth penalty, since
        // they're useful but not exact.
        if(!continuation && !extension) AiWorkspace.transpositionTable.putValue(getZobrist(), mValue, currentMaxDepth - mDepth, mGameLength);

        minifyState();
    }

    public void continuationOnChildren(int currentMaxDepth, int overallMaxDepth) {
        List<GameTreeNode> successorStates = getBranches();

        List<MoveRecord> successorMoves = generateSuccessorMoves(currentMaxDepth);

        boolean cutoff = false;
        int cutoffType = 0;

        int distanceToFirstCutoff = 0;
        boolean savedDistanceToFirstCutoff = false;

        for(GameTreeNode node : successorStates) {
            if (cutoff) {
                break;
            }

            // Node will be null in e.g. berserk tafl, where moves are legal
            // according to the movement rules, but not legal according to
            // special rules, like the berserk rule.
            if (node == null) {
                continue;
            }

            successorMoves.remove(node.getEnteringMove());

            node.explore(currentMaxDepth, overallMaxDepth, mAlpha, mBeta, null, mContinuation);
            short evaluation = node.getValue();
            distanceToFirstCutoff++;

            if (evaluation != Evaluator.NO_VALUE) {
                if (mValue == Evaluator.NO_VALUE) {
                    mValue = evaluation;
                }

                cutoff = handleEvaluationResults(evaluation, distanceToFirstCutoff);
            }
        }

        if(!cutoff && successorMoves.size() > 0) {
            for (MoveRecord move : successorMoves) {
                if (cutoff) {
                    break;
                }

                GameTreeState node = considerMove(move.start, move.end);
                // Node will be null in e.g. berserk tafl, where moves are legal
                // according to the movement rules, but not legal according to
                // special rules, like the berserk rule.
                if(node == null) {
                    continue;
                }

                mBranches.add(node);
                node.explore(currentMaxDepth, overallMaxDepth, mAlpha, mBeta, null, mContinuation);
                distanceToFirstCutoff++;

                short evaluation = node.getValue();

                // A/B pruning
                if (evaluation != Evaluator.NO_VALUE) {
                    if (mValue == Evaluator.NO_VALUE) {
                        mValue = evaluation;
                    }

                    cutoff = handleEvaluationResults(evaluation, distanceToFirstCutoff);

                    if(cutoff) {
                        AiWorkspace.killerMoveTable.putMove(mDepth, move);
                    }
                }
            }
        }

        // If we have no legal moves, the other side wins
        if(successorMoves.size() == 0 && successorStates.size() == 0) {
            if(getCurrentSide().isAttackingSide()) {
                mVictory = DEFENDER_WIN;
            }
            else {
                mVictory = ATTACKER_WIN;
            }
            mValue = evaluate();
        }
        
        minifyState();
/*
        System.out.println("CONTINUATION");
        System.out.println("My depth: " + mDepth);
        System.out.println("My children: " + mBranches.size());
        System.out.println("My current max depth: " + currentMaxDepth);
        System.out.println("My overall max depth: " + overallMaxDepth);
        */
    }

    private boolean handleEvaluationResults(short nextStateEvaluation, int distanceToFirstCutoff) {

        boolean cutoff = false;
        if (isMaximizingNode()) {
            mValue = (short) Math.max(mValue, nextStateEvaluation);
            mAlpha = (short) Math.max(mAlpha, mValue);

            //System.out.println("Max node depth " + mDepth + " Child value " + nextStateEvaluation + " This value " + mValue + " Alpha " + mAlpha + " Beta " + mBeta);
            if (mBeta <= mAlpha) {
                //System.out.println("Beta cutoff");
                if(workspace.mBetaCutoffs.length > mDepth) {
                    workspace.mBetaCutoffs[mDepth]++;
                    workspace.mBetaCutoffDistances[mDepth] += distanceToFirstCutoff;
                }
                cutoff = true;
            }
        } else {
            mValue = (short) Math.min(mValue, nextStateEvaluation);
            mBeta = (short) Math.min(mBeta, mValue);

            //System.out.println("Min node depth " + mDepth + " Child value " + nextStateEvaluation + " This value " + mValue + " Alpha " + mAlpha + " Beta " + mBeta);
            if (mBeta <= mAlpha) {
                //System.out.println("Alpha cutoff");
                if(workspace.mBetaCutoffs.length > mDepth) {
                    workspace.mAlphaCutoffs[mDepth]++;
                    workspace.mAlphaCutoffDistances[mDepth] += distanceToFirstCutoff;
                }
                cutoff = true;
            }
        }

        return cutoff;
    }

    private void minifyState() {
        // All moves explored; minify this state
        if(mDepth != 0) {
            if(mValue == Evaluator.NO_VALUE) {
                short evaluation = workspace.transpositionTable.getValue(mValue, mCurrentMaxDepth - mDepth, mGameLength);
                if(evaluation != Evaluator.NO_VALUE) {
                    setValue(evaluation);
                }
                else {
                    setValue(workspace.evaluator.evaluate(GameTreeState.this, mCurrentMaxDepth, mDepth));
                }
                OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Warning: provisional evaluation for state at depth " + mDepth + " with " + mBranches.size() + " children");
            }
            MinimalGameTreeNode minifiedNode = new MinimalGameTreeNode(mParent, mDepth, mCurrentMaxDepth, mEnteringMove, mAlpha, mBeta, mValue, mBranches, getCurrentSide().isAttackingSide(), mZobristHash, mVictory, mGameLength);
            if (mParent != null) {
                mParent.replaceChild(GameTreeState.this, minifiedNode);
            }
            for (GameTreeNode branch : mBranches) {
                branch.setParent(minifiedNode);
            }
        }
    }

    private List<MoveRecord> generateSuccessorMoves(int currentMaxDepth) {
        List<MoveRecord> successorMoves = new ArrayList<>();
        List<Character> taflmen = new ArrayList<>();
        taflmen.addAll(getCurrentSide().getTaflmen());

        boolean considerJumps = mGame.getRules().canSideJump(getCurrentSide());
        int berserkMode = mGame.getRules().getBerserkMode();

        // Generate all successor moves.
        for (char taflman : taflmen) {
            Coord start = Taflman.getCurrentSpace(GameTreeState.this, taflman);
            if (start == null) continue;

            List<Character> startAdjacent = getBoard().getAdjacentNeighbors(start);
            boolean taflmanJumpCaptures = false;
            if (considerJumps) {
                if (Taflman.getJumpMode(mGame.getRules(), taflman) == Taflman.JUMP_CAPTURE) {
                    taflmanJumpCaptures = true;
                }
            }

            for (Coord dest : Taflman.getAllowableDestinations(GameTreeState.this, taflman)) {
                MoveRecord move = new MoveRecord(start, dest);

                List<Character> destAdjacent = getBoard().getAdjacentNeighbors(dest);
                for (char destAdjacentTaflman : destAdjacent) {
                    if (Taflman.isCapturedBy(GameTreeState.this, destAdjacentTaflman, taflman, dest, false)) {
                        move.captures.add(Taflman.getCurrentSpace(GameTreeState.this, destAdjacentTaflman));
                    }
                }

                if (taflmanJumpCaptures) {
                    for (char destAdjacentTaflman : destAdjacent) {
                        if (startAdjacent.contains(destAdjacentTaflman)) {
                            if (Taflman.isCapturedBy(GameTreeState.this, destAdjacentTaflman, taflman, dest, true)) {
                                move.captures.add(Taflman.getCurrentSpace(GameTreeState.this, destAdjacentTaflman));
                            }
                        }
                    }
                }

                successorMoves.add(move);
            }
        }

        if(workspace.isMoveOrderingAllowed()) {
            successorMoves.sort((o1, o2) -> {
                int o1CaptureCount = o1.captures.size();
                int o2CaptureCount = o2.captures.size();

                boolean o1ChangeTurn = true;
                boolean o2ChangeTurn = true;

                if (berserkMode == Rules.BERSERK_ANY_MOVE && o1CaptureCount > 0) o1ChangeTurn = false;
                if (berserkMode == Rules.BERSERK_ANY_MOVE && o2CaptureCount > 0) o2ChangeTurn = false;

                long o1Zobrist = updateZobristHash(mZobristHash, getBoard(), o1, o1ChangeTurn);
                long o2Zobrist = updateZobristHash(mZobristHash, getBoard(), o2, o2ChangeTurn);

                short o1Entry = AiWorkspace.transpositionTable.getValue(o1Zobrist, currentMaxDepth - mDepth, mGameLength);
                short o2Entry = AiWorkspace.transpositionTable.getValue(o2Zobrist, currentMaxDepth - mDepth, mGameLength);

                // The less-good move ordering on berserk moves is almost certainly faster than calculating
                // the destinations and capturing moves from the other position every time.
                if (berserkMode == Rules.BERSERK_CAPTURE_ONLY && o1CaptureCount > 0) o1Entry = Evaluator.NO_VALUE;
                if (berserkMode == Rules.BERSERK_CAPTURE_ONLY && o2CaptureCount > 0) o2Entry = Evaluator.NO_VALUE;

                int o1KillerValue = AiWorkspace.killerMoveTable.rateMove(mDepth, o1);
                int o2KillerValue = AiWorkspace.killerMoveTable.rateMove(mDepth, o2);

                // Killer moves: 20
                // Captures: 10
                // Transposition table hit: 5

                int sortValue = 0;

                if (o1KillerValue > o2KillerValue) {
                    sortValue += 20;
                } else if (o1KillerValue < o2KillerValue) {
                    sortValue -= 20;
                }

                if (o1CaptureCount > o2CaptureCount) {
                    sortValue += 10;
                } else if (o1CaptureCount < o2CaptureCount) {
                    sortValue -= 10;
                }

                if (o1Entry != Evaluator.NO_VALUE && o2Entry == Evaluator.NO_VALUE) {
                    sortValue += 5;
                } else if (o1Entry == Evaluator.NO_VALUE && o2Entry != Evaluator.NO_VALUE) {
                    sortValue -= 5;
                } else if (o1Entry > o2Entry) {
                    sortValue += 5;
                } else if (o1Entry < o2Entry) {
                    sortValue -= 5;
                }

                return sortValue;
            });

            // Defenders sort moves from low to high, attackers sort moves from high to low.
            if(getCurrentSide().isAttackingSide()) {
                Collections.reverse(successorMoves);
            }

        }

        return successorMoves;
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
        return mZobristHash;
    }

    @Override
    public void revalueParent(int depthOfObservation) {
        GameTreeNodeMethods.revalueParent(this, depthOfObservation);
    }

    @Override
    public void printChildEvaluations() {
        GameTreeNodeMethods.printChildEvaluations(this);
    }

    public void printPathEvaluations() {
        for(int i = 0; i < getBranches().size(); i++) {
            List<GameTreeNode> path = getNthPath(i);

            if(path.size() > 0) {
                System.out.println(path.get(0).getEnteringMove() + " (d" + path.get(0).getDepth() + ") " + "(v" + path.get(0).getVictory() + ") " + path.get(0).getValue());

                for(int j = 1; j < path.size(); j++) {
                    System.out.println("\t" + path.get(j).getEnteringMove() + " (d" + path.get(j).getDepth() + ")" + " (s" + path.get(j-1).getBranches().size() + ") " + "(" + path.get(j).getVictory() + ") " + path.get(j).getValue());
                }
            }
        }
    }

    public void printTree(String prefix) {
        if(getParentNode() == null) {
            System.out.println(prefix + "Root " + (isMaximizingNode() ? "(+) " : "(-) ") + "(d" + getDepth() + ")" + " (s0) " + "(w" + getVictory() + ") " + "(v" + getValue() + ") " + "(a" + getAlpha() + ") " + "(b" + getBeta() + ") ");
        }
        else {
            System.out.println(prefix + getEnteringMove() + (isMaximizingNode() ? " (+) " : " (-) ") + "(d" + getDepth() + ")" + " (s" + getParentNode().getBranches().size() + ") " + "(w" + getVictory() + ") " + "(v" + getValue() + ") " + "(a" + getAlpha() + ") " + "(b" + getBeta() + ") ");
        }

        for(int i = 0; i < getBranches().size(); i++) {
            GameTreeNode n = getNthChild(i);
            GameTreeState s;
            if(n instanceof MinimalGameTreeNode) {
                s = GameTreeState.getStateForMinimalNode(workspace.getTreeRoot(), (MinimalGameTreeNode) n);
            }
            else {
                s = (GameTreeState) getNthChild(i);
            }
            s.printTree(prefix + "\t");
        }
    }
}
