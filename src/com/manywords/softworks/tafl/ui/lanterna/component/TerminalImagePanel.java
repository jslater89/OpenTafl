package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextImage;
import com.googlecode.lanterna.gui2.AbstractInteractableComponent;
import com.googlecode.lanterna.gui2.InteractableRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalImagePanel extends AbstractInteractableComponent<TerminalImagePanel> {
    private final TextImage mImage;
    private final TerminalSize mMinimumSize;

    public TerminalImagePanel(TextImage image) {
        this(image, new TerminalSize(0, 0));
    }
    public TerminalImagePanel(TextImage image, TerminalSize minimumSize) {
        mImage = image;
        mMinimumSize = minimumSize;
    }

    public TextImage getImage() { return mImage; }
    public TerminalSize getMinimumSize() { return mMinimumSize; }

    @Override
    protected InteractableRenderer<TerminalImagePanel> createDefaultRenderer() {
        return new InteractableRenderer<TerminalImagePanel>() {
            @Override
            public TerminalPosition getCursorLocation(TerminalImagePanel terminalImagePanel) {
                return null;
            }

            @Override
            public TerminalSize getPreferredSize(TerminalImagePanel terminalImagePanel) {
                TerminalSize imageSize = terminalImagePanel.getImage().getSize();
                TerminalSize panelMinimum = terminalImagePanel.getMinimumSize();

                if(panelMinimum.getColumns() > imageSize.getColumns() || panelMinimum.getRows() > imageSize.getRows()) {
                    return panelMinimum;
                }
                else {
                    return imageSize;
                }
            }

            @Override
            public void drawComponent(TextGUIGraphics textGUIGraphics, TerminalImagePanel terminalImagePanel) {
                TerminalSize imageSize = terminalImagePanel.getImage().getSize();
                TerminalSize panelSize = terminalImagePanel.getSize();

                // Either start at 0,0 if panelSize is the same as imageSize, or start at half the difference, if the panel
                // is larger.
                int startColumn = (panelSize.getColumns() > imageSize.getColumns() ? (panelSize.getColumns() - imageSize.getColumns()) / 2 : 0);
                int startRow = (panelSize.getRows() > imageSize.getRows() ? (panelSize.getRows() - imageSize.getRows()) / 2 : 0);

                textGUIGraphics.drawImage(new TerminalPosition(startColumn, startRow), mImage);
            }
        };
    }
}
