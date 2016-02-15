package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextImage;
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalImagePanel extends AbstractComponent<TerminalImagePanel> {
    private final TextImage mImage;

    public TerminalImagePanel(TextImage image) {
        mImage = image;
    }

    public TextImage getImage() { return mImage; }

    @Override
    protected ComponentRenderer<TerminalImagePanel> createDefaultRenderer() {
        return new ComponentRenderer<TerminalImagePanel>() {
            @Override
            public TerminalSize getPreferredSize(TerminalImagePanel terminalImagePanel) {
                return terminalImagePanel.getImage().getSize();
            }

            @Override
            public void drawComponent(TextGUIGraphics textGUIGraphics, TerminalImagePanel terminalImagePanel) {
                textGUIGraphics.drawImage(new TerminalPosition(0, 0), mImage);
            }
        };
    }
}
