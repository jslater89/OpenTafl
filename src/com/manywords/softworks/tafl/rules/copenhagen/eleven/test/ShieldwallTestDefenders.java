package com.manywords.softworks.tafl.rules.copenhagen.eleven.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.seabattle.nine.SeaBattle9Defenders;
import com.manywords.softworks.tafl.rules.taflmen.King;

import java.util.ArrayList;
import java.util.List;

public class ShieldwallTestDefenders extends SeaBattle9Defenders {
    public ShieldwallTestDefenders(Board board) {
        super(board);
    }

    public ShieldwallTestDefenders(Board board, List<TaflmanHolder> taflmen) {
        super(board, taflmen);
    }

    public List<TaflmanHolder> generateTaflmen() {
        ArrayList<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(9);

        taflmen.add(new King((byte) 0, Coord.get(0, 1), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(1, 7), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(2, 8), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 3, Taflman.TYPE_TAFLMAN, Coord.get(3, 8), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 4, Taflman.TYPE_TAFLMAN, Coord.get(3, 0), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 5, Taflman.TYPE_TAFLMAN, Coord.get(4, 0), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 6, Taflman.TYPE_TAFLMAN, Coord.get(5, 0), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 7, Taflman.TYPE_TAFLMAN, Coord.get(2, 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 8, Taflman.TYPE_TAFLMAN, Coord.get(1, 2), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 9, Taflman.TYPE_TAFLMAN, Coord.get(0, 3), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new ShieldwallTestDefenders(board, getStartingTaflmen());
    }
}
