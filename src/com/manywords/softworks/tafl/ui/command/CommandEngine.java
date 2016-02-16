package com.manywords.softworks.tafl.ui.command;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.player.Player;

public class CommandEngine {
    private Game mGame;
    private Player mAttacker;
    private Player mDefender;
    private Player mCurrentPlayer;
    private UiCallback mUiCallback;

    private int mSearchDepth = 4;

    public CommandEngine(Game g, UiCallback callback, Player attacker, Player defender) {
        mGame = g;
        mUiCallback = callback;
        mAttacker = attacker;
        mDefender = defender;
    }

    public void setSearchDepth(int depth) {
        mSearchDepth = depth;
    }

    public void startGame() {
        mAttacker.setCallback(mMoveCallback);
        mDefender.setCallback(mMoveCallback);

        if (mGame.getCurrentState().getCurrentSide().isAttackingSide()) {
            mCurrentPlayer = mAttacker;
        } else {
            mCurrentPlayer = mDefender;
        }

        mUiCallback.gameStarting();
        waitForNextMove();
    }

    private void waitForNextMove() {
        mUiCallback.awaitingMove(mGame.getCurrentSide().isAttackingSide());
        mCurrentPlayer.getNextMove(mUiCallback, mGame, mSearchDepth);
    }

    public void finishGame() {
        mAttacker.stop();
        mDefender.stop();
        mUiCallback.gameFinished();
    }

    private final Player.MoveCallback mMoveCallback = new Player.MoveCallback() {

        @Override
        public void onMoveDecided(Player player, MoveRecord move) {
            String message = "Illegal play. ";
            if(player != mCurrentPlayer) {
                message += "Not your turn.";
                mUiCallback.moveResult(new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, message, null), null);
            }
            int result =
                    mGame.getCurrentState().moveTaflman(
                            mGame.getCurrentState().getBoard().getOccupier(move.start.x, move.start.y),
                            mGame.getCurrentState().getSpaceAt(move.end.x, move.end.y));

            if (result == GameState.ATTACKER_WIN) {
                mUiCallback.victoryForSide(mGame.getCurrentState().getAttackers());
                finishGame();
                return;
            }
            else if (result == GameState.DEFENDER_WIN) {
                mUiCallback.victoryForSide(mGame.getCurrentState().getDefenders());
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

                mUiCallback.moveResult(new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, message, null), move);
            }
            else {
                mCurrentPlayer = (mGame.getCurrentSide().isAttackingSide() ? mAttacker : mDefender);
                mUiCallback.moveResult(new CommandResult(CommandResult.Type.MOVE, CommandResult.SUCCESS, "", null), move);
                mUiCallback.gameStateAdvanced();
            }

            waitForNextMove();
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
        // 5. HISTORY COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.History) {
            String gameRecord = mGame.getHistoryString();

            return new CommandResult(CommandResult.Type.HISTORY, CommandResult.SUCCESS, "", gameRecord);
        }
        // 5. HELP COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.Help) {
            return new CommandResult(CommandResult.Type.HELP, CommandResult.SUCCESS, "", null);
        }
        // 5. QUIT COMMAND: SUCCESS
        else if(command instanceof HumanCommandParser.Quit) {
            return new CommandResult(CommandResult.Type.QUIT, CommandResult.SUCCESS, "", null);
        }

        return new CommandResult(CommandResult.Type.NONE, CommandResult.FAIL, "Command not recognized", null);
    }

    public Game getGame() {
        return mGame;
    }
}