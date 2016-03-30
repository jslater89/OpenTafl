package com.manywords.softworks.tafl.ui.command;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.player.ExternalEnginePlayer;
import com.manywords.softworks.tafl.ui.player.Player;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineHost;

import java.util.ArrayList;
import java.util.List;

public class CommandEngine {
    private Game mGame;
    private Player mAttacker;
    private Player mDefender;
    private Player mCurrentPlayer;
    private Player mLastPlayer;
    private ExternalEnginePlayer mDummyAnalysisPlayer;
    private ExternalEngineHost mAnalysisEngine;
    private List<UiCallback> mUiCallbacks = new ArrayList<UiCallback>(1);
    private UiCallback mPrimaryUiCallback;

    private boolean mInGame = false;

    private int mThinkTime = 4;

    public CommandEngine(Game g, UiCallback callback, Player attacker, Player defender) {
        mGame = g;
        mUiCallbacks.add(callback);
        mPrimaryUiCallback = callback;

        mAttacker = attacker;
        mAttacker.setAttackingSide(true);
        mAttacker.setGame(g);
        mAttacker.setupPlayer();

        mDefender = defender;
        mDefender.setAttackingSide(false);
        mDefender.setGame(g);
        mDefender.setupPlayer();

        if(TerminalSettings.analysisEngine && ExternalEngineHost.validateEngineFile(TerminalSettings.analysisEngineFile)) {
            mDummyAnalysisPlayer = new ExternalEnginePlayer();
            mDummyAnalysisPlayer.setGame(g);
            mDummyAnalysisPlayer.setCallback(mPlayerCallback);

            mAnalysisEngine = new ExternalEngineHost(mDummyAnalysisPlayer, TerminalSettings.analysisEngineFile);
            mAnalysisEngine.setGame(g);
        }
    }

    public void addUiCallback(UiCallback callback) {
        if(mUiCallbacks.contains(callback)) return;

        mUiCallbacks.add(callback);
    }

    public void removeUiCallback(UiCallback callback) {
        if(callback.equals(mPrimaryUiCallback)) throw new IllegalArgumentException("Can't remove primary UI callback!");

        mUiCallbacks.remove(callback);
    }

    public void setSearchDepth(int depth) {
        mThinkTime = depth;
    }

    public void startGame() {
        if(mGame.getClock() != null) {
            mGame.getClock().setCallback(mClockCallback);
        }

        mThinkTime = TerminalSettings.aiThinkTime;
        mAttacker.setCallback(mPlayerCallback);
        mDefender.setCallback(mPlayerCallback);

        if (mGame.getCurrentState().getCurrentSide().isAttackingSide()) {
            mCurrentPlayer = mAttacker;
        } else {
            mCurrentPlayer = mDefender;
        }

        mInGame = true;
        callbackGameStarting();
        mGame.start();
        waitForNextMove();
    }

    public Player getCurrentPlayer() {
        return mCurrentPlayer;
    }

    private void waitForNextMove() {
        if(!mInGame) return;

        callbackAwaitingMove(mCurrentPlayer, mGame.getCurrentSide().isAttackingSide());
        mCurrentPlayer.getNextMove(mPrimaryUiCallback, mGame, mThinkTime);
    }

    public void finishGame() {
        mInGame = false;

        mGame.finish();
        mAttacker.stop();
        mDefender.stop();
        callbackGameFinished();
    }

    private final GameClock.GameClockCallback mClockCallback = new GameClock.GameClockCallback() {
        @Override
        public void timeUpdate(Side currentSide) {
            callbackTimeUpdate(currentSide);

            mAttacker.timeUpdate();
            mDefender.timeUpdate();
        }

        @Override
        public void timeExpired(Side currentSide) {
            mPrimaryUiCallback.statusText("Time expired!");
            if(currentSide.isAttackingSide()) {
                callbackVictoryForSide(mGame.getCurrentState().getDefenders());
            }
            else {
                callbackVictoryForSide(mGame.getCurrentState().getAttackers());
            }

            finishGame();
        }
    };

    private final Player.PlayerCallback mPlayerCallback = new Player.PlayerCallback() {

        @Override
        public void onMoveDecided(Player player, MoveRecord move) {
            if(player == mDummyAnalysisPlayer) return;

            String message = "Illegal play. ";
            if(player != mCurrentPlayer) {
                message += "Not your turn.";
                callbackMoveResult(new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, message, null), null);
                return;
            }
            int result =
                    mGame.getCurrentState().moveTaflman(
                            mGame.getCurrentState().getBoard().getOccupier(move.start.x, move.start.y),
                            mGame.getCurrentState().getSpaceAt(move.end.x, move.end.y)).getLastMoveResult();

            if (result == GameState.ATTACKER_WIN) {
                callbackVictoryForSide(mGame.getCurrentState().getAttackers());
                finishGame();
                return;
            }
            else if (result == GameState.DEFENDER_WIN) {
                callbackVictoryForSide(mGame.getCurrentState().getDefenders());
                finishGame();
                return;
            }
            else if (result == GameState.DRAW) {
                callbackVictoryForSide(null);
                finishGame();
                return;
            }
            else if (result != GameState.GOOD_MOVE) {
                if (result == GameState.ILLEGAL_SIDE) {
                    message += "Not your taflman.";
                }
                else {
                    message += "Move disallowed.";
                }

                if(mCurrentPlayer.isAttackingSide()) {
                    mAttacker.moveResult(result);
                }
                else {
                    mDefender.moveResult(result);
                }

                callbackMoveResult(new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, message, null), move);
            }
            else {
                mLastPlayer = mCurrentPlayer;
                mCurrentPlayer = (mGame.getCurrentSide().isAttackingSide() ? mAttacker : mDefender);
                callbackMoveResult(new CommandResult(CommandResult.Type.MOVE, CommandResult.SUCCESS, "", null), move);
                callbackGameStateAdvanced();

                // Send a move result to the last player to move.
                if(mLastPlayer.isAttackingSide()) {
                    mAttacker.moveResult(result);
                }
                else {
                    mDefender.moveResult(result);
                }

                // Send an opponent move update to the other player.
                if(mAttacker != mLastPlayer) {
                    mAttacker.opponentMove(move);
                }
                else {
                    mDefender.opponentMove(move);
                }
            }

            waitForNextMove();
        }

        @Override
        public void notifyResignation(Player player) {
            mGame.getCurrentState().winByResignation(player.isAttackingSide());
            callbackVictoryForSide(player.isAttackingSide() ? getGame().getCurrentState().getAttackers() : getGame().getCurrentState().getDefenders());
            finishGame();
        }
    };

    public CommandResult executeCommand(Command command) {
        // 1. NULL COMMAND: FAILURE
        if(command == null) {
            return new CommandResult(CommandResult.Type.NONE, CommandResult.FAIL, "Command not recognized", null);
        }
        // 2. COMMAND WITH ERROR: FAILURE
        else if(!command.getError().equals("")) {
            return new CommandResult(CommandResult.Type.SENT, CommandResult.FAIL, command.mError, null);
        }
        // 3. MOVE COMMAND: RETURN MOVE RECORD (receiver sends to callback after verifying side &c)
        else if(command instanceof HumanCommandParser.Move) {
            if(!mInGame) {
                return new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, "Game over", null);
            }
            HumanCommandParser.Move m = (HumanCommandParser.Move) command;
            if(m.from == null || m.to == null) {
                return new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, "Invalid coords", null);
            }

            String message = "";

            char piece = mGame.getCurrentState().getPieceAt(m.from.x, m.from.y);
            if (piece == Taflman.EMPTY) {
                message = "No taflman at " + m.from;
                return new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, message, null);
            }

            Coord destination = mGame.getCurrentState().getSpaceAt(m.to.x, m.to.y);
            MoveRecord record = new MoveRecord(Taflman.getCurrentSpace(mGame.getCurrentState(), piece), destination);
            return new CommandResult(CommandResult.Type.MOVE, CommandResult.SUCCESS, "", record);
        }
        // 4. INFO COMMAND: SUCCESS (command parser does all the required verification)
        else if(command instanceof HumanCommandParser.Info) {
            return new CommandResult(CommandResult.Type.INFO, CommandResult.SUCCESS, "", null);
        }
        // 5. SHOW COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.Show) {
            return new CommandResult(CommandResult.Type.SHOW, CommandResult.SUCCESS, "", null);
        }
        // 6. HISTORY COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.History) {
            String gameRecord = mGame.getHistoryString();

            return new CommandResult(CommandResult.Type.HISTORY, CommandResult.SUCCESS, "", gameRecord);
        }
        // 7. HELP COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.Help) {
            return new CommandResult(CommandResult.Type.HELP, CommandResult.SUCCESS, "", null);
        }
        // 8. QUIT COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.Rules) {
            return new CommandResult(CommandResult.Type.RULES, CommandResult.SUCCESS, "", null);
        }
        // 9. QUIT COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.Quit) {
            return new CommandResult(CommandResult.Type.QUIT, CommandResult.SUCCESS, "", null);
        }
        // 10. ANALYZE COMMAND
        else if(command instanceof HumanCommandParser.Analyze) {
            HumanCommandParser.Analyze a = (HumanCommandParser.Analyze) command;

            if(mAnalysisEngine == null) {
                return new CommandResult(CommandResult.Type.ANALYZE, CommandResult.FAIL, "No analysis engine loaded", null);
            }
            else {
                mAnalysisEngine.analyzePosition(a.moves, a.seconds, mGame.getCurrentState());
                return new CommandResult(CommandResult.Type.ANALYZE, CommandResult.SUCCESS, "", null);
            }
        }

        return new CommandResult(CommandResult.Type.NONE, CommandResult.FAIL, "Command not recognized", null);
    }

    public Game getGame() {
        return mGame;
    }

    private void callbackGameStarting() {
        for(UiCallback c : mUiCallbacks) {
            c.gameStarting();
        }
    }

    private void callbackAwaitingMove(Player p, boolean isAttackingSide) {
        for(UiCallback c : mUiCallbacks) {
            c.awaitingMove(p, isAttackingSide);
        }
    }

    private void callbackGameFinished() {
        for(UiCallback c : mUiCallbacks) {
            c.gameFinished();
        }
    }

    private void callbackTimeUpdate(Side side) {
        for(UiCallback c : mUiCallbacks) {
            c.timeUpdate(side);
        }
    }

    private void callbackVictoryForSide(Side side) {
        for(UiCallback c : mUiCallbacks) {
            c.victoryForSide(side);
        }
    }

    private void callbackMoveResult(CommandResult result, MoveRecord move) {
        for(UiCallback c : mUiCallbacks) {
            c.moveResult(result, move);
        }
    }

    private void callbackGameStateAdvanced() {
        for(UiCallback c : mUiCallbacks) {
            c.gameStateAdvanced();
        }
    }
}