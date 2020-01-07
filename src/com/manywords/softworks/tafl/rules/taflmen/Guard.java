package com.manywords.softworks.tafl.rules.taflmen;

import com.manywords.softworks.tafl.rules.*;

public class Guard extends TaflmanImpl {
    public Guard(byte id, Coord startingSpace, Side side, Board board, Rules rules) {
        super(id, TYPE_GUARD, startingSpace, side, board, rules);
    }
}
