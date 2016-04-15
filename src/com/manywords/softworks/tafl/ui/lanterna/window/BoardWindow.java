package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Panel;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalImagePanel;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class BoardWindow extends BasicWindow {
    private Game mGame;
    private ReplayGame mReplayGame;
    private LogicalScreen.TerminalCallback mCallback;
    private TerminalBoardImage mBoardImage;
    public BoardWindow(String title, Game g, LogicalScreen.TerminalCallback callback) {
        super(title);
        mGame = g;

        Panel p = new Panel();
        mBoardImage = new TerminalBoardImage();
        TerminalImagePanel boardImagePanel = new TerminalImagePanel(mBoardImage, new TerminalSize(40, 25));
        p.addComponent(boardImagePanel);

        this.setComponent(p);
        rerenderBoard();
    }

    public void enterReplay(ReplayGame rg) {
        mReplayGame = rg;
    }

    public void setGame(Game g) {
        mGame = g;
    }

    public void leaveReplay() {
        mReplayGame = null;
    }

    public void rerenderBoard() {
        Game toRender = (mReplayGame != null ? mReplayGame.getGame() : mGame);
        mBoardImage.renderBoard(toRender.getCurrentState(), null, null, null, null);
        invalidate();
    }

    public void rerenderBoard(Coord location, List<Coord> stops, List<Coord> moves, List<Coord> captures) {
        Game toRender = (mReplayGame != null ? mReplayGame.getGame() : mGame);
        mBoardImage.renderBoard(toRender.getCurrentState(), location, stops, moves, captures);
    }
}
