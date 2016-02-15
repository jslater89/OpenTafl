package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.collections.TaflmanCoordMap;
import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

import java.util.List;
import java.util.Set;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalBoardImage extends BasicTextImage {
    private static int boardDimension;
    private static int rowHeight = 3;
    private static int colWidth = 5;
    private static int spaceHeight = rowHeight - 1;
    private static int spaceWidth = colWidth - 1;
    public static void init(int dimension) {
        boardDimension = dimension;
    }

    public TerminalBoardImage() {
        this(null);
    }

    public TerminalBoardImage(GameState state) {
        // +--- per row, + extra
        // |
        // |
        //  per column, + extra
        super(boardDimension * colWidth + 1, boardDimension * rowHeight + 1);

        renderBoardBackground();
        if(state != null) {
            renderBoard(state, null, null, null, null);
        }
    }

    public void renderBoard(GameState state, Coord highlight, List<Coord> allowableDestinations, List<Coord> allowableMoves, List<Coord> captureSpaces) {
        if(allowableDestinations != null) renderAllowableDestinations(allowableDestinations);
        if(allowableMoves != null) renderAllowableMoves(allowableMoves);
        if(captureSpaces != null) renderCapturingMoves(captureSpaces);
        if(highlight != null) renderHighlight(highlight);

        renderSpecialSpaces(state.getBoard().getRules());
        renderTaflmen(state.getBoard());
    }

    private void renderBoardBackground() {
        TextCharacter plus = new TextCharacter('+', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter dash = new TextCharacter('-', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter pipe = new TextCharacter('|', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        TextCharacter space = new TextCharacter(' ', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);

        for (int row = 0; row < boardDimension; row++) {
            int yTop = row * rowHeight;
            int yBottom = row * rowHeight + rowHeight;
            int rowLabelIdx = ((yTop + yBottom) / 2);

            for (int col = 0; col < boardDimension; col++) {
                int xLeft = col * colWidth;
                int xRight = col * colWidth + colWidth;
                int colLabelIdx = ((xLeft + xRight) / 2);

                for (int y = yTop; y < yBottom + 1; y++) {
                    for (int x = xLeft; x < xRight + 1; x++) {
                        // Draw the top or bottom of a space
                        if (y % rowHeight == 0) {
                            if (y == 0 && x == colLabelIdx) {
                                String columnLabel = "" + (col + 1);
                                if (columnLabel.length() == 1) {
                                    setCharacterAt(x, y, new TextCharacter(columnLabel.charAt(0)));
                                }
                                else {
                                    setCharacterAt(x++, y, new TextCharacter(columnLabel.charAt(0)));
                                    setCharacterAt(x, y, new TextCharacter(columnLabel.charAt(1)));
                                }
                            }
                            else if (x % colWidth == 0) setCharacterAt(x, y, plus);
                            else setCharacterAt(x, y, dash);
                        }
                        // Draw the middle of a space
                        else {
                            if (x == 0 && y == rowLabelIdx) { setCharacterAt(x, y, new TextCharacter((char) ('a' + row))); }
                            else if (x % colWidth == 0) setCharacterAt(x, y, pipe);
                            else setCharacterAt(x, y, space);
                        }
                    }
                }
            }
        }
    }

    private void renderAllowableDestinations(List<Coord> coords) {

    }

    private void renderAllowableMoves(List<Coord> coords) {

    }

    private void renderCapturingMoves(List<Coord> coords) {

    }

    private void renderHighlight(Coord highlight) {

    }

    private void renderSpecialSpaces(Rules rules) {
        TextCharacter star = new TextCharacter('*', TerminalThemeConstants.BLUE, TerminalThemeConstants.DKGRAY, TerminalThemeConstants.NO_SGRS);
        Set<Coord> specialSpaces = rules.getCornerSpaces();
        specialSpaces.addAll(rules.getCenterSpaces());
        specialSpaces.addAll(rules.getAttackerForts());
        specialSpaces.addAll(rules.getDefenderForts());

        for(Coord c : specialSpaces) {
            TerminalPosition spaceLoc = getSpaceTopLeftForCoord(c);
            int yStart = spaceLoc.getRow();
            int xStart = spaceLoc.getColumn();
            for(int y = yStart; y < yStart + spaceHeight; y++) {
                for(int x = xStart; x < xStart + spaceWidth; x++) {
                    setCharacterAt(x, y, star);
                }
            }

        }
    }

    private void renderTaflmen(Board board) {
        TaflmanCoordMap taflmanMap  = board.getCachedTaflmanLocations();
        for(char taflman : taflmanMap.getTaflmen()) {
            Coord c = taflmanMap.get(taflman);

            TextColor color = (Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS ? TerminalThemeConstants.RED : TerminalThemeConstants.WHITE);
            TextColor bg = TerminalThemeConstants.DKGRAY;

            TerminalPosition spaceLoc = getSpaceTopLeftForCoord(c);
            int yStart = spaceLoc.getRow() + 1;
            int xStart = spaceLoc.getColumn();

            String symbol = Taflman.getStringSymbol(taflman, spaceWidth);
            for(int i = xStart; i < xStart + spaceWidth; i++) {
                if(i - xStart >= symbol.length()) break;
                setCharacterAt(i, yStart, new TextCharacter(symbol.charAt(i - xStart), color, bg, TerminalThemeConstants.NO_SGRS));
            }
        }
    }

    private TerminalPosition getSpaceTopLeftForCoord(Coord c) {
        int yStart = c.y * rowHeight + 1;
        int xStart = c.x * colWidth + 1;
        return new TerminalPosition(xStart, yStart);
    }
}
