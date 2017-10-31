package com.manywords.softworks.tafl.rules.taflmen;

import com.manywords.softworks.tafl.rules.*;

public class Mercenary extends TaflmanImpl {
    public Mercenary(byte id, Coord startingSpace, Side side, Board board, Rules rules) {
        super(id, TYPE_MERCENARY, startingSpace, side, board, rules);
    }
}
