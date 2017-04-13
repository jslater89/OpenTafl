package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.gui2.BasicWindow;

/**
 * Created by jay on 4/13/17.
 */
public abstract class FocusableBasicWindow extends BasicWindow {
    public FocusableBasicWindow(String title) {
        super(title);
    }

    public abstract void notifyFocus(boolean focused);
}
