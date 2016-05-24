package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.manywords.softworks.tafl.network.client.ClientGameInformation;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jay on 5/23/16.
 */
public class GameListWindow extends BasicWindow {
    public interface GameListWindowHost {
    }
    private LogicalScreen.TerminalCallback mTerminalCallback;
    private GameListWindowHost mHost;
    private Table<String> mGameTable;
    private List<ClientGameInformation> mGameList;

    public GameListWindow(LogicalScreen.TerminalCallback terminalCallback, GameListWindowHost host) {
        super("Game List");

        mTerminalCallback = terminalCallback;
        mHost = host;

        Panel p = new Panel();
        mGameTable = new Table<>("Rules", "Attackers", "Defenders", "Password", "Spectators");
        mGameList = new ArrayList<>();

        generateDebugGames();

        updateTable();

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

    private void updateTable() {
        TableModel<String> model = mGameTable.getTableModel();
        for(int i = 0; i < model.getRowCount(); i++) {
            model.removeRow(i);
        }
        for(ClientGameInformation g : mGameList) {
            model.addRow(g.rulesName, g.attackerUsername, g.defenderUsername, g.password ? "Y" : "N", "" + g.spectators);
        }

        if(model.getRowCount() == 0) {
            model.addRow("", "", "", "", "");
        }
        mGameTable.setTableModel(model);

        System.out.println("Rows: " + model.getRowCount());
        System.out.println("Columns: " + model.getColumnCount());
        System.out.println("Column labels: " + model.getColumnLabels());
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

        System.out.println(size);

        mGameTable.setVisibleRows(size.getRows() - 2);

        TerminalSize preferredSize = mGameTable.getPreferredSize();
        TerminalSize newSize = new TerminalSize(size.getColumns() - 2, preferredSize.getRows());
        mGameTable.setPreferredSize(newSize);
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handledByScreen = mTerminalCallback.handleKeyStroke(key);
        return handledByScreen || super.handleInput(key);
    }

    private void generateDebugGames() {
        for(int i = 0; i < 50; i++) {
            switch(new Random().nextInt(3)) {
                case 0:
                    mGameList.add(new ClientGameInformation("Brandub 7x7", "Fishbreath", "otherguy", true, 0));
                    break;
                case 1:
                    mGameList.add(new ClientGameInformation("Tablut 15x15", "Shenmage", "parvusimperator", false, 2));
                    break;
                case 2:
                    mGameList.add(new ClientGameInformation("Foteviken Tablut 9x9", "Nasa", "OpenTafl AI", false, 28));
                    break;
            }
        }
    }
}
