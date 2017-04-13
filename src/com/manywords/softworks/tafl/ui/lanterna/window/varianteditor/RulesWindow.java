package com.manywords.softworks.tafl.ui.lanterna.window.varianteditor;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Panel;

/**
 * Created by jay on 4/13/17.
 */
public class RulesWindow extends BasicWindow {
    public RulesWindow() {
        super("Rules");
        buildWindow();
    }

    private void buildWindow() {
        Panel p = new Panel();
        setComponent(p);
    }
}
