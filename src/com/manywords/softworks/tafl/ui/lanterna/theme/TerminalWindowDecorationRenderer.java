package com.manywords.softworks.tafl.ui.lanterna.theme;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.*;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalWindowDecorationRenderer extends DefaultWindowDecorationRenderer {

    public TextGUIGraphics draw(TextGUI textGUI, TextGUIGraphics graphics, Window window) {
        String title = window.getTitle();
        if(title == null) {
            title = "";
        }

        ThemeDefinition themeDefinition = window.getComponent().getThemeDefinition();
        char horizontalLine = themeDefinition.getCharacter("HORIZONTAL_LINE", '─');
        char verticalLine = themeDefinition.getCharacter("VERTICAL_LINE", '│');
        char bottomLeftCorner = themeDefinition.getCharacter("BOTTOM_LEFT_CORNER", '└');
        char topLeftCorner = themeDefinition.getCharacter("TOP_LEFT_CORNER", '┌');
        char bottomRightCorner = themeDefinition.getCharacter("BOTTOM_RIGHT_CORNER", '┘');
        char topRightCorner = themeDefinition.getCharacter("TOP_RIGHT_CORNER", '┐');
        TerminalSize drawableArea = graphics.getSize();
        graphics.applyThemeStyle(themeDefinition.getNormal());

        graphics.drawLine(new TerminalPosition(0, drawableArea.getRows() - 2), new TerminalPosition(0, 1), verticalLine);
        graphics.drawLine(new TerminalPosition(1, 0), new TerminalPosition(drawableArea.getColumns() - 2, 0), horizontalLine);
        graphics.setCharacter(0, 0, topLeftCorner);
        graphics.setCharacter(0, drawableArea.getRows() - 1, bottomLeftCorner);
        graphics.drawLine(new TerminalPosition(drawableArea.getColumns() - 1, 1), new TerminalPosition(drawableArea.getColumns() - 1, drawableArea.getRows() - 2), verticalLine);
        graphics.drawLine(new TerminalPosition(1, drawableArea.getRows() - 1), new TerminalPosition(drawableArea.getColumns() - 2, drawableArea.getRows() - 1), horizontalLine);
        graphics.setCharacter(drawableArea.getColumns() - 1, 0, topRightCorner);
        graphics.setCharacter(drawableArea.getColumns() - 1, drawableArea.getRows() - 1, bottomRightCorner);
        if(!title.isEmpty()) {
            graphics.putString(2, 0, TerminalTextUtils.fitString(title, drawableArea.getColumns() - 3));
        }

        return graphics.newTextGraphics(new TerminalPosition(1, 1), graphics.getSize().withRelativeColumns(-2).withRelativeRows(-2));
    }

    public TerminalSize getDecoratedSize(Window window, TerminalSize contentAreaSize) {
        return contentAreaSize.withRelativeColumns(2).withRelativeRows(2).max(new TerminalSize(TerminalTextUtils.getColumnWidth(window.getTitle()) + 4, 1));
    }
}
