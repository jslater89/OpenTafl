package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalEnterTerminatedTextBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class CommandWindow extends BasicWindow {
    private AdvancedTerminalHelper.TerminalCallback mCallback;
    private TerminalEnterTerminatedTextBox mTextBox;

    private int mCommandBufferSize = 25;
    private int mCommandBufferPosition = 0;
    private List<String> mCommandBuffer = new ArrayList<>(mCommandBufferSize);

    public CommandWindow(AdvancedTerminalHelper.TerminalCallback callback) {
        super("Command");
        mCallback = callback;

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        Label l = new Label("Enter command (enter 'help' for list):");
        mTextBox = new TerminalEnterTerminatedTextBox(new TerminalEnterTerminatedTextBox.TextBoxCallback() {
            @Override
            public void onEnterPressed(String input) {
                addToBuffer(input);
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

        mCommandBuffer.add(0, "");
    }

    private void addToBuffer(String input) {
        mCommandBufferPosition = 0;
        if(mCommandBuffer.size() > 1 && mCommandBuffer.get(1).equals(input)) return;

        mCommandBuffer.add(1, input);

        while(mCommandBuffer.size() > mCommandBufferSize) {
            mCommandBuffer.remove(mCommandBufferSize);
        }
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        if(key.getKeyType() == KeyType.ArrowUp) {
            if(++mCommandBufferPosition >= mCommandBuffer.size()) {
                mCommandBufferPosition = mCommandBuffer.size() - 1;
            }
            mTextBox.setText(mCommandBuffer.get(mCommandBufferPosition));
            return true;
        }
        else if (key.getKeyType() == KeyType.ArrowDown) {
            if(--mCommandBufferPosition < 0) {
                mCommandBufferPosition = 0;
            }
            mTextBox.setText(mCommandBuffer.get(mCommandBufferPosition));
            return true;
        }
        return super.handleInput(key);
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        mTextBox.setPreferredSize(new TerminalSize(getSize().getColumns(), 1));
    }
}
