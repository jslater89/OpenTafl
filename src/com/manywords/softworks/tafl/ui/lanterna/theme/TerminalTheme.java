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
    private final ThemeDefinition definition;

    public TerminalTheme(WindowPostRenderer postRenderer, WindowDecorationRenderer decorationRenderer) {
        this(postRenderer, decorationRenderer, false);
    }

    public TerminalTheme(WindowPostRenderer postRenderer, WindowDecorationRenderer decorationRenderer, boolean rawMode) {
        super(postRenderer, decorationRenderer);
        definition = new TerminalThemeDefinition(rawMode);
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
