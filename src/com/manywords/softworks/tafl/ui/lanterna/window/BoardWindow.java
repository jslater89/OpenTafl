package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Panel;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalImagePanel;

import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class BoardWindow extends BasicWindow {
    private Game mGame;
    private AdvancedTerminal.TerminalCallback mCallback;
    private TerminalBoardImage mBoardImage;
    public BoardWindow(String title, Game g, AdvancedTerminal.TerminalCallback callback) {
        super(title);
        mGame = g;

        Panel p = new Panel();
        mBoardImage = new TerminalBoardImage();
        TerminalImagePanel boardImagePanel = new TerminalImagePanel(mBoardImage, new TerminalSize(40, 25));
        p.addComponent(boardImagePanel);

        this.setComponent(p);
        rerenderBoard();
    }

    public void rerenderBoard() {
        mBoardImage.renderBoard(mGame.getCurrentState(), null, null, null, null);
    }

    public void rerenderBoard(Coord location, List<Coord> stops, List<Coord> moves, List<Coord> captures) {
        mBoardImage.renderBoard(mGame.getCurrentState(), location, stops, moves, captures);
    }
}
