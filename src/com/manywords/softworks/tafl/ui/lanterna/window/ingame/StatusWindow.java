package com.manywords.softworks.tafl.ui.lanterna.window.ingame;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingLabel;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

/**
 * Created by jay on 2/15/16.
 */
public class StatusWindow extends BasicWindow {
    private ScrollingLabel mTextDisplay;
    private Label mAttackerClockDisplay, mDefenderClockDisplay;
    public StatusWindow(LogicalScreen.TerminalCallback callback) {
        super("Information");

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        Label textLabel = new Label("Status");
        textLabel.addStyle(SGR.UNDERLINE);

        mTextDisplay = new ScrollingLabel("");

        Label clockLabel = new Label("Clock");
        clockLabel.addStyle(SGR.UNDERLINE);

        mAttackerClockDisplay = new Label("");
        mDefenderClockDisplay = new Label("");

        Panel clockPanel = new Panel();
        clockPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        clockPanel.addComponent(mAttackerClockDisplay);
        clockPanel.addComponent(mDefenderClockDisplay);

        p.addComponent(textLabel);
        p.addComponent(mTextDisplay);
        p.addComponent(clockLabel);
        p.addComponent(clockPanel);

        setComponent(p);
    }

    public void addStatus(String text) {
        Log.println(Log.Level.VERBOSE, "Got status line: " + text);
        mTextDisplay.addLine(text);
    }

    public void handleTimeUpdate(boolean attackingSide, TimeSpec attackerEntry, TimeSpec defenderEntry) {
        if(attackerEntry != null) {
            String attackerString = (attackingSide ? "ATTACKER" : "Attacker") + "\n" + attackerEntry.toHumanString();
            mAttackerClockDisplay.setText(attackerString);
        }

        if(defenderEntry != null) {
            String defenderString = (attackingSide ? "Defender" : "DEFENDER") + "\n" + defenderEntry.toHumanString();
            mDefenderClockDisplay.setText(defenderString);
        }
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        if(key.getKeyType() == KeyType.PageUp) {
            mTextDisplay.handleScroll(true, true);
        }
        else if(key.getKeyType() == KeyType.PageDown) {
            mTextDisplay.handleScroll(true, false);
        }
        return super.handleInput(key);
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        if(mTextDisplay != null) {
            mTextDisplay.setLabelWidth(this.getSize().getColumns());
            if(getSize().getRows() > 12) {
                mTextDisplay.setSize(new TerminalSize(getSize().getColumns(), getSize().getRows() - 6));
            }
            else {
                mTextDisplay.setSize(new TerminalSize(getSize().getColumns(), getSize().getRows() - 3));
            }
            mTextDisplay.setPreferredSize(mTextDisplay.getSize());
        }
    }
}
