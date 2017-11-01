package com.manywords.softworks.tafl.rules.tawlbwrdd.eleven;

import com.manywords.softworks.tafl.rules.*;

import java.util.ArrayList;
import java.util.List;

public class Tawlbwrdd11Attackers extends Side {
    public Tawlbwrdd11Attackers(Board board) {
        super(board);
    }

    public Tawlbwrdd11Attackers(Board board, List<TaflmanHolder> taflmen) {
        super(board, taflmen);
    }

    @Override
    public boolean isAttackingSide() {
        return true;
    }

    @Override
    public boolean hasKnights() {
        return false;
    }

    @Override
    public boolean hasMercenaries() {
        return false;
    }

    @Override
    public boolean hasCommanders() {
        return false;
    }

    @Override
    public Side deepCopy(Board board) {
        return new Tawlbwrdd11Attackers(board, getStartingTaflmen());
    }

    public List<TaflmanHolder> generateTaflmen() {
        List<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(24);

        // Right side
        taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(0, 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(0, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(0, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 3, Taflman.TYPE_TAFLMAN, Coord.get(1, 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 4, Taflman.TYPE_TAFLMAN, Coord.get(2, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 5, Taflman.TYPE_TAFLMAN, Coord.get(1, 6), this, getBoard(), getBoard().getRules()));

        // Top
        taflmen.add(new TaflmanImpl((byte) 6, Taflman.TYPE_TAFLMAN, Coord.get(4, 0), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 7, Taflman.TYPE_TAFLMAN, Coord.get(5, 0), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 8, Taflman.TYPE_TAFLMAN, Coord.get(6, 0), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 9, Taflman.TYPE_TAFLMAN, Coord.get(4, 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 10, Taflman.TYPE_TAFLMAN, Coord.get(5, 2), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 11, Taflman.TYPE_TAFLMAN, Coord.get(6, 1), this, getBoard(), getBoard().getRules()));

        // Left side
        taflmen.add(new TaflmanImpl((byte) 12, Taflman.TYPE_TAFLMAN, Coord.get(10, 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 13, Taflman.TYPE_TAFLMAN, Coord.get(10, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 14, Taflman.TYPE_TAFLMAN, Coord.get(10, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 15, Taflman.TYPE_TAFLMAN, Coord.get(9, 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 16, Taflman.TYPE_TAFLMAN, Coord.get(8, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 17, Taflman.TYPE_TAFLMAN, Coord.get(9, 6), this, getBoard(), getBoard().getRules()));

        // Bottom
        taflmen.add(new TaflmanImpl((byte) 18, Taflman.TYPE_TAFLMAN, Coord.get(4, 10), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 19, Taflman.TYPE_TAFLMAN, Coord.get(5, 10), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 20, Taflman.TYPE_TAFLMAN, Coord.get(6, 10), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 21, Taflman.TYPE_TAFLMAN, Coord.get(4, 9), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 22, Taflman.TYPE_TAFLMAN, Coord.get(5, 8), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 23, Taflman.TYPE_TAFLMAN, Coord.get(6, 9), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }
}
