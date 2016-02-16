package com.manywords.softworks.tafl.ui.command;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.player.Player;

public class CommandEngine {
    private Game mGame;
    public CommandEngine(Game g) {
        mGame = g;
    }

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