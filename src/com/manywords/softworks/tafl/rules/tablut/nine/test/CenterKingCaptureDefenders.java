package com.manywords.softworks.tafl.rules.tablut.nine.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.tablut.nine.Tablut9Defenders;
import com.manywords.softworks.tafl.rules.taflmen.King;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 4/9/16.
 */
public class CenterKingCaptureDefenders extends Tablut9Defenders {
    public CenterKingCaptureDefenders(Board board) {
        super(board);
    }

    public CenterKingCaptureDefenders(Board board, List<TaflmanHolder> taflmen) {
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

    public List<TaflmanHolder> generateTaflmen() {
        ArrayList<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(9);

        taflmen.add(new King((byte) 0, Coord.get(4, 4), this, getBoard(), getBoard().getRules()));

        int x = 4;
        int y = 4;

        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(x + 1, y + 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(x - 1, y - 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 3, Taflman.TYPE_TAFLMAN, Coord.get(x + 1, y - 1), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 4, Taflman.TYPE_TAFLMAN, Coord.get(x - 1, y + 1), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new Tablut9Defenders(board, getStartingTaflmen());
    }
}
