package com.manywords.softworks.tafl.rules.taflmen;

import com.manywords.softworks.tafl.rules.*;

public class Commander extends TaflmanImpl {
    public Commander(byte id, Coord startingSpace, Side side, Board board, Rules rules) {
        super(id, TYPE_COMMANDER, startingSpace, side, board, rules);
    }
}
