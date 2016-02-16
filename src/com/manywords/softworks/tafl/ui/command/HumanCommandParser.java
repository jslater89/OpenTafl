package com.manywords.softworks.tafl.ui.command;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;

/**
 * Created by jay on 2/15/16.
 */
public class HumanCommandParser {
    public static Command parseCommand(CommandEngine engine, String command) {
        return null;
    }

    public static Move newMoveCommand(CommandEngine engine, String command) {
        return new Move(engine, command);
    }

    public static class Move extends Command {
        public final Coord from;
        public final Coord to;
        public Move(CommandEngine engine, String command) {
            String[] commandParts = command.split(" ");
            if (commandParts.length != 3) {
                mError = "Wrong command format, try move [file+rank] [file+rank] (e.g. move a4 a3)";
                from = null;
                to = null;
                return;
            }
            else {
                Game g = engine.getGame();
                int boardSize = g.getCurrentState().getBoard().getBoardDimension();

                String fromString = commandParts[1].toLowerCase();
                String toString = commandParts[2].toLowerCase();

                if (!Board.validateChessNotation(fromString, boardSize) || !Board.validateChessNotation(toString, boardSize)) {
                    mError = "Incorrect space formats: $fromString $toString";
                    from = null;
                    to = null;
                    return;
                }

                from = Board.getCoordFromChessNotation(fromString);
                to = Board.getCoordFromChessNotation(toString);
            }
        }
    }
    public static class Info extends Command {

    }
    public static class Show extends Command {

    }
    public static class History extends Command {

    }
    public static class Help extends Command {

    }
    public static class Quit extends Command {

    }
}
