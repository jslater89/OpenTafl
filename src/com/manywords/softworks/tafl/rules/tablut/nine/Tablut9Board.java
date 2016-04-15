package com.manywords.softworks.tafl.rules.tablut.nine;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.BoardImpl;

public class Tablut9Board extends BoardImpl {
    public Tablut9Board() {
        super(9);
    }

    public Tablut9Board(Board board) {
        super(board);
    }

    @Override
    public int getBoardDimension() {
        return 9;
    }

    @Override
    public Board deepCopy() {
        // The board doesn't change.
        Board board = new Tablut9Board(this);
        board.setRules(getRules());
        return board;
    }
}
