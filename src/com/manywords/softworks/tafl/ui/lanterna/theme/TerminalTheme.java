package com.manywords.softworks.tafl.ui.lanterna.theme;

import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.graphics.ThemeDefinition;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalTheme implements Theme {
    private final ThemeDefinition definition = new TerminalThemeDefinition();
    @Override
    public ThemeDefinition getDefaultDefinition() {
        return definition;
    }

    @Override
    public ThemeDefinition getDefinition(Class<?> aClass) {
        return definition;
    }
}
