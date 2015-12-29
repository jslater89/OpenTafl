package com.manywords.softworks.tafl.rules.seabattle.nine.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.seabattle.nine.SeaBattle9Attackers;

import java.util.ArrayList;
import java.util.List;

public class EncirclementTestAttackers extends SeaBattle9Attackers {
    public EncirclementTestAttackers(Board board) {
        super(board);
    }

    public EncirclementTestAttackers(Board board, List<TaflmanHolder> taflmen) {
        super(board, taflmen);
    }

    public List<TaflmanHolder> generateTaflmen() {
        ArrayList<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(9);

        int x = 4;
        int y = 4;

        taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(3, 8), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(2, 7), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(2, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 3, Taflman.TYPE_TAFLMAN, Coord.get(3, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 4, Taflman.TYPE_TAFLMAN, Coord.get(3, 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 5, Taflman.TYPE_TAFLMAN, Coord.get(5, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 6, Taflman.TYPE_TAFLMAN, Coord.get(5, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 7, Taflman.TYPE_TAFLMAN, Coord.get(5, 7), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 8, Taflman.TYPE_TAFLMAN, Coord.get(5, 8), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new EncirclementTestAttackers(board, getStartingTaflmen());
    }
}
