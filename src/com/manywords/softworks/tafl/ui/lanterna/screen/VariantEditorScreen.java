package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.window.ingame.BoardWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.varianteditor.OptionsWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.varianteditor.RulesWindow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    private int mFocusedWindow = FOCUS_OPTIONS;
    private static final int FOCUS_BOARD = 0;
    private static final int FOCUS_RULES = 1;
    private static final int FOCUS_OPTIONS = 2;

    public void setActive(AdvancedTerminal t, WindowBasedTextGUI gui) {
        super.setActive(t, gui);
        mTerminalCallback = new VariantEditorTerminalCallback();

        createNewRules(13);
        enterUi();
    }

    private void createNewRules(int size) {
        // Mostly for drawing
        mStartingBoard = new GenericBoard(size);

        // The real work happens here
        mAttackers = new GenericSide(mStartingBoard, true);
        mAttackers.setStartingTaflmen(new ArrayList<>());

        mDefenders = new GenericSide(mStartingBoard, false);
        mDefenders.setStartingTaflmen(new ArrayList<>());

        GenericRules newRules = new GenericRules(mStartingBoard, mAttackers, mDefenders);
        if(mRules != null) newRules.copyNonDimensionalRules(mRules);
        mRules = newRules;

        if(mRulesWindow != null) {
            mRulesWindow.updateScreen(mRules);
        }
    }

    private void loadRules(Rules r) {
        mRules = GenericRules.copyRules(r);

        mStartingBoard = (GenericBoard) mRules.getBoard();
        mAttackers = (GenericSide) mRules.getAttackers();
        mDefenders = (GenericSide) mRules.getDefenders();

        mRulesWindow.updateScreen(mRules);
    }

    protected void enterUi() {
        createWindows();
        // Windows laid out in layoutWindows, since we have to add the board window before
        // we can get its size
        addWindows();
    }

    private void createWindows() {
        createBoardWindow();

        mRulesWindow = new RulesWindow(mTerminalCallback);
        mRulesWindow.updateScreen(mRules);
        mOptionsWindow = new OptionsWindow(mTerminalCallback, new OptionsHost());
    }

    private void createBoardWindow() {
        if(mBoardWindow != null) mGui.removeWindow(mBoardWindow);

        TerminalBoardImage.init(mStartingBoard.getBoardDimension());
        mBoardWindow = new BoardWindow("Board Design", mStartingBoard, mTerminalCallback);
        mBoardWindow.setUnhandledKeyCallback(new BoardEditCallback());
        mBoardWindow.renderBoard(mStartingBoard);
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

        // command beneath the board window, status to the right
        int minRulesWidth = 85;
        int rulesWidth = Math.max(minRulesWidth, leftoverRight - 4);
        rulesPosition = new TerminalPosition(boardWindowWidth + 2, 0);
        rulesSize = new TerminalSize(rulesWidth, size.getRows() - 2);

        optionsPosition = new TerminalPosition(0, boardWindowHeight + 2);

        int minOptionsHeight = 4;
        int optionsHeight = Math.max(size.getRows() - boardWindowHeight - 4, minOptionsHeight);
        optionsSize = new TerminalSize(boardWindowWidth, optionsHeight);


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

        setFocusedWindow(FOCUS_OPTIONS);

        mGui.waitForWindowToClose(mOptionsWindow);
    }

    protected void removeWindows() {
        mGui.removeWindow(mBoardWindow);
        mGui.removeWindow(mRulesWindow);
        mGui.removeWindow(mOptionsWindow);
    }

    protected void leaveUi() {
        removeWindows();
    }


    private void cycleTaflmanType(Coord location, char taflman) {
        if(mAttackers.getStartingTaflmen().size() + mDefenders.getStartingTaflmen().size() + (taflman == Taflman.EMPTY ? 1 : 0) > 254) {
            cycleFocus(FOCUS_BACKWARD);
            MessageDialog.showMessageDialog(mGui, "Too many taflmen", "OpenTafl supports at most 254 taflmen per game.");
            cycleFocus(FOCUS_FORWARD);
            return;
        }

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
                    newTaflman |= Taflman.TYPE_MERCENARY;
                    break;
                case Taflman.TYPE_MERCENARY:
                    newTaflman |= Taflman.TYPE_GUARD;
                    break;
                case Taflman.TYPE_GUARD:
                    newTaflman |= Taflman.TYPE_KNIGHT;
                    break;
                case Taflman.TYPE_KNIGHT:
                    if(Taflman.getPackedSide(newTaflman) == Taflman.SIDE_DEFENDERS) {
                        newTaflman |= Taflman.TYPE_KING;
                        break;
                    }
                    // for attackers, fall through
                case Taflman.TYPE_KING:
                    newTaflman = Taflman.EMPTY;
                    break;
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

    private void cycleSpaceType(Coord location) {
        SpaceType currentType = mRules.getSpaceTypeFor(location);

        List<Coord> attackerForts = new ArrayList<>(mRules.getAttackerForts());
        List<Coord> defenderForts = new ArrayList<>(mRules.getDefenderForts());
        List<Coord> centerSpaces = new ArrayList<>(mRules.getCenterSpaces());
        List<Coord> cornerSpaces = new ArrayList<>(mRules.getCornerSpaces());

        attackerForts.remove(location);
        defenderForts.remove(location);
        centerSpaces.remove(location);
        cornerSpaces.remove(location);

        switch(currentType) {
            case NONE:
                cornerSpaces.add(location);
                break;
            case CORNER:
                centerSpaces.add(location);
                break;
            case CENTER:
                attackerForts.add(location);
                break;
            case ATTACKER_FORT:
                defenderForts.add(location);
                break;
            //Otherwise: do nothing, it's already removed.
        }

        mRules.setAttackerForts(new ArrayList<>());
        mRules.setDefenderForts(new ArrayList<>());
        mRules.setCenterSpaces(new ArrayList<>());
        mRules.setCornerSpaces(new ArrayList<>());

        mRules.setAttackerForts(attackerForts);
        mRules.setDefenderForts(defenderForts);
        mRules.setCenterSpaces(centerSpaces);
        mRules.setCornerSpaces(cornerSpaces);
        updateBoard();
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
        mStartingBoard = new GenericBoard(mRules.boardSize);

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
                        cycleSpaceType(location);
                        break;
                    case 'i':
                        cycleTaflmanSide(location);
                        break;
                }
                onFocusPositionChanged(location);
            }
        }

        @Override
        public void onMoveRequested(MoveRecord move) {

        }

        @Override
        public void onFocusPositionChanged(Coord focusPosition) {
            mOptionsWindow.updateStatus(focusPosition, mRules.getSpaceTypeFor(focusPosition), mStartingBoard.getOccupier(focusPosition));
        }
    }

    private void cycleFocus(int direction) {
        if(direction == FOCUS_FORWARD) {
            mFocusedWindow = ++mFocusedWindow % 3;
        }
        else {
            mFocusedWindow -= 1;
            if(mFocusedWindow < 0) mFocusedWindow = 2;
        }

        setFocusedWindow(mFocusedWindow);
    }

    private void setFocusedWindow(int focusedWindow) {
        switch(focusedWindow) {
            case FOCUS_BOARD:
                mGui.setActiveWindow(mBoardWindow);
                mBoardWindow.notifyFocus(true);
                mRulesWindow.notifyFocus(false);
                mOptionsWindow.notifyFocus(false);
                break;
            case FOCUS_RULES:
                mGui.setActiveWindow(mRulesWindow);
                mBoardWindow.notifyFocus(false);
                mRulesWindow.notifyFocus(true);
                mOptionsWindow.notifyFocus(false);
                break;
            case FOCUS_OPTIONS:
                mGui.setActiveWindow(mOptionsWindow);
                mBoardWindow.notifyFocus(false);
                mRulesWindow.notifyFocus(false);
                mOptionsWindow.notifyFocus(true);
                break;
        }
    }

    private class OptionsHost implements OptionsWindow.OptionsWindowHost {

        @Override
        public void newLayout(int dimension) {
            createNewRules(dimension);
            createBoardWindow();

            mGui.addWindow(mBoardWindow);
            layoutWindows(mTerminal.getSize());

            setFocusedWindow(FOCUS_OPTIONS);
        }

        @Override
        public void loadRules(Rules r) {
            if(r == null) return;

            VariantEditorScreen.this.loadRules(r);

            createBoardWindow();
            mGui.addWindow(mBoardWindow);
            layoutWindows(mTerminal.getSize());

            setFocusedWindow(FOCUS_OPTIONS);
        }

        @Override
        public void saveRules() {
            mRulesWindow.updateRules(mRules);

            // TODO: check rules for consistency
            if(mRules.howManyAttackers() + mRules.howManyDefenders() > 255) {
                MessageDialog.showMessageDialog(mGui, "Invalid rules", "OpenTafl does not support rules with more than 255 taflmen total.");
                return;
            }

            File f = TerminalUtils.showFileChooserDialog(mGui, "Save rule set", "Save", new File("user-rules"));

            if(f != null) {
                if(!f.getName().endsWith(".otr")) {
                    f = new File(f.getParent(), f.getName() + ".otr");
                }

                if(f.exists()) {
                    MessageDialogButton result = MessageDialog.showMessageDialog(mGui, "File exists", "Overwrite existing file?", MessageDialogButton.Yes, MessageDialogButton.No);
                    if(result == MessageDialogButton.No) return;
                }

                try {
                    BufferedWriter w = new BufferedWriter(new FileWriter(f));
                    w.write(RulesSerializer.getRulesRecord(mRules, true));
                    w.flush();
                    w.close();
                }
                catch (IOException e) {
                    Log.println(Log.Level.NORMAL, "Failed to write user rules file: " + e);
                    Log.stackTrace(Log.Level.NORMAL, e);
                }
            }

            Variants.reloadRules();
        }

        @Override
        public void quit() {
            MessageDialogButton result = MessageDialog.showMessageDialog(mGui, "Confirm quit", "Quit, discarding any unsaved changes?", MessageDialogButton.Yes, MessageDialogButton.No);
            if(result == MessageDialogButton.Yes) {
                mTerminalCallback.changeActiveScreen(new MainMenuScreen());
            }
        }

        @Override
        public Rules getRules() {
            mRulesWindow.updateRules(mRules);
            return mRules;
        }
    }

    private class VariantEditorTerminalCallback extends DefaultTerminalCallback {

        @Override
        public void changeActiveScreen(LogicalScreen screen) {
            mTerminal.changeActiveScreen(screen);
        }

        @Override
        public boolean handleKeyStroke(KeyStroke key) {
            if(key.getKeyType() == KeyType.Tab) {
                cycleFocus(FOCUS_FORWARD);
                return true;
            }
            else if(key.getKeyType() == KeyType.ReverseTab) {
                cycleFocus(FOCUS_BACKWARD);
                return true;
            }
            return false;
        }
    }
}
