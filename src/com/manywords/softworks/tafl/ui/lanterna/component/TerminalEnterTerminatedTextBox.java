package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalEnterTerminatedTextBox extends TextBox {
    public interface TextBoxCallback {
        public void onEnterPressed(String input);
        public void onPageKeyPressed(KeyStroke key);
    }

    private TextBoxCallback mCallback;

    public TerminalEnterTerminatedTextBox(TextBoxCallback c) {
        mCallback = c;
    }

    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        if(keyStroke.getKeyType() == KeyType.Enter) {
            mCallback.onEnterPressed(getText());
            setText("");
            return Result.HANDLED;
        }
        else if(keyStroke.getKeyType() == KeyType.PageUp || keyStroke.getKeyType() == KeyType.PageDown) {
            mCallback.onPageKeyPressed(keyStroke);
            return Result.HANDLED;
        }
        else {
            return super.handleKeyStroke(keyStroke);
        }
    }
}
