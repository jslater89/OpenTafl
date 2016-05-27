package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.network.client.ClientGameInformation;
import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.network.packet.pregame.CreateGamePacket;
import com.manywords.softworks.tafl.network.packet.pregame.JoinGamePacket;
import com.manywords.softworks.tafl.network.packet.utility.ErrorPacket;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.ChatWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.GameDetailWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.GameListWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.ServerLoginDialog;

import java.util.List;
import java.util.UUID;

/**
 * Created by jay on 5/23/16.
 */
public class ServerLobbyScreen extends LogicalScreen {
    private ClientServerConnection mConnection;

    private GameListWindow mGameList;
    private GameDetailWindow mGameDetail;
    private ChatWindow mChatWindow;

    protected ServerLobbyTerminalCallback mTerminalCallback;

    private int mFocusedWindow = 0;
    private static final int FOCUS_LIST = 0;
    private static final int FOCUS_DETAIL = 1;
    private static final int FOCUS_CHAT = 2;
    private static final int FOCUS_FORWARD = 3;
    private static final int FOCUS_BACKWARD = 4;


    public ServerLobbyScreen(String hostname, int port) {
        mConnection = new ClientServerConnection(TerminalSettings.onlineServerHost, TerminalSettings.onlineServerPort, new ClientServerCallback());
    }

    @Override
    public void onResized(Terminal terminal, TerminalSize terminalSize) {
        layoutWindows(terminalSize);
    }

    private void enterUi() {
        createWindows();
        layoutWindows(mGui.getScreen().getTerminalSize());

        ServerLoginDialog dialog = new ServerLoginDialog("Login to server " + TerminalSettings.onlineServerHost + ":" + TerminalSettings.onlineServerPort);
        dialog.setHints(TerminalThemeConstants.CENTERED_MODAL);
        dialog.showDialog(mGui);

        if(dialog.canceled || dialog.username.equals("") || dialog.hashedPassword.equals("")) {
            if(!dialog.canceled) {
                MessageDialogBuilder b = new MessageDialogBuilder();
                MessageDialog d = b.setTitle("Login failed").setText("No credentials entered.").addButton(MessageDialogButton.OK).build();
                d.setHints(TerminalThemeConstants.CENTERED_MODAL);
                d.showDialog(mGui);
            }
            mTerminalCallback.changeActiveScreen(new MainMenuScreen());
        }
        else if(!mConnection.connect(dialog.username, dialog.hashedPassword)) {
            MessageDialogBuilder b = new MessageDialogBuilder();
            MessageDialog d = b.setTitle("Connection failed").setText("Server connection failed.").addButton(MessageDialogButton.OK).build();
            d.setHints(TerminalThemeConstants.CENTERED_MODAL);
            d.showDialog(mGui);

            mTerminalCallback.changeActiveScreen(new MainMenuScreen());
        }
        else {
            addWindows();
        }
    }

    private void createWindows() {
        mGameList = new GameListWindow(mTerminalCallback, mTerminalCallback);
        mGameDetail = new GameDetailWindow(mTerminalCallback, mTerminalCallback);
        mChatWindow = new ChatWindow(mTerminalCallback, mTerminalCallback);
    }

    private void addWindows() {
        mGui.addWindow(mGameList);
        mGui.addWindow(mGameDetail);
        mGui.addWindow(mChatWindow);

        mGui.setActiveWindow(mGameList);
        mGameList.notifyFocus(true);

        mGui.waitForWindowToClose(mGameList);
    }

    private void layoutWindows(TerminalSize size) {
        mGameList.setHints(TerminalThemeConstants.MANUAL_LAYOUT);
        mGameDetail.setHints(TerminalThemeConstants.MANUAL_LAYOUT);
        mChatWindow.setHints(TerminalThemeConstants.MANUAL_LAYOUT);

        int serverListHeight = size.getRows() / 2 - 2;
        int otherHeights = size.getRows() - serverListHeight - 4;

        int detailWidth = size.getColumns() / 2 - 4;
        int chatWidth = size.getColumns() - detailWidth - 4;

        mGameList.setSize(new TerminalSize(size.getColumns() - 2, serverListHeight));
        mGameDetail.setSize(new TerminalSize(detailWidth, otherHeights));
        mChatWindow.setSize(new TerminalSize(chatWidth, otherHeights));

        mGameList.setPosition(new TerminalPosition(0, 0));
        mChatWindow.setPosition(new TerminalPosition(0, serverListHeight + 2));
        mGameDetail.setPosition(new TerminalPosition(chatWidth + 2, serverListHeight + 2));
    }

    private void removeWindows() {
        mGui.removeWindow(mGameList);
        mGui.removeWindow(mGameDetail);
        mGui.removeWindow(mChatWindow);

        mGameList = null;
        mGameDetail = null;
        mChatWindow = null;
    }

    private void leaveUi() {
        mConnection.disconnect();
        removeWindows();
    }

    private void cycleFocus(int direction) {
        if(direction == FOCUS_FORWARD) {
            mFocusedWindow = ++mFocusedWindow % 3;
        }
        else {
            mFocusedWindow -= 1;
            if(mFocusedWindow < 0) mFocusedWindow = 2;
        }

        setFocusedWindow(mFocusedWindow);
    }

    private void setFocusedWindow(int focusedWindow) {
        switch(focusedWindow) {
            case FOCUS_LIST:
                mGui.setActiveWindow(mGameList);
                mGameList.notifyFocus(true);
                mGameDetail.notifyFocus(false);
                mChatWindow.notifyFocus(false);
                break;
            case FOCUS_DETAIL:
                mGui.setActiveWindow(mGameDetail);
                mGameList.notifyFocus(false);
                mGameDetail.notifyFocus(true);
                mChatWindow.notifyFocus(false);
                break;
            case FOCUS_CHAT:
                mGui.setActiveWindow(mChatWindow);
                mGameList.notifyFocus(false);
                mGameDetail.notifyFocus(false);
                mChatWindow.notifyFocus(true);
                break;
        }
    }

    @Override
    public void setActive(AdvancedTerminal t, WindowBasedTextGUI gui) {
        super.setActive(t, gui);

        mTerminalCallback = new ServerLobbyTerminalCallback();

        enterUi();
    }

    @Override
    public void setInactive() {
        super.setInactive();

        leaveUi();
    }

    private class ServerLobbyTerminalCallback extends DefaultTerminalCallback implements ChatWindow.ChatWindowHost, GameListWindow.GameListWindowHost, GameDetailWindow.GameDetailHost {
        @Override
        public boolean handleKeyStroke(KeyStroke key) {
            if(key.getKeyType() == KeyType.Tab) {
                cycleFocus(FOCUS_FORWARD);
                return true;
            }
            else if(key.getKeyType() == KeyType.ReverseTab) {
                cycleFocus(FOCUS_BACKWARD);
                return true;
            }
            return false;
        }

        @Override
        public void changeActiveScreen(LogicalScreen screen) {
            mTerminal.changeActiveScreen(screen);
        }

        @Override
        public void sendChatMessage(String message) {
            mConnection.sendChatMessage(TerminalSettings.onlinePlayerName, message);
        }

        @Override
        public void requestGameUpdate() {
            mConnection.requestGameUpdate();
        }

        @Override
        public void joinGame(JoinGamePacket packet) {
            mConnection.sendJoinGameMessage(packet);
        }

        @Override
        public void createGame(CreateGamePacket packet) {
            mConnection.sendCreateGameMessage(packet);
        }

        public void leaveGame() {
            mConnection.sendLeaveGameMessage();
        }
    }

    private class ClientServerCallback implements ClientServerConnection.ClientServerCallback {

        @Override
        public void onStateChanged(ClientServerConnection.State newState) {
            if(mGameDetail != null) mGameDetail.onConnectionStateChanged(newState);
        }

        @Override
        public void onChatMessageReceived(String sender, String message) {
            mChatWindow.onChatMessageReceived(sender, message);
        }

        @Override
        public void onSuccessReceived() {

        }

        @Override
        public void onErrorReceived(String message) {
            if(message.equals(ErrorPacket.LOGIN_FAILED)) {
                MessageDialogBuilder b = new MessageDialogBuilder();
                b.setTitle("Login failed");
                b.setText("Login/registration credentials invalid.");
                b.addButton(MessageDialogButton.OK);
                MessageDialog d = b.build();
                d.setHints(TerminalThemeConstants.CENTERED_MODAL);

                // Ordinarily comes from a non-UI thread
                TerminalUtils.runOnUiThread(mGui, () -> {
                    d.showDialog(mGui);
                    mTerminalCallback.changeActiveScreen(new MainMenuScreen());
                });
            }
            else {
                MessageDialogBuilder b = new MessageDialogBuilder();
                b.setTitle("Unhandled error");
                b.setText("Error packet message:\n" + message);
                b.addButton(MessageDialogButton.OK);
                MessageDialog d = b.build();
                d.setHints(TerminalThemeConstants.CENTERED_MODAL);

                // Ordinarily comes from a non-UI thread
                TerminalUtils.runOnUiThread(mGui, () -> {
                    d.showDialog(mGui);
                    mTerminalCallback.changeActiveScreen(new MainMenuScreen());
                });
            }
        }

        @Override
        public void onGameListReceived(List<ClientGameInformation> games) {
            mGameList.updateGameList(games);
        }

        @Override
        public void onDisconnect(boolean planned) {
            if(!planned) {
                MessageDialogBuilder b = new MessageDialogBuilder();
                b.setTitle("Server connection failed");
                b.setText("Server terminated the connection.");
                b.addButton(MessageDialogButton.OK);
                MessageDialog d = b.build();
                d.setHints(TerminalThemeConstants.CENTERED_MODAL);

                // Ordinarily comes from a non-UI thread
                TerminalUtils.runOnUiThread(mGui, () -> {
                    d.showDialog(mGui);
                    mTerminalCallback.changeActiveScreen(new MainMenuScreen());
                });
            }
        }
    }
}
