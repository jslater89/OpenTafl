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

    private void finishGame() {
        mUiCallback.gameFinished();
    }

    private final Player.MoveCallback mMoveCallback = new Player.MoveCallback() {

        @Override
        public void onMoveDecided(MoveRecord move) {
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
                String message = "Illegal play. ";
                if (result == GameState.ILLEGAL_SIDE) {
                    message += "Not your taflman.";
                }
                else {
                    message += "Move disallowed.";
                }

                mUiCallback.moveResult(new CommandResult(CommandResult.Type.NONE, CommandResult.FAIL, message, null), move);
                mUiCallback.gameStateAdvanced();
            }
            else {
                mCurrentPlayer = (mGame.getCurrentSide().isAttackingSide() ? mAttacker : mDefender);
                mUiCallback.moveResult(new CommandResult(CommandResult.Type.NONE, CommandResult.SUCCESS, "", null), move);
                mUiCallback.gameStateAdvanced();
            }

            waitForNextMove();
        }
    };

    public CommandResult executeCommand(Command command) {
        if(command == null) {
            return new CommandResult(CommandResult.Type.NONE, CommandResult.FAIL, "No information", null);
        }
        if(command instanceof HumanCommandParser.Move) {
            HumanCommandParser.Move m = (HumanCommandParser.Move) command;
            if(m.from == null || m.to == null) {
                return new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, "Invalid coords", null);
            }
            int fromX = m.from.x;
            int fromY = m.from.y;
            int toX = m.to.x;
            int toY = m.to.y;
            int boardSize = mGame.getCurrentState().getBoard().getBoardDimension();

            String message = "";

            if (fromX < 0 || fromX >= boardSize || fromY < 0 || fromY >= boardSize) {
                message = "Piece out of bounds";
                return new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, message, null);
            }

            if (toX < 0 || toX >= boardSize || toY < 0 || toY >= boardSize) {
                message = "Destination out of bounds";
                return new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, message, null);
            }

            char piece = mGame.getCurrentState().getPieceAt(fromX, fromY);

            if (piece == Taflman.EMPTY) {
                message = "No piece at $fromX $fromY";
                return new CommandResult(CommandResult.Type.MOVE, CommandResult.FAIL, message, null);
            }
            Coord destination = mGame.getCurrentState().getSpaceAt(toX, toY);
            MoveRecord record = new MoveRecord(Taflman.getCurrentSpace(mGame.getCurrentState(), piece), destination);
            return new CommandResult(CommandResult.Type.MOVE, CommandResult.SUCCESS, "", record);
        }

        return new CommandResult(CommandResult.Type.NONE, CommandResult.FAIL, "No information", null);
    }

    public Game getGame() {
        return mGame;
    }
}