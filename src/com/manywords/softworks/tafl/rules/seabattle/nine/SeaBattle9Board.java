package com.manywords.softworks.tafl.rules.seabattle.nine;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.BoardImpl;

public class SeaBattle9Board extends BoardImpl {
    public SeaBattle9Board() {
        super();
    }

    public SeaBattle9Board(Board board) {
        super(board);
    }

    @Override
    public int getBoardDimension() {
        return 9;
    }

    @Override
    public Board deepCopy() {
        // The board doesn't change.
        Board board = new SeaBattle9Board(this);
        board.setRules(getRules());
        return board;
    }
}
