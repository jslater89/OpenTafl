package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.manywords.softworks.tafl.rules.Taflman.EMPTY;

public class GameTreeState extends GameState implements GameTreeNode {
    public static AiWorkspace workspace;
    private static final boolean DEBUG = false;

    public static final int DEFENDER = -1;
    public static final int ATTACKER = 1;

    private GameTreeNode mParent;
    final int mDepth;
    int mCurrentMaxDepth;
    private short mAlpha;
    private short mBeta;
    private short mValue = Evaluator.NO_VALUE;
    List<GameTreeNode> mBranches = new ArrayList<GameTreeNode>();
    private GameTreeNode mInflatedFrom;
    private boolean mValueFromTransposition = false;

    private boolean mContinuation = false;

    private String debugOutputString = null;

    private GameTreeState(int errorMoveResult) {
        super(errorMoveResult);
        mDepth = 0;
    }

    public GameTreeState(AiWorkspace workspace, GameState copyState) {
        super(copyState);
        mGame = workspace;
        this.workspace = workspace;

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
        GameTreeState nextState;

        // result should be good move except in cases like berserk,
        // where most moves on a berserk turn are illegal.
        if(nextGameState.getLastMoveResult() >= LOWEST_NONERROR_RESULT) {
            mGame.advanceState(this, nextGameState, nextGameState.getBerserkingTaflman() == EMPTY, nextGameState.getBerserkingTaflman(), true);
            nextState = new GameTreeState(workspace, nextGameState, this);
        }
        else {
            nextState = new GameTreeState(nextGameState.getLastMoveResult());
        }

        return nextState;
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
        return getPathStartingWithNode(getBestChild());
    }

    public List<GameTreeNode> getNthPath(int i) {
        return getPathStartingWithNode(getNthChild(i));
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

    public static List<GameTreeNode> getPathStartingWithNode(GameTreeNode node) {
        List<GameTreeNode> bestPath = new ArrayList<GameTreeNode>();

        while (node != null) {
            bestPath.add(node);
            node = node.getBestChild();
        }

        return bestPath;
    }

    public GameTreeNode getBestChild() {
        return GameTreeNodeMethods.getBestChild(this);
    }

    @Override
    public List<List<MoveRecord>> getAllEnteringSequences() {
        return GameTreeNodeMethods.getAllEnteringSequences(this);
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
    public GameTreeNode getChildForPath(List<MoveRecord> moves) {
        return GameTreeNodeMethods.getChildForPath(this, moves);
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

    @Override
    public boolean valueFromTransposition() {
        return mValueFromTransposition;
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

    public GameTreeNode explore(int currentMaxDepth, int overallMaxDepth, short alpha, short beta, AiThreadPool threadPool, boolean continuation) {
        mContinuation = continuation;
        mCurrentMaxDepth = currentMaxDepth;

        setAlpha(alpha);
        setBeta(beta);

        int remainingDepth = mCurrentMaxDepth - mDepth;

        boolean extension = false;
        if(overallMaxDepth < currentMaxDepth) {
            extension = true;
        }

        if(extension && mDepth <= overallMaxDepth) {
            if(workspace.mNoTime && mValue != Evaluator.NO_VALUE) return this; // If we don't have a value, we'll fall out elsewhere
            else if(mVictory > HIGHEST_NONTERMINAL_RESULT) return this;
        }

        short cachedValue = Evaluator.NO_VALUE;

        if(mGame.getRules().threefoldRepetitionResult() != Rules.THIRD_REPETITION_IGNORED && workspace.getRepetitions().getRepetitionCount(getZobrist()) > 1) {
            // If this state has occurred more than once in the history, we're in danger of a threefold repetition,
            // provided the rules call for threefold repetition results. We should therefore search this position, rather
            // than hit the TT.
            workspace.mRepetitionsIgnoreTranspositionTable++;
            cachedValue = Evaluator.NO_VALUE;
        }
        else if(!extension && !continuation) {
            // Always allow TT hits in the main tree once we've accounted for repetitions.
            cachedValue = AiWorkspace.transpositionTable.getValue(getZobrist(), remainingDepth, mGameLength);
        }
        else if(mDepth > overallMaxDepth) {
            // If we're in an extension and past the point we've already explored, transposition table hits are allowed.
            // No transposition hits allowed in the first level of depthin', I guess.
            cachedValue = AiWorkspace.transpositionTable.getValue(getZobrist(), remainingDepth, mGameLength);
        }

        int fallout = 0;
        if (cachedValue != Evaluator.NO_VALUE && mDepth > 0) {
            mValue = cachedValue;
            mValueFromTransposition = true;

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
            fallout = 1;
        }
        else if (mDepth != 0
                && (checkVictory() > HIGHEST_NONTERMINAL_RESULT
                || mDepth >= currentMaxDepth
                || (workspace.mNoTime)
                || (!extension && workspace.mContinuationTime)
                || (extension && continuation && workspace.mHorizonTime))) {
            // If we're at depth 0, go explore another level, just to be safe.
            // If this is a victory, evaluate and stop exploring.
            // If we've hit the target depth, evaluate and stop exploring.
            // If we're out of time, stop exploring.
            // If we're in continuation time and not in continuation search, stop exploring.
            // If we're in continuation search, in horizon time, and not in horizon search, stop exploring.

            // If this is a continuation search, this is a new node (i.e. NO_VALUE), and we're beneath the previous
            // horizon, then this node is pretty much useless—it hasn't been searched fully, and therefore is not to
            // be trusted.
            if(continuation && mDepth <= overallMaxDepth && mValue == Evaluator.NO_VALUE) {
                mValue = Evaluator.INTENTIONALLY_UNVALUED;
            }
            else if(extension && mDepth > overallMaxDepth && workspace.mNoTime) {
                // If this is an extension search and we're here because we're out of time, then this is also a useless
                // node we can't trust.
                mValue = Evaluator.INTENTIONALLY_UNVALUED;
            }
            else {
                mValue = evaluate();
            }

            // Leaf nodes we don't get to finish are always in the transposition table at depth 0.
            // This also holds for e.g. continuation search and other things.
            AiWorkspace.transpositionTable.putValue(getZobrist(), mValue, 0, mGameLength);
            fallout = 2;
        }
        else {
            // This state has occurred in the history for our children
            workspace.getRepetitions().increment(this.getZobrist());

            // Explore our children
            boolean unvaluedChildren;
            if(extension && continuation && mDepth < overallMaxDepth) {
                // In continuation search, we don't want to change pre-existing values inside the current horizon,
                // unless we've explored all the way to the bottom of a subtree. Nodes with a pre-existing value
                // existed in the tree before. New nodes have NO_VALUE.

                // If we have unvalued children and our value is not NO_VALUE, then we've probably gained information
                // over the course of the search. At the very least, we haven't lost any. We've explored at least one
                // subtree (or possibly zero subtrees) to the new max depth. We remove the unvalued nodes and move along.

                // If our value is NO_VALUE and we have unvalued children, then we can't count on this node to be
                // accurate. Set our value to UNVALUED so our parent knows to remove us.
                short preContinuationValue = mValue;
                unvaluedChildren = continuationOnChildren(currentMaxDepth, overallMaxDepth);

                // If I'm not a new node (i.e., I don't have NO_VALUE, we've gained information over the course of this
                // search, and it's safe to leave this as is.
                if(unvaluedChildren && preContinuationValue == Evaluator.NO_VALUE) mValue = Evaluator.INTENTIONALLY_UNVALUED;
            }
            else if(extension && !continuation) {
                short preExtensionValue = mValue;
                // In horizon search, since we aren't changing the tree until the very end, we don't need to do as
                // much bookkeeping.
                unvaluedChildren = continuationOnChildren(currentMaxDepth, overallMaxDepth);

                // If I'm the root node of the horizon search (i.e., I had a value beforehand), and I had unvalued
                // children (i.e. my search did not complete), go back to the cached value. Otherwise, be unvalued, so
                // the root node knows to be unvalued.
                if(unvaluedChildren && preExtensionValue != Evaluator.NO_VALUE) mValue = preExtensionValue;
                if(unvaluedChildren && preExtensionValue == Evaluator.NO_VALUE) mValue = Evaluator.INTENTIONALLY_UNVALUED;
            }
            else {
                this.mValue = Evaluator.NO_VALUE;
                exploreChildren(currentMaxDepth, overallMaxDepth, continuation, extension);
            }

            if(mValue == Evaluator.NO_VALUE) {

                throw new IllegalStateException("Unvalued state after search");
            }

            // This state has not occurred in the history for not-our-children
            workspace.getRepetitions().decrement(this.getZobrist());

            fallout = 3;
        }

        /* This is useful for debugging continuation search troubles, so I'm leaving it in for now.
        for(GameTreeNode child : getBranches()) {
            if(child.getValue() == Evaluator.INTENTIONALLY_UNVALUED) {
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Siblings of mine: " + getEnteringMoveSequence());
                for(GameTreeNode sibling : getBranches()) {
                    int pathSize = GameTreeState.getPathStartingWithNode(sibling).size();
                    OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Sibling " + sibling.getEnteringMove() + " (" + pathSize + "): " + sibling.getValue() + (sibling.valueFromTransposition() ? "T" : ""));
                }
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Time? " + workspace.mContinuationTime + " " + workspace.mHorizonTime + " " + workspace.mNoTime);
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Depths: " + mDepth + "/" + mCurrentMaxDepth + "/" + overallMaxDepth);
                throw new IllegalStateException("Unvalued node snuck in!");
            }
        }
        */

        return minifyState();
    }

    public static GameTreeState getStateForNode(GameTreeState rootNode, GameTreeNode nodeToReplace) {
        GameTreeState desiredState = new GameTreeState(rootNode);
        for (MoveRecord m : nodeToReplace.getEnteringMoveSequence()) {
            GameTreeState nextState = desiredState.considerMove(m.start, m.end);

            if(nextState.getLastMoveResult() < LOWEST_NONERROR_RESULT) {
                RawTerminal.renderGameState(desiredState);
                OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Last move result: " + m + "(" +nodeToReplace.getEnteringMoveSequence() + ") " + GameState.getStringForMoveResult(nextState.getLastMoveResult()));
                throw new IllegalStateException("Illegal move somehow!");
            }

            desiredState = nextState;
        }

        desiredState.mInflatedFrom = nodeToReplace;
        desiredState.mParent = nodeToReplace.getParentNode();
        desiredState.mAlpha = nodeToReplace.getAlpha();
        desiredState.mBeta = nodeToReplace.getBeta();
        desiredState.mValue = nodeToReplace.getValue();
        desiredState.mBranches = nodeToReplace.getBranches();
        desiredState.mVictory = nodeToReplace.getVictory();

        for(GameTreeNode node : nodeToReplace.getBranches()) {
            node.setParent(desiredState);
        }
        desiredState.getParentNode().replaceChild(nodeToReplace, desiredState);
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

    private boolean exploreChildren(int currentMaxDepth, int overallMaxDepth, boolean continuation, boolean extension) {
        List<MoveRecord> successorMoves = new ArrayList<>();

        if(!continuation || getBranches().size() == 0) {
            successorMoves = generateSuccessorMoves(currentMaxDepth);
        }

        boolean cutoff = false;
        int cutoffType = 0;

        int distanceToFirstCutoff = 0;
        boolean savedDistanceToFirstCutoff = false;
        boolean unvaluedChild = false;
        for (MoveRecord move : successorMoves) {
            GameTreeState node = considerMove(move.start, move.end);
            // Node will be null in e.g. berserk tafl, where moves are legal
            // according to the movement rules, but not legal according to
            // special rules, like the berserk rule.
            // TODO: account for threefold repetition forbidden?
            if(node.getLastMoveResult() < LOWEST_NONERROR_RESULT) {
                continue;
            }

            mBranches.add(node);

            if(DEBUG) for(int i = 0; i < mDepth; i++) System.out.print("\t");
            if(DEBUG) System.out.println(mDepth + " exploring child " + node.getEnteringMoveSequence() + " with value/alpha/beta " + mValue + "/" + mAlpha + "/" + mBeta);



            node.explore(currentMaxDepth, overallMaxDepth, mAlpha, mBeta, null, mContinuation);
            GameTreeNode minified = node.explore(currentMaxDepth, overallMaxDepth, mAlpha, mBeta, null, mContinuation);
            distanceToFirstCutoff++;

            short evaluation = node.getValue();

            // evaluation might be unvalued for unfinished continuations
            if(evaluation == Evaluator.INTENTIONALLY_UNVALUED) {
                unvaluedChild = true;
                mBranches.remove(minified);

                // We can skip everything else after we hit an intentionally unvalued node:
                // we can't use the data.
                continue;
            }


            // A/B pruning
            if (evaluation != Evaluator.NO_VALUE) {
                if (mValue == Evaluator.NO_VALUE) {
                    mValue = evaluation;

                    if(DEBUG) for(int i = 0; i < mDepth; i++) System.out.print("\t");
                    if(DEBUG) System.out.println(mDepth + " setting value to child value: " + evaluation);
                }

                cutoff = handleEvaluationResults(evaluation, distanceToFirstCutoff) && workspace.areCutoffsAllowed();

                if(cutoff) {
                    AiWorkspace.killerMoveTable.putMove(mDepth, move);

                    if(workspace.isHistoryTableAllowed()) {
                        AiWorkspace.historyTable.putMove(getCurrentSide().isAttackingSide(), currentMaxDepth - mDepth, move);
                    }

                    if(DEBUG) for(int i = 0; i < mDepth; i++) System.out.print("\t");
                    if(DEBUG) System.out.println("Cutoff at depth " + mDepth + " with value/alpha/beta " + mValue + "/" + mAlpha + "/" + mBeta);
                    break;
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

        return unvaluedChild;
    }

    private boolean continuationOnChildren(int currentMaxDepth, int overallMaxDepth) {
        List<GameTreeNode> successorStates = new ArrayList<>(getBranches());

        List<MoveRecord> successorMoves = generateSuccessorMoves(currentMaxDepth);

        boolean cutoff = false;
        int cutoffType = 0;

        int distanceToFirstCutoff = 0;
        boolean savedDistanceToFirstCutoff = false;

        for(GameTreeNode node : successorStates) {
            if (workspace.mHorizonTime) break;

            if (cutoff) {
                // Remove nodes which see cutoffs.
                getBranches().remove(node);
                continue;
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

        boolean unvaluedChild = false;
        if(!cutoff && successorMoves.size() > 0) {
            for (MoveRecord move : successorMoves) {
                if (cutoff) {
                    break;
                }

                GameTreeState node = considerMove(move.start, move.end);
                // Node will be null in e.g. berserk tafl, where moves are legal
                // according to the movement rules, but not legal according to
                // special rules, like the berserk rule.
                if(node.getLastMoveResult() < LOWEST_NONERROR_RESULT) {
                    continue;
                }

                mBranches.add(node);
                GameTreeNode minified = node.explore(currentMaxDepth, overallMaxDepth, mAlpha, mBeta, null, mContinuation);
                distanceToFirstCutoff++;

                short evaluation = node.getValue();

                // evaluation might be unvalued for unfinished continuations
                if(evaluation == Evaluator.INTENTIONALLY_UNVALUED) {
                    unvaluedChild = true;
                    mBranches.remove(minified);

                    // We can skip everything else after we hit an intentionally unvalued node:
                    // we can't use the data.
                    continue;
                }

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

        /*
        System.out.println("CONTINUATION");
        System.out.println("My depth: " + mDepth);
        System.out.println("My children: " + mBranches.size());
        System.out.println("My current max depth: " + currentMaxDepth);
        System.out.println("My overall max depth: " + overallMaxDepth);
        */
        return unvaluedChild;
    }

    private boolean handleEvaluationResults(short nextStateEvaluation, int distanceToFirstCutoff) {

        boolean cutoff = false;
        if (isMaximizingNode()) {
            mValue = (short) Math.max(mValue, nextStateEvaluation);
            mAlpha = (short) Math.max(mAlpha, mValue);

            if(DEBUG) for(int i = 0; i < mDepth + 1; i++) System.out.print("\t");
            if(DEBUG) System.out.println(mDepth + 1 + " value: " + nextStateEvaluation);
            if(DEBUG) for(int i = 0; i < mDepth + 1; i++) System.out.print("\t");
            if(DEBUG) System.out.println("New " + mDepth + " value/alpha/beta " + mValue + "/" + mAlpha + "/" + mBeta);
            if (workspace.areCutoffsAllowed() && mBeta <= mAlpha) {
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

            if(DEBUG) for(int i = 0; i < mDepth + 1; i++) System.out.print("\t");
            if(DEBUG) System.out.println(mDepth + 1 + " value: " + nextStateEvaluation);
            if(DEBUG) for(int i = 0; i < mDepth + 1; i++) System.out.print("\t");
            if(DEBUG) System.out.println("New " + mDepth + " value/alpha/beta " + mValue + "/" + mAlpha + "/" + mBeta);
            if (workspace.areCutoffsAllowed() && mBeta <= mAlpha) {
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

    private GameTreeNode minifyState() {
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
            MinimalGameTreeNode minifiedNode = new MinimalGameTreeNode(mParent, mDepth, mCurrentMaxDepth, mEnteringMove, mAlpha, mBeta, mValue, mValueFromTransposition, mBranches, getCurrentSide().isAttackingSide(), mZobristHash, mVictory, mGameLength);
            if (mParent != null) {
                mParent.replaceChild(GameTreeState.this, minifiedNode);
            }
            for (GameTreeNode branch : mBranches) {
                branch.setParent(minifiedNode);
            }
            return minifiedNode;
        }
        return this;
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
            List<MoveRecord> killerMoves = new ArrayList<>(AiWorkspace.killerMoveTable.killersToKeep);
            List<MoveRecord> capturingMoves = new ArrayList<>(4);
            List<MoveRecord> transpositionMoves = new ArrayList<MoveRecord>();
            List<MoveRecord> sortedSuccessors = new ArrayList<>(successorMoves.size());
            List<MoveRecord> historyMoves = new ArrayList<>(successorMoves.size());

            // Get the moves of various sorts of interest
            for(MoveRecord m : successorMoves) {
                if(AiWorkspace.killerMoveTable.rateMove(mDepth, m) > 0) killerMoves.add(m);
                else if(workspace.isHistoryTableAllowed()) {
                    int rating = AiWorkspace.historyTable.getRating(getCurrentSide().isAttackingSide(), m);
                    if(rating > 0) {
                        historyMoves.add(m);
                    }
                }
                else if(m.captures.size() > 0) capturingMoves.add(m);
                else {
                    boolean changeTurn = (berserkMode != Rules.BERSERK_ANY_MOVE || m.captures.size() == 0);
                    long zobrist = updateZobristHash(mZobristHash, getBoard(), m, changeTurn);
                    int transpositionTableRating = AiWorkspace.transpositionTable.getValue(zobrist, currentMaxDepth - mDepth, mGameLength);

                    if(transpositionTableRating != Evaluator.NO_VALUE) transpositionMoves.add(m);
                }
            }

            // Remove the interesting moves and sort them
            successorMoves.removeAll(killerMoves);
            successorMoves.removeAll(capturingMoves);
            successorMoves.removeAll(historyMoves);
            successorMoves.removeAll(transpositionMoves);

            historyMoves.sort((o1, o2) -> {
                int o1HistoryValue = AiWorkspace.historyTable.getRating(getCurrentSide().isAttackingSide(), o1);
                int o2HistoryValue = AiWorkspace.historyTable.getRating(getCurrentSide().isAttackingSide(), o2);

                return o2HistoryValue - o1HistoryValue;
            });

            transpositionMoves.sort((o1, o2) -> {
                boolean o1ChangeTurn = (berserkMode != Rules.BERSERK_ANY_MOVE || o1.captures.size() == 0);
                long o1Zobrist = updateZobristHash(mZobristHash, getBoard(), o1, o1ChangeTurn);
                int o1Rating = AiWorkspace.transpositionTable.getValue(o1Zobrist, currentMaxDepth - mDepth, mGameLength);

                boolean o2ChangeTurn = (berserkMode != Rules.BERSERK_ANY_MOVE || o2.captures.size() == 0);
                long o2Zobrist = updateZobristHash(mZobristHash, getBoard(), o2, o2ChangeTurn);
                int o2Rating = AiWorkspace.transpositionTable.getValue(o2Zobrist, currentMaxDepth - mDepth, mGameLength);

                if(isMaximizingNode()) {
                    return o2Rating - o1Rating;
                }
                else {
                    return o1Rating - o2Rating;
                }
            });


            List<MoveRecord> topHalfTranspositions = transpositionMoves.subList(0, transpositionMoves.size() / 2);
            List<MoveRecord> bottomHalfTranspositions = transpositionMoves.subList(transpositionMoves.size() / 2, transpositionMoves.size());


            // Add them in sorted order
            sortedSuccessors.addAll(killerMoves);
            sortedSuccessors.addAll(historyMoves);
            sortedSuccessors.addAll(topHalfTranspositions);
            sortedSuccessors.addAll(capturingMoves);
            sortedSuccessors.addAll(bottomHalfTranspositions);
            sortedSuccessors.addAll(successorMoves);

            return sortedSuccessors;
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

        for(GameTreeNode n : getBranches()) {
            GameTreeState s;
            if(n instanceof MinimalGameTreeNode) {
                s = GameTreeState.getStateForNode(workspace.getTreeRoot(), (MinimalGameTreeNode) n);
            }
            else {
                s = (GameTreeState) n;
            }
            s.printTree(prefix + "\t");
        }
    }
}
