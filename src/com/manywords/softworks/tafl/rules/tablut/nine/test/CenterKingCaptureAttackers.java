package com.manywords.softworks.tafl.rules.tablut.nine.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.tablut.nine.Tablut9Attackers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 4/9/16.
 */
public class CenterKingCaptureAttackers extends Tablut9Attackers {
    public CenterKingCaptureAttackers(Board board) {
        super(board);
    }

    public CenterKingCaptureAttackers(Board board, List<TaflmanHolder> taflmen) {
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

    public List<TaflmanHolder> generateTaflmen() {
        ArrayList<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(12);

        int x = 4;
        int y = 4;

        // Bottom group
        taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(x, y + 1), this, getBoard(), getBoard().getRules()));

        // Top group
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(x, y - 2), this, getBoard(), getBoard().getRules()));

        // Left group
        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(x - 1, y), this, getBoard(), getBoard().getRules()));

        // Right group
        taflmen.add(new TaflmanImpl((byte) 3, Taflman.TYPE_TAFLMAN, Coord.get(x + 2, y), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new Tablut9Attackers(board, getStartingTaflmen());
    }
}
