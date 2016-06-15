package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;

import java.util.regex.Pattern;

/**
 * Created by jay on 5/23/16.
 */
public class ServerLoginDialog extends DialogWindow {
    public boolean canceled = false;
    public String username = "";
    public String hashedPassword = "";

    public TextBox mUsernameInput, mPasswordInput;
    Panel mMainPanel;

    public ServerLoginDialog(String title) {
        super(title);

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        Label serverLabel = new Label(TerminalSettings.onlineServerHost + ":" + TerminalSettings.onlineServerPort);

        final Label usernameLabel = new Label("Username");
        mUsernameInput = new TextBox();
        mUsernameInput.setValidationPattern(Pattern.compile("([[a-z][A-Z][0-9]\\-_\\.\\*])+"));

        final Label passwordLabel = new Label("Password");
        mPasswordInput = new TextBox();
        mPasswordInput.setMask('*');
        mPasswordInput.setValidationPattern(Pattern.compile("([[a-z][A-Z][0-9]\\-_\\.\\*])+"));

        final Button finishButton = new Button("Login", () -> {
            username = mUsernameInput.getText();
            hashedPassword = PasswordHasher.hashPassword(username, mPasswordInput.getText());

            TerminalSettings.onlinePlayerName = username;

            ServerLoginDialog.this.close();
        });

        final Button cancelButton = new Button("Cancel", () -> {
            canceled = true;
            ServerLoginDialog.this.close();
        });

        p.addComponent(serverLabel);

        p.addComponent(usernameLabel);
        p.addComponent(mUsernameInput);

        p.addComponent(passwordLabel);
        p.addComponent(mPasswordInput);

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        buttonPanel.addComponent(finishButton);
        buttonPanel.addComponent(cancelButton);

        p.addComponent(buttonPanel);


        setComponent(p);
    }

    @Override
    public TerminalSize getPreferredSize() {
        TerminalSize size = super.getPreferredSize();

        TerminalSize inputSize = new TerminalSize(size.getColumns() - 2, 1);

        mUsernameInput.setPreferredSize(inputSize);
        mPasswordInput.setPreferredSize(inputSize);

        return size;
    }
}
