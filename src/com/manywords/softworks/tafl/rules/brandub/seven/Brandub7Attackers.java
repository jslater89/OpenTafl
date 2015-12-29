package com.manywords.softworks.tafl.rules.brandub.seven;

import com.manywords.softworks.tafl.rules.*;

import java.util.ArrayList;
import java.util.List;


public class Brandub7Attackers extends Side {
    public Brandub7Attackers(Board board) {
        super(board);
    }

    public Brandub7Attackers(Board board, List<TaflmanHolder> taflmen) {
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
    public boolean hasCommanders() {
        return false;
    }

    @Override
    public List<TaflmanHolder> generateTaflmen() {
        List<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(8);

        int x = 3;
        int y = 3;

        // Bottom group
        taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x, y + 2), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(x, y + 3), this, getBoard(), getBoard().getRules()));

        // Top group
        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(x, y - 2), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 3, Taflman.TYPE_TAFLMAN, Coord.get(x, y - 3), this, getBoard(), getBoard().getRules()));


        // Left group
        taflmen.add(new TaflmanImpl((byte) 4, Taflman.TYPE_TAFLMAN, Coord.get(x - 2, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 5, Taflman.TYPE_TAFLMAN, Coord.get(x - 3, y), this, getBoard(), getBoard().getRules()));

        // Right group
        taflmen.add(new TaflmanImpl((byte) 6, Taflman.TYPE_TAFLMAN, Coord.get(x + 2, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 7, Taflman.TYPE_TAFLMAN, Coord.get(x + 3, y), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new Brandub7Attackers(board, getStartingTaflmen());
    }
}
