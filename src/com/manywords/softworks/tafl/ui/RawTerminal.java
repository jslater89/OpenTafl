package com.manywords.softworks.tafl.ui;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.ui.player.LocalAi;
import com.manywords.softworks.tafl.ui.player.LocalHuman;
import com.manywords.softworks.tafl.ui.player.Player;
import com.manywords.softworks.tafl.ui.player.Player.MoveCallback;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class RawTerminal implements UiCallback {
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static String ANSI_RESET = "\u001B[0m";
    public static String ANSI_BLACK = "\u001B[30m";
    public static String ANSI_RED = "\u001B[31m";
    public static String ANSI_GREEN = "\u001B[32m";
    public static String ANSI_YELLOW = "\u001B[33m";
    public static String ANSI_BLUE = "\u001B[34m";
    public static String ANSI_PURPLE = "\u001B[35m";
    public static String ANSI_CYAN = "\u001B[36m";
    public static String ANSI_WHITE = "\u001B[37m";

    public static final int ATTACKING_SIDE = 0;
    public static final int DEFENDING_SIDE = 1;

    public static String SPACER = "";

    private Game mGame;
    private boolean mUiRunning = false;

    private boolean mInMenu = true;
    private boolean mInOptions = false;

    private boolean mInGame = false;


    private final int WAITING_FOR_MOVE = 0;
    private final int MOVE_INVALID = -1;
    private final int MOVE_VALID = 1;
    private final int IDLE = -2;
    private AtomicInteger mMoveStatus = new AtomicInteger(IDLE);

    private boolean mPostGame = false;
    private ConsoleReader mConsoleReader;

    // Player zero is always the attacker.
    private Player[] mPlayers = new Player[2];
    private int mCurrentPlayer = 0;

    private int mSearchDepth = 4;

    private static void println(Object string) {
        System.out.println(string);
    }

    private static void print(Object string) {
        System.out.print(string);
    }

    public void runUi() {
        try {
            mConsoleReader = new ConsoleReader();
        } catch (IOException e) {
            println("Error starting console reader!");
            System.exit(-1);
        }

        String systemName = System.getProperty("os.name");

        boolean disableColor = systemName.contains("Windows") || !mConsoleReader.getTerminal().isAnsiSupported();
        if (disableColor) {
            ANSI_RESET = "";
            ANSI_BLACK = "";
            ANSI_RED = "";
            ANSI_GREEN = "";
            ANSI_YELLOW = "";
            ANSI_BLUE = "";
            ANSI_PURPLE = "";
            ANSI_CYAN = "";
            ANSI_WHITE = "";
        }

        mUiRunning = true;

        try {
            mConsoleReader.clearScreen();
        } catch (IOException e) {
            println("Console reader error!");
            System.exit(-1);
        }

        // Defending side
        mPlayers[1] = new LocalHuman();

        // Attacking side
        mPlayers[0] = new LocalAi();

        mPlayers[0].setCallback(mMoveCallback);
        mPlayers[1].setCallback(mMoveCallback);

        printMenuHeader();

        while (mUiRunning) {
            if (mInMenu) {
                waitForMenuInput();
            } else if (mInOptions) {
                waitForOptionsInput();
            } else if (mInGame) {
                if (mMoveStatus.intValue() != WAITING_FOR_MOVE) {
                    if(mMoveStatus.intValue() == MOVE_VALID) {
                        GameState state = mGame.getCurrentState();
                        if (state.getCurrentSide().isAttackingSide()) {
                            mCurrentPlayer = 0;
                        }
                        else {
                            mCurrentPlayer = 1;
                        }
                    }

                    waitForNextMove();
                }
            } else if (mPostGame) {
                waitForPostGameInput();
            }
        }
    }

    private final MoveCallback mMoveCallback = new MoveCallback() {

        @Override
        public void onMoveDecided(MoveRecord move) {
            int result =
                    mGame.getCurrentState().moveTaflman(
                            mGame.getCurrentState().getBoard().getOccupier(move.start.x, move.start.y),
                            mGame.getCurrentState().getSpaceAt(move.end.x, move.end.y));

            if (result != GameState.GOOD_MOVE) {
                mMoveStatus.set(MOVE_INVALID);
                print("Illegal play. ");
                if (result == GameState.ILLEGAL_SIDE) {
                    println("Not your taflman.");
                }
                else {
                    println("Move disallowed.");
                }
            }
            else {
                mMoveStatus.set(MOVE_VALID);
            }
        }
    };

    public void startGame(Game game) {
        mGame = game;

        mInMenu = false;
        mPostGame = false;

        mInGame = true;

        if (mGame.getCurrentState().getCurrentSide().isAttackingSide()) {
            mCurrentPlayer = ATTACKING_SIDE;
        } else {
            mCurrentPlayer = DEFENDING_SIDE;
        }

        //System.out.println("Running AI search for JIT optimization.");
        //new AiWorkspace(mGame, mGame.getCurrentState()).explore(2);

        renderGameState(mGame.getCurrentState());
        printStatus();
    }

    public static void disableColor() {
        ANSI_RESET = "";
        ANSI_BLACK = "";
        ANSI_RED = "";
        ANSI_GREEN = "";
        ANSI_YELLOW = "";
        ANSI_BLUE = "";
        ANSI_PURPLE = "";
        ANSI_CYAN = "";
        ANSI_WHITE = "";
    }

    public static void enableColor() {
        ANSI_RESET = RESET;
        ANSI_BLACK = BLACK;
        ANSI_RED = RED;
        ANSI_GREEN = GREEN;
        ANSI_YELLOW = YELLOW;
        ANSI_BLUE = BLUE;
        ANSI_PURPLE = PURPLE;
        ANSI_CYAN = CYAN;
        ANSI_WHITE = WHITE;
    }

    public boolean inGame() {
        return mInGame;
    }

    public void victoryForSide(Side side) {
        if (side.isAttackingSide()) {
            println("Attackers win!");
        } else {
            println("Defenders win!");
        }

        mInGame = false;
        mPostGame = true;
        renderGameState(mGame.getCurrentState());
    }

    public void gameStateAdvanced() {
        renderGameState(mGame.getCurrentState());
        printStatus();
    }

    private void waitForPostGameInput() {
        String command = "";
        try {
            command = mConsoleReader.readLine("Command (quit, history): ");
            command = command.toLowerCase();
            mConsoleReader.clearScreen();
        } catch (IOException e) {
            println("Console reader error!");
            System.exit(-1);
        }

        renderGameState(mGame.getCurrentState());
        if (command.startsWith("quit")) {
            mPostGame = false;
            mInMenu = true;
        }

        // Handle history command
        if (command.startsWith("history")) {
            for (GameState state : mGame.getHistory()) {
                MoveRecord record = state.getExitingMove();
                println(record);
            }
        }
    }

    private void printMenuHeader() {
        println("\n\n\n");
        println("\t\t\tOpenTafl");
        println("\t\t\tThe old Norse board game,");
        println("\t\t\tin an old computer style.");
        println("\n\n\n");
    }

    private String getPlayerType(Player player) {
        switch (player.getType()) {
            case HUMAN:
                return "Local human player";
            case AI:
                return "AI player";
            case NETWORK:
                return "Network player";
            default:
                return "How did you get here?";
        }
    }

    private void printPlayers() {
        println("Attacking player: " + getPlayerType(mPlayers[ATTACKING_SIDE]));
        println("Defending player: " + getPlayerType(mPlayers[DEFENDING_SIDE]));
    }

    private void waitForMenuInput() {
        String command = "";
        try {
            command = mConsoleReader.readLine("Command (variants, options, play, help, quit): ");
            command = command.toLowerCase();
            mConsoleReader.clearScreen();
            printMenuHeader();
        } catch (IOException e) {
            println("Console reader error!");
            System.exit(-1);
        }


        if (command.startsWith("variants")) {
            for (String desc : BuiltInVariants.rulesDescriptions) {
                println(desc);
            }
        } else if (command.startsWith("options")) {
            mInMenu = false;
            mInOptions = true;

            printCurrentOptions();
        } else if (command.startsWith("play")) {
            String[] commandParts = command.split(" ");
            if (commandParts.length != 2) {
                //printMenuHeader();
                println("Wrong command format. Try: ");
                println("'play [variant-number]'");

                return;
            }
            try {
                int variantSelection = Integer.parseInt(commandParts[1]);

                if (variantSelection > BuiltInVariants.availableRules.size()) {
                    println("Unavailable variant. See: ");
                    println("'variants'");

                    return;
                }

                startGame(new Game(BuiltInVariants.availableRules.get(variantSelection - 1), this));

            } catch (NumberFormatException e) {
                println("Wrong command format. Try: ");
                println("'play [variant-number]'");
            }
        } else if (command.startsWith("help")) {
            println("Coming someday");
        } else if (command.startsWith("quit")) {
            mUiRunning = false;
        }
    }

    private void printColorTest() {
        String colorTest = "";
        colorTest += ANSI_BLUE + "+-" + ANSI_CYAN + "a" + ANSI_BLUE + "-+-" + ANSI_CYAN + "b" + ANSI_BLUE + "-+-" + ANSI_CYAN + "c" + ANSI_BLUE + "-+\n";
        colorTest += ANSI_CYAN + "1" + ANSI_BLUE + "   |   |   |\n";
        colorTest += ANSI_BLUE + "|   |" + ANSI_WHITE + "<+>" + ANSI_BLUE + "|   |\n";
        colorTest += ANSI_BLUE + "+---+---+---+" + ANSI_RESET + "\n";

        println(colorTest);
    }

    private void printCurrentOptions() {
        printPlayers();
        println("Color: " + (ANSI_RESET.equals("") ? "off" : "on"));
        println("AI search depth: " + mSearchDepth);
    }

    private void waitForOptionsInput() {
        String command = "";
        try {
            command = mConsoleReader.readLine("Command (players, color, searchdepth, back): ");
            command = command.toLowerCase();
            mConsoleReader.clearScreen();
            printMenuHeader();
        } catch (IOException e) {
            println("Console reader error!");
            System.exit(-1);
        }

        if (command.startsWith("players")) {
            String[] commandParts = command.split(" ");
            if (commandParts.length == 4) {
                int side = -1;
                if (commandParts[2].startsWith("attackers")) {
                    side = ATTACKING_SIDE;
                } else if (commandParts[2].startsWith("defenders")) {
                    side = DEFENDING_SIDE;
                } else {
                    println("Unknown side. Use 'attackers' or 'defenders'.");
                }

                if (commandParts[3].startsWith("ai")) {
                    mPlayers[side] = new LocalAi();
                    mPlayers[side].setCallback(mMoveCallback);
                } else if (commandParts[3].startsWith("human")) {
                    mPlayers[side] = new LocalHuman();
                    mPlayers[side].setCallback(mMoveCallback);
                } else if (commandParts[3].startsWith("network")) {
                    println("Network play not implemented. Falling back to local human.");
                    mPlayers[side] = new LocalHuman();
                    mPlayers[side].setCallback(mMoveCallback);
                } else {
                    println("Unknown player type. Use 'ai', 'human', or 'network'.");
                }
            } else {
                println("To set players, try:");
                println("'players set [attackers|defenders] [ai|human|network]");
            }
        } else if (command.startsWith("color")) {
            String[] commandParts = command.split(" ");
            if (commandParts.length == 2) {
                if (commandParts[1].equals("on")) {
                    enableColor();
                    printColorTest();
                } else if (commandParts[1].equals("off")) {
                    disableColor();
                    printColorTest();
                } else {
                    printColorTest();
                    println("Unknown parameter. Try:");
                    println("color [on|off]");
                }
            } else {
                printColorTest();
                println("Try:");
                println("color [on|off]");
            }
        } else if (command.startsWith("searchdepth")) {
            String[] commandParts = command.split(" ");
            if (commandParts.length == 2) {
                try {
                    int depth = Integer.parseInt(commandParts[1]);
                    if (depth > 3) {
                        println("Warning: search depths of more than 4 may be very slow for all games except Brandub.");
                    }
                    mSearchDepth = depth;
                } catch (NumberFormatException e) {

                }
            }
        } else if (command.startsWith("back")) {
            mInOptions = false;
            mInMenu = true;
        }

        if (!command.startsWith("back")) {
            printCurrentOptions();
        }
    }

    private void waitForNextMove() {
        if(mInGame) {
            mMoveStatus.set(WAITING_FOR_MOVE);
            mPlayers[mCurrentPlayer].getNextMove(this, mGame, mSearchDepth);
        }
        else {
            mMoveStatus.set(IDLE);
        }
    }

    public MoveRecord waitForHumanMoveInput() {
        return waitForInGameInput();
    }

    public MoveRecord waitForInGameInput() {
        String command = "";
        try {
            command = mConsoleReader.readLine("Command (move, info, show, history, help, quit): ");
            command = command.toLowerCase();
            mConsoleReader.clearScreen();
        } catch (IOException e) {
            println("Console reader error!");
            System.exit(-1);
        }


        // Handle info command
        if (command.startsWith("info")) {
            String[] commandParts = command.split(" ");
            if (commandParts.length != 2) {
                println("Wrong command format, try info [file+rank] [file+rank] (e.g. info a4)");
            } else {
                int boardSize = mGame.getGameRules().getBoard().getBoardDimension();
                String chessString = commandParts[1].toLowerCase();

                if (!Board.validateChessNotation(chessString, boardSize)) {
                    println("Incorrect space format: " + chessString);
                    return null;
                }

                int x = Board.getCoordMapFromChessNotation(chessString).get("x");
                int y = Board.getCoordMapFromChessNotation(chessString).get("y");

                if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) {
                    println(chessString + " out of bounds");
                } else {
                    char piece = mGame.getCurrentState().getPieceAt(x, y);
                    if (piece != Taflman.EMPTY) {
                        List<Coord> stops = Taflman.getAllowableDestinations(mGame.getCurrentState(), piece);
                        List<Coord> moves = Taflman.getAllowableMoves(mGame.getCurrentState(), piece);
                        List<Coord> captures = Taflman.getCapturingMoves(mGame.getCurrentState(), piece);

                        renderGameStateWithAllowableMoves(
                                mGame.getCurrentState(), mGame.getCurrentState().getSpaceAt(x, y),
                                stops, moves, captures);
                    } else {
                        println("No taflman at " + chessString);
                    }
                }
            }
        }

        // Handle show command
        if (command.startsWith("show")) {
            renderGameState(mGame.getCurrentState());
            printStatus();
        }

        // Handle history command
        if (command.startsWith("history")) {
            for (GameState state : mGame.getHistory()) {
                MoveRecord record = state.getExitingMove();
                println(record);
            }
        }

        // Handle quit command
        if (command.startsWith("quit")) {
            mInGame = false;
            mInMenu = true;

            mPlayers[0].stop();
            mPlayers[1].stop();

            printMenuHeader();
        }

        if (command.startsWith("move")) {
            String[] commandParts = command.split(" ");
            if (commandParts.length != 3) {
                println("Wrong command format, try move [file+rank] [file+rank] (e.g. move a4 a3)");
            } else {
                int boardSize = mGame.getCurrentState().getBoard().getBoardDimension();

                String fromString = commandParts[1].toLowerCase();
                String toString = commandParts[2].toLowerCase();

                if (!Board.validateChessNotation(fromString, boardSize) || !Board.validateChessNotation(toString, boardSize)) {
                    println("Incorrect space formats: $fromString $toString");
                    return null;
                }

                Map<String, Integer> fromCoords = Board.getCoordMapFromChessNotation(fromString);
                Map<String, Integer> toCoords = Board.getCoordMapFromChessNotation(toString);

                int fromX = fromCoords.get("x");
                int fromY = fromCoords.get("y");
                int toX = toCoords.get("x");
                int toY = toCoords.get("y");

                if (fromX < 0 || fromX >= boardSize || fromY < 0 || fromY >= boardSize) {
                    println("Piece out of bounds");
                    return null;
                }

                if (toX < 0 || toX >= boardSize || toY < 0 || toY >= boardSize) {
                    println("Destination out of bounds");
                    return null;
                }

                char piece = mGame.getCurrentState().getPieceAt(fromX, fromY);

                if (piece == Taflman.EMPTY) {
                    println("No piece at $fromX $fromY");
                    return null;
                }
                Coord destination = mGame.getCurrentState().getSpaceAt(toX, toY);

                MoveRecord record = new MoveRecord(Taflman.getCurrentSpace(mGame.getCurrentState(), piece), destination);
                return record;
            }
        }
        return null;
    }

    private void printStatus() {
        int lengthInPlies = mGame.getHistory().size();
        if (mGame.getCurrentState().getCurrentSide().isAttackingSide()) {
            println(lengthInPlies + " moves elapsed. Attackers to play.");
        } else {
            println(lengthInPlies + " moves elapsed. Defenders to play.");
        }
    }

    public static String getGameStateString(GameState state) {
        List<Coord> specialSpaces = new ArrayList<Coord>();
        specialSpaces.addAll(state.getBoard().getRules().getCenterSpaces());

        for (Coord corner : state.getBoard().getRules().getCornerSpaces()) {
            specialSpaces.add(corner);
        }

        String boardAsText = "";

        for (int i = 0; i < state.getBoard().getBoardDimension(); i++) {
            boardAsText += renderRow(i, state.getBoard(), specialSpaces, null, null, null, null);
        }

        return boardAsText;
    }

    public static void renderGameState(GameState state) {
        /*
        for(GameState oldState : state.mGame.getHistory()) {
            println("Old zobrist: " + oldState.mZobristHash);
        }
        */
        println("Zobrist: " + state.mZobristHash);
        println("OTN: " + state.getOTNPositionString());
        print(getGameStateString(state));
    }

    public static void renderGameStateWithAllowableMoves(GameState state, Coord highlight, List<Coord> allowableDestinations, List<Coord> allowableMoves, List<Coord> captureSpaces) {
        allowableMoves.removeAll(allowableDestinations);
        List<Coord> specialSpaces = new ArrayList<Coord>();
        specialSpaces.addAll(state.getBoard().getRules().getCenterSpaces());

        for (Coord corner : state.getBoard().getRules().getCornerSpaces()) {
            specialSpaces.add(corner);
        }

        String boardAsText = "";

        for (int i = 0; i < state.getBoard().getBoardDimension(); i++) {
            boardAsText += renderRow(i, state.getBoard(), specialSpaces, highlight, allowableDestinations, allowableMoves, captureSpaces);
        }

        print(boardAsText);
    }

    public static void renderGameStateWithReachableSpaces(GameState state, Coord highlight, Set<Coord> reachableSpaces) {
        String boardAsText = "";
        List<Coord> specialSpaces = new ArrayList<Coord>();
        specialSpaces.addAll(state.getBoard().getRules().getCenterSpaces());

        for (Coord corner : state.getBoard().getRules().getCornerSpaces()) {
            specialSpaces.add(corner);
        }

        List<Coord> reachableSpacesList = new ArrayList<Coord>();
        reachableSpacesList.addAll(reachableSpaces);

        for (int i = 0; i < state.getBoard().getBoardDimension(); i++) {
            boardAsText += renderRow(i, state.getBoard(), specialSpaces, highlight, reachableSpacesList, null, null);
        }

        print(boardAsText);
    }

    public static void renderBoard(Board board) {
        String boardAsText = "";
        List<Coord> specialSpaces = new ArrayList<Coord>();
        specialSpaces.addAll(board.getRules().getCenterSpaces());

        for (Coord corner : board.getRules().getCornerSpaces()) {
            specialSpaces.add(corner);
        }

        for (int i = 0; i < board.getBoardDimension(); i++) {
            boardAsText += renderRow(i, board, specialSpaces, null, null, null, null);
        }

        print(boardAsText);
    }

    private static String renderRow(int rank, Board board, List<Coord> special, Coord highlight, List<Coord> allowableDestinations, List<Coord> allowableMoves, List<Coord> captureSpaces) {
        String rowString = "";
        String allowableSpaceFill = "-";
        String allowableStopFill = ".";
        String captureSpaceFill = "/";
        String rankString = Board.getChessNotation(Coord.get(0, rank)).get("rank");

        if (board.getBoardDimension() > 9) SPACER = " ";

        // If index == 0, we're the first row, so we need to draw our own top.
        if (rank == 0) {
            rowString += SPACER + ANSI_BLUE;
            for (int i = 0; i < board.getBoardDimension(); i++) {
                String fileString = Board.getChessNotation(Coord.get(i, rank)).get("file");
                /*
				if(i + 1 >= 10) {
					rowString += "+-" + ANSI_CYAN + "${i + 1}" + ANSI_BLUE;
				}*/
                //else {
                rowString += "+-" + ANSI_CYAN + fileString + ANSI_BLUE + "-";
                //}
            }
            rowString += "+\n";
            rowString += ANSI_RESET;
        }

        // Draw the top row inside the space.
        for (int i = 0; i < board.getBoardDimension(); i++) {
            if (i == 0) {
                if (rank < 9) rowString += SPACER;
                rowString += ANSI_CYAN + rankString + ANSI_BLUE;
            }

            Coord space = Coord.get(i, rank);
            char occupier = board.getOccupier(space);

            if (occupier != Taflman.EMPTY) {
                if (special.contains(space)) {
                    if (highlight != null
                            && highlight.x == i && highlight.y == rank) {
                        String color = (Taflman.getSide(occupier).isAttackingSide() ? ANSI_RED : ANSI_WHITE);
                        rowString += "*" + color + "*" + ANSI_BLUE + "*|";
                    } else {
                        rowString += ANSI_BLUE + "***|";
                    }
                } else {
                    if (highlight != null
                            && highlight.x == i && highlight.y == rank) {
                        String color = (Taflman.getSide(occupier).isAttackingSide() ? ANSI_RED : ANSI_WHITE);
                        rowString += color + " * " + ANSI_BLUE + "|";
                    } else {
                        rowString += ANSI_BLUE + "   |";
                    }
                }
            } else if (captureSpaces != null && captureSpaces.contains(space)) {
                rowString += ANSI_YELLOW;
                rowString += captureSpaceFill;
                rowString += captureSpaceFill;
                rowString += captureSpaceFill;
                rowString += ANSI_BLUE;
                rowString += "|";
            } else if (allowableMoves != null && allowableMoves.contains(space)) {
                rowString += ANSI_GREEN;
                rowString += allowableSpaceFill;
                rowString += allowableSpaceFill;
                rowString += allowableSpaceFill;
                rowString += ANSI_BLUE;
                rowString += "|";
            } else if (allowableDestinations != null && allowableDestinations.contains(space)) {
                rowString += ANSI_GREEN;
                rowString += allowableStopFill;
                rowString += allowableStopFill;
                rowString += allowableStopFill;
                rowString += ANSI_BLUE;
                rowString += "|";
            } else {
                if (special.contains(space)) {
                    rowString += ANSI_BLUE;
                    rowString += "***|";
                } else {
                    rowString += ANSI_BLUE;
                    rowString += "   |";
                }
            }
        }
        rowString += "\n";
        rowString += ANSI_RESET;

        // Draw the middle row and a piece, if present
        for (int i = 0; i < board.getBoardDimension(); i++) {
            if (i == 0) {
                rowString += SPACER;
                rowString += ANSI_BLUE;
                rowString += "|";
            }

            Coord space = Coord.get(i, rank);
            char occupier = board.getOccupier(space);
            if (occupier != Taflman.EMPTY) {
                String color = (Taflman.getPackedSide(occupier) == Taflman.SIDE_ATTACKERS ? ANSI_RED : ANSI_WHITE);
                rowString += color + Taflman.getStringSymbol(occupier);
                rowString += ANSI_BLUE;
                rowString += "|";
            } else if (captureSpaces != null && captureSpaces.contains(space)) {
                rowString += ANSI_YELLOW;
                rowString += captureSpaceFill;
                rowString += captureSpaceFill;
                rowString += captureSpaceFill;
                rowString += ANSI_BLUE;
                rowString += "|";
            } else if (allowableMoves != null && allowableMoves.contains(space)) {
                rowString += ANSI_GREEN;
                rowString += allowableSpaceFill;
                rowString += allowableSpaceFill;
                rowString += allowableSpaceFill;
                rowString += ANSI_BLUE;
                rowString += "|";
            } else if (allowableDestinations != null && allowableDestinations.contains(space)) {
                rowString += ANSI_GREEN;
                rowString += allowableStopFill;
                rowString += allowableStopFill;
                rowString += allowableStopFill;
                rowString += ANSI_BLUE;
                rowString += "|";
            } else {
                if (special.contains(space)) {
                    rowString += ANSI_BLUE;
                    rowString += "***|";
                } else {
                    rowString += ANSI_BLUE;
                    rowString += "   |";
                }
            }
        }
        rowString += "\n";
        rowString += ANSI_RESET;
		
		/*
		// Draw the bottom row of the space, same as the top row of the space
		for(int i = 0; i < row.size(); i++) {
			if(i == 0) {
				rowString += "|";
			}
			
			Space space = row.get(i);
			if(space.occupier != null) {
				rowString += "   |";
			}
			else if(captureSpaces?.contains(space)) {
				rowString += captureSpaceFill;
				rowString += captureSpaceFill;
				rowString += captureSpaceFill;
				rowString += "|";
			}
			else if(allowableSpaces?.contains(space)) {
				rowString += allowableSpaceFill;
				rowString += allowableSpaceFill;
				rowString += allowableSpaceFill;
				rowString += "|";
			}
			else {
				rowString += "   |";
			}
		}
		rowString += "\n";
		*/

        rowString += SPACER;
        rowString += ANSI_BLUE;
        rowString += "+";
        for (int i = 0; i < board.getBoardDimension(); i++) {
            rowString += "---+";
        }
        rowString += "\n";
        rowString += ANSI_RESET;

        return rowString;
    }
}
