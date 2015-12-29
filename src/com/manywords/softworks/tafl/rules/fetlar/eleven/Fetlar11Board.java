package com.manywords.softworks.tafl.rules.fetlar.eleven;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.BoardImpl;

public class Fetlar11Board extends BoardImpl {
    public Fetlar11Board() {
        super();
    }

    public Fetlar11Board(Board board) {
        super(board);
    }

    @Override
    public int getBoardDimension() {
        return 11;
    }

    @Override
    public Board deepCopy() {
        // The board doesn't change.
        Board board = new Fetlar11Board(this);
        board.setRules(getRules());
        return board;
    }
}
