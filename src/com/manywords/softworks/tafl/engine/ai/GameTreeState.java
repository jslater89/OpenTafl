package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    private String debugOutputString = null;

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
        getBranches().sort(new Comparator<GameTreeNode>() {
            @Override
            public int compare(GameTreeNode o1, GameTreeNode o2) {
                if(isMaximizingNode()) {
                    return -(o1.getValue() - o2.getValue());
                }
                else {
                    // low to high
                    return (o1.getValue() - o2.getValue());
                }
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
            if(mGame.historyContainsHash(child.getZobrist())) {
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
        else return workspace.evaluator.evaluate(this);
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

    public short explore(int currentMaxDepth, int overallMaxDepth, short alpha, short beta, AiThreadPool threadPool) {
        setAlpha(alpha);
        setBeta(beta);
        boolean extension = false;
        if(overallMaxDepth < currentMaxDepth) {
            extension = true;
        }
        mCurrentMaxDepth = currentMaxDepth;

        short cachedValue = Evaluator.NO_VALUE;
        if(!extension) {
            cachedValue = AiWorkspace.transpositionTable.getValue(getZobrist(), mCurrentMaxDepth - mDepth, mGameLength);
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

            MinimalGameTreeNode smallChild = new MinimalGameTreeNode(mParent, mDepth, currentMaxDepth, mEnteringMove, mAlpha, mBeta, mValue, mBranches, getCurrentSide().isAttackingSide(), mZobristHash, mVictory, mGameLength);
            mParent.replaceChild(GameTreeState.this, smallChild);
        } else if (mDepth != 0 && (checkVictory() != GOOD_MOVE || mDepth >= currentMaxDepth || (workspace.mNoTime) || (!extension && workspace.mExtensionTime))) {
            // If this is a victory, evaluate and stop exploring.
            // If we've hit the target depth, evaluate and stop exploring.
            // If we're out of time and this isn't the root node, stop exploring.
            // If we're in extension time and not in extension search, stop exploring.
            // If we're at depth 0, go explore another level, just to be safe.
            mValue = evaluate();

            AiWorkspace.transpositionTable.putValue(getZobrist(), mValue, mCurrentMaxDepth - mDepth, mGameLength);

            // Replace small child
            MinimalGameTreeNode smallChild = new MinimalGameTreeNode(mParent, mDepth, currentMaxDepth, mEnteringMove, mAlpha, mBeta, mValue, mBranches, getCurrentSide().isAttackingSide(), mZobristHash, mVictory, mGameLength);
            mParent.replaceChild(GameTreeState.this, smallChild);
        } else {
            this.mValue = Evaluator.NO_VALUE;
            new ExploreTask(this, currentMaxDepth, overallMaxDepth, threadPool).doTask();
            //threadPool.execute(new ExploreTask(this, maxDepth, threadPool));
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

    private class ExploreTask implements Runnable {
        GameTreeState mState;
        int mOverallMaxDepth;
        AiThreadPool mThreadPool;

        public ExploreTask(GameTreeState state, int currentMaxDepth, int overallMaxDepth, AiThreadPool threadPool) {
            mCurrentMaxDepth = currentMaxDepth;
            mOverallMaxDepth = overallMaxDepth;
            mState = state;
            mThreadPool = threadPool;
        }

        //@Override
        public void doTask() {
            List<Character> taflmen = new ArrayList<Character>();
            taflmen.addAll(mState.getCurrentSide().getTaflmen());

            List<MoveRecord> successorMoves = new ArrayList<MoveRecord>();

            boolean considerJumps = mGame.getRules().canSideJump(mState.getCurrentSide());

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

                    long nextZobrist = updateZobristHash(mZobristHash, getBoard(), move);
                    if(!mGame.historyContainsHash(nextZobrist) && !treeParentsContainHash(nextZobrist)) {
                        successorMoves.add(move);
                    }
                }
            }

            successorMoves.sort(new Comparator<MoveRecord>() {
                @Override
                public int compare(MoveRecord o1, MoveRecord o2) {
                    int o1CaptureCount = o1.captures.size();
                    int o2CaptureCount = o2.captures.size();

                    long o1Zobrist = updateZobristHash(mZobristHash, getBoard(), o1);
                    long o2Zobrist = updateZobristHash(mZobristHash, getBoard(), o2);

                    short o1Entry = workspace.transpositionTable.getValue(o1Zobrist, mCurrentMaxDepth - mDepth, mGameLength);
                    short o2Entry = workspace.transpositionTable.getValue(o2Zobrist, mCurrentMaxDepth - mDepth, mGameLength);

                    // No transposition table entries? Sort the one with more captures first.
                    if(o1Entry == Evaluator.NO_VALUE && o2Entry == Evaluator.NO_VALUE) {
                        if(o1CaptureCount > o2CaptureCount) return -1;
                        else if(o2CaptureCount > o1CaptureCount) return 1;
                        else return 0;
                    }

                    // if one has a transposition table entry and the other doesn't,
                    // put the one with the entry first.
                    if (o1Entry != Evaluator.NO_VALUE && o2Entry == Evaluator.NO_VALUE) {
                        return -1;
                    }
                    if (o1Entry == Evaluator.NO_VALUE && o2Entry != Evaluator.NO_VALUE) {
                        return 1;
                    }

                    // if both have entries, compare the values, and sort them with the higher value
                    // first. If the entries are equal, sort the one with more captures first.
                    if (o1Entry > o2Entry) {
                        return -1;
                    } else if (o2Entry > o1Entry) {
                        return 1;
                    } else {
                        if(o1CaptureCount > o2CaptureCount) return -1;
                        else if(o2CaptureCount > o1CaptureCount) return 1;
                        else return 0;
                    }
                }
            });

            boolean cutoff = false;
            int cutoffType = 0;

            int distanceToFirstCutoff = 0;
            boolean savedDistanceToFirstCutoff = false;
            for (MoveRecord move : successorMoves) {
                if (cutoff) {
                    break;
                }

                GameTreeState node = mState.considerMove(move.start, move.end);
                // Node will be null in e.g. berserk tafl, where moves are legal
                // according to the movement rules, but not legal according to
                // special rules, like the berserk rule.
                if(node == null) {
                    continue;
                }

                mBranches.add(node);
                node.explore(mCurrentMaxDepth, mOverallMaxDepth, mAlpha, mBeta, mThreadPool);
                distanceToFirstCutoff++;

                short evaluation = node.getValue();

                // A/B pruning
                if (evaluation != Evaluator.NO_VALUE) {
                    if (mValue == Evaluator.NO_VALUE) {
                        mValue = evaluation;
                    }

                    if (getCurrentSide().isAttackingSide()) {
                        mValue = (short) Math.max(mValue, evaluation);
                        mAlpha = (short) Math.max(mAlpha, mValue);

                        //System.out.println("Depth " + mDepth + " Attacker value " + mValue + " Alpha " + mAlpha + " Beta " + mBeta);
                        if (mBeta <= mAlpha) {
                            //System.out.println("Beta cutoff");
                            cutoffType = 1;
                            if(workspace.mBetaCutoffs.length > mDepth) {
                                workspace.mBetaCutoffs[mDepth]++;
                                workspace.mBetaCutoffDistances[mDepth] += distanceToFirstCutoff;
                            }
                            cutoff = true;
                        }
                    } else {
                        mValue = (short) Math.min(mValue, evaluation);
                        mBeta = (short) Math.min(mBeta, mValue);

                        //System.out.println("Depth " + mDepth + " Defender value " + mValue + " Alpha " + mAlpha + " Beta " + mBeta);
                        if (mBeta <= mAlpha) {
                            //System.out.println("Alpha cutoff");
                            cutoffType = 0;
                            if(workspace.mBetaCutoffs.length > mDepth) {
                                workspace.mAlphaCutoffs[mDepth]++;
                                workspace.mAlphaCutoffDistances[mDepth] += distanceToFirstCutoff;
                            }
                            cutoff = true;
                        }
                    }
                }
            }

            // If we have no legal moves, the other side wins
            if(mBranches.size() == 0) {
                if(getCurrentSide().isAttackingSide()) {
                    mVictory = DEFENDER_WIN;
                }
                else {
                    mVictory = ATTACKER_WIN;
                }
                mValue = evaluate();
            }

            workspace.transpositionTable.putValue(getZobrist(), mValue, mCurrentMaxDepth - mDepth, mGameLength);
            mTaflmanMoveCache = null;

            // All moves explored; minify this state
            if(mDepth != 0) {
                if(mValue == Evaluator.NO_VALUE) {
                    short evaluation = workspace.transpositionTable.getValue(mValue, mCurrentMaxDepth - mDepth, mGameLength);
                    if(evaluation == Evaluator.NO_VALUE) {
                        evaluation = workspace.evaluator.evaluate(GameTreeState.this);
                    }
                    setValue(evaluation);
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

        @Override
        public void run() {
            doTask();
        }
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
}
