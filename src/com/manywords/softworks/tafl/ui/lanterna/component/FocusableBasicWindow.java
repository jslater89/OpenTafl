package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.input.KeyStroke;
import com.manywords.softworks.tafl.ui.Ansi;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

/**
 * Created by jay on 4/13/17.
 */
public abstract class FocusableBasicWindow extends BasicWindow {
    private String mCanonicalTitle;
    private boolean mFocused;
    protected LogicalScreen.TerminalCallback mCallback;

    public FocusableBasicWindow(String title, LogicalScreen.TerminalCallback callback) {
        super(title);
        mCanonicalTitle = title;
        mCallback = callback;
    }

    public void notifyFocus(boolean focused) {
        if(focused) {
            mFocused = true;
            setTitle(Ansi.UNDERLINE + mCanonicalTitle.toUpperCase() + Ansi.UNDERLINE_OFF);
        }
        else {
            mFocused = false;
            setTitle(mCanonicalTitle);
        }
    }

    protected boolean isFocused() {
        return mFocused;
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        if(!isFocused()) return false;

        return mCallback.handleKeyStroke(key) || super.handleInput(key);
    }
}
