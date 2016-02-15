package com.manywords.softworks.tafl.ui;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalTheme;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalWindowDecorationRenderer;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalWindowPostRenderer;
import com.manywords.softworks.tafl.ui.lanterna.window.BoardWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.CommandWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.MainMenuWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.StatusWindow;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class LanternaTerminal extends SwingTerminalFrame implements UiCallback {
    public interface TerminalCallback {
        public void onMenuNavigation(com.googlecode.lanterna.gui2.Window destination);
        public void onEnteringGame(BoardWindow bw, StatusWindow sw, CommandWindow cw);

        public UiCallback getUiCallback();
    }

    private MultiWindowTextGUI mGui;

    private BoardWindow mBoardWindow;
    private StatusWindow mStatusWindow;
    private CommandWindow mCommandWindow;

    public LanternaTerminal() {
        super();
        setTitle("OpenTafl");
        setSize(1024, 768);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

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

    private void layoutGameWindows() {
        if(mBoardWindow == null || mStatusWindow == null || mCommandWindow == null) return;

        mGui.removeWindow(mBoardWindow);
        mGui.removeWindow(mStatusWindow);
        mGui.removeWindow(mCommandWindow);

        mGui.addWindow(mBoardWindow);
        mGui.addWindow(mStatusWindow);
        mGui.addWindow(mCommandWindow);

        mBoardWindow.setHints(TerminalThemeConstants.BOARD_WINDOW);
        mStatusWindow.setHints(TerminalThemeConstants.STATUS_WINDOW);
        mCommandWindow.setHints(TerminalThemeConstants.COMMAND_WINDOW);

        TerminalSize screenSize = mGui.getScreen().getTerminalSize();
        TerminalSize boardWindowSize = mBoardWindow.getPreferredSize();
        TerminalSize commandWindowSize = mCommandWindow.getPreferredSize();
        TerminalSize statusWindowSize = mStatusWindow.getPreferredSize();

        mBoardWindow.setPosition(new TerminalPosition(0, 0));

        if(statusWindowSize.getColumns() + 3<= (screenSize.getColumns() - boardWindowSize.getColumns())) {
            mStatusWindow.setPosition(new TerminalPosition(boardWindowSize.getColumns() + 3, 0));
        }
        else {
            mStatusWindow.setPosition(new TerminalPosition(0, boardWindowSize.getRows() + 3));
        }

        if(commandWindowSize.getColumns() + 3 <= (screenSize.getColumns() - boardWindowSize.getColumns())) {
            mCommandWindow.setPosition(new TerminalPosition(boardWindowSize.getColumns() + 3, statusWindowSize.getRows() + 3));
        }
        else {
            mCommandWindow.setPosition(new TerminalPosition(0, boardWindowSize.getRows() + statusWindowSize.getRows() + 3 + 3));
        }

        mGui.waitForWindowToClose(mBoardWindow);
    }

    @Override
    public void gameStateAdvanced() {

    }

    @Override
    public void victoryForSide(Side side) {

    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
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
            layoutGameWindows();
        }

        @Override
        public UiCallback getUiCallback() {
            return LanternaTerminal.this;
        }
    };
}
