package com.manywords.softworks.tafl.rules.tablut.nine;

import com.manywords.softworks.tafl.rules.*;

import java.util.ArrayList;
import java.util.List;


public class Tablut9Attackers extends Side {
    public Tablut9Attackers(Board board) {
        super(board);
    }

    public Tablut9Attackers(Board board, List<TaflmanHolder> taflmen) {
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
    public boolean hasGuards() {
        return false;
    }

    public List<TaflmanHolder> generateTaflmen() {
        ArrayList<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(12);

        int x = 4;
        int y = 4;

        // Bottom group
        taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x, y + 3), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(x, y + 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(x + 1, y + 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 3, Taflman.TYPE_TAFLMAN, Coord.get(x - 1, y + 4), this, getBoard(), getBoard().getRules()));

        // Top group
        taflmen.add(new TaflmanImpl((byte) 4, Taflman.TYPE_TAFLMAN, Coord.get(x, y - 3), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 5, Taflman.TYPE_TAFLMAN, Coord.get(x, y - 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 6, Taflman.TYPE_TAFLMAN, Coord.get(x + 1, y - 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 7, Taflman.TYPE_TAFLMAN, Coord.get(x - 1, y - 4), this, getBoard(), getBoard().getRules()));

        // Left group
        taflmen.add(new TaflmanImpl((byte) 8, Taflman.TYPE_TAFLMAN, Coord.get(x - 3, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 9, Taflman.TYPE_TAFLMAN, Coord.get(x - 4, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 10, Taflman.TYPE_TAFLMAN, Coord.get(x - 4, y + 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 11, Taflman.TYPE_TAFLMAN, Coord.get(x - 4, y - 1), this, getBoard(), getBoard().getRules()));

        // Right group
        taflmen.add(new TaflmanImpl((byte) 12, Taflman.TYPE_TAFLMAN, Coord.get(x + 3, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 13, Taflman.TYPE_TAFLMAN, Coord.get(x + 4, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 14, Taflman.TYPE_TAFLMAN, Coord.get(x + 4, y + 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 15, Taflman.TYPE_TAFLMAN, Coord.get(x + 4, y - 1), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new Tablut9Attackers(board, getStartingTaflmen());
    }
}
