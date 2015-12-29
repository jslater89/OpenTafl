package com.manywords.softworks.tafl.rules.seabattle.nine.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.seabattle.nine.SeaBattle9Defenders;
import com.manywords.softworks.tafl.rules.taflmen.King;

import java.util.ArrayList;
import java.util.List;

public class EncirclementTestDefenders extends SeaBattle9Defenders {
    public EncirclementTestDefenders(Board board) {
        super(board);
    }

    public EncirclementTestDefenders(Board board, List<TaflmanHolder> taflmen) {
        super(board, taflmen);
    }

    public List<TaflmanHolder> generateTaflmen() {
        ArrayList<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(9);

        taflmen.add(new King((byte) 0, Coord.get(4, 6), this, getBoard(), getBoard().getRules()));

        int x = 4;
        int y = 4;

        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x, y+1), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x+1, y+1), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x, y-1), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x-1, y-1), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x+1, y-1), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x+1, y), this, getBoard(), getBoard().getRules()));
        //taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x-1, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(3, 6), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new EncirclementTestDefenders(board, getStartingTaflmen());
    }
}
