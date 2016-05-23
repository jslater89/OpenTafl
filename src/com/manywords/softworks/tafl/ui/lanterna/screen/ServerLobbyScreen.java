package com.manywords.softworks.tafl.ui.lanterna.screen;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;
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
    }

    @Override
    public void onResized(Terminal terminal, TerminalSize terminalSize) {
        layoutWindows(terminalSize);
    }

    private void enterUi() {
        if(!mConnection.connect()) {
            // TODO: error message, return to main menu
        }
        else {
            createWindows();
            layoutWindows(mGui.getScreen().getTerminalSize());
            addWindows();
        }
    }

    private void createWindows() {
        mGameList = new GameListWindow();
        mGameDetail = new GameDetailWindow();
        mChatWindow = new ChatWindow();
    }

    private void addWindows() {
        mGui.addWindow(mGameList);
        mGui.addWindow(mGameDetail);
        mGui.addWindow(mChatWindow);

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
        removeWindows();
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

    private class ServerLobbyTerminalCallback extends DefaultTerminalCallback {

        @Override
        public void changeActiveScreen(LogicalScreen screen) {
            mTerminal.changeActiveScreen(screen);
        }
    }
}
