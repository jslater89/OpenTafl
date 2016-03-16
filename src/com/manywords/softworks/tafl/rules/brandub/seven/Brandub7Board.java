package com.manywords.softworks.tafl.rules.brandub.seven;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.BoardImpl;


public class Brandub7Board extends BoardImpl {
    public Brandub7Board() {
        super(7);
    }

    public Brandub7Board(Board board) {
        super(board);
    }

    @Override
    public int getBoardDimension() {
        return 7;
    }

    @Override
    public Board deepCopy() {
        Board board = new Brandub7Board(this);
        board.setRules(getRules());
        return board;
    }
}
