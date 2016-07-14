package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.manywords.softworks.tafl.network.packet.GameInformation;
import com.manywords.softworks.tafl.network.packet.pregame.JoinGamePacket;
import com.manywords.softworks.tafl.network.packet.pregame.SpectateGamePacket;
import com.manywords.softworks.tafl.ui.Ansi;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 5/23/16.
 */
public class GameListWindow extends BasicWindow {
    public interface GameListWindowHost {
        public void requestGameUpdate();
        public void joinGame(GameInformation gameInfo, JoinGamePacket packet);
    }
    private LogicalScreen.TerminalCallback mTerminalCallback;
    private GameListWindowHost mHost;
    private Table<String> mGameTable;
    private List<GameInformation> mGameList;

    private static final String[] COLUMNS = {"Rules", "Attackers", "Defenders", "Clock Setting", "Started", "Password", "Saved game", "Spectators"};

    public GameListWindow(LogicalScreen.TerminalCallback terminalCallback, GameListWindowHost host) {
        super("Game List");

        mTerminalCallback = terminalCallback;
        mHost = host;

        Panel p = new Panel();
        mGameTable = new Table<>(COLUMNS);
        mGameList = new ArrayList<>();

        mGameTable.setSelectAction(() -> {
            if(mGameTable.getTableModel().getRowCount() == 0) return;

            final GameInformation gameInformation = mGameList.get(mGameTable.getSelectedRow());
            JoinGameDialog d = new JoinGameDialog("Join game", gameInformation);
            d.setHints(TerminalThemeConstants.CENTERED_MODAL);
            
            d.showDialog(getTextGUI());
            if(!d.canceled) {
                mHost.joinGame(gameInformation, d.packet);
            }

            getTextGUI().setActiveWindow(GameListWindow.this);
        });

        p.addComponent(mGameTable);

        setComponent(p);
    }

    public void notifyFocus(boolean focused) {
        if(focused) {
            setTitle(Ansi.UNDERLINE + "GAME LIST" + Ansi.UNDERLINE_OFF);
        }
        else {
            setTitle("Game List");
        }
    }

    public void updateGameList(List<GameInformation> games) {
        mGameList = games;
        updateTable();
    }

    private void updateTable() {
        TableModel<String> model = new TableModel<>(COLUMNS);
        for(GameInformation g : mGameList) {
            String clockSetting = "Untimed";
            if(g.clockSetting.isEnabled()) clockSetting = g.clockSetting.toHumanString();
            model.addRow(g.rulesName, g.attackerUsername, g.defenderUsername, clockSetting, g.started ? "Y" : "N", g.password ? "Y" : "N", g.loaded ? "Y" : "N", "" + g.spectators);
        }

        mGameTable.setTableModel(model);
    }

    @Override
    public void setSize(TerminalSize size) {
        super.setSize(size);

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
