package com.manywords.softworks.tafl.rules.tawlbwrdd.eleven;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.BoardImpl;

public class Tawlbwrdd11Board extends BoardImpl {
    public Tawlbwrdd11Board() {
        super();
    }

    public Tawlbwrdd11Board(Board board) {
        super(board);
    }

    @Override
    public int getBoardDimension() {
        return 11;
    }

    @Override
    public Board deepCopy() {
        // The board doesn't change.
        Board board = new Tawlbwrdd11Board(this);
        board.setRules(getRules());
        return board;
    }
}
