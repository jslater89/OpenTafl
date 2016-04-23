package com.manywords.softworks.tafl.ui.command;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.HumanReadableRulesPrinter;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.player.ExternalEnginePlayer;
import com.manywords.softworks.tafl.ui.player.Player;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineHost;

import java.util.ArrayList;
import java.util.List;

public class CommandEngine {
    private UiCallback.Mode mMode;

    private Game mGame;
    private ReplayGame mReplay;
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
        mMode = UiCallback.Mode.GAME;
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
            mAnalysisEngine = mDummyAnalysisPlayer.setupAnalysisEngine();
        }
    }

    public void enterReplay(ReplayGame rg) {
        mMode = UiCallback.Mode.REPLAY;
        mReplay = rg;
        callbackModeChange(UiCallback.Mode.REPLAY, rg);
    }
    public void leaveReplay() {
        mReplay.prepareForGameStart();
        mMode = UiCallback.Mode.GAME;
    }
    public void enterGame(Game g) {
        mMode = UiCallback.Mode.GAME;
        mReplay = null;
        mGame = g;
        g.setUiCallback(mPrimaryUiCallback);
        mAttacker.setGame(g);
        mDefender.setGame(g);

        callbackModeChange(UiCallback.Mode.GAME, g);
    }

    public void addUiCallback(UiCallback callback) {
        if(mUiCallbacks.contains(callback)) return;

        mUiCallbacks.add(callback);
    }

    public void removeUiCallback(UiCallback callback) {
        if(callback.equals(mPrimaryUiCallback)) throw new IllegalArgumentException("Can't remove primary UI callback!");

        mUiCallbacks.remove(callback);
    }

    public void startGame() {
        if(mInGame) {
            // Don't restart a started game
            return;
        }
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
        finishGame(false);
    }

    private void finishGame(boolean quiet) {
        mInGame = false;
        mGame.finish();

        stopPlayers();

        if(!quiet) {
            callbackGameFinished();
        }
    }

    public void stopPlayers() {
        mAttacker.stop();
        mDefender.stop();
        if(mAnalysisEngine != null) {
            mDummyAnalysisPlayer.stop();
        }
    }

    public void shutdown() {
        mAttacker.quit();
        mDefender.quit();

        if(mAnalysisEngine != null) {
            mDummyAnalysisPlayer.quit();
        }
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

            int replayPosition = -1;
            if(getMode() == UiCallback.Mode.REPLAY) {
                replayPosition = mReplay.getPosition();
                leaveReplay();
            }

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

            if(replayPosition != -1) {
                enterReplay(new ReplayGame(mGame));
                mReplay.setPosition(replayPosition);
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
            String gameRecord;
            if(mMode == UiCallback.Mode.REPLAY) {
                gameRecord = mReplay.getHistoryStringWithPositionMarker();
            }
            else {
                gameRecord = mGame.getHistoryString();
            }

            return new CommandResult(CommandResult.Type.HISTORY, CommandResult.SUCCESS, "", gameRecord);
        }
        // 7. HELP COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.Help) {
            return new CommandResult(CommandResult.Type.HELP, CommandResult.SUCCESS, "", null);
        }
        // 8. RULES COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.Rules) {
            return new CommandResult(CommandResult.Type.RULES, CommandResult.SUCCESS, "", HumanReadableRulesPrinter.getHumanReadableRules(getGame().getRules()));
        }
        // 9. SAVE COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.Save) {
            return new CommandResult(CommandResult.Type.SAVE, CommandResult.SUCCESS, "", null);
        }
        // 10. QUIT COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.Quit) {
            return new CommandResult(CommandResult.Type.QUIT, CommandResult.SUCCESS, "", null);
        }
        // 11. ANALYZE COMMAND
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
        // 12. REPLAY START COMMAND
        else if(command instanceof HumanCommandParser.ReplayEnter) {
            ReplayGame rg = new ReplayGame(mGame);
            enterReplay(rg);
            return new CommandResult(CommandResult.Type.REPLAY_ENTER, CommandResult.SUCCESS, "", null);
        }
        // 13. REPLAY PLAY HERE COMMAND
        else if(command instanceof HumanCommandParser.ReplayPlayHere) {
            if(mInGame) {
                finishGame(true);
            }

            Game g = mReplay.getGame();
            mReplay.prepareForGameStart(mReplay.getPosition());

            if(g.getClock() != null) {
                GameClock.TimeSpec attackerClock = mReplay.getTimeGuess(true);
                GameClock.TimeSpec defenderClock = mReplay.getTimeGuess(false);

                g.getClock().getClockEntry(true).setTime(attackerClock);
                g.getClock().getClockEntry(false).setTime(defenderClock);
            }
            else if(TerminalSettings.timeSpec.mainTime != 0 || TerminalSettings.timeSpec.overtimeTime != 0) {
                GameClock clock = new GameClock(g, g.getCurrentState().getAttackers(), g.getCurrentState().getDefenders(), TerminalSettings.timeSpec);

                g.setClock(clock);
            }

            enterGame(g);
            return new CommandResult(CommandResult.Type.REPLAY_PLAY_HERE, CommandResult.SUCCESS, "", null);
        }
        // 14. REPLAY RETURN COMMAND
        else if(command instanceof HumanCommandParser.ReplayReturn) {
            leaveReplay();

            return new CommandResult(CommandResult.Type.REPLAY_RETURN, CommandResult.SUCCESS, "", null);
        }
        // 15. REPLAY NEXT COMMAND
        else if(command instanceof HumanCommandParser.ReplayNext) {
            GameState state = mReplay.nextState();

            mAttacker.positionChanged(state);
            mDefender.positionChanged(state);

            if(state != null) return new CommandResult(CommandResult.Type.REPLAY_NEXT, CommandResult.SUCCESS, "", null);
            else return new CommandResult(CommandResult.Type.REPLAY_NEXT, CommandResult.FAIL, "At the end of the game history.", null);
        }
        // 16. REPLAY PREV COMMAND
        else if(command instanceof HumanCommandParser.ReplayPrevious) {
            GameState state = mReplay.previousState();

            mAttacker.positionChanged(state);
            mDefender.positionChanged(state);

            if(state != null) return new CommandResult(CommandResult.Type.REPLAY_PREVIOUS, CommandResult.SUCCESS, "", null);
            else return new CommandResult(CommandResult.Type.REPLAY_PREVIOUS, CommandResult.FAIL, "At the start of the game history.", null);
        }
        // 17. REPLAY JUMP COMMAND
        else if(command instanceof HumanCommandParser.ReplayJump) {
            HumanCommandParser.ReplayJump j = (HumanCommandParser.ReplayJump) command;
            GameState state = mReplay.setTurnIndex(j.turnIndex);

            mAttacker.positionChanged(state);
            mDefender.positionChanged(state);

            if(state != null) return new CommandResult(CommandResult.Type.REPLAY_JUMP, CommandResult.SUCCESS, "", null);
            else return new CommandResult(CommandResult.Type.REPLAY_JUMP, CommandResult.FAIL, "Turn index " + j + " out of bounds.", null);
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

    private void callbackModeChange(UiCallback.Mode mode, Object gameObject) {
        for(UiCallback c : mUiCallbacks) {
            c.modeChanging(mode, gameObject);
        }
    }

    public UiCallback.Mode getMode() {
        return mMode;
    }
}