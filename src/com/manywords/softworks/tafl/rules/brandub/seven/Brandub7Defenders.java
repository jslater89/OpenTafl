package com.manywords.softworks.tafl.rules.brandub.seven;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.taflmen.King;

import java.util.ArrayList;
import java.util.List;

public class Brandub7Defenders extends Side {
    public Brandub7Defenders(Board board) {
        super(board);
    }

    public Brandub7Defenders(Board board, List<TaflmanHolder> taflmen) {
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
    public boolean hasMercenaries() {
        return false;
    }

    @Override
    public boolean hasCommanders() {
        return false;
    }

    @Override
    public List<TaflmanHolder> generateTaflmen() {
        List<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(9);

        int x = 3;
        int y = 3;

        taflmen.add(new King((byte) 8, Coord.get(x, y), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 9, Taflman.TYPE_TAFLMAN, Coord.get(x, y + 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 10, Taflman.TYPE_TAFLMAN, Coord.get(x, y - 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 11, Taflman.TYPE_TAFLMAN, Coord.get(x + 1, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 12, Taflman.TYPE_TAFLMAN, Coord.get(x - 1, y), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new Brandub7Defenders(board, getStartingTaflmen());
    }
}
