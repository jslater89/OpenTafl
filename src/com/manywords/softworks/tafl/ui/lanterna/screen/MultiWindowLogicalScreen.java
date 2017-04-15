package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;

/**
 * Created by jay on 4/13/17.
 */
public abstract class MultiWindowLogicalScreen extends LogicalScreen {
    protected abstract void enterUi();
    protected abstract void leaveUi();
    protected abstract void addWindows();
    protected abstract void removeWindows();
    protected abstract void layoutWindows(TerminalSize size);

    public void setActive(AdvancedTerminal t, WindowBasedTextGUI gui, TerminalCallback callback) {
        super.setActive(t, gui);
        mTerminalCallback = callback;
        enterUi();
    }

    @Override
    public void onResized(Terminal terminal, TerminalSize terminalSize) {
        layoutWindows(terminalSize);
    }

    @Override
    public void setInactive() {
        super.setInactive();
        leaveUi();
    }
}
