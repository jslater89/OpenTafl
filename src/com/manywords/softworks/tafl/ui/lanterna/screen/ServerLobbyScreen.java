package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.ChatWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.GameDetailWindow;
import com.manywords.softworks.tafl.ui.lanterna.window.serverlobby.GameListWindow;

/**
 * Created by jay on 5/23/16.
 */
public class ServerLobbyScreen extends LogicalScreen {
    private ClientServerConnection mConnection;

    private GameListWindow mGameList;
    private GameDetailWindow mGameDetail;
    private ChatWindow mChatWindow;

    public ServerLobbyScreen(String hostname, int port) {
        mConnection = new ClientServerConnection(hostname, port);

        if(!mConnection.connect()) {
            // TODO: error message, return to main menu
        }
    }

    @Override
    public void onResized(Terminal terminal, TerminalSize terminalSize) {
        layoutWindows(terminalSize);
    }

    private void layoutWindows(TerminalSize size) {

    }

    private class ServerLobbyTerminalCallback extends DefaultTerminalCallback {

        @Override
        public void changeActiveScreen(LogicalScreen screen) {
            mTerminal.changeActiveScreen(screen);
        }
    }
}
