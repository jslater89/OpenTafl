package com.manywords.softworks.tafl.rules.copenhagen.eleven;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.BoardImpl;

public class Copenhagen11Board extends BoardImpl {
    public Copenhagen11Board() {
        super();
    }

    public Copenhagen11Board(Board board) {
        super(board);
    }

    @Override
    public int getBoardDimension() {
        return 11;
    }

    @Override
    public Board deepCopy() {
        // The board doesn't change.
        Board board = new Copenhagen11Board(this);
        board.setRules(getRules());
        return board;
    }
}
