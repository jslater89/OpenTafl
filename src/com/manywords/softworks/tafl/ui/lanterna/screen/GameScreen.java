package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.command.Command;
import com.manywords.softworks.tafl.command.CommandEngine;
import com.manywords.softworks.tafl.command.CommandParser;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.*;
import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.ai.AiWorkspace;
import com.manywords.softworks.tafl.engine.ai.GameTreeState;
import com.manywords.softworks.tafl.engine.ai.evaluators.FishyEvaluator;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.network.client.ClientServerConnection.ClientServerCallback;
import com.manywords.softworks.tafl.network.packet.ClientInformation;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.ingame.VictoryPacket;
import com.manywords.softworks.tafl.network.server.GameRole;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.Ansi;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingMessageDialog;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.window.ingame.*;
import com.manywords.softworks.tafl.ui.lanterna.window.mainmenu.LoadNotationDialog;
import com.manywords.softworks.tafl.ui.lanterna.window.selfplay.SelfplayWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jay on 4/2/16.
 */
public class GameScreen extends LogicalScreen implements UiCallback {
    private String mTitle;
    private BoardWindow mBoardWindow;
    private StatusWindow mStatusWindow;
    private CommandWindow mCommandWindow;

    private UiWorkerThread mCommandEngineThread;
    private Game mGame;
    private CommandEngine mCommandEngine;
    private ClientServerConnection mServerConnection;
    private ClientServerCallback mServerCallback = new ServerCallback();

    private ReplayGame mReplay;
    // Used for spectators to catch up to the current state.
    private List<DetailedMoveRecord> mPregameHistory;
    private List<TimeSpec> mPregameTimeSpecs;

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

    public void setServerConnection(ClientServerConnection c) {
        mServerConnection = c;
    }

    @Override
    public void setActive(AdvancedTerminal t, WindowBasedTextGUI gui) {
        super.setActive(t, gui);
        mTerminalCallback = new GameScreenTerminalCallback();

        // Set up network game stuff, if necessary
        if(mServerConnection != null) {
            mServerConnection.setCallback(mServerCallback);
        }

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

    public void setHistory(List<DetailedMoveRecord> history) {
        mPregameHistory = history;
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
        mStatusWindow.setHints(TerminalThemeConstants.MANUAL_LAYOUT);
        mCommandWindow.setHints(TerminalThemeConstants.MANUAL_LAYOUT);

        TerminalSize screenSize = size;
        TerminalSize boardWindowSize = mBoardWindow.getPreferredSize();

        mBoardWindow.setPosition(new TerminalPosition(0, 0));
        int leftoverRight = screenSize.getColumns() - boardWindowSize.getColumns();
        TerminalPosition statusPosition, commandPosition;
        TerminalSize statusSize, commandSize;

        int boardWindowWidth = boardWindowSize.getColumns();
        commandSize = new TerminalSize(boardWindowWidth, 4);

        int boardWindowHeight = Math.max(boardWindowSize.getRows(), screenSize.getRows() - commandSize.getRows() - 4);

        if(leftoverRight < 20) {
            boardWindowHeight = boardWindowSize.getRows();

            int leftoverBottom = screenSize.getRows() - 2 - boardWindowHeight - 2;

            // status and command stacked beneath the board window
            statusPosition = new TerminalPosition(0, boardWindowHeight + 2);
            statusSize = new TerminalSize(boardWindowWidth, leftoverBottom - 6);
            
            commandPosition = new TerminalPosition(0, boardWindowHeight + 2 + statusSize.getRows() + 2);
        }
        else {
            // command beneath the board window, status to the right
            statusPosition = new TerminalPosition(boardWindowWidth + 2, 0);
            statusSize = new TerminalSize(leftoverRight - 4, screenSize.getRows() - 2);

            commandPosition = new TerminalPosition(0, boardWindowHeight + 2);
        }

        mBoardWindow.setSize(new TerminalSize(boardWindowWidth, boardWindowHeight));

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

        mCommandEngine.stopPlayers();
        mCommandEngine.shutdown();
        mCommandEngineThread.cancel();

        if(mServerConnection != null) {
            mServerConnection.sendLeaveGameMessage();
            mTerminal.changeActiveScreen(new ServerLobbyScreen(mServerConnection));
        }
        else {
            mTerminal.changeActiveScreen(new MainMenuScreen());
        }
    }

    @Override
    public void gameStarting() {
        statusText("Game starting");
        mInGame = true;

        if(mSelfplayWindow != null) {

            mStatusWindow.setTitle("Information " + mSelfplayWindow.getRunner().getTitle());

        }
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
    public void timeUpdate(boolean currentSideAttackers) {
        mStatusWindow.handleTimeUpdate(currentSideAttackers, mGame.getClock().getClockEntry(true).toTimeSpec(), mGame.getClock().getClockEntry(false).toTimeSpec());
    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {
        if(result.result == CommandResult.FAIL) {
            statusText(result.message);
        }
        else {
            statusText(Ansi.UNDERLINE + "Last move" + Ansi.UNDERLINE_OFF + ": " + move);
            int repeats = mGame.getCurrentState().countPositionOccurrences();
            if(repeats > 1) {
                statusText("This position has repeated " + repeats + " times!");
            }
            mBoardWindow.rerenderBoard();
        }
    }

    @Override
    public void statusText(String text) {
        if(mStatusWindow != null) mStatusWindow.addStatus(text);
    }

    @Override
    public void modalStatus(String title, String text) {
        mGui.getGUIThread().invokeLater(() -> new ScrollingMessageDialog(title, text, MessageDialogButton.Close).showDialog(mGui));
    }

    @Override
    public void gameStateAdvanced() {
        mBoardWindow.rerenderBoard();
    }

    @Override
    public void victoryForSide(Side side) {
        if(!mCommandEngine.isInGame() && !mInReplay) return;

        mBoardWindow.rerenderBoard();

        if(!mInReplay) {
            // Notify the player if this is a victory on move repetition
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
                OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Leaving game, entering selfplay window");
                mSelfplayWindow.notifyGameFinished(mGame);

                // Shut down the players so they don't clutter us
                mCommandEngine.shutdown();

                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Removing windows");
                mBoardWindow.close();
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Removed board");
                mStatusWindow.close();
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Removed status");
                mCommandWindow.close();
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Removed command");
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Removed windows");
            });

            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Started selfplay window thread");
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
        public void changeActiveScreen(LogicalScreen screen) {
            mTerminal.changeActiveScreen(screen);
        }

        @Override
        public void onEnteringGameScreen(Game g, String title) {
            // Set up a game thread
            blockUntilCommandEngineReady(g);

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
            if(mCommandEngine != null) {
                mCommandEngine.finishGameQuietly();
                mCommandEngine = null;
            }

            OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Starting command engine thread");
            startCommandEngineThread(g);

            while(mCommandEngine == null) {
                try {
                    OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Waiting...");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    OpenTafl.logStackTrace(OpenTafl.LogLevel.CHATTY, e);
                }
            }

            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Command engine ready");
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
                    if(mServerConnection != null) {
                        NetworkClientPlayer networkPlayer = mServerConnection.getNetworkPlayer();
                        LocalHuman localPlayer = (LocalHuman) Player.getNewPlayer(Player.Type.HUMAN);

                        Player attacker, defender;

                        OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Network player role: " + networkPlayer.getGameRole());

                        // Wait for the game joining to finish.
                        while(networkPlayer.getGameRole() == GameRole.OUT_OF_GAME) {
                            try {
                                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Waiting for other player role");
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                // doesn't matter
                            }
                        }

                        if(networkPlayer.getGameRole() == GameRole.ATTACKER) {
                            attacker = networkPlayer;
                            defender = localPlayer;
                        }
                        else if(networkPlayer.getGameRole() == GameRole.DEFENDER) {
                            attacker = localPlayer;
                            defender = networkPlayer;
                        }
                        else { /*(networkPlayer.getGameRole() == GameRole.KIBBITZER)*/
                            attacker = new SpectatorPlayer(mServerConnection);
                            defender = new SpectatorPlayer(mServerConnection);

                            // Doesn't matter which one
                            mServerConnection.setNetworkPlayer((SpectatorPlayer) attacker);
                        }

                        if(mServerConnection.hasHistory()) {
                            mPregameHistory = mServerConnection.consumeHistory();
                        }

                        if(mServerConnection.getLastClockSetting() != null) {
                            mServerConnection.sendClockUpdateRequest();
                        }

                        if(mPregameHistory != null) {
                            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Game screen consuming history");
                            for(DetailedMoveRecord m : mPregameHistory) {
                                g.getCurrentState().makeMove(m);


                            }
                        }

                        mCommandEngine = new CommandEngine(g, GameScreen.this, attacker, defender);
                    }
                    else {
                        mCommandEngine = new CommandEngine(g, GameScreen.this, TerminalSettings.getNewPlayer(TerminalSettings.attackers), TerminalSettings.getNewPlayer(TerminalSettings.defenders));
                    }
                }
            });
            mCommandEngineThread.start();
        }

        @Override
        public void onEnteringReplay(ReplayGame rg) {
            mInReplay = true;
            mReplay = rg;

            tryTimeUpdate();
            updateComments();

            mBoardWindow.enterReplay(rg);
            mBoardWindow.rerenderBoard();
        }

        @Override
        public void onEnteringGame(Game g) {
            mInReplay = false;
            mBoardWindow.setGame(g);
            mBoardWindow.leaveReplay();
            mBoardWindow.rerenderBoard();
            mGame = g;

            mCommandEngine.startGame();
        }

        @Override
        public void handleInGameCommand(String command) {
            if(command.startsWith("dumptree")) {
                AiWorkspace w = GameTreeState.workspace;
                if(w != null) {
                    w.getTreeRoot().printTree("");
                }

                return;
            }
            else if(command.startsWith("dumphistory")) {
                if(mReplay != null) {
                    mReplay.dumpHistory();
                }

                return;
            }
            else if(command.startsWith("dumplasteval")) {
                Player lastPlayer = (mCommandEngine.getCurrentPlayer().isAttackingSide() ? mCommandEngine.getDefendingPlayer() : mCommandEngine.getAttackingPlayer());
                if(lastPlayer instanceof ExternalEnginePlayer) {
                    ExternalEnginePlayer lastExternalPlayer = (ExternalEnginePlayer) lastPlayer;

                    lastExternalPlayer.getExternalEngineHost().dumpEvaluation(0);
                }
                else {
                    OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Last player not external engine");
                }
                return;
            }
            else if(command.startsWith("dumpcureval")) {
                FishyEvaluator.debug = true;
                AiWorkspace.evaluator.initialize(mGame.getRules());
                AiWorkspace.evaluator.evaluate(mGame.getCurrentState(), 0, 0);
                OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, FishyEvaluator.debugString);
                FishyEvaluator.debug = false;
                return;
            }


            Command c = CommandParser.parseCommand(mCommandEngine, command);
            if(c == null){
                // This is handled below.
            }
            else if(!getAvailableCommands().contains(c.getType())) {
                statusText("Command not available at this time.");
                return;
            }
            else if(getAvailableCommands().contains(c.getType())) {
                if (c.getType() == Command.Type.REPLAY_RETURN) {
                    if(!checkReplaySave()) return;
                }
            }

            CommandResult r = mCommandEngine.executeCommand(c);

            if(r.result != CommandResult.SUCCESS) {
                if(r.type == Command.Type.NONE) {
                    if(!command.isEmpty()) {
                        statusText("Unrecognized command: " + command);
                    }
                }
                else {
                    statusText(r.message);
                }
            }
            else if (r.type == Command.Type.MOVE) {
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
            else if (r.type == Command.Type.INFO) {
                CommandParser.Info infoCommand = (CommandParser.Info) c;
                mBoardWindow.rerenderBoard(infoCommand.location, infoCommand.stops, infoCommand.moves, infoCommand.captures);
            }
            else if (r.type == Command.Type.SHOW) {
                mBoardWindow.rerenderBoard();
            }
            else if (r.type == Command.Type.HISTORY) {
                String gameRecord = (String) r.extra;
                statusText(gameRecord);
            }
            else if (r.type == Command.Type.HELP) {
                String helpString = CommandParser.getHelpString(getAvailableCommands());

                ScrollingMessageDialog dialog = new ScrollingMessageDialog("OpenTafl " + OpenTafl.CURRENT_VERSION + " Help", helpString, MessageDialogButton.Close);
                dialog.setSize(new TerminalSize(Math.min(70, mGui.getScreen().getTerminalSize().getColumns() - 2), 30));
                dialog.showDialog(mGui);
            }
            else if (r.type == Command.Type.SAVE) {
                String title;
                boolean saveReplay;
                if(mInReplay) {
                    title = "Save replay";
                    saveReplay = true;
                }
                else {
                    title = "Save game";
                    saveReplay = false;
                }

                File saveFile;
                if(mCommandEngine.getMode() == Mode.GAME) saveFile = TerminalUtils.showFileChooserDialog(mGui, title, "Save", new File("saved-games"));
                else saveFile = TerminalUtils.showFileChooserDialog(mGui, title, "Save", new File("saved-games/replays"));

                if(saveFile == null) {
                    return;
                }
                else if(saveFile.exists()) {
                    MessageDialogButton result = MessageDialog.showMessageDialog(mGui, "Overwrite file?", "File with name " + saveFile.getName() +  " already exists.\nOverwrite?", MessageDialogButton.Yes, MessageDialogButton.No);
                    if(result.equals(MessageDialogButton.No)) return;
                }

                boolean success;
                if(saveReplay) {
                    success = GameSerializer.writeReplayToFile(mReplay, saveFile, true);
                    mCommandEngine.getReplay().markClean();
                }
                else {
                    success = GameSerializer.writeGameToFile(mGame, saveFile, true);
                }

                if(!success) {
                    MessageDialog.showMessageDialog(mGui, "Unable to save", "Unable to write savegame file.");
                }
            }
            else if (r.type == Command.Type.RULES) {
                String rulesString = (String) r.extra;

                ScrollingMessageDialog dialog = new ScrollingMessageDialog("Rules", rulesString, MessageDialogButton.Close);
                dialog.setSize(new TerminalSize(Math.min(70, mGui.getScreen().getTerminalSize().getColumns() - 2), 30));
                dialog.showDialog(mGui);
            }
            else if (r.type == Command.Type.QUIT) {
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
                    if(mServerConnection != null) {
                        mServerConnection.sendGameEndedMessage();

                        if(mServerConnection.getGameRole() == GameRole.ATTACKER) {
                            mCommandEngine.networkVictory(VictoryPacket.Victory.DEFENDER);
                        }
                        else if(mServerConnection.getGameRole() == GameRole.DEFENDER) {
                            mCommandEngine.networkVictory(VictoryPacket.Victory.ATTACKER);
                        }
                        else {
                            mCommandEngine.finishGame();
                            statusText("Finished game. Enter 'quit' again to return to menu.");
                        }
                    }
                    else {
                        mCommandEngine.finishGame();
                        statusText("Finished game. Enter 'quit' again to return to menu.");
                    }
                }
                else {
                    if(checkReplaySave()) leaveGameUi();
                }
            }
            else if(r.type == Command.Type.ANALYZE) {
                statusText("AI analysis beginning.");
            }
            else if(r.type == Command.Type.REPLAY_ENTER) {
                statusText("Entered replay mode.");
                mBoardWindow.rerenderBoard();
            }
            else if(r.type == Command.Type.REPLAY_PLAY_HERE) {
                statusText("Starting new game from this position...");
                mBoardWindow.rerenderBoard();
            }
            else if(r.type == Command.Type.REPLAY_RETURN) {
                statusText("Returning to game.");
                mBoardWindow.rerenderBoard();
            }
            else if(r.type == Command.Type.REPLAY_NEXT) {
                mBoardWindow.rerenderBoard();
                tryTimeUpdate();
                updateComments();

                int result = (Integer) r.extra;
                if(result > GameState.GOOD_MOVE) { // All results > GOOD_MOVE are special, and should have some status
                    statusText(GameState.getStringForMoveResult(result));
                }
            }
            else if(r.type == Command.Type.REPLAY_PREVIOUS) {
                mBoardWindow.rerenderBoard();
                tryTimeUpdate();
                updateComments();
            }
            else if(r.type == Command.Type.REPLAY_JUMP) {
                mBoardWindow.rerenderBoard();
                tryTimeUpdate();
                updateComments();
            }
            else if(r.type == Command.Type.VARIATION) {
                tryTimeUpdate();
                updateComments();
                mBoardWindow.rerenderBoard();

                int result = (Integer) r.extra;
                if(result > GameState.GOOD_MOVE) { // All results > GOOD_MOVE are special, and should have some status
                    statusText(GameState.getStringForMoveResult(result));
                }
            }
            else if(r.type == Command.Type.DELETE) {
                tryTimeUpdate();
                updateComments();
                mBoardWindow.rerenderBoard();
            }
            else if(r.type == Command.Type.ANNOTATE) {
                AnnotationDialog d = new AnnotationDialog("Edit annotation", mTerminal.getSize(), mReplay);
                d.setHints(TerminalThemeConstants.CENTERED_MODAL);
                d.showDialog(mGui);

                mCommandEngine.getReplay().markDirty();

                updateComments();
            }
            else if(r.type == Command.Type.CHAT) {
                if(mServerConnection != null) {
                    ClientServerConnection.ChatType type =
                            (mServerConnection.getGameRole() == GameRole.KIBBITZER) ?
                                    ClientServerConnection.ChatType.SPECTATOR : ClientServerConnection.ChatType.GAME;
                    mServerConnection.sendChatMessage(type, mServerConnection.getUsername(), r.message);
                }
            }
            else if(r.type == Command.Type.CLIPBOARD_PASTE) {
                LoadNotationDialog d = new LoadNotationDialog(mTerminalCallback, GameScreen.this);
                d.setHints(TerminalThemeConstants.CENTERED_MODAL);
                d.showLoadNotationDialog(mGui);
            }
            else if (r.type == Command.Type.TAGS) {
                TagSettingsDialog d = new TagSettingsDialog(mCommandEngine.getReplay());
                d.setHints(TerminalThemeConstants.CENTERED_MODAL);
                d.showDialog(mGui);
            }
            else {
                if (r.message != null && !r.message.isEmpty()) {
                    statusText(r.message);
                }
            }
        }

        private boolean checkReplaySave() {
            if(mCommandEngine.getReplay() != null && !mCommandEngine.getReplay().getMode().isPuzzleMode() && mCommandEngine.getReplay().isDirty()) {
                MessageDialogBuilder builder = new MessageDialogBuilder();
                builder.setTitle("Replay not saved!");
                builder.setText("This replay has been changed, but not yet saved.\nContinuing will quit and discard changes.");
                builder.addButton(MessageDialogButton.Continue);
                builder.addButton(MessageDialogButton.Cancel);
                MessageDialog dialog = builder.build();
                MessageDialogButton result = dialog.showDialog(mGui);

                if(result.equals(MessageDialogButton.Cancel)) {
                    return false;
                }
            }
            return true;
        }

        private void tryTimeUpdate() {
            if(!mInGame) {
                boolean currentSide = mReplay.getCurrentState().getCurrentSide().isAttackingSide();
                TimeSpec attackerClock = mReplay.getTimeGuess(true);
                TimeSpec defenderClock = mReplay.getTimeGuess(false);
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
            }

            statusText("--- Start of replay ---");
            if(tags != null && tags.containsKey(Game.Tag.START_COMMENT)) {
                String commentText = tags.get(Game.Tag.START_COMMENT);
                if(!commentText.isEmpty()) {
                    if(mReplay.getMode().isPuzzleMode()) {
                        commentText = commentText.replaceAll(ReplayGame.hintRegex, "").trim();
                    }
                    statusText(commentText);
                }
            }
        }

        private void updateComments() {
            if(!mInGame) {
                DetailedMoveRecord m = (DetailedMoveRecord) mReplay.getCurrentState().getEnteringMove();
                if(m == null) {
                    tryStartingComments(mReplay);
                }
                else {
                    if(mReplay.isAtPuzzleStart()) {
                        tryStartingComments(mReplay);
                    }

                    statusText(Ansi.UNDERLINE + "Last move" + Ansi.UNDERLINE_OFF + ": " + m);
                    if(!m.getComment().trim().isEmpty()) {
                        String commentText = m.getComment();
                        if(mReplay.getMode().isPuzzleMode()) {
                            commentText = commentText.replaceAll(ReplayGame.hintRegex, "").trim();
                        }
                        statusText(commentText);
                    }
                }
            }
        }

        private List<Command.Type> getAvailableCommands() {
            List<Command.Type> types = new ArrayList<>();

            if(mInGame) {
                types.add(Command.Type.MOVE);
            }

            if(mInReplay && mServerConnection == null) {
                types.add(Command.Type.REPLAY_PLAY_HERE);
            }

            if(mInGame || mPostGame) {
                types.add(Command.Type.REPLAY_ENTER);
            }

            if(mInReplay) {
                types.add(Command.Type.REPLAY_NEXT);
                types.add(Command.Type.REPLAY_PREVIOUS);
                types.add(Command.Type.REPLAY_JUMP);
                types.add(Command.Type.REPLAY_RETURN);
                types.add(Command.Type.VARIATION);
                types.add(Command.Type.DELETE);
                types.add(Command.Type.ANNOTATE);
                types.add(Command.Type.TAGS);
            }

            if(mCommandEngine.getMode() == Mode.REPLAY && mCommandEngine.getReplay().getMode().isPuzzleMode()) {
                if(mCommandEngine.getReplay().isInPuzzlePrestart()) {
                    types.remove(Command.Type.VARIATION);
                }
                types.remove(Command.Type.ANNOTATE);
                types.remove(Command.Type.TAGS);
                types.add(Command.Type.HINT);
            }

            if(mServerConnection != null) {
                types.add(Command.Type.CHAT);
            }

            if(mInGame || mPostGame || mInReplay) {
                types.add(Command.Type.INFO);
                types.add(Command.Type.SHOW);
                types.add(Command.Type.ANALYZE);
                types.add(Command.Type.HISTORY);
                types.add(Command.Type.HELP);
                types.add(Command.Type.RULES);
                types.add(Command.Type.SAVE);
                types.add(Command.Type.CLIPBOARD_COPY);
                types.add(Command.Type.QUIT);
            }

            if(mServerConnection != null) {
                GameInformation info = mServerConnection.getLastJoinedGameInfo();
                if(info != null) {
                    if(!info.allowReplay) {
                        types.remove(Command.Type.ANALYZE);
                        types.remove(Command.Type.REPLAY_ENTER);
                    }
                }
            }

            return types;
        }

        @Override
        public boolean handleKeyStroke(KeyStroke key) {
            if(key.getKeyType() == KeyType.PageUp || key.getKeyType() == KeyType.PageDown) {
                mStatusWindow.handleInput(key);
                return true;
            }
            else return false;
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

    private class ServerCallback implements ClientServerCallback {

        @Override
        public void onStateChanged(ClientServerConnection.State newState) {

        }

        @Override
        public void onChatMessageReceived(ClientServerConnection.ChatType type, String sender, String message) {
            if(type == ClientServerConnection.ChatType.GAME) statusText(sender + ": " + message);
            if(type == ClientServerConnection.ChatType.SPECTATOR
                    && mServerConnection != null
                    && mServerConnection.getGameRole() == GameRole.KIBBITZER) {
                statusText(sender + ": " + message);
            }
        }

        @Override
        public void onSuccessReceived(String message) {

        }

        @Override
        public void onErrorReceived(String message) {

        }

        @Override
        public void onGameListReceived(List<GameInformation> games) {

        }

        @Override
        public void onClientListReceived(List<ClientInformation> clients) {

        }

        @Override
        public void onDisconnect(boolean planned) {
            if(!planned) {
                MessageDialogBuilder b = new MessageDialogBuilder();
                b.setTitle("Server connection failed");
                b.setText("Server terminated the connection.");
                b.addButton(MessageDialogButton.OK);
                MessageDialog d = b.build();
                d.setHints(TerminalThemeConstants.CENTERED_MODAL);

                TerminalUtils.runOnUiThread(mGui, () -> d.showDialog(mGui));
            }

            statusText("Server connection failed, game ended");
            mCommandEngine.finishGame();
            mServerConnection = null;
        }

        @Override
        public Game getGame() {
            return mGame;
        }

        @Override
        public void onStartGame(Rules r, List<DetailedMoveRecord> history) {

        }

        @Override
        public void onHistoryReceived(List<DetailedMoveRecord> moves) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Game screen received history");
            Rules r = mGame.getRules();
            Game g = new Game(r, GameScreen.this);

            for(MoveRecord move : moves) {
                int moveResult = g.getCurrentState().makeMove(move);
                if(moveResult != GameState.GOOD_MOVE) {
                    // TODO: punt back to lobby
                }
            }

            mTerminalCallback.onEnteringGameScreen(g, r.getName());
        }

        @Override
        public void onServerMoveReceived(MoveRecord move) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.CHATTY, "Game screen received spectator move: " + move);
            mCommandEngine.getCurrentPlayer().onMoveDecided(move);
            TerminalUtils.runOnUiThread(mGui, () -> mBoardWindow.rerenderBoard());
        }

        @Override
        public void onClockUpdateReceived(TimeSpec attackerClock, TimeSpec defenderClock) {
            if(mGame.getClock() != null) {
                mGame.getClock().handleNetworkTimeUpdate(attackerClock, defenderClock);
            }
        }

        @Override
        public void onVictory(VictoryPacket.Victory victory) {
            if(mCommandEngine.isInGame()) {
                mCommandEngine.networkVictory(victory);
            }
        }
    }

}
