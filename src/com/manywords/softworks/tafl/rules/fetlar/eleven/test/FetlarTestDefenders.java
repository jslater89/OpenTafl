package com.manywords.softworks.tafl.rules.fetlar.eleven.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.taflmen.King;

import java.util.ArrayList;
import java.util.List;

public class FetlarTestDefenders extends Side {
    public FetlarTestDefenders(Board board) {
        super(board);
    }

    public FetlarTestDefenders(Board board, List<TaflmanHolder> taflmen) {
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
    public Side deepCopy(Board board) {
        return new FetlarTestDefenders(board, getStartingTaflmen());
    }

    public List<TaflmanHolder> generateTaflmen() {
        List<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(13);

        // 5,5 is center
        taflmen.add(new King((byte) 18, Coord.get(0, 2), this, getBoard(), getBoard().getRules()));

        // Adjacent spaces to 5,5
        taflmen.add(new TaflmanImpl((byte) 19, Taflman.TYPE_TAFLMAN, Coord.get(5, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 20, Taflman.TYPE_TAFLMAN, Coord.get(6, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 21, Taflman.TYPE_TAFLMAN, Coord.get(4, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 22, Taflman.TYPE_TAFLMAN, Coord.get(4, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 23, Taflman.TYPE_TAFLMAN, Coord.get(6, 4), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 24, Taflman.TYPE_TAFLMAN, Coord.get(6, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 25, Taflman.TYPE_TAFLMAN, Coord.get(4, 4), this, getBoard(), getBoard().getRules()));

        // The 'point spaces'
        taflmen.add(new TaflmanImpl((byte) 26, Taflman.TYPE_TAFLMAN, Coord.get(5, 7), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 27, Taflman.TYPE_TAFLMAN, Coord.get(7, 5), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 28, Taflman.TYPE_TAFLMAN, Coord.get(3, 5), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }
}
