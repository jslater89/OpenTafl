package com.manywords.softworks.tafl.ui.lanterna.window.mainmenu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.ServerLobbyScreen;
import com.manywords.softworks.tafl.ui.lanterna.window.selfplay.SelfplayWindow;

import java.io.File;

/**
 * Created by jay on 2/15/16.
 */
public class MainMenuWindow extends BasicWindow {
    private LogicalScreen.TerminalCallback mTerminalCallback;
    public MainMenuWindow(LogicalScreen.TerminalCallback callback) {
        mTerminalCallback = callback;

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());
        p.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center));

        Label l1 = new Label("OpenTafl " + OpenTafl.CURRENT_VERSION);
        p.addComponent(l1);

        Label l2 = new Label("The old Norse board game,");
        p.addComponent(l2);

        Label l3 = new Label("In an old computer style.");
        p.addComponent(l3);

        EmptySpace e1 = new EmptySpace(new TerminalSize(0, 1));
        p.addComponent(e1);

        Button playButton = new Button("Play", () -> TerminalUtils.startGame(getTextGUI(), mTerminalCallback));
        p.addComponent(playButton);

        Button networkButton = new Button("Join server", () -> mTerminalCallback.changeActiveScreen(new ServerLobbyScreen("localhost", 11541)));
        p.addComponent(networkButton);

        Button optionsButton = new Button("Options", () -> mTerminalCallback.onMenuNavigation(new OptionsMenuWindow(mTerminalCallback)));
        p.addComponent(optionsButton);

        Button loadGameButton = new Button("Load game", () -> {
            File gameFile = TerminalUtils.showFileChooserDialog(getTextGUI(), "Select saved game", "Open", new File("saved-games"));
            if(gameFile == null) {
                return;
            }

            GameSerializer.GameContainer g = GameSerializer.loadGameRecordFile(gameFile);
            ReplayGame rg = new ReplayGame(g.game, g.moves);
            TerminalUtils.startSavedGame(rg, getTextGUI(), mTerminalCallback);

        });
        p.addComponent(loadGameButton);

        Button viewReplayButton = new Button("View replay", () -> {
            File gameFile = TerminalUtils.showFileChooserDialog(getTextGUI(), "Select saved replay", "Open", new File("saved-games/replays"));
            if(gameFile == null) {
                return;
            }

            GameSerializer.GameContainer g = GameSerializer.loadGameRecordFile(gameFile);
            ReplayGame rg = new ReplayGame(g.game, g.moves);
            TerminalUtils.startReplay(rg, getTextGUI(), mTerminalCallback);

        });
        p.addComponent(viewReplayButton);

        if(OpenTafl.DEV_MODE) {
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
