package com.manywords.softworks.tafl.rules.berserk.eleven;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.BoardImpl;

public class Berserk11Board extends BoardImpl {
    public Berserk11Board() {
        super();
    }

    public Berserk11Board(Board board) {
        super(board);
    }

    @Override
    public int getBoardDimension() {
        return 11;
    }

    @Override
    public Board deepCopy() {
        // The board doesn't change.
        Board board = new Berserk11Board(this);
        board.setRules(getRules());
        return board;
    }
}
