package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.pregame.JoinGamePacket;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by jay on 5/26/16.
 */
public class JoinGameDialog extends DialogWindow {
    public boolean canceled = false;
    public boolean spectate = false;
    public String hashedPassword = "";

    public JoinGamePacket packet = null;

    public JoinGameDialog(String title, GameInformation gameInfo) {
        super(title);

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        boolean freeSideAttackers = gameInfo.freeSideAttackers();
        final Label usernameLabel = new Label("Joining as " + (freeSideAttackers ? "attackers" : "defenders"));

        final Label passwordLabel = new Label("Password");
        final TextBox passwordInput = new TextBox();
        passwordInput.setMask('*');
        passwordInput.setValidationPattern(Pattern.compile("([[a-z][A-Z][0-9]\\-_\\.\\*])+"));

        final Button joinButton = new Button("Join", () -> {
            hashedPassword = PasswordHasher.hashPassword("", passwordInput.getText());
            packet = createPacket(gameInfo, spectate, hashedPassword);

            JoinGameDialog.this.close();
        });

        final Button spectateButton = new Button("Spectate", () -> {
            spectate = true;
            hashedPassword = PasswordHasher.hashPassword("", passwordInput.getText());
            packet = createPacket(gameInfo, spectate, hashedPassword);

            JoinGameDialog.this.close();
        });

        final Button cancelButton = new Button("Cancel", () -> {
            canceled = true;
            JoinGameDialog.this.close();
        });

        p.addComponent(usernameLabel);

        p.addComponent(passwordLabel);
        p.addComponent(passwordInput);

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        buttonPanel.addComponent(joinButton);
        buttonPanel.addComponent(spectateButton);
        buttonPanel.addComponent(cancelButton);

        p.addComponent(buttonPanel);

        setComponent(p);
    }

    private JoinGamePacket createPacket(GameInformation gameInfo, boolean spectate, String hashedPassword) {
        UUID gameId = UUID.fromString(gameInfo.uuid);

        return new JoinGamePacket(gameId, spectate, hashedPassword);
    }
}
