package com.manywords.softworks.tafl.rules.berserk.eleven.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.taflmen.Commander;

import java.util.ArrayList;
import java.util.List;

public class JumpCaptureTestAttackers extends Side {
    public JumpCaptureTestAttackers(Board board) {
        super(board);
    }

    public JumpCaptureTestAttackers(Board board, List<TaflmanHolder> taflmen) {
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
        return true;
    }

    @Override
    public Side deepCopy(Board board) {
        return new JumpCaptureTestAttackers(board, getStartingTaflmen());
    }

    @Override
    public boolean hasGuards() {
        return false;
    }

    @Override
    public List<TaflmanHolder> generateTaflmen() {
        List<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(13);

        // King jump test
        taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(5, 6), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(5, 8), this, getBoard(), getBoard().getRules()));

        // Knight capture test
        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(4, 3), this, getBoard(), getBoard().getRules()));
        taflmen.add(new Commander((byte) 3, Coord.get(4, 1), this, getBoard(), getBoard().getRules()));

        // Left side
        taflmen.add(new Commander((byte) 4, Coord.get(0, 6), this, getBoard(), getBoard().getRules()));

        // Bottom
        taflmen.add(new Commander((byte) 5, Coord.get(6, 5), this, getBoard(), getBoard().getRules()));

        taflmen.add(new Commander((byte) 6, Coord.get(10, 8), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 7, Taflman.TYPE_TAFLMAN, Coord.get(2, 10), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }
}
