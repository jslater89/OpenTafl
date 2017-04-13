package com.manywords.softworks.tafl.ui.lanterna.window.mainmenu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.ServerLobbyScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.VariantEditorScreen;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

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

        Button variantButton = new Button("Variant editor", () -> mTerminalCallback.changeActiveScreen(new VariantEditorScreen()));
        p.addComponent(variantButton);

        Button playButton = new Button("Play", () -> TerminalUtils.startGame(getTextGUI(), mTerminalCallback));
        p.addComponent(playButton);

        Button networkButton = new Button("Join server", () -> mTerminalCallback.changeActiveScreen(new ServerLobbyScreen()));
        p.addComponent(networkButton);

        Button optionsButton = new Button("Options", () -> mTerminalCallback.onMenuNavigation(new OptionsMenuWindow(mTerminalCallback)));
        p.addComponent(optionsButton);

        Button loadGameButton = new Button("Load game", () -> {
            File gameFile = TerminalUtils.showFileChooserDialog(getTextGUI(), "Select saved game", "Open", new File("saved-games"));
            if(gameFile == null) {
                return;
            }

            GameSerializer.GameContainer g = null;
            try {
                g = GameSerializer.loadGameRecordFile(gameFile);
            }
            catch(NotationParseException e) {
                MessageDialogBuilder builder = new MessageDialogBuilder();
                builder.setTitle("Failed to load game record");
                builder.setText("Game record parsing failed at index: " + e.index + "\n" +
                        "With context: " + e.context);
                builder.addButton(MessageDialogButton.OK);
                builder.build().showDialog(getTextGUI());
                return;
            }
            ReplayGame rg = new ReplayGame(g.game, g.moves, g.variations);
            TerminalUtils.startSavedGame(rg, getTextGUI(), mTerminalCallback);

        });
        p.addComponent(loadGameButton);

        Button viewReplayButton = new Button("View replay", () -> {
            File gameFile = TerminalUtils.showFileChooserDialog(getTextGUI(), "Select saved replay", "Open", new File("saved-games/replays"));
            if(gameFile == null) {
                return;
            }

            GameSerializer.GameContainer g = null;
            try {
                g = GameSerializer.loadGameRecordFile(gameFile);
            }
            catch(NotationParseException e) {
                MessageDialogBuilder builder = new MessageDialogBuilder();
                builder.setTitle("Failed to load game record");
                builder.setText("Game record parsing failed at index: " + e.index + "\n" +
                        "With context: " + e.context);
                builder.addButton(MessageDialogButton.OK);
                builder.build().showDialog(getTextGUI());
                return;
            }

            ReplayGame rg = new ReplayGame(g.game, g.moves, g.variations);

            if(rg.getMode().isPuzzleMode()) {
                MessageDialogBuilder b = new MessageDialogBuilder();
                b.setTitle("Puzzle detected");
                b.setText("This replay contains a puzzle. Should OpenTafl load it as a puzzle?");
                b.addButton(MessageDialogButton.Yes);
                b.addButton(MessageDialogButton.No);
                MessageDialog d = b.build();
                MessageDialogButton result = d.showDialog(getTextGUI());

                if(result.equals(MessageDialogButton.No)) rg.setMode(ReplayGame.ReplayMode.REPLAY);
            }

            TerminalUtils.startReplay(rg, getTextGUI(), mTerminalCallback);

        });
        p.addComponent(viewReplayButton);

        Button loadNotationButton = new Button("Load notation", () -> {
            LoadNotationDialog d = new LoadNotationDialog(mTerminalCallback, null);
            d.setHints(TerminalThemeConstants.CENTERED_MODAL);
            d.showLoadNotationDialog(getTextGUI());
        });
        p.addComponent(loadNotationButton);

        Button downloadFromPTO = new Button("Extras", () -> mTerminalCallback.onMenuNavigation(new OtherOperationsWindow(mTerminalCallback)));
        p.addComponent(downloadFromPTO);

        Button quitButton = new Button("Quit", () -> mTerminalCallback.onMenuNavigation(null));
        p.addComponent(quitButton);

        /*
        TerminalBoardImage.init(11);
        TerminalBoardImage i = new TerminalBoardImage();
        TerminalImagePanel panel = new TerminalImagePanel(i);
        p.addComponent(panel);

        Game game = new Game(Copenhagen.newCopenhagen11(), null);
        i.renderState(game.getCurrentState(), null, null, null, null);
        */

        this.setComponent(p);
    }
}
