package com.manywords.softworks.tafl.rules.seabattle.nine.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.seabattle.nine.SeaBattle9Attackers;

import java.util.ArrayList;
import java.util.List;

public class SimpleVictoryTestAttackers extends SeaBattle9Attackers {
    public SimpleVictoryTestAttackers(Board board) {
        super(board);
    }

    public SimpleVictoryTestAttackers(Board board, List<TaflmanHolder> taflmen) {
        super(board, taflmen);
    }

    public List<TaflmanHolder> generateTaflmen() {
        ArrayList<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(9);

        int x = 4;
        int y = 4;

        // Bottom group
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x, y+3), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x, y+4), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x+1, y+4), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x-1, y+4), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x, y + 1), this, getBoard(), getBoard().getRules()));

        // Top group
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x, y-3), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x, y-4), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x+1, y-4), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x-1, y-4), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(x, y - 1), this, getBoard(), getBoard().getRules()));

        // Left group
//		taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x-3, y), this, getBoard(), getBoard().getRules()));
//		taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x-4, y), this, getBoard(), getBoard().getRules()));
//		taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x-4, y+1), this, getBoard(), getBoard().getRules()));
//		taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x-4, y-1), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(x - 1, y), this, getBoard(), getBoard().getRules()));

        // Right group
//		taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x+3, y), this, getBoard(), getBoard().getRules()));
//		taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x+4, y), this, getBoard(), getBoard().getRules()));
//		taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x+4, y+1), this, getBoard(), getBoard().getRules()));
//		taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x+4, y-1), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 3, Taflman.TYPE_TAFLMAN, Coord.get(x + 2, y), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new SimpleVictoryTestAttackers(board, getStartingTaflmen());
    }
}
