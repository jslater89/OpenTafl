package com.manywords.softworks.tafl.command;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.replay.MoveAddress;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.UiCallback;

import java.util.List;

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
        else if(command.startsWith("rules")) {
            return newRulesCommand(engine, command);
        }
        else if(command.startsWith("save")) {
            return newSaveCommand(engine, command);
        }
        else if(command.startsWith("quit")) {
            return newQuitCommand(engine, command);
        }
        else if(command.startsWith("analyze")) {
            return newAnalyzeCommand(engine, command);
        }
        else if(command.startsWith("replay")) {
            return newReplayEnterCommand(engine, command);
        }
        else if(command.startsWith("play-here")) {
            return newReplayPlayHereCommand(engine, command);
        }
        else if(command.startsWith("return")) {
            return newReplayReturnCommand(engine, command);
        }
        else if(command.startsWith("next")) {
            return newReplayNextCommand(engine, command);
        }
        else if(command.startsWith("previous")) {
            return newReplayPreviousCommand(engine, command);
        }
        else if(command.startsWith("jump")) {
            return newReplayJumpCommand(engine, command);
        }
        else if(command.startsWith("variation")) {
            return newVariationCommand(engine, command);
        }
        else if(command.startsWith("delete")) {
            return newDeleteCommand(engine, command);
        }
        else if(command.startsWith("annotate")) {
            return newAnnotateCommand(engine, command);
        }
        else if(command.startsWith("chat")) {
            return newChatCommand(engine, command);
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
    public static Rules newRulesCommand(CommandEngine engine, String command) { return new Rules(engine, command); }
    public static Save newSaveCommand(CommandEngine engine, String command) { return new Save(engine,command); }
    public static Quit newQuitCommand(CommandEngine engine, String command) { return new Quit(engine, command); }
    public static Analyze newAnalyzeCommand(CommandEngine engine, String command) { return new Analyze(engine, command); }
    public static ReplayEnter newReplayEnterCommand(CommandEngine engine, String command) { return new ReplayEnter(engine, command); }
    public static ReplayPlayHere newReplayPlayHereCommand(CommandEngine engine, String command) { return new ReplayPlayHere(engine, command); }
    public static ReplayReturn newReplayReturnCommand(CommandEngine engine, String command) { return new ReplayReturn(engine, command); }
    public static ReplayNext newReplayNextCommand(CommandEngine engine, String command) { return new ReplayNext(engine, command); }
    public static ReplayPrevious newReplayPreviousCommand(CommandEngine engine, String command) { return new ReplayPrevious(engine, command); }
    public static ReplayJump newReplayJumpCommand(CommandEngine engine, String command) { return new ReplayJump(engine, command); }
    public static Variation newVariationCommand(CommandEngine engine, String command) { return new Variation(engine, command); }
    public static Delete newDeleteCommand(CommandEngine engine, String command) { return new Delete(engine, command); }
    public static Annotate newAnnotateCommand(CommandEngine engine, String command) { return new Annotate(engine, command); }
    public static Chat newChatCommand(CommandEngine engine, String command) { return new Chat(engine, command); }

    public static class Move extends Command {
        public final Coord from;
        public final Coord to;
        public Move(CommandEngine engine, String command) {
            super(Type.MOVE);
            if(engine.getMode() != UiCallback.Mode.GAME) {
                mError = "Not in game mode.";
            }
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
            super(Type.INFO);
            String[] commandParts = command.split(" ");
            if (commandParts.length != 2) {
                mError = "Wrong command format, try info [file+rank] [file+rank] (e.g. info a4)";
                location = null;
                stops = null;
                moves = null;
                captures = null;
                return;
            } else {
                int boardSize = engine.getGame().getRules().getBoard().getBoardDimension();
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
            super(Type.SHOW);
            // Always succeeds
        }
    }
    public static class History extends Command {
        public History(CommandEngine engine, String command) {
            super(Type.HISTORY);
            // Always succeeds
        }
    }
    public static class Help extends Command {
        public Help(CommandEngine engine, String command) {
            super(Type.HELP);
            // Always succeeds
        }
    }
    public static class Rules extends Command {
        public Rules(CommandEngine engine, String command) {
            super(Type.RULES);
            // Always succeeds
        }
    }
    public static class Save extends Command {
        public Save(CommandEngine engine, String command) {
            super(Type.SAVE);
            // Always succeeds
        }
    }
    public static class Quit extends Command {
        public Quit(CommandEngine engine, String command) {
            super(Type.QUIT);
            // Always succeeds
        }
    }
    public static class Analyze extends Command {
        public final int moves;
        public final int seconds;

        public Analyze(CommandEngine engine, String command) {
            super(Type.ANALYZE);
            String[] commandParts = command.split(" ");
            if(commandParts.length == 3) {
                int tempMoves, tempSecs;
                try {
                    tempMoves = Integer.parseInt(commandParts[1]);
                }
                catch(NumberFormatException e) {
                    mError = "Non-numeric moves";
                    tempMoves = -1;
                }
                try {
                    tempSecs = Integer.parseInt(commandParts[2]);
                }
                catch(NumberFormatException e) {
                    mError = "Non-numeric seconds";
                    tempSecs = -1;
                }

                if(!(tempMoves == -1 || tempSecs == -1)) {
                    moves = tempMoves;
                    seconds = tempSecs;
                }
                else {
                    moves = -1;
                    seconds = -1;
                }
            }
            else {
                moves = -1;
                seconds = -1;
                mError = "Improperly-formatted command";
            }
        }
    }
    public static class ReplayEnter extends Command {
        public ReplayEnter(CommandEngine engine, String command) {
            super(Type.REPLAY_ENTER);
            if(engine.getMode() == UiCallback.Mode.REPLAY) {
                mError = "Already in replay mode.";
            }
        }
    }
    public static class ReplayPlayHere extends Command {
        public ReplayPlayHere(CommandEngine engine, String command) {
            super(Type.REPLAY_PLAY_HERE);
            if(engine.getMode() != UiCallback.Mode.REPLAY) {
                mError = "Not in replay mode.";
            }
        }
    }
    public static class ReplayReturn extends Command {
        public ReplayReturn(CommandEngine engine, String command) {
            super(Type.REPLAY_RETURN);
            if(engine.getMode() != UiCallback.Mode.REPLAY) {
                mError = "Not in replay mode.";
            }
        }
    }
    public static class ReplayNext extends Command {
        public final int nextVariation;

        public ReplayNext(CommandEngine engine, String command) {
            super(Type.REPLAY_NEXT);
            if(engine.getMode() != UiCallback.Mode.REPLAY) {
                mError = "Not in replay mode.";
            }

            String[] elements = command.split(" ");
            if(elements.length > 2) {
                nextVariation = -1;
            }
            else if(elements.length == 2) {
                int test = -1;
                try {
                    test = Integer.parseInt(elements[1]);
                }
                catch (NumberFormatException e) {
                }

                if(test == 0) {
                    test = -1;
                }
                nextVariation = test;
            }
            else {
                nextVariation = -2;
            }

            if(nextVariation == -1) {
                mError = "Improperly-formatted command.";
            }
        }
    }
    public static class ReplayPrevious extends Command {
        public ReplayPrevious(CommandEngine engine, String command) {
            super(Type.REPLAY_PREVIOUS);
            if(engine.getMode() != UiCallback.Mode.REPLAY) {
                mError = "Not in replay mode.";
            }
        }
    }
    public static class ReplayJump extends Command {
        public final MoveAddress moveAddress;

        public ReplayJump(CommandEngine engine, String command) {
            super(Type.REPLAY_JUMP);
            if(engine.getMode() != UiCallback.Mode.REPLAY) {
                mError = "Not in replay mode.";
            }

            String[] commandParts = command.split(" ");
            if(commandParts.length != 2) {
                mError = "Improperly-formatted command.";
                moveAddress = null;
            }
            else {
                moveAddress = MoveAddress.parseAddress(commandParts[1]);
                if(moveAddress == null) {
                    mError = "Argument to jump not a move address: " + commandParts[1];
                }
            }
        }
    }
    public static class Variation extends Command {
        public final Coord from;
        public final Coord to;
        public Variation(CommandEngine engine, String command) {
            super(Type.VARIATION);

            if(engine.getMode() != UiCallback.Mode.REPLAY) {
                mError = "Not in replay mode.";
                from = null;
                to = null;
                return;
            }
            String[] commandParts = command.split(" ");
            if (commandParts.length != 3) {
                mError = "Wrong command format, try variation [file+rank] [file+rank] (e.g. variation a4 a3)";
                from = null;
                to = null;
                return;
            }
            else {
                ReplayGame g = engine.getReplay();
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
    public static class Delete extends Command {
        public final MoveAddress moveAddress;

        public Delete(CommandEngine engine, String command) {
            super(Type.DELETE);

            if(engine.getMode() != UiCallback.Mode.REPLAY) {
                mError = "Not in replay mode.";
            }

            String[] commandParts = command.split(" ");
            if(commandParts.length != 2) {
                mError = "Improperly-formatted command.";
                moveAddress = null;
            }
            else {
                moveAddress = MoveAddress.parseAddress(commandParts[1]);
                if(moveAddress == null) {
                    mError = "Argument to jump not a move address: " + commandParts[1];
                }
            }
        }
    }
    public static class Annotate extends Command {
        public Annotate(CommandEngine engine, String command) {
            super(Type.ANNOTATE);
        }
    }
    public static class Chat extends Command {
        public final String message;
        public Chat(CommandEngine engine, String command) {
            super(Type.CHAT);
            message = command.replaceFirst("chat", "").trim();
        }
    }

    public static String getHelpString(List<Command.Type> types) {
        StringBuilder help = new StringBuilder();

        for(Command.Type t : types) {
            help.append(getHelpString(t));
        }

        return help.toString();
    }

    public static String getHelpString(Command.Type type) {
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
                                "Show the game history so far. Displays turn indices in game, and move addresses in replay mode. " +
                                "In replay mode, only the first move address per turn is given, e.g. '1a'. Later moves in the turn " +
                                "are addressed by increasing the letter code: the move immediately after '1a' is '1b'.\n\n";
            case ANALYZE:
                return
                        "analyze [moves-to-return] [seconds]\n" +
                                "e.g. analyze 5 30\n" +
                                "Instruct the analysis engine to analyze the current board state, returning scores for up to moves-to-return potential moves.\n\n";

            case HELP:
                return
                        "help\n" +
                                "Show this message, but you could have figured that out yourself.\n\n";
            case RULES:
                return
                        "rules\n" +
                                "Show the rules of the game.\n\n";
            case SAVE:
                break;
            case QUIT:
                return
                        "quit\n" +
                                "Quit the current game, or return to the main menu.\n\n";
            case REPLAY_ENTER:
                return
                        "replay\n" +
                                "Enter replay mode for the current game.\n\n";
            case REPLAY_PLAY_HERE:
                return
                        "play-here\n" +
                                "Leave replay mode, starting a new game at the currently-displayed state.\n\n";
            case REPLAY_RETURN:
                return
                        "return\n" +
                                "Leave replay mode, returning to current state of the game in progress.\n\n";
            case REPLAY_NEXT:
                return
                        "next\n" +
                                "Move forward in the replay one step.\n\n" +
                                "next [number]\n" +
                                "Move to the variation from the current state indexed by the number given.\n\n";
            case REPLAY_PREVIOUS:
                return
                        "previous\n" +
                                "Move backward in the replay one step.\n\n";
            case REPLAY_JUMP:
                return
                        "jump [move-address]\n" +
                                "Jump to the given move address in the replay.\n\n";
            case VARIATION:
                return "variation [space-notation] [space-notation]\n" +
                                "Make a variation from the current state, moving the taflman at the first location to the space at the second location.\n\n";
            case DELETE:
                return "delete [move-address]\n" +
                                "Delete the variation addressed by the given move, as well as all of its children. Be careful when deleting states in the principal variation! " +
                                "Doing so will destroy the rest of the game record.\n\n";
            case ANNOTATE:
                return "annotate\n" +
                                "Open an editor dialog to modify the annotation displayed when this board position is displayed.\n\n";
            case CHAT:
                return
                        "chat [text]\n" +
                                "Send a chat message to other players in the current game.\n\n";
        }

        return "";
    }
}
