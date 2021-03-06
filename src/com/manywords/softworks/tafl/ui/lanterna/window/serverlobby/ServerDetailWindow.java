package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.network.packet.ClientInformation;
import com.manywords.softworks.tafl.network.packet.ingame.HistoryPacket;
import com.manywords.softworks.tafl.network.packet.pregame.CreateGamePacket;
import com.manywords.softworks.tafl.ui.Ansi;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.screen.MainMenuScreen;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

import java.util.List;
import java.util.UUID;

/**
 * Created by jay on 5/23/16.
 */
public class ServerDetailWindow extends BasicWindow {
    public interface GameDetailHost {
        public void requestGameUpdate();
        public void createGame(CreateGamePacket packet);
        public void loadGame(HistoryPacket packet, TimeSpec attackerClock, TimeSpec defenderClock);
        public void leaveGame();
        public void disconnect();
    }
    LogicalScreen.TerminalCallback mCallback;
    private GameDetailHost mHost;

    private Table<String> mUserTable;
    private Panel mButtonPanel;
    private GameCreateButton mGameCreationButton;
    private GameCreateButtonAction mGameCreationButtonAction;

    private CreateGamePacket mCreatePacket;

    public ServerDetailWindow(LogicalScreen.TerminalCallback terminalCallback, GameDetailHost host) {
        super("Server Info");

        mCallback = terminalCallback;
        mHost = host;

        Panel p = new Panel();

        mUserTable = new Table<>("Connected Users");

        mButtonPanel = new Panel();
        mButtonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        mGameCreationButtonAction = new GameCreateButtonAction();
        mGameCreationButton = new GameCreateButton("Create game", mGameCreationButtonAction);
        mGameCreationButtonAction.setButton(mGameCreationButton);

        Button refreshButton = new Button("Refresh list", () -> mHost.requestGameUpdate());

        Button exitButton = new Button("Leave server", () -> {
            mHost.disconnect();
            mCallback.changeActiveScreen(new MainMenuScreen());
        });
        mButtonPanel.addComponent(mGameCreationButton);
        mButtonPanel.addComponent(refreshButton);
        mButtonPanel.addComponent(exitButton);

        p.addComponent(mButtonPanel);
        p.addComponent(mUserTable);

        setComponent(p);
    }

    public void updateClientList(List<ClientInformation> clients) {
        TableModel<String> model = new TableModel<>("Connected Users");
        for(ClientInformation c : clients) {
            model.addRow(c.toString());
        }

        mUserTable.setTableModel(model);
    }

    private class GameCreateButton extends Button {
        private boolean creating = true;

        public boolean isCreating() { return creating; }
        public boolean isCanceling() { return !creating; }

        public void setMode(boolean creating) {
            this.creating = creating;

            if(creating) {
                setLabel("Create game");
            }
            else {
                setLabel("Leave game");
            }
        }

        public GameCreateButton(String label, Runnable action) {
            super(label, action);
        }
    }

    private class GameCreateButtonAction implements Runnable {
        private GameCreateButton mButton;

        public void setButton(GameCreateButton button) {
            mButton = button;
        }

        @Override
        public void run() {
            if(mButton.isCreating()) {
                CreateGameDialog d = new CreateGameDialog("Create game");
                d.setHints(TerminalThemeConstants.CENTERED_MODAL);
                d.showDialog(getTextGUI());

                if (!d.canceled) {
                    if(d.hashedPassword.equals("")) d.hashedPassword = "none";
                    mCreatePacket = new CreateGamePacket(UUID.randomUUID(), d.attackingSide, d.hashedPassword, d.rules.getOTRString(false), d.timeSpec, d.combineChat, d.allowReplay);
                    mHost.createGame(mCreatePacket);

                    if(d.history != null) {
                        mHost.loadGame(new HistoryPacket(d.history, d.rules.boardSize), d.attackerClock, d.defenderClock);
                    }
                }
            }
            else if(mButton.isCanceling()) {
                mHost.leaveGame();
            }

            getTextGUI().setActiveWindow(ServerDetailWindow.this);

        }
    }

    public void onConnectionStateChanged(ClientServerConnection.State state) {
        if(state == ClientServerConnection.State.IN_PREGAME || state == ClientServerConnection.State.CREATING_GAME || state == ClientServerConnection.State.JOINING_GAME) {
            mGameCreationButton.setMode(false);
        }
        else {
            mGameCreationButton.setMode(true);
            mCreatePacket = null;
        }
    }

    public void notifyFocus(boolean focused) {
        if(focused) {
            setTitle(Ansi.UNDERLINE + "SERVER DETAILS" + Ansi.UNDERLINE_OFF);        }
        else {
            setTitle("Server Details");
        }
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        mUserTable.setVisibleRows(size.getRows() - 3);
        mButtonPanel.setPreferredSize(new TerminalSize(size.getColumns(), 3));
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handledByScreen = mCallback.handleKeyStroke(key);
        return handledByScreen || super.handleInput(key);
    }
}
