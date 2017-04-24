package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.collections.TaflmanCoordMap;
import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalBoardImage extends BasicTextImage implements SimpleInteractable {
    public interface Callback {
        void onUnhandledKey(KeyStroke key, Coord location);
        void onMoveRequested(MoveRecord move);
        void onFocusPositionChanged(Coord focusPosition);
    }

    private static int boardDimension;
    private int mRowHeight = 3;
    private int mColWidth = 5;
    private int mSpaceHeight = mRowHeight - 1;
    private int mSpaceWidth = mColWidth - 1;
    private int mLeftPad = 0;

    private GameState mCurrentState;
    private Board mCurrentBoard;

    // For interactability
    private boolean mAllowMovement = true;
    private boolean mFocused;
    private Coord mFocusPosition;
    private Coord mSelectedPosition;
    private Callback mCallback;

    public static void init(int dimension) {
        boardDimension = dimension;
    }

    public TerminalBoardImage() {
        this(null, 3, 5);
    }

    public TerminalBoardImage(int rowHeight, int colWidth) {
        this(null, rowHeight, colWidth);
    }

    public TerminalBoardImage(GameState state) {
        this(state, 3, 5);
    }

    public TerminalBoardImage(GameState state, int rowHeight, int colWidth) {
        // +--- per row, + extra
        // |
        // |
        //  per column, + extra
        // If row height is 2, add an extra two columns for the row index labels
        super(boardDimension * colWidth + (rowHeight == 2 ? 3 : 1), boardDimension * rowHeight + 1);

        mLeftPad = (rowHeight == 2 ? 1 : 0);
        mRowHeight = rowHeight;
        mColWidth = colWidth;
        mSpaceHeight = mRowHeight - 1;
        mSpaceWidth = mColWidth - 1;

        mFocused = false;
        mFocusPosition = Coord.get(boardDimension / 2, boardDimension / 2);

        renderBoardBackground();
        if(state != null) {
            renderState(state, null, null, null, null);
        }
    }

    public void renderState(GameState state, Coord highlight, List<Coord> allowableDestinations, List<Coord> allowableMoves, List<Coord> captureSpaces) {
        mCurrentState = state;
        if(mCurrentState != null) renderBoard(mCurrentState.getBoard(), highlight, allowableDestinations, allowableMoves, captureSpaces);
    }

    public void renderBoard(Board board, Coord highlight, List<Coord> allowableDestinations, List<Coord> allowableMoves, List<Coord> captureSpaces) {
        mCurrentBoard = board;
        if(mCurrentBoard != null) rerender(highlight, allowableDestinations, allowableMoves, captureSpaces);
    }

    private void rerender(Coord highlight, List<Coord> allowableDestinations, List<Coord> allowableMoves, List<Coord> captureSpaces) {
        if(mCurrentBoard == null) return;

        // Render in order of most spaces to fewest, for maximum information preservation
        clearSpaces();
        renderSpecialSpaces(mCurrentBoard.getRules());

        if(allowableMoves != null) renderAllowableMoves(allowableMoves);
        if(allowableDestinations != null) renderAllowableDestinations(highlight, allowableDestinations);
        if(captureSpaces != null) renderCapturingMoves(highlight, captureSpaces);
        if(highlight != null) renderHighlight(highlight);

        if(mFocused) {
            renderHighlight(mFocusPosition);
            if(mSelectedPosition != null) {
                renderSelection(mSelectedPosition);
                renderMovement(mSelectedPosition, mFocusPosition);
            }
        }

        renderTaflmen(mCurrentBoard);
    }

    private void renderBoardBackground() {
        TextCharacter plus = new TextCharacter('+', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter dash = new TextCharacter('-', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter pipe = new TextCharacter('|', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter space = new TextCharacter(' ', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);

        int yFarTop = 0;
        int yFarBottom = boardDimension * mRowHeight;
        int xFarLeft = mLeftPad;
        int xFarRight = mLeftPad + boardDimension * mColWidth;

        for (int row = 0; row < boardDimension; row++) {
            int yTop = row * mRowHeight;
            int yBottom = row * mRowHeight + mRowHeight;
            int rowLabelIdx = ((yTop + yBottom) / 2);

            for (int col = 0; col < boardDimension; col++) {
                int xLeft = col * mColWidth + mLeftPad;
                int xRight = col * mColWidth + mColWidth + mLeftPad;
                int colLabelIdx = ((xLeft + xRight) / 2);

                for (int y = yTop; y < yBottom + 1; y++) {
                    for (int x = xLeft; x < xRight + 1; x++) {
                        // Draw the top or bottom of a space
                        if (y % mRowHeight == 0) {
                            if (y == yFarTop && x == colLabelIdx) {
                                setCharacterAt(x, y, new TextCharacter((char) ('a' + col)));
                            }
                            else if(y == yFarBottom && x == colLabelIdx) {
                                setCharacterAt(x, y, new TextCharacter((char) ('a' + col)));
                            }
                            else if (x % mColWidth == mLeftPad) setCharacterAt(x, y, plus);
                            else setCharacterAt(x, y, dash);
                        }
                        // Draw the middle of a space
                        else {
                            String rowLabel = "" + (boardDimension - row);
                            if (x == xFarLeft && y == rowLabelIdx) {
                                if (mRowHeight == 2 && rowLabel.length() > 1) {
                                    setCharacterAt(xLeft - 1, y, new TextCharacter(rowLabel.charAt(0)));
                                    setCharacterAt(xLeft, y, new TextCharacter(rowLabel.charAt(1)));
                                }
                                else if (rowLabel.length() == 1) {
                                    setCharacterAt(x, y, new TextCharacter(rowLabel.charAt(0)));
                                }
                                else {
                                    setCharacterAt(x, y, new TextCharacter(rowLabel.charAt(0)));
                                }
                            }
                            else if (x == xFarLeft && y == rowLabelIdx + 1 && rowLabel.length() > 1) {
                                setCharacterAt(x, y, new TextCharacter(rowLabel.charAt(1)));
                            }
                            else if (x == xFarRight && y == rowLabelIdx) {
                                if (mRowHeight == 2 && rowLabel.length() > 1) {
                                    setCharacterAt(xRight, y, new TextCharacter(rowLabel.charAt(0)));
                                    setCharacterAt(xRight + 1, y, new TextCharacter(rowLabel.charAt(1)));
                                }
                                else if (rowLabel.length() == 1) {
                                    setCharacterAt(x, y, new TextCharacter(rowLabel.charAt(0)));
                                }
                                else {
                                    setCharacterAt(x, y, new TextCharacter(rowLabel.charAt(0)));
                                }
                            }
                            else if (x == xFarRight && y == rowLabelIdx + 1 && rowLabel.length() > 1) {
                                setCharacterAt(x, y, new TextCharacter(rowLabel.charAt(1)));
                            }
                            else if (x % mColWidth == mLeftPad) setCharacterAt(x, y, pipe);
                            else setCharacterAt(x, y, space);
                        }
                    }
                }
            }
        }
    }

    private void clearSpaces() {
        TextCharacter space = new TextCharacter(' ', TerminalThemeConstants.DKGRAY, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        for(int x = 0; x < boardDimension; x++) {
            for(int y = 0; y < boardDimension; y++) {
                fillCoord(space, Coord.get(x, y));
            }
        }
    }

    private void renderAllowableDestinations(Coord start, List<Coord> coords) {
        TextCharacter dot = new TextCharacter('.', TerminalThemeConstants.GREEN, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        if(TerminalSettings.advancedDestinationRendering) {
            for(Coord c : coords) {
                if(c.toString().length() <= mSpaceWidth) {
                    fillCoord(c.toString(), TerminalThemeConstants.GREEN, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS, dot, c);
                }
                else if(c.x == start.x) {
                    // Render number
                    String row = "" + (c.y + 1);
                    fillCoord(row, TerminalThemeConstants.GREEN, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS, dot, c);
                }
                else {
                    // Render letter
                    char file = (char) ('a' + c.x);
                    String fileString = "" + file;
                    fillCoord(fileString, TerminalThemeConstants.GREEN, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS, dot, c);
                }
            }
        }
        else {
            fillCoords(dot, coords);
        }
    }

    private void renderAllowableMoves(List<Coord> coords) {
        TextCharacter dash = new TextCharacter('-', TerminalThemeConstants.GREEN, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        fillCoords(dash, coords);
    }

    private void renderCapturingMoves(Coord start, List<Coord> coords) {
        TextCharacter slash = new TextCharacter('/', TerminalThemeConstants.YELLOW, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        if(TerminalSettings.advancedDestinationRendering) {
            for(Coord c : coords) {
                if(c.toString().length() <= mSpaceWidth) {
                    fillCoord(c.toString(), TerminalThemeConstants.YELLOW, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS, slash, c);
                }
                else if(c.x == start.x) {
                    // Render number
                    String row = "" + (c.y + 1);
                    fillCoord(row, TerminalThemeConstants.YELLOW, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS, slash, c);
                }
                else {
                    // Render letter
                    char file = (char) ('a' + c.x);
                    String fileString = "" + file;
                    fillCoord(fileString, TerminalThemeConstants.YELLOW, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS, slash, c);
                }
            }
        }
        else {
            fillCoords(slash, coords);
        }
    }

    private void renderHighlight(Coord highlight) {
        TextCharacter star = new TextCharacter('*', TerminalThemeConstants.WHITE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        fillCoord(star, highlight);
    }

    private void renderSelection(Coord selected) {
        TextCharacter circle = new TextCharacter('o', TerminalThemeConstants.WHITE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        fillCoord(circle, selected);
    }

    private void renderMovement(Coord start, Coord end) {
        TextCharacter circle = new TextCharacter('o', TerminalThemeConstants.WHITE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter up = new TextCharacter('^', TerminalThemeConstants.WHITE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter down = new TextCharacter('v', TerminalThemeConstants.WHITE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter left = new TextCharacter('<', TerminalThemeConstants.WHITE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter right = new TextCharacter('>', TerminalThemeConstants.WHITE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);

        TextCharacter dash = new TextCharacter('-', TerminalThemeConstants.LTGRAY, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter pipe = new TextCharacter('|', TerminalThemeConstants.LTGRAY, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);

        TextCharacter direction = circle;
        if(start.y < end.y) direction = up;
        if(start.y > end.y) direction = down;
        if(start.x > end.x) direction = left;
        if(start.x < end.x) direction = right;

        TextCharacter unstoppable = dash;
        if(start.x == end.x) {
            unstoppable = pipe;
        }

        char taflman = mCurrentState.getBoard().getOccupier(start);
        List<Coord> moves = new ArrayList<>();
        List<Coord> stops = new ArrayList<>();
        if(mCurrentState != null) {
            moves = Taflman.getAllowableMoves(mCurrentState, taflman);
            stops = Taflman.getAllowableDestinations(mCurrentState, taflman);
        }

        boolean skip = false;

        // Render the focus position as the move character, the not-stoppable dash character,
        // or a regular highlight, depending on whether the focus position can be stopped on,
        // moved through, or nothing at all. If nothing at all, don't render the moves up
        // to that point.
        if(stops.contains(mFocusPosition)) fillCoord(direction, mFocusPosition);
        else if(moves.contains(mFocusPosition)) fillCoord(unstoppable, mFocusPosition);
        else if(mFocusPosition.equals(mSelectedPosition)) renderSelection(mFocusPosition);
        else {
            skip = true;
            renderHighlight(mFocusPosition);
        }

        if(!skip) {
            for (Coord intervening : Coord.getInterveningSpaces(boardDimension, start, end)) {
                if (stops.contains(intervening)) fillCoord(direction, intervening);
                else if (moves.contains(intervening)) fillCoord(unstoppable, intervening);
            }
        }
    }

    private void fillCoords(TextCharacter character, Collection<Coord> coords) {
        for(Coord c : coords) {
            fillCoord(character, c);
        }
    }

    private void fillCoord(CharSequence string, TextColor fg, TextColor bg, EnumSet<SGR> sgrs, TextCharacter pad, Coord coord) {
        if(string.length() > mSpaceWidth) throw new IllegalArgumentException("Expecting wrapping? Who do you think I am?");

        TerminalPosition spaceLoc = getSpaceTopLeftForCoord(coord);
        int xStart = spaceLoc.getColumn() + mLeftPad;
        int yStart = spaceLoc.getRow();
        int xEnd = xStart + mSpaceWidth;
        int yEnd = yStart + mSpaceHeight;

        int yStringLoc = (yStart + yEnd) / 2;
        int xStringCenter = (xStart + xEnd) / 2;
        int xStringStart = xStringCenter - (string.length() / 2);
        int xStringEnd = xStringStart + string.length();

        // Prefer space at the end of the cell over space at the start
        if(xStringEnd == xEnd && xStringStart > xStart) {
            xStringStart--;
            xStringEnd--;
        }

        // Prefer space at the bottom of the cell over space at the top
        if(yStringLoc == yEnd - 1 && yStringLoc > yStart) {
            yStringLoc--;
        }

        for(int y = yStart; y < yEnd; y++) {
            for(int x = xStart; x < xEnd; x++) {
                if(y != yStringLoc) {
                    setCharacterAt(x, y, pad);
                }
                else {
                    if(x >= xStringStart && x < xStringEnd) {
                        int stringIndex = x - xStringStart;
                        char toPut = string.charAt(stringIndex);
                        TextCharacter textChar = new TextCharacter(toPut, fg, bg, sgrs);
                        setCharacterAt(x, y, textChar);
                    }
                    else {
                        setCharacterAt(x, y, pad);
                    }
                }
            }
        }
    }

    private void fillCoord(TextCharacter character, Coord coord) {
        TerminalPosition spaceLoc = getSpaceTopLeftForCoord(coord);
        int yStart = spaceLoc.getRow();
        int xStart = spaceLoc.getColumn() + mLeftPad;
        for(int y = yStart; y < yStart + mSpaceHeight; y++) {
            for(int x = xStart; x < xStart + mSpaceWidth; x++) {
                setCharacterAt(x, y, character);
            }
        }
    }

    private void renderSpecialSpaces(Rules rules) {
        TextCharacter star = new TextCharacter('*', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter circle = new TextCharacter('o', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter dot = new TextCharacter('.', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter dash = new TextCharacter('-', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        fillCoords(star, rules.getCornerSpaces());
        fillCoords(circle, rules.getCenterSpaces());
        fillCoords(dot, rules.getAttackerForts());
        fillCoords(dash, rules.getDefenderForts());
    }

    private void renderTaflmen(Board board) {
        TaflmanCoordMap taflmanMap  = board.getCachedTaflmanLocations();
        for(char taflman : taflmanMap.getTaflmen()) {
            if(taflman == Taflman.EMPTY) continue;

            Coord c = taflmanMap.getCoord(taflman);

            TextColor color = (Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS ? TerminalThemeConstants.RED : TerminalThemeConstants.WHITE);
            TextColor bg = TerminalThemeConstants.DKGRAY;

            TerminalPosition spaceLoc = getSpaceTopLeftForCoord(c);
            int yStart;
            if(mRowHeight <= 2) {
                yStart = spaceLoc.getRow();
            }
            else {
                yStart = spaceLoc.getRow() + 1;
            }
            int xStart = spaceLoc.getColumn() + mLeftPad;

            String symbol = Taflman.getStringSymbol(taflman, mSpaceWidth);
            for(int i = xStart; i < xStart + mSpaceWidth; i++) {
                if(i - xStart >= symbol.length()) break;
                setCharacterAt(i, yStart, new TextCharacter(symbol.charAt(i - xStart), color, bg, TerminalThemeConstants.NO_SGRS));
            }
        }
    }

    private TerminalPosition getSpaceTopLeftForCoord(Coord c) {

        int yStart = getSize().getRows() - ((c.y + 1) * mRowHeight);
        int xStart = c.x * mColWidth + 1;
        return new TerminalPosition(xStart, yStart);
    }

    private void focusPositionChanged() {
        // Render info about the taflmen under the cursor, or at the selected
        // location (if one exists)
        Coord location = mFocusPosition;
        if(mSelectedPosition != null) location = mSelectedPosition;

        List<Coord> moves = null;
        List<Coord> dests = null;
        List<Coord> captures = null;

        if(mCurrentState != null) {
            char taflman = mCurrentState.getBoard().getOccupier(location);

            if (taflman != Taflman.EMPTY) {
                moves = Taflman.getAllowableMoves(mCurrentState, taflman);
                dests = Taflman.getAllowableDestinations(mCurrentState, taflman);
                captures = Taflman.getCapturingMoves(mCurrentState, taflman);
            }
        }

        if(mCurrentBoard != null) rerender(null, moves, dests, captures);
        if(mCallback != null) mCallback.onFocusPositionChanged(mFocusPosition);
    }

    @Override
    public Interactable.Result handleKeyStroke(KeyStroke s) {
        Interactable.Result r = Interactable.Result.UNHANDLED;
        if(s.getKeyType() == KeyType.ArrowDown || (s.getKeyType() == KeyType.Character && s.getCharacter() == 's')) {
            mFocusPosition = mFocusPosition.offset(boardDimension, 0, -1);
            r = Interactable.Result.HANDLED;
        }
        else if(s.getKeyType() == KeyType.ArrowUp || (s.getKeyType() == KeyType.Character && s.getCharacter() == 'w')) {
            mFocusPosition = mFocusPosition.offset(boardDimension, 0, 1);
            r = Interactable.Result.HANDLED;
        }
        else if(s.getKeyType() == KeyType.ArrowRight || (s.getKeyType() == KeyType.Character && s.getCharacter() == 'd')) {
            mFocusPosition = mFocusPosition.offset(boardDimension, 1, 0);
            r = Interactable.Result.HANDLED;
        }
        else if(s.getKeyType() == KeyType.ArrowLeft || (s.getKeyType() == KeyType.Character && s.getCharacter() == 'a')) {
            mFocusPosition = mFocusPosition.offset(boardDimension, -1, 0);
            r = Interactable.Result.HANDLED;
        }
        else if(s.getKeyType() == KeyType.Character && s.getCharacter() == ' ' && mCurrentState != null) {
            // We can only select if the current state is not null. Otherwise, we're editing a board.
            if(mSelectedPosition == null) {
                char taflman = mCurrentState.getBoard().getOccupier(mFocusPosition);

                if(taflman != Taflman.EMPTY) {
                    mSelectedPosition = mFocusPosition;
                }
            }
            else if(mSelectedPosition.x != mFocusPosition.x && mSelectedPosition.y != mFocusPosition.y){
                mSelectedPosition = null;
            }
            else {
                // selected a space in line with another space

                // Temporary variable to keep the board image from trying to render mSelectedPosition after the
                // taflman has left (which happens before onMoveRequested returns)
                Coord startPosition = mSelectedPosition;
                mSelectedPosition = null;
                if(mCallback != null) mCallback.onMoveRequested(new MoveRecord(startPosition, mFocusPosition));
            }

            r = Interactable.Result.HANDLED;
        }
        else if(s.getKeyType() == KeyType.Escape) {
            mSelectedPosition = null;
            r = Interactable.Result.HANDLED;
        }

        if(r != Interactable.Result.UNHANDLED) {
            focusPositionChanged();
        }
        else {
            if(mCallback != null) mCallback.onUnhandledKey(s, mFocusPosition);
        }
        return r;
    }

    @Override
    public void notifyFocus(boolean focused) {
        mFocused = focused;
        if(focused) focusPositionChanged();
        else rerender(null, null, null, null);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }
}
