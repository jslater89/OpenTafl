package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.manywords.softworks.tafl.rules.GenericBoard;
import com.manywords.softworks.tafl.rules.GenericRules;
import com.manywords.softworks.tafl.rules.GenericSide;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.window.ingame.BoardWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.varianteditor.OptionsWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.varianteditor.RulesWindow;

import java.util.ArrayList;

/**
 * Created by jay on 4/12/17.
 */
public class VariantEditorScreen extends MultiWindowLogicalScreen {
    private BoardWindow mBoardWindow;
    private RulesWindow mRulesWindow;
    private OptionsWindow mOptionsWindow;

    private GenericBoard mStartingBoard;
    private GenericSide mAttackers;
    private GenericSide mDefenders;
    private GenericRules mRules;

    public void setActive(AdvancedTerminal t, WindowBasedTextGUI gui) {
        super.setActive(t, gui);
        mTerminalCallback = new VariantEditorTerminalCallback();

        mStartingBoard = new GenericBoard(9);

        mAttackers = new GenericSide(mStartingBoard, true);
        mAttackers.setStartingTaflmen(new ArrayList<>());

        mDefenders = new GenericSide(mStartingBoard, false);
        mDefenders.setStartingTaflmen(new ArrayList<>());

        mRules = new GenericRules(mStartingBoard, mAttackers, mDefenders);

        enterUi();
    }

    protected void enterUi() {
        createWindows();
        // Windows laid out in layoutWindows, since we have to add the board window before
        // we can get its size
        addWindows();
    }

    private void createWindows() {
        TerminalBoardImage.init(mStartingBoard.getBoardDimension());
        mBoardWindow = new BoardWindow("Board Design", mStartingBoard, mTerminalCallback);
        mRulesWindow = new RulesWindow();
        mOptionsWindow = new OptionsWindow();
    }

    protected void layoutWindows(TerminalSize size) {
        mBoardWindow.setHints(TerminalThemeConstants.BOARD_WINDOW);
        mRulesWindow.setHints(TerminalThemeConstants.MANUAL_LAYOUT);
        mOptionsWindow.setHints(TerminalThemeConstants.MANUAL_LAYOUT);

        TerminalSize boardWindowSize = mBoardWindow.getPreferredSize();

        mBoardWindow.setPosition(new TerminalPosition(0, 0));
        int leftoverRight = size.getColumns() - boardWindowSize.getColumns();
        TerminalPosition rulesPosition, optionsPosition;
        TerminalSize rulesSize, optionsSize;

        int boardWindowWidth = boardWindowSize.getColumns();
        int boardWindowHeight = boardWindowSize.getRows();

        if(leftoverRight < 20) {
            boardWindowHeight = boardWindowSize.getRows();

            int leftoverBottom = size.getRows() - 2 - boardWindowHeight - 2;

            // status and command stacked beneath the board window
            rulesPosition = new TerminalPosition(0, boardWindowHeight + 2);
            rulesSize = new TerminalSize(boardWindowWidth, leftoverBottom - 6);

            optionsPosition = new TerminalPosition(0, boardWindowHeight + 2 + rulesSize.getRows() + 2);
            optionsSize = new TerminalSize(boardWindowWidth, leftoverBottom - rulesSize.getRows() - 2);
        }
        else {
            // command beneath the board window, status to the right
            rulesPosition = new TerminalPosition(boardWindowWidth + 2, 0);
            rulesSize = new TerminalSize(leftoverRight - 4, size.getRows() - 2);

            optionsPosition = new TerminalPosition(0, boardWindowHeight + 2);
            optionsSize = new TerminalSize(boardWindowWidth, size.getRows() - boardWindowHeight - 4);
        }

        mBoardWindow.setSize(new TerminalSize(boardWindowWidth, boardWindowHeight));

        mRulesWindow.setPosition(rulesPosition);
        mRulesWindow.setSize(rulesSize);

        mOptionsWindow.setPosition(optionsPosition);
        mOptionsWindow.setSize(optionsSize);
    }

    protected void addWindows() {
        mGui.addWindow(mBoardWindow);
        mGui.addWindow(mRulesWindow);
        mGui.addWindow(mOptionsWindow);

        layoutWindows(mGui.getScreen().getTerminalSize());

        mGui.setActiveWindow(mOptionsWindow);
        //mOptionsWindow.notifyFocus(true);

        mGui.waitForWindowToClose(mOptionsWindow);
    }

    protected void removeWindows() {

    }

    protected void leaveUi() {
        removeWindows();
    }

    private class VariantEditorTerminalCallback extends DefaultTerminalCallback {

        @Override
        public void changeActiveScreen(LogicalScreen screen) {
            mTerminal.changeActiveScreen(screen);
        }
    }
}
