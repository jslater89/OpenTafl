package com.manywords.softworks.tafl.rules.taflmen;

import com.manywords.softworks.tafl.rules.*;

public class King extends TaflmanImpl {
    public King(byte id, Coord startingSpace, Side side, Board board, Rules rules) {
        super(id, TYPE_KING, startingSpace, side, board, rules);
    }
}
