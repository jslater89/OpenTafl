package com.manywords.softworks.tafl.rules.seabattle.nine;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.taflmen.King;

import java.util.ArrayList;
import java.util.List;

public class SeaBattle9Defenders extends Side {
    public SeaBattle9Defenders(Board board) {
        super(board);
    }

    public SeaBattle9Defenders(Board board, List<TaflmanHolder> taflmen) {
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
    public boolean hasGuards() {
        return false;
    }

    public List<TaflmanHolder> generateTaflmen() {
        ArrayList<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(9);

        int x = 4;
        int y = 4;

        taflmen.add(new King((byte) 16, Coord.get(x, y), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 17, Taflman.TYPE_TAFLMAN, Coord.get(x, y + 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 18, Taflman.TYPE_TAFLMAN, Coord.get(x, y + 2), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 19, Taflman.TYPE_TAFLMAN, Coord.get(x, y - 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 20, Taflman.TYPE_TAFLMAN, Coord.get(x, y - 2), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 21, Taflman.TYPE_TAFLMAN, Coord.get(x + 2, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 22, Taflman.TYPE_TAFLMAN, Coord.get(x + 1, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 23, Taflman.TYPE_TAFLMAN, Coord.get(x - 1, y), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 24, Taflman.TYPE_TAFLMAN, Coord.get(x - 2, y), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new SeaBattle9Defenders(board, getStartingTaflmen());
    }
}
