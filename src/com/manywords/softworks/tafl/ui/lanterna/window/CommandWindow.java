package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalEnterTerminatedTextBox;

/**
 * Created by jay on 2/15/16.
 */
public class CommandWindow extends BasicWindow {
    private AdvancedTerminalHelper.TerminalCallback mCallback;
    private TerminalEnterTerminatedTextBox mTextBox;
    public CommandWindow(Game g, AdvancedTerminalHelper.TerminalCallback callback) {
        super("Command");
        mCallback = callback;

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        Label l = new Label("Enter command (enter 'help' for list):");
        mTextBox = new TerminalEnterTerminatedTextBox(new TerminalEnterTerminatedTextBox.TextBoxCallback() {
            @Override
            public void onEnterPressed(String input) {
                mCallback.handleInGameCommand(input);
            }

            @Override
            public void onPageKeyPressed(KeyStroke key) {
                mCallback.handleKeyStroke(key);
            }
        });


        p.addComponent(l);
        p.addComponent(mTextBox);

        setComponent(p);
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        mTextBox.setPreferredSize(new TerminalSize(getSize().getColumns(), 1));
    }
}
