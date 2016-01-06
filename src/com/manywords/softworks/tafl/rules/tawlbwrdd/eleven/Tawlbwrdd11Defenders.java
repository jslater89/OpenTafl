package com.manywords.softworks.tafl.rules.tawlbwrdd.eleven;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.taflmen.King;

import java.util.ArrayList;
import java.util.List;

public class Tawlbwrdd11Defenders extends Side {
    public Tawlbwrdd11Defenders(Board board) {
        super(board);
    }

    public Tawlbwrdd11Defenders(Board board, List<TaflmanHolder> taflmen) {
        super(board, taflmen);
    }

    @Override
    public boolean isAttackingSide() {
        return false;
    }

    @Override
    public boolean hasKnights() {
        return false;
    }

    @Override
    public boolean hasCommanders() {
        return false;
    }

    @Override
    public Side deepCopy(Board board) {
        return new Tawlbwrdd11Defenders(board, getStartingTaflmen());
    }

    public List<TaflmanHolder> generateTaflmen() {
        List<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(13);

        // 6,6 is center
        taflmen.add(new King((byte) 0, Coord.get(5, 5), this, getBoard(), getBoard().getRules()));

        // Adjacent spaces to 6,6
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(5, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(6, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 3, Taflman.TYPE_TAFLMAN, Coord.get(5, 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 4, Taflman.TYPE_TAFLMAN, Coord.get(4, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 5, Taflman.TYPE_TAFLMAN, Coord.get(4, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 6, Taflman.TYPE_TAFLMAN, Coord.get(6, 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 7, Taflman.TYPE_TAFLMAN, Coord.get(6, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 8, Taflman.TYPE_TAFLMAN, Coord.get(4, 4), this, getBoard(), getBoard().getRules()));

        // The 'point spaces'
        taflmen.add(new TaflmanImpl((byte) 9, Taflman.TYPE_TAFLMAN, Coord.get(5, 7), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 10, Taflman.TYPE_TAFLMAN, Coord.get(7, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 11, Taflman.TYPE_TAFLMAN, Coord.get(5, 3), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 12, Taflman.TYPE_TAFLMAN, Coord.get(3, 5), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }
}
