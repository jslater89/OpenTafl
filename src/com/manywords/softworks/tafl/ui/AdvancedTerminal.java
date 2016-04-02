package com.manywords.softworks.tafl.ui;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.ResizeListener;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.manywords.softworks.tafl.engine.*;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.command.Command;
import com.manywords.softworks.tafl.ui.command.CommandEngine;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.command.HumanCommandParser;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingMessageDialog;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.screen.MainMenuScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.UiScreen;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalTheme;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalWindowDecorationRenderer;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalWindowPostRenderer;
import com.manywords.softworks.tafl.ui.lanterna.window.*;
import com.manywords.softworks.tafl.ui.player.Player;
import com.manywords.softworks.tafl.ui.player.UiWorkerThread;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jay on 2/15/16.
 */
public class AdvancedTerminal<T extends Terminal> {

    private T mTerminal;

    private MultiWindowTextGUI mGui;
    private UiScreen mActiveScreen;

    private SelfplayWindow mSelfplayWindow;

    public AdvancedTerminal(T terminal) {
        super();
        mTerminal = terminal;

        if(terminal instanceof SwingTerminalFrame) {
            SwingTerminalFrame stf = (SwingTerminalFrame) terminal;
            stf.setTitle("OpenTafl");
            stf.setSize(1024, 768);
            stf.setResizable(true);
            stf.setLocationRelativeTo(null);
            stf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            stf.setVisible(true);
        }
        mTerminal.addResizeListener(new ResizeListener() {
            @Override
            public void onResized(Terminal terminal, TerminalSize terminalSize) {
                if(mActiveScreen != null) {
                    mActiveScreen.onResized(terminal, terminalSize);
                }
            }
        });

        Screen s = null;
        try {
            s = new TerminalScreen(mTerminal);
            s.startScreen();
        }
        catch(IOException e) {
            System.out.println("Failed to start");
            System.exit(0);
        }

        /* crashes bash?
        try {
            mTerminal.enterPrivateMode();
        } catch (IOException e) {
            // Best effort
        }
        */

        TerminalSettings.loadFromFile();

        mGui = new MultiWindowTextGUI(s, new DefaultWindowManager(new TerminalWindowDecorationRenderer()), new TerminalWindowPostRenderer(), new EmptySpace(TextColor.ANSI.BLACK));
        mGui.setTheme(new TerminalTheme());

        changeActiveScreen(new MainMenuScreen());
    }

    public void changeActiveScreen(UiScreen screen) {
        System.out.println("Terminal screen changing");
        if(mActiveScreen != null) {
            mActiveScreen.setInactive();
        }
        System.out.println("Deactivated old screen");
        System.out.println("Removed remaining windows");
        System.out.println("Starting new screen");

        mActiveScreen = screen;
        mActiveScreen.setSelfplayWindow(mSelfplayWindow);
        mActiveScreen.setActive(this, mGui);
    }

    public void setSelfplayWindow(Window tournamentWindow) {
        mSelfplayWindow = (SelfplayWindow) tournamentWindow;
        mActiveScreen.setSelfplayWindow(mSelfplayWindow);
    }
}
