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
import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.packet.ClientInformation;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.network.packet.ingame.HistoryPacket;
import com.manywords.softworks.tafl.network.packet.ingame.VictoryPacket;
import com.manywords.softworks.tafl.network.packet.pregame.CreateGamePacket;
import com.manywords.softworks.tafl.network.packet.pregame.JoinGamePacket;
import com.manywords.softworks.tafl.network.packet.utility.ErrorPacket;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.ChatWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.ServerDetailWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.GameListWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.ServerLoginDialog;

import java.util.List;

/**
 * Created by jay on 5/23/16.
 */
public class ServerLobbyScreen extends LogicalScreen {
    private ClientServerConnection mConnection;

    private GameListWindow mGameList;
    private ServerDetailWindow mServerDetail;
    private ChatWindow mChatWindow;

    protected ServerLobbyTerminalCallback mTerminalCallback;

    private int mFocusedWindow = 0;
    private static final int FOCUS_LIST = 0;
    private static final int FOCUS_DETAIL = 1;
    private static final int FOCUS_CHAT = 2;
    private static final int FOCUS_FORWARD = 3;
    private static final int FOCUS_BACKWARD = 4;


    public ServerLobbyScreen() {
        mConnection = new ClientServerConnection(TerminalSettings.onlineServerHost, TerminalSettings.onlineServerPort, new ClientServerCallback());
    }

    public ServerLobbyScreen(ClientServerConnection connection) {
        mConnection = connection;
        mConnection.setCallback(new ClientServerCallback());
    }

    @Override
    public void onResized(Terminal terminal, TerminalSize terminalSize) {
        layoutWindows(terminalSize);
    }

    private void enterUi() {
        createWindows();
        layoutWindows(mGui.getScreen().getTerminalSize());


        if(mConnection.getCurrentState() == ClientServerConnection.State.DISCONNECTED) {
            ServerLoginDialog dialog = new ServerLoginDialog("Login to server");
            dialog.setHints(TerminalThemeConstants.CENTERED_MODAL);
            dialog.showDialog(mGui);

            if (dialog.canceled || dialog.username.equals("") || dialog.hashedPassword.equals("")) {
                if (!dialog.canceled) {
                    MessageDialogBuilder b = new MessageDialogBuilder();
                    MessageDialog d = b.setTitle("Login failed").setText("No credentials entered.").addButton(MessageDialogButton.OK).build();
                    d.setHints(TerminalThemeConstants.CENTERED_MODAL);
                    d.showDialog(mGui);
                }
                mTerminalCallback.changeActiveScreen(new MainMenuScreen());
            }
            else if (!mConnection.connect(dialog.username, dialog.hashedPassword)) {
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
        else {
            addWindows();
        }
    }

    private void createWindows() {
        mGameList = new GameListWindow(mTerminalCallback, mTerminalCallback);
        mServerDetail = new ServerDetailWindow(mTerminalCallback, mTerminalCallback);
        mChatWindow = new ChatWindow(mTerminalCallback, mTerminalCallback);
    }

    private void addWindows() {
        mGui.addWindow(mGameList);
        mGui.addWindow(mServerDetail);
        mGui.addWindow(mChatWindow);

        mGui.setActiveWindow(mGameList);
        mGameList.notifyFocus(true);

        mGui.waitForWindowToClose(mGameList);
    }

    private void layoutWindows(TerminalSize size) {
        mGameList.setHints(TerminalThemeConstants.MANUAL_LAYOUT);
        mServerDetail.setHints(TerminalThemeConstants.MANUAL_LAYOUT);
        mChatWindow.setHints(TerminalThemeConstants.MANUAL_LAYOUT);

        int serverListHeight = size.getRows() / 2 - 2;
        int otherHeights = size.getRows() - serverListHeight - 4;

        int detailWidth = size.getColumns() / 2 - 4;
        int chatWidth = size.getColumns() - detailWidth - 4;

        mGameList.setSize(new TerminalSize(size.getColumns() - 2, serverListHeight));
        mServerDetail.setSize(new TerminalSize(detailWidth, otherHeights));
        mChatWindow.setSize(new TerminalSize(chatWidth, otherHeights));

        mGameList.setPosition(new TerminalPosition(0, 0));
        mChatWindow.setPosition(new TerminalPosition(0, serverListHeight + 2));
        mServerDetail.setPosition(new TerminalPosition(chatWidth + 2, serverListHeight + 2));
    }

    private void removeWindows() {
        mGui.removeWindow(mGameList);
        mGui.removeWindow(mServerDetail);
        mGui.removeWindow(mChatWindow);

        mGameList = null;
        mServerDetail = null;
        mChatWindow = null;
    }

    private void leaveUi() {
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
                mServerDetail.notifyFocus(false);
                mChatWindow.notifyFocus(false);
                break;
            case FOCUS_DETAIL:
                mGui.setActiveWindow(mServerDetail);
                mGameList.notifyFocus(false);
                mServerDetail.notifyFocus(true);
                mChatWindow.notifyFocus(false);
                break;
            case FOCUS_CHAT:
                mGui.setActiveWindow(mChatWindow);
                mGameList.notifyFocus(false);
                mServerDetail.notifyFocus(false);
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

    private class ServerLobbyTerminalCallback extends DefaultTerminalCallback implements ChatWindow.ChatWindowHost, GameListWindow.GameListWindowHost, ServerDetailWindow.GameDetailHost {
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
            mConnection.sendChatMessage(ClientServerConnection.ChatType.LOBBY, TerminalSettings.onlinePlayerName, message);
        }

        @Override
        public void requestGameUpdate() {
            mConnection.requestGameUpdate();
        }

        @Override
        public void joinGame(GameInformation gameInfo, JoinGamePacket packet) {
            mConnection.sendJoinGameMessage(gameInfo, packet);
        }

        @Override
        public void createGame(CreateGamePacket packet) {
            mConnection.sendCreateGameMessage(packet);
        }

        @Override
        public void loadGame(HistoryPacket packet) {
            mConnection.sendHistory(packet.moves, packet.boardSize);
        }

        public void leaveGame() {
            mConnection.sendLeaveGameMessage();
        }

        @Override
        public void disconnect() {
            mConnection.disconnect();
        }
    }

    private class ClientServerCallback implements ClientServerConnection.ClientServerCallback {

        @Override
        public void onStateChanged(ClientServerConnection.State newState) {
            if(mServerDetail != null) mServerDetail.onConnectionStateChanged(newState);
        }

        @Override
        public void onChatMessageReceived(ClientServerConnection.ChatType type, String sender, String message) {
            if(type.equals(ClientServerConnection.ChatType.LOBBY)) mChatWindow.onChatMessageReceived(sender, message);
        }

        @Override
        public void onSuccessReceived(String message) {

        }

        @Override
        public void onErrorReceived(String message) {
            if(message.equals(ErrorPacket.LOGIN_FAILED)) {
                showDialogOnUiThread("Login failed", "Login/registration credentials invalid.", true);
            }
            else if(message.equals(ErrorPacket.VERSION_MISMATCH)) {
                showDialogOnUiThread("Version mismatch",
                        "The server is running a different version of OpenTafl.\n" +
                                "Please visit softworks.manywords.press/opentafl for the latest version,\n" +
                                "or tell the server host to upgrade.", true);
            }
            else if(message.equals(ErrorPacket.ALREADY_HOSTING)) {
                showDialogOnUiThread("Already in game", "Leave your current game before joining another.", false);
            }
            else if(message.equals(ErrorPacket.GAME_ENDED)) {
                showDialogOnUiThread("Game over", "Cannot spectate ended games.", false);
            }
            else {
                showDialogOnUiThread("Unhandled error", "Error packet message:\n" + message, false);
            }
        }

        @Override
        public void onGameListReceived(List<GameInformation> games) {
            if(mGameList != null) mGameList.updateGameList(games);
        }

        @Override
        public void onClientListReceived(List<ClientInformation> clients) {
            if(mServerDetail != null) mServerDetail.updateClientList(clients);
        }

        @Override
        public void onDisconnect(boolean planned) {
            if(!planned) {
                showDialogOnUiThread("Server connection failed", "Server terminated the connection.", true);
            }
        }

        @Override
        public Game getGame() {
            return null;
        }

        @Override
        public void onStartGame(Rules r, List<MoveRecord> history) {
            final TimeSpec clockSetting = mConnection.getLastClockSetting();
            TerminalUtils.runOnUiThread(mGui, () -> TerminalUtils.startNetworkGame(mGui, mTerminalCallback, mConnection, r, clockSetting, history));
        }

        @Override
        public void onHistoryReceived(List<MoveRecord> moves) {
            OpenTafl.logPrintln(OpenTafl.LogLevel.NORMAL, "History delivered to server lobby screen");
        }

        @Override
        public void onServerMoveReceived(MoveRecord move) {

        }

        @Override
        public void onClockUpdateReceived(TimeSpec attackerClock, TimeSpec defenderClock) {

        }

        @Override
        public void onVictory(VictoryPacket.Victory victory) {

        }
    }

    private void showDialogOnUiThread(String title, String text, boolean critical) {
        MessageDialogBuilder b = new MessageDialogBuilder();
        b.setTitle(title);
        b.setText(text);
        b.addButton(MessageDialogButton.OK);
        MessageDialog d = b.build();
        d.setHints(TerminalThemeConstants.CENTERED_MODAL);

        // Ordinarily comes from a non-UI thread
        TerminalUtils.runOnUiThread(mGui, () -> {
            d.showDialog(mGui);
            if(critical) {
                mConnection.disconnect();
                mTerminalCallback.changeActiveScreen(new MainMenuScreen());
            }
        });
    }
}
