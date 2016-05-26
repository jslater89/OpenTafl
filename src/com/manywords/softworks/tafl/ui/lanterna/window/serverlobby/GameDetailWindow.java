package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.MainMenuScreen;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

/**
 * Created by jay on 5/23/16.
 */
public class GameDetailWindow extends BasicWindow {
    LogicalScreen.TerminalCallback mCallback;
    private Panel mButtonPanel;

    public GameDetailWindow(LogicalScreen.TerminalCallback terminalCallback) {
        super("Game Details");

        mCallback = terminalCallback;

        Panel p = new Panel();

        mButtonPanel = new Panel();
        mButtonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button gameCreationButton = new Button("Create game", () -> {
            CreateGameDialog d = new CreateGameDialog("Create game");
            d.setHints(TerminalThemeConstants.CENTERED_MODAL);
            d.showDialog(getTextGUI());

            if(!d.canceled) {
                System.out.println(d.rules.getName());
                System.out.println(d.rules.getOTRString());

                System.out.println(d.attackingSide);
                System.out.println(d.hashedPassword);
            }
            else {
                System.out.println("Canceled");
            }

            getTextGUI().setActiveWindow(this);
        });

        Button exitButton = new Button("Leave server", () -> {
            mCallback.changeActiveScreen(new MainMenuScreen());
        });
        mButtonPanel.addComponent(gameCreationButton);
        mButtonPanel.addComponent(exitButton);

        p.addComponent(mButtonPanel);


        setComponent(p);
    }

    public void notifyFocus(boolean focused) {
        if(focused) {
            setTitle("**GAME DETAILS**");
        }
        else {
            setTitle("Game Details");
        }
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        mButtonPanel.setPreferredSize(new TerminalSize(size.getColumns(), 3));
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handledByScreen = mCallback.handleKeyStroke(key);
        return handledByScreen || super.handleInput(key);
    }
}
