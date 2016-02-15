package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.copenhagen.Copenhagen;
import com.manywords.softworks.tafl.ui.LanternaTerminal;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalImagePanel;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.sun.scenario.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class MainMenuWindow extends BasicWindow {
    private LanternaTerminal.TerminalCallback mTerminalCallback;
    public MainMenuWindow(LanternaTerminal.TerminalCallback callback) {
        mTerminalCallback = callback;

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());
        p.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center));

        Label l1 = new Label("OpenTafl");
        p.addComponent(l1);

        Label l2 = new Label("The old Norse board game,");
        p.addComponent(l2);

        Label l3 = new Label("In an old computer style.");
        p.addComponent(l3);

        EmptySpace e1 = new EmptySpace(new TerminalSize(0, 1));
        p.addComponent(e1);

        Button playButton = new Button("Play", new Runnable() {
            @Override
            public void run() {
                Game g = new Game(BuiltInVariants.availableRules.get(TerminalSettings.variant), mTerminalCallback.getUiCallback());
                TerminalBoardImage.init(g.getGameRules().getBoard().getBoardDimension());

                BoardWindow bw = new BoardWindow(BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant), g, mTerminalCallback);
                CommandWindow cw = new CommandWindow(g, mTerminalCallback);
                StatusWindow sw = new StatusWindow(g, mTerminalCallback);

                mTerminalCallback.onEnteringGame(bw, sw, cw);
            }
        });
        p.addComponent(playButton);

        Button optionsButton = new Button("Options", new Runnable() {
            @Override
            public void run() {
                mTerminalCallback.onMenuNavigation(new OptionsMenuWindow(mTerminalCallback));
            }
        });
        p.addComponent(optionsButton);

        Button quitButton = new Button("Quit", new Runnable() {
            @Override
            public void run() {
                mTerminalCallback.onMenuNavigation(null);
            }
        });
        p.addComponent(quitButton);

        TerminalBoardImage.init(11);
        TerminalBoardImage i = new TerminalBoardImage();
        TerminalImagePanel panel = new TerminalImagePanel(i);
        p.addComponent(panel);

        Game game = new Game(Copenhagen.newCopenhagen11(), null);
        i.renderBoard(game.getCurrentState(), null, null, null, null);

        this.setComponent(p);
    }
}
