package com.manywords.softworks.tafl.ui.lanterna.window.ingame;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.input.KeyStroke;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.ui.Ansi;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalBoardImage;
import com.manywords.softworks.tafl.ui.lanterna.component.TerminalImagePanel;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;

import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class BoardWindow extends BasicWindow {
    private Game mGame;
    private ReplayGame mReplayGame;
    private LogicalScreen.TerminalCallback mCallback;
    private TerminalBoardImage mBoardImage;
    private String mCanonicalTitle;
    public BoardWindow(String title, Game g, LogicalScreen.TerminalCallback callback) {
        super(title);
        mCanonicalTitle = title;
        mGame = g;
        mCallback = callback;

        int rowHeight = 3;
        int colWidth = 5;

        Panel p = new Panel();
        if(mGame.getRules().getBoard().getBoardDimension() >= 15) {
            rowHeight = 2;
            colWidth = 3;
        }

        if(TerminalSettings.shrinkLargeBoards) {
            mBoardImage = new TerminalBoardImage(rowHeight, colWidth);
        }
        else {
            mBoardImage = new TerminalBoardImage(3, 5);
        }

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

    public void notifyFocus(boolean focused) {
        mBoardImage.notifyFocus(focused);
        if(focused) {
            setTitle(Ansi.UNDERLINE + mCanonicalTitle.toUpperCase() + Ansi.UNDERLINE_OFF);
        }
        else {
            setTitle(mCanonicalTitle);
        }
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handledByScreen = mCallback.handleKeyStroke(key);

        return handledByScreen || (mBoardImage.handleKeyStroke(key) != Interactable.Result.UNHANDLED);
    }
}
