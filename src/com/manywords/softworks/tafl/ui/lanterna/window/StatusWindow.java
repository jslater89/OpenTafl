package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingLabel;

/**
 * Created by jay on 2/15/16.
 */
public class StatusWindow extends BasicWindow {
    private ScrollingLabel mTextDisplay;
    private Label mClockDisplay;
    public StatusWindow(Game g, AdvancedTerminal.TerminalCallback callback) {
        super("Information");

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        Label textLabel = new Label("Status");
        textLabel.addStyle(SGR.UNDERLINE);
        mTextDisplay = new ScrollingLabel("");
        Label clockLabel = new Label("Clock");
        clockLabel.addStyle(SGR.UNDERLINE);
        mClockDisplay = new Label("");

        p.addComponent(textLabel);
        p.addComponent(mTextDisplay);
        p.addComponent(clockLabel);
        p.addComponent(mClockDisplay);

        setComponent(p);
    }

    public void addStatus(String text) {
        mTextDisplay.addLine(text);
    }


    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        if(mTextDisplay != null) {
            mTextDisplay.setLabelWidth(this.getSize().getColumns());
            mTextDisplay.setSize(new TerminalSize(getSize().getColumns(), getSize().getRows() - 4));
            mTextDisplay.setPreferredSize(mTextDisplay.getSize());
        }
    }
}