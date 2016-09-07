package com.manywords.softworks.tafl.ui.lanterna.window.mainmenu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.Utilities;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.screen.GameScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

/**
 * Created by jay on 8/27/16.
 */
public class LoadNotationDialog extends DialogWindow {
    private boolean mSuccess = true;

    public LoadNotationDialog(LogicalScreen.TerminalCallback terminalCallback, GameScreen gameScreen) {
        super("Load notation");

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        String explanation =
                "Use the 'paste' button below to enter text from the clipboard, then use the 'load' button to create " +
                        "a game.";

        explanation = TerminalUtils.linesToString(TerminalTextUtils.getWordWrappedText(60, explanation));
        Label explanationBox = new Label(explanation);
        mainPanel.addComponent(explanationBox);

        int width = 60;
        int height = 4;

        final TextBox box = new TextBox(new TerminalSize(width, height), TextBox.Style.MULTI_LINE);
        mainPanel.addComponent(box);

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button pasteButton = new Button("Paste", () -> {
            String clipboardText = Utilities.getFromClipboard();
            if(clipboardText != null && !clipboardText.isEmpty()) {
                box.setText(clipboardText);
            }
        });

        Button loadButton = new Button("Load", () -> {
            try {
                Rules r = RulesSerializer.loadRulesRecord(box.getText());
                if(r == null) {
                    mSuccess = false;
                    close();
                    return;
                }
                Game g = new Game(r, gameScreen);
                ReplayGame rg = ReplayGame.copyGameToReplay(g);

                if(gameScreen != null) {
                    terminalCallback.onEnteringGameScreen(g, r.getName());
                }
                else {
                    TerminalUtils.startSavedGame(rg, getTextGUI(), terminalCallback);
                }
            }
            catch(Exception e) {
                mSuccess = false;
                OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "Encountered exception loading notation");
                OpenTafl.logStackTrace(OpenTafl.LogLevel.NORMAL, e);
            }

            close();
        });

        Button cancelButton = new Button("Cancel", this::close);

        buttonPanel.addComponent(pasteButton);
        buttonPanel.addComponent(loadButton);
        buttonPanel.addComponent(cancelButton);

        mainPanel.addComponent(buttonPanel);

        setComponent(mainPanel);
    }

    public boolean showLoadNotationDialog(WindowBasedTextGUI textGUI) {
        super.showDialog(textGUI);
        if(!mSuccess) {
            MessageDialog.showMessageDialog(textGUI, "Failed to load notation", "Unable to read notation string.");
        }
        return mSuccess;
    }
}
