package com.manywords.softworks.tafl.ui.lanterna.theme;

import com.googlecode.lanterna.graphics.AbstractTheme;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.WindowDecorationRenderer;
import com.googlecode.lanterna.gui2.WindowPostRenderer;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalTheme extends AbstractTheme {
    private final ThemeDefinition definition = new TerminalThemeDefinition();

    public TerminalTheme(WindowPostRenderer postRenderer, WindowDecorationRenderer decorationRenderer) {
        super(postRenderer, decorationRenderer);
    }

    @Override
    public ThemeDefinition getDefaultDefinition() {
        return definition;
    }

    @Override
    public ThemeDefinition getDefinition(Class<?> aClass) {
        return definition;
    }
}
