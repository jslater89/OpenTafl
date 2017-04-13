package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.window.ingame.BoardWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.varianteditor.OptionsWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.varianteditor.RulesWindow;

import java.util.ArrayList;
import java.util.List;

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

        // Mostly for drawing
        mStartingBoard = new GenericBoard(9);

        // THe real work happens here
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
        mBoardWindow.setUnhandledKeyCallback(new BoardEditCallback());
        mBoardWindow.renderBoard(mStartingBoard);

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

        mGui.setActiveWindow(mBoardWindow);
        mBoardWindow.notifyFocus(true);

        mGui.waitForWindowToClose(mOptionsWindow);
    }

    protected void removeWindows() {

    }

    protected void leaveUi() {
        removeWindows();
    }


    private void cycleTaflmanType(Coord location, char taflman) {
        char newTaflman = Taflman.EMPTY;
        char packedSide = Taflman.getPackedSide(taflman);
        if(taflman == Taflman.EMPTY) {
            newTaflman |= Taflman.SIDE_ATTACKERS;
            newTaflman |= Taflman.TYPE_TAFLMAN;
            packedSide = Taflman.SIDE_ATTACKERS;
        }
        else {
            newTaflman |= Taflman.getPackedSide(taflman);

            switch(Taflman.getPackedType(taflman)) {
                case Taflman.TYPE_TAFLMAN:
                    newTaflman |= Taflman.TYPE_COMMANDER;
                    break;
                case Taflman.TYPE_COMMANDER:
                    newTaflman |= Taflman.TYPE_KNIGHT;
                    break;
                case Taflman.TYPE_KNIGHT:
                    if(Taflman.getPackedSide(newTaflman) == Taflman.SIDE_DEFENDERS) {
                        newTaflman |= Taflman.TYPE_KING;
                        break;
                    }
                case Taflman.TYPE_KING:
                    newTaflman = Taflman.EMPTY;
                }
        }

        Side s = packedSide == Taflman.SIDE_ATTACKERS ? mAttackers : mDefenders;
        List<Side.TaflmanHolder> currentTaflmen = s.getStartingTaflmen();

        Side.TaflmanHolder toRemove = null;
        for(Side.TaflmanHolder t : currentTaflmen) {
            if(t.coord.equals(location)) {
                toRemove = t;
                break;
            }
        }
        currentTaflmen.remove(toRemove);

        if(newTaflman != Taflman.EMPTY) currentTaflmen.add(new Side.TaflmanHolder(newTaflman, location));
        s.setStartingTaflmen(currentTaflmen);

        updateTaflmanIds();
        updateBoard();
    }

    private void cycleTaflmanSide(Coord location) {
        boolean attackerToDefender = false;
        boolean defenderToAttacker = false;
        Side.TaflmanHolder ofInterest = null;

        for(Side.TaflmanHolder t : mAttackers.getStartingTaflmen()) {
            if(t.coord.equals(location)) {
                ofInterest = t;
                attackerToDefender = true;
                break;
            }
        }

        for(Side.TaflmanHolder t : mDefenders.getStartingTaflmen()) {
            if(t.coord.equals(location)) {
                ofInterest = t;
                defenderToAttacker = true;
                break;
            }
        }

        if(attackerToDefender) {
            List<Side.TaflmanHolder> attackers = mAttackers.getStartingTaflmen();
            attackers.remove(ofInterest);

            List<Side.TaflmanHolder> defenders = mDefenders.getStartingTaflmen();
            char packed = Taflman.EMPTY;
            packed |= Taflman.getPackedType(ofInterest.packed);
            packed |= Taflman.SIDE_DEFENDERS;
            ofInterest = new Side.TaflmanHolder(packed, ofInterest.coord);
            defenders.add(ofInterest);

            updateTaflmanIds();
            updateBoard();
        }

        if(defenderToAttacker) {
            List<Side.TaflmanHolder> defenders = mDefenders.getStartingTaflmen();
            defenders.remove(ofInterest);

            List<Side.TaflmanHolder> attackers = mAttackers.getStartingTaflmen();
            char packed = Taflman.EMPTY;
            packed |= Taflman.getPackedType(ofInterest.packed);
            packed |= Taflman.SIDE_ATTACKERS;
            ofInterest = new Side.TaflmanHolder(packed, ofInterest.coord);
            attackers.add(ofInterest);

            updateTaflmanIds();
            updateBoard();
        }
    }

    private void updateTaflmanIds() {
        short currentId = 0;
        List<Side.TaflmanHolder> newDefenders = new ArrayList<>();
        List<Side.TaflmanHolder> newAttackers = new ArrayList<>();

        for(Side.TaflmanHolder t : mDefenders.getStartingTaflmen()) {
            char packed = Taflman.EMPTY;
            packed |= Taflman.getPackedSide(t.packed);
            packed |= Taflman.getPackedType(t.packed);
            packed |= currentId;

            newDefenders.add(new Side.TaflmanHolder(packed, t.coord));
            currentId += 1;
        }

        currentId = 0;
        for(Side.TaflmanHolder t : mAttackers.getStartingTaflmen()) {
            char packed = Taflman.EMPTY;
            packed |= Taflman.getPackedSide(t.packed);
            packed |= Taflman.getPackedType(t.packed);
            packed |= currentId;

            newAttackers.add(new Side.TaflmanHolder(packed, t.coord));
            currentId += 1;
        }

        mAttackers.setStartingTaflmen(newAttackers);
        mDefenders.setStartingTaflmen(newDefenders);
    }

    private void updateBoard() {
        mStartingBoard = new GenericBoard(9);

        mAttackers = new GenericSide(mStartingBoard, true, mAttackers.getStartingTaflmen());
        mDefenders = new GenericSide(mStartingBoard, false, mDefenders.getStartingTaflmen());

        mStartingBoard.setupTaflmen(mAttackers, mDefenders);
        mStartingBoard.setRules(mRules);
        mRules.setBoard(mStartingBoard);

        mBoardWindow.renderBoard(mStartingBoard);
    }

    private class BoardEditCallback implements TerminalBoardImage.Callback {

        @Override
        public void onUnhandledKey(KeyStroke key, Coord location) {
            if(key.getKeyType() == KeyType.Character) {
                switch(key.getCharacter()) {
                    case 'p':
                        char taflman = mStartingBoard.getOccupier(location);
                        cycleTaflmanType(location, taflman);

                        break;
                    case 't':
                        // cycle space (t)ype for location
                        break;
                    case 'i':
                        // cycle s(i)de for location
                        cycleTaflmanSide(location);
                        break;
                }
            }
        }

        @Override
        public void onMoveRequested(MoveRecord move) {

        }
    }

    private class VariantEditorTerminalCallback extends DefaultTerminalCallback {

        @Override
        public void changeActiveScreen(LogicalScreen screen) {
            mTerminal.changeActiveScreen(screen);
        }
    }
}
