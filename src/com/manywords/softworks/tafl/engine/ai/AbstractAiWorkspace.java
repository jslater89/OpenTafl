package com.manywords.softworks.tafl.engine.ai;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.ai.alphabeta.AlphaBetaGameTreeState;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.ui.UiCallback;

public abstract class AbstractAiWorkspace extends Game {
    public boolean chatty = Log.level == Log.Level.VERBOSE;
    public boolean silent = Log.level == Log.Level.CRITICAL;

    protected TimeSpec mClockLength;
    protected TimeSpec mTimeRemaining;

    public AbstractAiWorkspace(UiCallback ui, Game startingGame, GameState startingState) {
        super(startingGame.mZobristConstants, startingGame.getHistory(), startingGame.getRepetitions());
    }

    /**
     * Conduct a game tree search.
     *
     * This method should block until the tree search is complete.
     * @param maxThinkTime The amount of time allotted.
     */
    public abstract void explore(int maxThinkTime);

    /**
     * Stop immediately; time is almost up.
     */
    public abstract void crashStop();

    /**
     * Clean up following the search.
     */
    public abstract void stopExploring();

    /**
     *
     * @return The root node of the game tree (that is, the state corresponding to the constructor's startingState)
     */
    public abstract AlphaBetaGameTreeState getTreeRoot();

    /**
     * Log search information, either to the console or by using UiCallback.statusText.
     */
    public abstract void printSearchStats();

    /**
     * Return an evaluation of the given state.
     * @param childIndex The state (sorted; state 0 is the best, 1 is the second best, N-1 is the Nth best)
     * @return A string representation of the evaluation.
     */
    public abstract String dumpEvaluationFor(int childIndex);

    /**
     * Indicate how much time remains for the AI to use.
     * @param length The total length of the game clock.
     * @param entry The amount of time the AI has remaining.
     */
    public void setTimeRemaining(TimeSpec length, TimeSpec entry) {
        mClockLength = length;
        mTimeRemaining = entry;
    }
}
