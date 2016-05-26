package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;

import java.util.regex.Pattern;

/**
 * Created by jay on 5/23/16.
 */
public class ServerLoginDialog extends DialogWindow {
    public boolean canceled = false;
    public String username = "";
    public String hashedPassword = "";

    public ServerLoginDialog(String title) {
        super(title);

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        final Label usernameLabel = new Label("Username");
        final TextBox usernameInput = new TextBox();
        usernameInput.setValidationPattern(Pattern.compile("([[a-z][A-Z][0-9]\\-_\\.\\*])+"));

        final Label passwordLabel = new Label("Password");
        final TextBox passwordInput = new TextBox();
        passwordInput.setMask('*');
        passwordInput.setValidationPattern(Pattern.compile("([[a-z][A-Z][0-9]\\-_\\.\\*])+"));

        final Button finishButton = new Button("Login", () -> {
            username = usernameInput.getText();
            hashedPassword = String.valueOf(passwordInput.getText().hashCode()); //TODO: implement this

            TerminalSettings.onlinePlayerName = username;

            ServerLoginDialog.this.close();
        });

        final Button cancelButton = new Button("Cancel", () -> {
            canceled = true;
            ServerLoginDialog.this.close();
        });

        p.addComponent(usernameLabel);
        p.addComponent(usernameInput);

        p.addComponent(passwordLabel);
        p.addComponent(passwordInput);

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        buttonPanel.addComponent(finishButton);
        buttonPanel.addComponent(cancelButton);

        p.addComponent(buttonPanel);

        setComponent(p);
    }
}
