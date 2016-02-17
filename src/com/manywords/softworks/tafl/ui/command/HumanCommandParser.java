package com.manywords.softworks.tafl.ui.command;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.List;
import java.util.StringJoiner;

/**
 * Created by jay on 2/15/16.
 */
public class HumanCommandParser {
    public static Command parseCommand(CommandEngine engine, String command) {
        if(command.startsWith("move")) {
            return newMoveCommand(engine, command);
        }
        else if(command.startsWith("info")) {
            return newInfoCommand(engine, command);
        }
        else if(command.startsWith("show")) {
            return newShowCommand(engine, command);
        }
        else if(command.startsWith("history")) {
            return newHistoryCommand(engine, command);
        }
        else if(command.startsWith("help")) {
            return newHelpCommand(engine, command);
        }
        else if(command.startsWith("quit")) {
            return newQuitCommand(engine, command);
        }
        return null;
    }

    public static Move newMoveCommand(CommandEngine engine, String command) {
        return new Move(engine, command);
    }
    public static Info newInfoCommand(CommandEngine engine, String command) { return new Info(engine, command); }
    public static Show newShowCommand(CommandEngine engine, String command) { return new Show(engine, command); }
    public static History newHistoryCommand(CommandEngine engine, String command) { return new History(engine, command); }
    public static Help newHelpCommand(CommandEngine engine, String command) { return new Help(engine, command); }
    public static Quit newQuitCommand(CommandEngine engine, String command) { return new Quit(engine, command); }

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
                    mError = "Incorrect space format: " + fromString + " " + toString;
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
        public final Coord location;
        public final List<Coord> stops;
        public final List<Coord> moves;
        public final List<Coord> captures;

        public Info(CommandEngine engine, String command) {
            String[] commandParts = command.split(" ");
            if (commandParts.length != 2) {
                mError = "Wrong command format, try info [file+rank] [file+rank] (e.g. info a4)";
                location = null;
                stops = null;
                moves = null;
                captures = null;
                return;
            } else {
                int boardSize = engine.getGame().getGameRules().getBoard().getBoardDimension();
                String chessString = commandParts[1].toLowerCase();

                if (!Board.validateChessNotation(chessString, boardSize)) {
                    mError = "Incorrect space format: " + chessString;
                    location = null;
                    stops = null;
                    moves = null;
                    captures = null;
                    return;
                }

                int x = Board.getCoordMapFromChessNotation(chessString).get("x");
                int y = Board.getCoordMapFromChessNotation(chessString).get("y");

                char piece = engine.getGame().getCurrentState().getPieceAt(x, y);
                if (piece != Taflman.EMPTY) {
                    location = Coord.get(x, y);
                    stops = Taflman.getAllowableDestinations(engine.getGame().getCurrentState(), piece);
                    moves = Taflman.getAllowableMoves(engine.getGame().getCurrentState(), piece);
                    captures = Taflman.getCapturingMoves(engine.getGame().getCurrentState(), piece);
                } else {
                    location = null;
                    stops = null;
                    moves = null;
                    captures = null;
                    mError = "No taflman at " + chessString;
                }
            }
        }
    }
    public static class Show extends Command {
        public Show(CommandEngine engine, String command) {
            // Always succeeds
        }
    }
    public static class History extends Command {
        public History(CommandEngine engine, String command) {
            // Always succeeds
        }
    }
    public static class Help extends Command {
        public Help(CommandEngine engine, String command) {
            // Always succeeds
        }
    }
    public static class Rules extends Command {
        public Rules(CommandEngine engine, String command) {
            // Always succeeds
        }
    }
    public static class Quit extends Command {
        public Quit(CommandEngine engine, String command) {
            // Always succeeds
        }
    }

    public static String getHelpString(List<CommandResult.Type> types) {
        StringBuilder help = new StringBuilder();

        for(CommandResult.Type t : types) {
            help.append(getHelpString(t));
        }

        return help.toString();
    }

    public static String getHelpString(CommandResult.Type type) {
        switch (type) {

            case NONE:
                return "";
            case SENT:
                return "";
            case MOVE:
                return
                        "move [space-notation] [space-notation]\n" +
                                "e.g. move a4 a1\n" +
                                "Move the taflman at the first space to the second.\n\n";
            case INFO:
                return
                        "info [space-notation]\n" +
                                "e.g. info a4\n" +
                                "Show the allowable moves, destinations, and captures for the taflman at the given space.\n\n";
            case SHOW:
                return
                        "show\n" +
                                "Redraw the board.\n\n";
            case HISTORY:
                return
                        "history\n" +
                                "Show the game history so far.\n\n";
            case HELP:
                return
                        "help\n" +
                                "Show this message.\n\n";
            case RULES:
                return
                        "rules\n" +
                                "Show the rules of the game.\n\n";
            case QUIT:
                return
                        "quit\n" +
                                "Quit the current game, or return to the main menu.";
        }

        return "";
    }
}
