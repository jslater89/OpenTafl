package com.manywords.softworks.tafl.ui.lanterna.window.varianteditor;

import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.Panel;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.component.FocusableBasicWindow;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingLabel;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

/**
 * Created by jay on 4/13/17.
 */
public class RulesWindow extends FocusableBasicWindow {
    public RulesWindow(LogicalScreen.TerminalCallback callback) {
        super("Rules", callback);
        buildWindow();
    }

    private ScrollingLabel mLabel = new ScrollingLabel();

    private void buildWindow() {
        Panel p = new Panel();

        p.addComponent(mLabel);

        setComponent(p);
    }

    public void setLabel(String text) {
        if(getSize() != null)
            mLabel.setText(TerminalUtils.linesToString(TerminalTextUtils.getWordWrappedText(getSize().getColumns(), text)));
    }
}
