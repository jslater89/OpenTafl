package com.manywords.softworks.tafl.ui;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.ResizeListener;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalTheme;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalWindowDecorationRenderer;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalWindowPostRenderer;
import com.manywords.softworks.tafl.ui.lanterna.window.BoardWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.CommandWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.MainMenuWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.StatusWindow;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by jay on 2/15/16.
 */
public class AdvancedTerminal extends SwingTerminalFrame implements UiCallback {
    public interface TerminalCallback {
        public void onMenuNavigation(com.googlecode.lanterna.gui2.Window destination);
        public void onEnteringGame(BoardWindow bw, StatusWindow sw, CommandWindow cw);
        public void handleInGameCommand(String command);

        public UiCallback getUiCallback();
    }

    private MultiWindowTextGUI mGui;

    private BoardWindow mBoardWindow;
    private StatusWindow mStatusWindow;
    private CommandWindow mCommandWindow;

    public AdvancedTerminal() {
        super();
        setTitle("OpenTafl");
        setSize(1024, 768);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        addResizeListener(new ResizeListener() {
            @Override
            public void onResized(Terminal terminal, TerminalSize terminalSize) {
                layoutGameWindows(terminalSize);
            }
        });

        Screen s = null;
        try {
            s = new TerminalScreen(this);
            s.startScreen();
        }
        catch(IOException e) {
            System.out.println("Failed to start");
            System.exit(0);
        }
        mGui = new MultiWindowTextGUI(s, new DefaultWindowManager(new TerminalWindowDecorationRenderer()), new TerminalWindowPostRenderer(), new EmptySpace(TextColor.ANSI.BLACK));
        mGui.setTheme(new TerminalTheme());
        Window mainMenuWindow = new MainMenuWindow(mTerminalCallback);
        mainMenuWindow.setHints(TerminalThemeConstants.CENTERED);
        mGui.addWindowAndWait(mainMenuWindow);
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
            statusPosition = new TerminalPosition(0, boardWindowHeight + 2);
            statusSize = new TerminalSize(boardWindowWidth, leftoverBottom - 6);

            leftoverBottom -= statusSize.getRows();
            leftoverBottom -= 4;

            commandPosition = new TerminalPosition(0, boardWindowHeight + 2 + statusSize.getRows() + 2);
            commandSize = new TerminalSize(boardWindowWidth, leftoverBottom);
        }
        else if(leftoverRight < 40){
            statusPosition = new TerminalPosition(boardWindowWidth + 2, 0);
            statusSize = new TerminalSize(leftoverRight - 4, boardWindowHeight);

            commandPosition = new TerminalPosition(0, boardWindowHeight + 2);
            commandSize = new TerminalSize(boardWindowWidth, 4);
        }
        else {
            statusPosition = new TerminalPosition(boardWindowWidth + 2, 0);
            statusSize = new TerminalSize(leftoverRight - 4, boardWindowHeight - 6);

            commandPosition = new TerminalPosition(boardWindowWidth + 2, statusSize.getRows() + 2);
            commandSize = new TerminalSize(leftoverRight - 4, 4);
        }

        mStatusWindow.setPosition(statusPosition);
        mStatusWindow.setSize(statusSize);

        mCommandWindow.setPosition(commandPosition);
        mCommandWindow.setSize(commandSize);
    }

    @Override
    public void gameStarting() {

    }

    @Override
    public void awaitingMove(boolean isAttackingSide) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {

    }

    @Override
    public void gameStateAdvanced() {

    }

    @Override
    public void victoryForSide(Side side) {

    }

    @Override
    public void gameFinished() {

    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
    }

    @Override
    public boolean inGame() {
        return false;
    }

    private TerminalCallback mTerminalCallback = new TerminalCallback() {
        @Override
        public void onMenuNavigation(Window destination) {
            if(destination == null) System.exit(0);

            mGui.removeWindow(mGui.getActiveWindow());
            destination.setHints(TerminalThemeConstants.CENTERED);
            mGui.addWindowAndWait(destination);
        }

        @Override
        public void onEnteringGame(BoardWindow bw, StatusWindow sw, CommandWindow cw) {
            mGui.removeWindow(mGui.getActiveWindow());
            mBoardWindow = bw;
            mStatusWindow = sw;
            mCommandWindow = cw;
            addBoardWindows();
        }

        int count = 0;
        @Override
        public void handleInGameCommand(String command) {
            mStatusWindow.addStatus("Lalalallalalallalallalallalalassdfasdfasdfasdfgdsfasdfasdfasdgasdfewagasdf" + count++);
        }

        @Override
        public UiCallback getUiCallback() {
            return AdvancedTerminal.this;
        }
    };
}
