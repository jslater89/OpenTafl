package com.manywords.softworks.tafl.ui.lanterna.window.mainmenu;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyType;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.playtaflonline.PlayTaflOnlineDownloader;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

import java.io.File;

/**
 * Created by jay on 2/10/17.
 */
public class OtherOperationsWindow extends BasicWindow {
    private LogicalScreen.TerminalCallback mTerminalCallback;
    public OtherOperationsWindow(LogicalScreen.TerminalCallback callback) {
        mTerminalCallback = callback;

        buildWindow();
    }

    private void buildWindow() {
        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        setTitle("Download PlayTaflOnline.com game");

        Label label = new Label("Enter PlayTaflOnline.com game number");
        p.addComponent(label);

        TextBox box = new TextBox();
        box.setInputFilter((interactable, keyStroke) -> {
            if(keyStroke.getKeyType() == KeyType.Backspace
                    || keyStroke.getKeyType() == KeyType.Delete
                    || keyStroke.getKeyType() == KeyType.ArrowDown
                    || keyStroke.getKeyType() == KeyType.ArrowUp
                    || keyStroke.getKeyType() == KeyType.ArrowLeft
                    || keyStroke.getKeyType() == KeyType.ArrowRight
                    || keyStroke.getKeyType() == KeyType.Enter) return true;
            else return Character.isDigit(keyStroke.getCharacter());
        });

        p.addComponent(box);

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        p.addComponent(buttonPanel);

        Button enter = new Button("Enter", () -> {
            int number = Integer.parseInt(box.getText());
            PlayTaflOnlineDownloader downloader = new PlayTaflOnlineDownloader(new PlayTaflOnlineDownloader.DownloadListener() {

                @Override
                public void onDownloadCompleted(File gameRecord) {
                    TerminalUtils.runOnUiThread(getTextGUI(), () -> {
                        MessageDialogBuilder builder = new MessageDialogBuilder();
                        builder.setTitle("Downloaded game");
                        builder.setText("Load game now?");
                        builder.addButton(MessageDialogButton.Yes);
                        builder.addButton(MessageDialogButton.No);
                        MessageDialogButton result = builder.build().showDialog(getTextGUI());

                        if(result.equals(MessageDialogButton.Yes)) {
                            try {
                                GameSerializer.GameContainer container = GameSerializer.loadGameRecordFile(gameRecord);
                                ReplayGame rg = new ReplayGame(container.game, container.moves, container.variations);
                                TerminalUtils.startReplay(rg, getTextGUI(), mTerminalCallback);
                            }
                            catch (NotationParseException e) {
                                builder = new MessageDialogBuilder();
                                builder.setTitle("Error loading game");
                                builder.setText("Game file is corrupt or incorrect.");
                                builder.addButton(MessageDialogButton.OK);
                                builder.build().showDialog(getTextGUI());
                                mTerminalCallback.onMenuNavigation(new MainMenuWindow(mTerminalCallback));
                            }
                        }
                        else {
                            mTerminalCallback.onMenuNavigation(new MainMenuWindow(mTerminalCallback));
                        }
                    });
                }

                @Override
                public void onDownloadFailed(Error code) {
                    String error = "Unknown error.";
                    switch(code) {
                        case BAD_URL:
                            error = "URL malformed.";
                            break;
                        case GAME_NOT_FOUND:
                            error = "Game does not exist.";
                            break;
                        case LOCAL_FILE_ERROR:
                            error = "Error writing saved game file.";
                            break;
                        case NETWORK_ERROR:
                            error = "Error downloading game record.";
                            break;
                        case PARSE_ERROR:
                            error = "Error parsing downloaded game record.";
                            break;
                    }

                    String finalError = error;
                    TerminalUtils.runOnUiThread(getTextGUI(), () -> {
                        MessageDialogBuilder builder = new MessageDialogBuilder();
                        builder.setTitle("Error")
                                .setText(finalError)
                                .addButton(MessageDialogButton.OK);
                        builder.build().showDialog(getTextGUI());
                    });
                }
            });

            downloader.downloadGameNumber(number);
        });

        buttonPanel.addComponent(enter);

        Button cancel = new Button("Cancel", () -> {
           mTerminalCallback.onMenuNavigation(new MainMenuWindow(mTerminalCallback));
        });
        buttonPanel.addComponent(cancel);

        setComponent(p);
    }
}
