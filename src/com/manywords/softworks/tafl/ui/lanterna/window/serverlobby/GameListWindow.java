package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.manywords.softworks.tafl.network.client.ClientGameInformation;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by jay on 5/23/16.
 */
public class GameListWindow extends BasicWindow {
    public interface GameListWindowHost {
        public void requestGameUpdate();
    }
    private LogicalScreen.TerminalCallback mTerminalCallback;
    private GameListWindowHost mHost;
    private Table<String> mGameTable;
    private List<ClientGameInformation> mGameList;

    private static final String[] COLUMNS = {"Rules", "Attackers", "Defenders", "Password", "Spectators"};
    private static final String[] EMPTY_ROW = {"", "", "", "", ""};

    public GameListWindow(LogicalScreen.TerminalCallback terminalCallback, GameListWindowHost host) {
        super("Game List");

        mTerminalCallback = terminalCallback;
        mHost = host;

        Panel p = new Panel();
        mGameTable = new Table<>(COLUMNS);
        mGameList = new ArrayList<>();

        //generateDebugGames();
        p.addComponent(mGameTable);

        setComponent(p);
    }

    public void notifyFocus(boolean focused) {
        if(focused) {
            setTitle("**GAME LIST**");
        }
        else {
            setTitle("Game List");
        }
    }

    public void updateGameList(List<ClientGameInformation> games) {
        mGameList = games;
        updateTable();
    }

    private void updateTable() {
        TableModel<String> model = new TableModel<>(COLUMNS);
        for(ClientGameInformation g : mGameList) {
            model.addRow(g.rulesName, g.attackerUsername, g.defenderUsername, g.password ? "Y" : "N", "" + g.spectators);
        }

        mGameTable.setTableModel(model);
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        System.out.println(size);

        mGameTable.setVisibleRows(size.getRows() - 2);

        //TerminalSize preferredSize = mGameTable.getPreferredSize();
        //TerminalSize newSize = new TerminalSize(size.getColumns() - 2, size.getRows() - 2);
        //mGameTable.setPreferredSize(newSize);
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handledByScreen = mTerminalCallback.handleKeyStroke(key);
        return handledByScreen || super.handleInput(key);
    }


}
