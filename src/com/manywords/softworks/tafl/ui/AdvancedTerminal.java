package com.manywords.softworks.tafl.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.ui.lanterna.screen.MainMenuScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalTheme;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalWindowDecorationRenderer;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalWindowPostRenderer;
import com.manywords.softworks.tafl.ui.lanterna.window.selfplay.SelfplayWindow;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by jay on 2/15/16.
 */
public class AdvancedTerminal<T extends Terminal> {

    private T mTerminal;
    private boolean mRawMode;

    private MultiWindowTextGUI mGui;
    private LogicalScreen mActiveScreen;

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
            mRawMode = false;
        }
        else {
            // No terminal output for on-the-terminal mode
            OpenTafl.logLevel = OpenTafl.LogLevel.SILENT;
            mRawMode = true;
        }

        mTerminal.addResizeListener((resizedTerminal, terminalSize) -> {
            if(mActiveScreen != null) {
                mActiveScreen.onResized(resizedTerminal, terminalSize);
            }
        });

        Screen s = null;
        try {
            s = new TerminalScreen(mTerminal);
            s.startScreen();
        }
        catch(IOException e) {
            System.out.println("Failed to start: " + e);
            System.exit(0);
        }

        mGui = new MultiWindowTextGUI(s, new DefaultWindowManager(), new TerminalWindowPostRenderer(), new EmptySpace(TextColor.ANSI.BLACK));
        mGui.setTheme(new TerminalTheme(new TerminalWindowPostRenderer(), new TerminalWindowDecorationRenderer(), mRawMode));
        //mGui.setTheme(LanternaThemes.getRegisteredTheme(LanternaThemes.getRegisteredThemes().iterator().next()));

        // Blocks
        changeActiveScreen(new MainMenuScreen());


        try {
            terminal.exitPrivateMode();
        } catch (IOException e) {
            System.out.println("Failed to exit private mode.");
            System.exit(0);
        }
    }

    public void changeActiveScreen(LogicalScreen screen) {
        if(mActiveScreen != null) {
            mActiveScreen.setInactive();
        }

        mActiveScreen = screen;

        try {
            Thread.sleep(50);
        }
        catch(Exception e) {}

        mActiveScreen.setSelfplayWindow(mSelfplayWindow);
        mActiveScreen.setActive(this, mGui);
    }

    public void setSelfplayWindow(Window tournamentWindow) {
        mSelfplayWindow = (SelfplayWindow) tournamentWindow;
        mActiveScreen.setSelfplayWindow(mSelfplayWindow);
    }
}
