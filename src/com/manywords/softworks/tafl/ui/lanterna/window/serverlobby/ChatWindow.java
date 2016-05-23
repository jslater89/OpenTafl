package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Panel;

/**
 * Created by jay on 5/23/16.
 */
public class ChatWindow extends BasicWindow {
    public ChatWindow() {
        super("Chat");

        Panel p = new Panel();

        setComponent(p);
    }
}
