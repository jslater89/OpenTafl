package com.manywords.softworks.tafl.command;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.*;
import com.manywords.softworks.tafl.engine.clock.GameClock;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.engine.replay.ReplayGameState;
import com.manywords.softworks.tafl.engine.replay.Variation;
import com.manywords.softworks.tafl.network.packet.ingame.VictoryPacket;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.HumanReadableRulesPrinter;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.command.player.ExternalEnginePlayer;
import com.manywords.softworks.tafl.command.player.Player;
import com.manywords.softworks.tafl.command.player.external.engine.ExternalEngineHost;

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

        if(TerminalSettings.analysisEngine && TerminalSettings.analysisEngineSpec != null) {
            mDummyAnalysisPlayer = new ExternalEnginePlayer();
            mDummyAnalysisPlayer.setGame(g);
            mDummyAnalysisPlayer.setCallback(mPlayerCallback);
            mAnalysisEngine = mDummyAnalysisPlayer.setupAnalysisEngine();
        }
    }

    public void enterReplay(ReplayGame rg) {
        mMode = UiCallback.Mode.REPLAY;
        mReplay = rg;

        Game g = mReplay.getGame();

        g.setUiCallback(mPrimaryUiCallback);
        if(mDummyAnalysisPlayer != null) {
            mDummyAnalysisPlayer.setGame(g);
        }

        callbackModeChange(UiCallback.Mode.REPLAY, rg);
    }

    public void leaveReplay() {
        mMode = UiCallback.Mode.GAME;

        if(mDummyAnalysisPlayer != null) {
            mDummyAnalysisPlayer.setGame(mGame);
        }

        callbackModeChange(UiCallback.Mode.GAME, mGame);
    }
    public void enterGame(Game g) {
        mMode = UiCallback.Mode.GAME;
        mReplay = null;
        mGame = g;
        g.setUiCallback(mPrimaryUiCallback);
        mAttacker.setGame(g);
        mDefender.setGame(g);

        if(mDummyAnalysisPlayer != null) {
            mDummyAnalysisPlayer.setGame(g);
        }

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
            mGame.getClock().updateClients();
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

    public boolean isInGame() {
        return mInGame;
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

    public void finishGameQuietly() {
        finishGame(true);
    }

    private void finishGame(boolean quiet) {
        mInGame = false;
        mGame.finish();

        stopPlayers();

        if(!quiet) {
            callbackGameFinished();
        }
    }

    public void networkVictory(VictoryPacket.Victory victory) {
        if (victory == VictoryPacket.Victory.ATTACKER) {
            callbackVictoryForSide(mGame.getCurrentState().getAttackers());
        }
        else if (victory == VictoryPacket.Victory.DEFENDER) {
            callbackVictoryForSide(mGame.getCurrentState().getDefenders());
        }
        else if (victory == VictoryPacket.Victory.DRAW) {
            callbackVictoryForSide(null);
        }

        finishGame();
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
        public void timeUpdate(boolean currentSideAttackers) {
            callbackTimeUpdate(currentSideAttackers);

            mAttacker.timeUpdate();
            mDefender.timeUpdate();
        }

        @Override
        public void timeExpired(boolean currentSideAttackers) {
            mPrimaryUiCallback.statusText("Time expired!");
            if(currentSideAttackers) {
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

            ReplayGameState replayState = null;
            if(getMode() == UiCallback.Mode.REPLAY) {
                replayState = mReplay.getCurrentState();
                leaveReplay();
            }

            String message = "Illegal play. ";
            if(player != mCurrentPlayer) {
                message += "Not your turn.";
                callbackMoveResult(new CommandResult(Command.Type.MOVE, CommandResult.FAIL, message, null), null);
                return;
            }
            int result = mGame.getCurrentState().makeMove(move);

            // non-error moves
            if(result >= GameState.LOWEST_NONERROR_RESULT) {
                mLastPlayer = mCurrentPlayer;
                mCurrentPlayer = (mGame.getCurrentSide().isAttackingSide() ? mAttacker : mDefender);
                callbackMoveResult(new CommandResult(Command.Type.MOVE, CommandResult.SUCCESS, "", null), move);
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

                callbackMoveResult(new CommandResult(Command.Type.MOVE, CommandResult.FAIL, message, null), move);
            }

            if(replayState != null) {
                enterReplay(ReplayGame.copyGameToReplay(mGame));
                mReplay.setPositionByAddress(replayState.getMoveAddress());
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
            return new CommandResult(Command.Type.NONE, CommandResult.FAIL, "Command not recognized", null);
        }
        // 2. COMMAND WITH ERROR: FAILURE
        else if(!command.getError().equals("")) {
            return new CommandResult(Command.Type.SENT, CommandResult.FAIL, command.mError, null);
        }
        // 3. MOVE COMMAND: RETURN MOVE RECORD (receiver sends to callback after verifying side &c)
        else if(command.getType() == Command.Type.MOVE) {
            if(!mInGame) {
                return new CommandResult(Command.Type.MOVE, CommandResult.FAIL, "Game over", null);
            }

            CommandParser.Move m = (CommandParser.Move) command;
            if(m.from == null || m.to == null) {
                return new CommandResult(Command.Type.MOVE, CommandResult.FAIL, "Invalid coords", null);
            }

            String message = "";

            char piece = mGame.getCurrentState().getPieceAt(m.from.x, m.from.y);
            if (piece == Taflman.EMPTY) {
                message = "No taflman at " + m.from;
                return new CommandResult(Command.Type.MOVE, CommandResult.FAIL, message, null);
            }

            Coord destination = mGame.getCurrentState().getSpaceAt(m.to.x, m.to.y);
            MoveRecord record = new MoveRecord(Taflman.getCurrentSpace(mGame.getCurrentState(), piece), destination);

            return new CommandResult(Command.Type.MOVE, CommandResult.SUCCESS, "", record);
        }
        // 4. INFO COMMAND: SUCCESS (command parser does all the required verification)
        else if(command.getType() == Command.Type.INFO) {
            return new CommandResult(Command.Type.INFO, CommandResult.SUCCESS, "", null);
        }
        // 5. SHOW COMMAND: SUCCESS
        else if(command.getType() == Command.Type.SHOW) {
            return new CommandResult(Command.Type.SHOW, CommandResult.SUCCESS, "", null);
        }
        // 6. HISTORY COMMAND: SUCCESS
        else if(command.getType() == Command.Type.HISTORY) {
            String gameRecord;
            if(mMode == UiCallback.Mode.REPLAY) {
                gameRecord = mReplay.getReplayModeInGameHistoryString();
            }
            else {
                gameRecord = mGame.getHistoryString();
            }

            return new CommandResult(Command.Type.HISTORY, CommandResult.SUCCESS, "", gameRecord);
        }
        // 7. HELP COMMAND: SUCCESS
        else if(command.getType() == Command.Type.HELP) {
            return new CommandResult(Command.Type.HELP, CommandResult.SUCCESS, "", null);
        }
        // 8. RULES COMMAND: SUCCESS
        else if(command.getType() == Command.Type.RULES) {
            return new CommandResult(Command.Type.RULES, CommandResult.SUCCESS, "", HumanReadableRulesPrinter.getHumanReadableRules(getGame().getRules()));
        }
        // 9. SAVE COMMAND: SUCCESS
        else if(command.getType() == Command.Type.SAVE) {
            return new CommandResult(Command.Type.SAVE, CommandResult.SUCCESS, "", null);
        }
        // 10. QUIT COMMAND: SUCCESS
        else if(command.getType() == Command.Type.QUIT) {
            return new CommandResult(Command.Type.QUIT, CommandResult.SUCCESS, "", null);
        }
        // 11. ANALYZE COMMAND
        else if(command.getType() == Command.Type.ANALYZE) {
            CommandParser.Analyze a = (CommandParser.Analyze) command;

            if(mAnalysisEngine == null) {
                return new CommandResult(Command.Type.ANALYZE, CommandResult.FAIL, "No analysis engine loaded", null);
            }
            else {
                Game g = mGame;
                if(mMode == UiCallback.Mode.REPLAY) {
                    g = mReplay.getGame();
                }

                mAnalysisEngine.analyzePosition(a.moves, a.seconds, g.getCurrentState());
                return new CommandResult(Command.Type.ANALYZE, CommandResult.SUCCESS, "", null);
            }
        }
        // 12. REPLAY START COMMAND
        else if(command.getType() == Command.Type.REPLAY_ENTER) {
            // TODO: if we already have an mReplay, update that off of the current game state.
            ReplayGame rg = ReplayGame.copyGameToReplay(mGame);
            enterReplay(rg);
            return new CommandResult(Command.Type.REPLAY_ENTER, CommandResult.SUCCESS, "", null);
        }
        // 13. REPLAY PLAY HERE COMMAND
        else if(command.getType() == Command.Type.REPLAY_PLAY_HERE) {
            if(mInGame) {
                finishGame(true);
            }

            Game g = mReplay.getGame();
            mReplay.prepareForGameStart(mReplay.getCurrentState().getMoveAddress());

            if(g.getClock() != null) {
                TimeSpec attackerClock = mReplay.getTimeGuess(true);
                TimeSpec defenderClock = mReplay.getTimeGuess(false);

                g.getClock().getClockEntry(true).setTime(attackerClock);
                g.getClock().getClockEntry(false).setTime(defenderClock);
            }
            else if(TerminalSettings.timeSpec.mainTime != 0 || TerminalSettings.timeSpec.overtimeTime != 0) {
                GameClock clock = new GameClock(g, TerminalSettings.timeSpec);

                g.setClock(clock);
            }

            enterGame(g);
            return new CommandResult(Command.Type.REPLAY_PLAY_HERE, CommandResult.SUCCESS, "", null);
        }
        // 14. REPLAY RETURN COMMAND
        else if(command.getType() == Command.Type.REPLAY_RETURN) {
            leaveReplay();

            return new CommandResult(Command.Type.REPLAY_RETURN, CommandResult.SUCCESS, "", null);
        }
        // 15. REPLAY NEXT COMMAND
        else if(command.getType() == Command.Type.REPLAY_NEXT) {
            CommandParser.ReplayNext n = ((CommandParser.ReplayNext) command);
            if(n.nextVariation == -1) {
                return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.FAIL, "Argument is not a variation index", null);
            }


            ReplayGameState state = null;
            ReplayGame.NavigationResult result = mReplay.nextState(n.nextVariation);
            state = mReplay.getCurrentState();

            switch(result) {
                case SUCCESS:
                    mAttacker.positionChanged(state);
                    mDefender.positionChanged(state);
                    return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.SUCCESS, "", state.getLastMoveResult());
                case INVALID_ARGUMENT:
                    return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.FAIL, "Invalid argument.", null);
                case END_OF_GAME:
                    return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.FAIL, "At the end of the game history.", null);
                case PUZZLE_DISALLOWS_NAVIGATION:
                    return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.FAIL, "Puzzle mode does not allow navigation to that state.", null);
            }
        }
        // 16. REPLAY PREV COMMAND
        else if(command.getType() == Command.Type.REPLAY_PREVIOUS) {
            ReplayGameState state = null;
            ReplayGame.NavigationResult result = mReplay.previousState();
            state = mReplay.getCurrentState();

            switch(result) {
                case SUCCESS:
                    mAttacker.positionChanged(state);
                    mDefender.positionChanged(state);
                    return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.SUCCESS, "", state.getLastMoveResult());
                case INVALID_ARGUMENT:
                    return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.FAIL, "Invalid argument.", null);
                case END_OF_GAME:
                    return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.FAIL, "At the end of the game history.", null);
                case PUZZLE_DISALLOWS_NAVIGATION:
                    return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.FAIL, "Puzzle mode does not allow navigation to that state.", null);
            }
        }
        // 17. REPLAY JUMP COMMAND
        else if(command.getType() == Command.Type.REPLAY_JUMP) {
            CommandParser.ReplayJump j = (CommandParser.ReplayJump) command;
            if(j.moveAddress != null) {
                ReplayGameState state = null;
                ReplayGame.NavigationResult result = mReplay.setPositionByAddress(j.moveAddress);
                state = mReplay.getCurrentState();

                mAttacker.positionChanged(state);
                mDefender.positionChanged(state);

                switch(result) {
                    case SUCCESS:
                        mAttacker.positionChanged(state);
                        mDefender.positionChanged(state);
                        return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.SUCCESS, "", state.getLastMoveResult());
                    case INVALID_ARGUMENT:
                        return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.FAIL, "Invalid argument.", null);
                    case END_OF_GAME:
                        return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.FAIL, "At the end of the game history.", null);
                    case PUZZLE_DISALLOWS_NAVIGATION:
                        return new CommandResult(Command.Type.REPLAY_NEXT, CommandResult.FAIL, "Puzzle mode does not allow navigation to that state.", null);
                }
            }

            return new CommandResult(Command.Type.REPLAY_JUMP, CommandResult.FAIL, "Move address out of bounds.", null);
        }
        // 18. VARIATION COMMAND
        else if(command.getType() == Command.Type.VARIATION) {
            CommandParser.Variation v = (CommandParser.Variation) command;

            if(v.from == null || v.to == null) {
                return new CommandResult(Command.Type.VARIATION, CommandResult.FAIL, "Invalid coords", null);
            }

            String message = "";

            char piece = mReplay.getCurrentState().getPieceAt(v.from.x, v.from.y);
            if (piece == Taflman.EMPTY) {
                message = "No taflman at " + v.from;
                return new CommandResult(Command.Type.VARIATION, CommandResult.FAIL, message, null);
            }

            Coord destination = mReplay.getCurrentState().getSpaceAt(v.to.x, v.to.y);
            MoveRecord record = new MoveRecord(Taflman.getCurrentSpace(mReplay.getCurrentState(), piece), destination);

            ReplayGameState result = mReplay.makeVariation(record);
            int moveResult = result.getLastMoveResult();

            OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Variation result: " + moveResult);
            if(moveResult < GameState.LOWEST_NONERROR_RESULT) {
                return new CommandResult(Command.Type.VARIATION, CommandResult.FAIL, GameState.getStringForMoveResult(moveResult), moveResult);
            }
            else {
                return new CommandResult(Command.Type.VARIATION, CommandResult.SUCCESS, "", moveResult);
            }
        }
        else if(command.getType() == Command.Type.DELETE) {
            CommandParser.Delete d = (CommandParser.Delete) command;

            boolean deleted = mReplay.deleteVariation(d.moveAddress);

            if(deleted) return new CommandResult(Command.Type.DELETE, CommandResult.SUCCESS, "", null);
            else return new CommandResult(Command.Type.DELETE, CommandResult.FAIL, "No variation with address " + d.moveAddress + " to delete.", null);
        }
        // 19. ANNOTATE COMMAND
        else if(command.getType() == Command.Type.ANNOTATE) {
            return new CommandResult(Command.Type.ANNOTATE, CommandResult.SUCCESS, "", null);
        }
        // 20. CLIPBOARD-COPY COMMAND
        else if(command.getType() == Command.Type.CLIPBOARD_COPY) {
            String completePositionString = "";
            if(mMode == UiCallback.Mode.GAME) {
                completePositionString = mGame.getCurrentState().getPasteableRulesString();
            }
            else if(mMode == UiCallback.Mode.REPLAY) {
                completePositionString = mReplay.getCurrentState().getPasteableRulesString();
            }

            Utilities.pushToClipboard(completePositionString);
            return new CommandResult(Command.Type.CLIPBOARD_COPY, CommandResult.SUCCESS, "Copied position to clipboard", null);
        }
        // 21. CLIPBOARD-PASTE COMMAND
        else if(command.getType() == Command.Type.CLIPBOARD_PASTE) {
            return new CommandResult(Command.Type.CLIPBOARD_PASTE, CommandResult.SUCCESS, "", null);
        }
        // 22. CHAT COMMAND
        else if(command.getType() == Command.Type.CHAT) {
            CommandParser.Chat c = (CommandParser.Chat) command;
            return new CommandResult(Command.Type.CHAT, CommandResult.SUCCESS, c.message, null);
        }

        return new CommandResult(Command.Type.NONE, CommandResult.FAIL, "Command not recognized", null);
    }

    public Game getGame() {
        return mGame;
    }

    public ReplayGame getReplay() {
        return mReplay;
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

    private void callbackTimeUpdate(boolean currentSideAttackers) {
        for(UiCallback c : mUiCallbacks) {
            c.timeUpdate(currentSideAttackers);
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

    public ExternalEnginePlayer getAnalysisPlayer() {
        return mDummyAnalysisPlayer;
    }
}