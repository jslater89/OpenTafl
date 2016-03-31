package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;

import java.io.File;

/**
 * Created by jay on 2/15/16.
 */
public class MainMenuWindow extends BasicWindow {
    private AdvancedTerminalHelper.TerminalCallback mTerminalCallback;
    public MainMenuWindow(AdvancedTerminalHelper.TerminalCallback callback) {
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

        Button playButton = new Button("Play", () -> TerminalUtils.startGame(getTextGUI(), mTerminalCallback));
        p.addComponent(playButton);

        Button optionsButton = new Button("Options", () -> mTerminalCallback.onMenuNavigation(new OptionsMenuWindow(mTerminalCallback)));
        p.addComponent(optionsButton);

        if(OpenTafl.DEV_MODE) {
            Button loadGameButton = new Button("Load replay", () -> {
                GameSerializer.GameContainer g = GameSerializer.loadGameRecordFile(new File("selfplay-results/gamelog.otg"));
                ReplayGame rg = new ReplayGame(g.game, g.moves);

            });
            p.addComponent(loadGameButton);
            Button tourneyButton = new Button("AI selfplay", () -> mTerminalCallback.onMenuNavigation(new SelfplayWindow(mTerminalCallback)));
            p.addComponent(tourneyButton);
        }

        Button quitButton = new Button("Quit", () -> mTerminalCallback.onMenuNavigation(null));
        p.addComponent(quitButton);

        /*
        TerminalBoardImage.init(11);
        TerminalBoardImage i = new TerminalBoardImage();
        TerminalImagePanel panel = new TerminalImagePanel(i);
        p.addComponent(panel);

        Game game = new Game(Copenhagen.newCopenhagen11(), null);
        i.renderBoard(game.getCurrentState(), null, null, null, null);
        */

        this.setComponent(p);
    }
}
