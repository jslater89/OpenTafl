package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.pregame.JoinGamePacket;
import com.manywords.softworks.tafl.network.packet.pregame.SpectateGamePacket;

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

    private TextBox mPasswordInput;

    public JoinGameDialog(String title, GameInformation gameInfo) {
        super(title);

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        boolean freeSideAttackers = gameInfo.freeSideAttackers();
        final Label usernameLabel = new Label("Joining as " + (freeSideAttackers ? "attackers" : "defenders"));

        final Label passwordLabel = new Label("Password");
        mPasswordInput = new TextBox();
        mPasswordInput.setMask('*');
        mPasswordInput.setValidationPattern(Pattern.compile("([[a-z][A-Z][0-9]\\-_\\.\\*])+"));

        final Button joinButton = new Button("Join", () -> {
            hashedPassword = PasswordHasher.hashPassword("", mPasswordInput.getText());
            packet = createPacket(JoinGamePacket.Type.JOIN, gameInfo, spectate, hashedPassword);

            JoinGameDialog.this.close();
        });

        final Button spectateButton = new Button("Spectate", () -> {
            spectate = true;
            hashedPassword = PasswordHasher.hashPassword("", mPasswordInput.getText());
            packet = createPacket(JoinGamePacket.Type.SPECTATE, gameInfo, spectate, hashedPassword);

            JoinGameDialog.this.close();
        });

        final Button cancelButton = new Button("Cancel", () -> {
            canceled = true;
            JoinGameDialog.this.close();
        });

        p.addComponent(usernameLabel);

        p.addComponent(passwordLabel);
        p.addComponent(mPasswordInput);

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        if(!gameInfo.started) buttonPanel.addComponent(joinButton);
        buttonPanel.addComponent(spectateButton);
        buttonPanel.addComponent(cancelButton);

        p.addComponent(buttonPanel);

        setComponent(p);
    }

    private JoinGamePacket createPacket(JoinGamePacket.Type type, GameInformation gameInfo, boolean spectate, String hashedPassword) {
        UUID gameId = UUID.fromString(gameInfo.uuid);

        if(type == JoinGamePacket.Type.JOIN) return new JoinGamePacket(gameId, spectate, hashedPassword);
        else return new SpectateGamePacket(gameId, spectate, hashedPassword);
    }

    @Override
    public TerminalSize getPreferredSize() {
        TerminalSize size = super.getPreferredSize();

        TerminalSize inputSize = new TerminalSize(size.getColumns() - 2, 1);

        mPasswordInput.setPreferredSize(inputSize);

        return size;
    }
}
