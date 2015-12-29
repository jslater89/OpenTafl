package com.manywords.softworks.tafl.rules.taflmen;

import com.manywords.softworks.tafl.rules.*;

public class Knight extends TaflmanImpl {
    public Knight(byte id, Coord startingSpace, Side side, Board board, Rules rules) {
        super(id, TYPE_KNIGHT, startingSpace, side, board, rules);
    }
}
