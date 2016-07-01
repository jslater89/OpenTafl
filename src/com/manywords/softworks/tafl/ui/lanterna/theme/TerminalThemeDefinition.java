package com.manywords.softworks.tafl.ui.lanterna.theme;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.AbstractTheme;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.graphics.ThemeStyle;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.ComponentRenderer;

import java.util.EnumSet;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalThemeDefinition implements ThemeDefinition {
    @Override
    public ThemeStyle getNormal() {
        return new ThemeStyle() {
            @Override
            public TextColor getForeground() {
                return TerminalThemeConstants.LTGRAY;
            }

            @Override
            public TextColor getBackground() {
                return TerminalThemeConstants.DKGRAY;
            }

            @Override
            public EnumSet<SGR> getSGRs() {
                return EnumSet.noneOf(SGR.class);
            }
        };
    }

    @Override
    public ThemeStyle getPreLight() {
        return new ThemeStyle() {
            @Override
            public TextColor getForeground() {
                return TerminalThemeConstants.LTGRAY;
            }

            @Override
            public TextColor getBackground() {
                return TerminalThemeConstants.DKGRAY;
            }

            @Override
            public EnumSet<SGR> getSGRs() {
                return EnumSet.noneOf(SGR.class);
            }
        };
    }

    @Override
    public ThemeStyle getSelected() {
        return new ThemeStyle() {
            @Override
            public TextColor getForeground() {
                return TerminalThemeConstants.WHITE;
            }

            @Override
            public TextColor getBackground() {
                return TerminalThemeConstants.GRAY;
            }

            @Override
            public EnumSet<SGR> getSGRs() {
                return EnumSet.noneOf(SGR.class);
            }
        };
    }

    @Override
    public ThemeStyle getActive() {
        return new ThemeStyle() {
            @Override
            public TextColor getForeground() {
                return TerminalThemeConstants.WHITE;
            }

            @Override
            public TextColor getBackground() {
                return TerminalThemeConstants.GRAY;
            }

            @Override
            public EnumSet<SGR> getSGRs() {
                return EnumSet.noneOf(SGR.class);
            }
        };
    }

    @Override
    public ThemeStyle getInsensitive() {
        return new ThemeStyle() {
            @Override
            public TextColor getForeground() {
                return TerminalThemeConstants.WHITE;
            }

            @Override
            public TextColor getBackground() {
                return TerminalThemeConstants.DKGRAY;
            }

            @Override
            public EnumSet<SGR> getSGRs() {
                return EnumSet.noneOf(SGR.class);
            }
        };
    }

    @Override
    public ThemeStyle getCustom(String s) {
        return new ThemeStyle() {
            @Override
            public TextColor getForeground() {
                return TerminalThemeConstants.LTGRAY;
            }

            @Override
            public TextColor getBackground() {
                return TerminalThemeConstants.DKGRAY;
            }

            @Override
            public EnumSet<SGR> getSGRs() {
                return EnumSet.noneOf(SGR.class);
            }
        };
    }

    @Override
    public ThemeStyle getCustom(String s, ThemeStyle themeStyle) {
        return getCustom(s);
    }

    @Override
    public boolean getBooleanProperty(String s, boolean b) {
        return false;
    }

    @Override
    public boolean isCursorVisible() {
        return false;
    }

    @Override
    public char getCharacter(String s, char c) {
        return c;
    }

    @Override
    public <T extends Component> ComponentRenderer<T> getRenderer(Class<T> type) {
        return null;
    }

}
