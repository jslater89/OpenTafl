package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.command.Command;
import com.manywords.softworks.tafl.ui.command.CommandEngine;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.command.HumanCommandParser;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingMessageDialog;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.window.*;
import com.manywords.softworks.tafl.ui.player.Player;
import com.manywords.softworks.tafl.ui.player.UiWorkerThread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jay on 4/2/16.
 */
public class GameScreen extends UiScreen implements UiCallback {
    private String mTitle;
    private BoardWindow mBoardWindow;
    private StatusWindow mStatusWindow;
    private CommandWindow mCommandWindow;

    private UiWorkerThread mCommandEngineThread;
    private Game mGame;
    private CommandEngine mCommandEngine;

    private ReplayGame mReplay;

    private boolean mInGame;
    private boolean mInReplay;
    private boolean mPostGame;

    public GameScreen(Game g, String title) {
        mGame = g;
        mTitle = title;
    }

    public GameScreen(ReplayGame g, String title) {
        mReplay = g;
        mTitle = title;
    }

    @Override
    public void setActive(AdvancedTerminal t, WindowBasedTextGUI gui) {
        super.setActive(t, gui);
        mTerminalCallback = new GameScreenTerminalCallback();

        if(mGame != null) {
            mTerminalCallback.onEnteringGameScreen(mGame, mTitle);
        }
        else if(mReplay != null) {
            mTerminalCallback.onEnteringGameScreen(mReplay, mTitle);
        }
        else {
            throw new IllegalStateException("No game for GameScreen!");
        }
    }

    @Override
    public void onResized(Terminal terminal, TerminalSize terminalSize) {
        layoutGameWindows(terminalSize);
    }

    private void addBoardWindows() {
        mGui.addWindow(mBoardWindow);
        mGui.addWindow(mStatusWindow);
        mGui.addWindow(mCommandWindow);

        layoutGameWindows(mGui.getScreen().getTerminalSize());

        mGui.waitForWindowToClose(mBoardWindow);
    }

    private void layoutGameWindows(TerminalSize size) {
        if(mBoardWindow == null || mStatusWindow == null || mCommandWindow == null) return;

        mBoardWindow.setHints(TerminalThemeConstants.BOARD_WINDOW);
        mStatusWindow.setHints(TerminalThemeConstants.STATUS_WINDOW);
        mCommandWindow.setHints(TerminalThemeConstants.COMMAND_WINDOW);

        TerminalSize screenSize = size;
        TerminalSize boardWindowSize = mBoardWindow.getPreferredSize();

        mBoardWindow.setPosition(new TerminalPosition(0, 0));
        int leftoverRight = screenSize.getColumns() - boardWindowSize.getColumns();
        int leftoverBottom = screenSize.getRows() - boardWindowSize.getRows();
        int boardWindowHeight = boardWindowSize.getRows();
        int boardWindowWidth = boardWindowSize.getColumns();
        TerminalPosition statusPosition, commandPosition;
        TerminalSize statusSize, commandSize;

        if(leftoverRight < 20) {
            // status and command stacked beneath the board window
            statusPosition = new TerminalPosition(0, boardWindowHeight + 2);
            statusSize = new TerminalSize(boardWindowWidth, leftoverBottom - 6);

            leftoverBottom -= statusSize.getRows();
            leftoverBottom -= 4;

            commandPosition = new TerminalPosition(0, boardWindowHeight + 2 + statusSize.getRows() + 2);
            commandSize = new TerminalSize(boardWindowWidth, leftoverBottom);
        }
        else {
            // command beneath the board window, status to the right
            statusPosition = new TerminalPosition(boardWindowWidth + 2, 0);
            statusSize = new TerminalSize(leftoverRight - 4, boardWindowHeight + 2 + 4);

            commandPosition = new TerminalPosition(0, boardWindowHeight + 2);
            commandSize = new TerminalSize(boardWindowWidth, 4);
        }

        mStatusWindow.setPosition(statusPosition);
        mStatusWindow.setSize(statusSize);

        mCommandWindow.setPosition(commandPosition);
        mCommandWindow.setSize(commandSize);
    }

    private void leaveGameUi() {
        mGui.removeWindow(mBoardWindow);
        mGui.removeWindow(mStatusWindow);
        mGui.removeWindow(mCommandWindow);

        mBoardWindow = null;
        mStatusWindow = null;
        mCommandWindow = null;

        mCommandEngine.shutdown();
        mCommandEngineThread.cancel();

        mTerminal.changeActiveScreen(new MainMenuScreen());
    }

    @Override
    public void gameStarting() {
        statusText("Game starting");
        mInGame = true;
    }

    @Override
    public void modeChanging(UiCallback.Mode mode, Object gameObject) {
        if(mode == UiCallback.Mode.GAME) mTerminalCallback.onEnteringGame((Game) gameObject);
        else if(mode == UiCallback.Mode.REPLAY) mTerminalCallback.onEnteringReplay((ReplayGame) gameObject);
    }

    @Override
    public void awaitingMove(Player currentPlayer, boolean isAttackingSide) {
        statusText("Awaiting" + (isAttackingSide ? " attacker " : " defender ") + "move");
    }

    @Override
    public void timeUpdate(Side side) {
        mStatusWindow.handleTimeUpdate(side, mGame.getClock().getClockEntry(true).toTimeSpec(), mGame.getClock().getClockEntry(false).toTimeSpec());
    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {
        if(result.result == CommandResult.FAIL) {
            statusText(result.message);
        }
        else {
            statusText("Last move: " + move);
            int repeats = mGame.getCurrentState().countPositionOccurrences();
            if(repeats > 1) {
                statusText("This position has repeated " + repeats + " times!");
            }
            mBoardWindow.rerenderBoard();
        }
    }

    @Override
    public void statusText(String text) {
        mStatusWindow.addStatus(text);
    }

    @Override
    public void modalStatus(String title, String text) {
        mGui.getGUIThread().invokeLater(new Runnable() {
            @Override
            public void run() {
                new ScrollingMessageDialog(title, text, MessageDialogButton.Close).showDialog(mGui);
            }
        });
    }

    @Override
    public void gameStateAdvanced() {
        mBoardWindow.rerenderBoard();
    }

    @Override
    public void victoryForSide(Side side) {
        mBoardWindow.rerenderBoard();

        // Notify the player if this is a victory on move repetition
        int repeats = mGame.getCurrentState().countPositionOccurrences();
        repeats++;
        if(repeats > 2) {
            statusText("This position has repeated " + repeats + " times!");
        }

        if(side == null) {
            statusText("Draw!");
        }
        else if(side.isAttackingSide()) {
            statusText("Attackers win!");
        }
        else {
            statusText("Defenders win!");
        }
    }

    @Override
    public void gameFinished() {
        mInGame = false;
        mPostGame = true;

        if(mSelfplayWindow != null) {
            // Run this stuff on the UI thread.
            mGui.getGUIThread().invokeLater(() -> {
                System.out.println("Leaving game, entering selfplay window");
                mSelfplayWindow.notifyGameFinished(mGame);

                System.out.println("Removing windows");
                mBoardWindow.close();
                System.out.println("Removed board");
                mStatusWindow.close();
                System.out.println("Removed status");
                mCommandWindow.close();
                System.out.println("Removed command");
                System.out.println("Removed windows");
            });

            System.out.println("Started selfplay window thread");
        }
    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
    }

    @Override
    public boolean inGame() {
        return mInGame;
    }

    private class GameScreenTerminalCallback extends DefaultTerminalCallback {
        @Override
        public void changeActiveScreen(UiScreen screen) {
            mTerminal.changeActiveScreen(screen);
        }

        @Override
        public void onEnteringGameScreen(Game g, String title) {
            // Set up a game thread
            blockUntilCommandEngineReady(g);
            System.out.println("Command engine ready");

            if(mBoardWindow == null || mStatusWindow == null || mCommandWindow == null) {
                createWindows(g, g.getRules().getName());
                mCommandEngine.enterGame(g);

                // This is our UI thread (blocking call)
                addBoardWindows();
            }
            else {
                mCommandEngine.enterGame(g);
            }
        }

        @Override
        public void onEnteringGameScreen(ReplayGame rg, String title) {
            // Set up a game thread
            blockUntilCommandEngineReady(rg.getGame());

            if(mBoardWindow == null || mStatusWindow == null || mCommandWindow == null) {
                createWindows(rg.getGame(), rg.getGame().getRules().getName());
                mCommandEngine.enterReplay(rg);

                // This is our UI thread (blocking call)
                addBoardWindows();
            }
            else {
                mCommandEngine.enterReplay(rg);
            }
        }

        private void createWindows(Game g, String title) {
            TerminalBoardImage.init(g.getRules().getBoard().getBoardDimension());
            BoardWindow bw = new BoardWindow(title, g, this);
            CommandWindow cw = new CommandWindow(this);
            StatusWindow sw = new StatusWindow(this);

            mBoardWindow = bw;
            mStatusWindow = sw;
            mCommandWindow = cw;
        }

        private void blockUntilCommandEngineReady(Game g) {
            // Set up a game thread
            if(mCommandEngine == null) {
                System.out.println("Starting command engine thread");
                startCommandEngineThread(g);
            }

            while(mCommandEngine == null) {
                try {
                    System.out.println("Waiting...");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Command engine ready");
        }

        private void startCommandEngineThread(Game g) {

            mCommandEngineThread = new UiWorkerThread(new UiWorkerThread.UiWorkerRunnable() {
                private boolean mRunning = true;
                @Override
                public void cancel() {
                    mCommandEngine = null;
                    mRunning = false;
                }

                @Override
                public void run() {
                    mCommandEngine = new CommandEngine(g, GameScreen.this, TerminalSettings.getNewPlayer(TerminalSettings.attackers), TerminalSettings.getNewPlayer(TerminalSettings.defenders));
                }
            });
            mCommandEngineThread.start();
        }

        @Override
        public void onEnteringReplay(ReplayGame rg) {
            mInReplay = true;
            mReplay = rg;

            tryTimeUpdate();
            tryStartingComments(rg);

            mBoardWindow.enterReplay(rg);
            mBoardWindow.rerenderBoard();
        }

        @Override
        public void onEnteringGame(Game g) {
            mInReplay = false;
            mBoardWindow.setGame(g);
            mBoardWindow.leaveReplay();
            mGame = g;

            mCommandEngine.startGame();
        }

        @Override
        public void handleInGameCommand(String command) {
            /*
            if(command.startsWith("dump")) {
                if(mInGame) {
                    System.out.println(GameSerializer.getGameRecord(mGame, true));
                }
                else {
                    System.out.println(GameSerializer.getGameRecord(mReplay.getGame(), true));
                }
                return;
            }
            */

            Command c = HumanCommandParser.parseCommand(mCommandEngine, command);
            CommandResult r = mCommandEngine.executeCommand(c);

            if(r.result != CommandResult.SUCCESS) {
                statusText(r.message);
            }
            else if (r.type == CommandResult.Type.MOVE) {
                if(r.extra != null) {
                    if(mCommandEngine.getCurrentPlayer().getType() == Player.Type.HUMAN) {
                        mCommandEngine.getCurrentPlayer().onMoveDecided((MoveRecord) r.extra);
                    }
                    else {
                        statusText("Not your turn!");
                    }
                }
                else {
                    throw new IllegalStateException("Received successful move command with no move record");
                }
            }
            else if (r.type == CommandResult.Type.INFO) {
                HumanCommandParser.Info infoCommand = (HumanCommandParser.Info) c;
                mBoardWindow.rerenderBoard(infoCommand.location, infoCommand.stops, infoCommand.moves, infoCommand.captures);
            }
            else if (r.type == CommandResult.Type.SHOW) {
                mBoardWindow.rerenderBoard();
            }
            else if (r.type == CommandResult.Type.HISTORY) {
                String gameRecord = (String) r.extra;
                statusText(gameRecord);
            }
            else if (r.type == CommandResult.Type.HELP) {
                String helpString = HumanCommandParser.getHelpString(getCurrentCommands());

                ScrollingMessageDialog dialog = new ScrollingMessageDialog("OpenTafl Help", helpString, MessageDialogButton.Close);
                dialog.setSize(new TerminalSize(Math.min(70, mGui.getScreen().getTerminalSize().getColumns() - 2), 30));
                dialog.showDialog(mGui);
            }
            else if (r.type == CommandResult.Type.SAVE) {
                String title;
                Game game;
                if(mInGame) {
                    title = "Save game";
                    game = mGame;
                }
                else {
                    title = "Save replay";
                    game = mReplay.getGame();
                }

                File saveFile = TerminalUtils.showFileChooserDialog(mGui, title, "Save", new File("saved-games"));

                if(saveFile == null) {
                    return;
                }
                else if(saveFile.exists()) {
                    MessageDialogButton result = MessageDialog.showMessageDialog(mGui, "Overwrite file?", "File with name " + saveFile.getName() +  " already exists.\nOverwrite?", MessageDialogButton.Yes, MessageDialogButton.No);
                    if(result.equals(MessageDialogButton.No)) return;
                }

                boolean success = GameSerializer.writeGameToFile(game, saveFile, true);
                if(!success) {
                    MessageDialog.showMessageDialog(mGui, "Unable to save", "Unable to write savegame file.");
                }
            }
            else if (r.type == CommandResult.Type.QUIT) {
                if(mSelfplayWindow != null) {
                    SelfplayWindow w = mSelfplayWindow;
                    mSelfplayWindow = null;
                    mCommandEngine.finishGame();
                    mGui.removeWindow(mBoardWindow);
                    mGui.removeWindow(mCommandWindow);
                    mGui.removeWindow(mStatusWindow);
                    w.getRunner().finishTournament();
                    return;
                }

                if(mInGame) {
                    // Leave the game thread running for history.
                    statusText("Finished game. Enter 'quit' again to return to menu.");
                    mCommandEngine.finishGame();
                }
                else {
                    leaveGameUi();
                }
            }
            else if(r.type == CommandResult.Type.ANALYZE) {
                statusText("AI analysis beginning.");
            }
            else if(r.type == CommandResult.Type.REPLAY_ENTER) {
                statusText("Entered replay mode.");
                mBoardWindow.rerenderBoard();
            }
            else if(r.type == CommandResult.Type.REPLAY_PLAY_HERE) {
                statusText("Starting new game from this position...");
                mBoardWindow.rerenderBoard();
            }
            else if(r.type == CommandResult.Type.REPLAY_RETURN) {
                statusText("Returning to game.");
                mBoardWindow.rerenderBoard();
            }
            else if(r.type == CommandResult.Type.REPLAY_NEXT) {
                mBoardWindow.rerenderBoard();
                tryTimeUpdate();
                updateComments();
            }
            else if(r.type == CommandResult.Type.REPLAY_PREVIOUS) {
                mBoardWindow.rerenderBoard();
                tryTimeUpdate();
                updateComments();
            }
            else if(r.type == CommandResult.Type.REPLAY_JUMP) {
                mBoardWindow.rerenderBoard();
                tryTimeUpdate();
                updateComments();
            }
        }

        private void tryTimeUpdate() {
            if(!mInGame) {
                Side currentSide = mReplay.getCurrentState().getCurrentSide();
                GameClock.TimeSpec attackerClock = mReplay.getTimeGuess(true);
                GameClock.TimeSpec defenderClock = mReplay.getTimeGuess(false);
                mStatusWindow.handleTimeUpdate(currentSide, attackerClock, defenderClock);
            }
        }

        private void tryStartingComments(ReplayGame rg) {
            Map<String, String> tags = rg.getGame().getTagMap();
            if(tags != null) {
                if(tags.containsKey(Game.Tag.COMPILER)) {
                    statusText("OpenTafl game file compiled by: " + tags.get(Game.Tag.COMPILER));
                }
                if(tags.containsKey(Game.Tag.ANNOTATOR)) {
                    statusText("Annotations by: " + tags.get(Game.Tag.ANNOTATOR));
                }
                if(tags.containsKey(Game.Tag.SITE)) {
                    statusText("Game location: " + tags.get(Game.Tag.SITE));
                }
                if(tags.containsKey(Game.Tag.EVENT)) {
                    statusText("Game played as part of: " + tags.get(Game.Tag.EVENT));
                }
                if(tags.containsKey(Game.Tag.DATE)) {
                    statusText("Game played on: " + tags.get(Game.Tag.DATE));
                }
                if(tags.containsKey(Game.Tag.DEFENDERS)) {
                    statusText("Defenders played by: " + tags.get(Game.Tag.DEFENDERS));
                }
                if(tags.containsKey(Game.Tag.ATTACKERS)) {
                    statusText("Attackers played by: " + tags.get(Game.Tag.ATTACKERS));
                }
                if(tags.containsKey(Game.Tag.VARIANT)) {
                    statusText("Rules variant: " + tags.get(Game.Tag.VARIANT));
                }
                if(tags.containsKey(Game.Tag.TIME_CONTROL)) {
                    statusText("Initial clock setting: " + tags.get(Game.Tag.TIME_CONTROL));
                }
                if(tags.containsKey(Game.Tag.START_COMMENT)) {
                    statusText(tags.get(Game.Tag.START_COMMENT));
                }
            }

            statusText("--- Start of replay ---");
        }

        private void updateComments() {
            if(!mInGame) {
                DetailedMoveRecord m = (DetailedMoveRecord) mReplay.getCurrentState().getEnteringMove();
                if(m == null) {
                    tryStartingComments(mReplay);
                }
                else {
                    statusText("Last move: " + m);
                    if(!m.getComment().trim().isEmpty()) {
                        statusText(m.getComment());
                    }
                }
            }
        }

        private List<CommandResult.Type> getCurrentCommands() {
            List<CommandResult.Type> types = new ArrayList<>();

            if(mInGame) {
                types.add(CommandResult.Type.MOVE);
            }

            if(mInReplay) {
                types.add(CommandResult.Type.REPLAY_NEXT);
                types.add(CommandResult.Type.REPLAY_PREVIOUS);
                types.add(CommandResult.Type.REPLAY_JUMP);
                types.add(CommandResult.Type.REPLAY_PLAY_HERE);
                types.add(CommandResult.Type.REPLAY_RETURN);
            }

            if(mInGame || mPostGame) {
                types.add(CommandResult.Type.REPLAY_ENTER);
            }

            if(mInGame || mPostGame || mInReplay) {
                types.add(CommandResult.Type.INFO);
                types.add(CommandResult.Type.SHOW);
                types.add(CommandResult.Type.ANALYZE);
                types.add(CommandResult.Type.HISTORY);
                types.add(CommandResult.Type.HELP);
                types.add(CommandResult.Type.SAVE);
                types.add(CommandResult.Type.QUIT);
            }

            return types;
        }

        @Override
        public void handleKeyStroke(KeyStroke key) {
            if(key.getKeyType() == KeyType.PageUp || key.getKeyType() == KeyType.PageDown) {
                mStatusWindow.handleInput(key);
            }
        }

        @Override
        public UiCallback getUiCallback() {
            return GameScreen.this;
        }

        @Override
        public void setSelfplayWindow(Window w) {
            mTerminal.setSelfplayWindow(w);
        }
    }

}