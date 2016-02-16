package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.gui2.*;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalEnterTerminatedTextBox;

/**
 * Created by jay on 2/15/16.
 */
public class CommandWindow extends BasicWindow {
    private AdvancedTerminal.TerminalCallback mCallback;
    public CommandWindow(Game g, AdvancedTerminal.TerminalCallback callback) {
        super("Command");
        mCallback = callback;

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        Label l = new Label("Enter command (...)");
        TerminalEnterTerminatedTextBox tb = new TerminalEnterTerminatedTextBox(new TerminalEnterTerminatedTextBox.TextBoxCallback() {
            @Override
            public void onEnterPressed(String input) {
                mCallback.handleInGameCommand(input);
            }
        });

        p.addComponent(l);
        p.addComponent(tb);

        setComponent(p);
    }
}
