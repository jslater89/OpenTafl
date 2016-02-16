package com.manywords.softworks.tafl.ui.lanterna.theme;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Window;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalThemeConstants {
    public static final TextColor DKGRAY = new TextColor.RGB(0x05, 0x05, 0x05);
    public static final TextColor LTGRAY = new TextColor.RGB(0xcc, 0xcc, 0xcc);
    public static final TextColor WHITE = new TextColor.RGB(0xff, 0xff, 0xff);
    public static final TextColor BLUE = TextColor.ANSI.BLUE;
    public static final TextColor RED = TextColor.ANSI.RED;
    public static final TextColor GREEN = TextColor.ANSI.GREEN;
    public static final TextColor YELLOW = TextColor.ANSI.YELLOW;

    public static final EnumSet<SGR> NO_SGRS = EnumSet.noneOf(SGR.class);

    public static final ArrayList<Window.Hint> CENTERED = new ArrayList<Window.Hint>();
    static {
        CENTERED.add(Window.Hint.CENTERED);
    }

    public static final ArrayList<Window.Hint> FULL_SCREEN = new ArrayList<Window.Hint>();
    static {
        FULL_SCREEN.add(Window.Hint.FULL_SCREEN);
    }

    public static final ArrayList<Window.Hint> FIXED_SIZE = new ArrayList<Window.Hint>();
    static {
        FIXED_SIZE.add(Window.Hint.FIXED_SIZE);
    }

    public static final ArrayList<Window.Hint> BOARD_WINDOW = new ArrayList<Window.Hint>();
    static {
        BOARD_WINDOW.add(Window.Hint.FIXED_POSITION);
        BOARD_WINDOW.add(Window.Hint.NO_FOCUS);
    }

    public static final ArrayList<Window.Hint> STATUS_WINDOW = new ArrayList<Window.Hint>();
    static {
        STATUS_WINDOW.add(Window.Hint.FIXED_POSITION);
        STATUS_WINDOW.add(Window.Hint.FIXED_SIZE);
    }

    public static final ArrayList<Window.Hint> COMMAND_WINDOW = new ArrayList<Window.Hint>();
    static {
        COMMAND_WINDOW.add(Window.Hint.FIXED_POSITION);
        COMMAND_WINDOW.add(Window.Hint.FIXED_SIZE);
    }
}
