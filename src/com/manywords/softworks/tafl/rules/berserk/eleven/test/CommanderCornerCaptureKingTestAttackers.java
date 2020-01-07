package com.manywords.softworks.tafl.rules.berserk.eleven.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.taflmen.Commander;

import java.util.ArrayList;
import java.util.List;

public class CommanderCornerCaptureKingTestAttackers extends Side {
    public CommanderCornerCaptureKingTestAttackers(Board board) {
        super(board);
    }

    public CommanderCornerCaptureKingTestAttackers(Board board, List<TaflmanHolder> taflmen) {
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
    public boolean hasGuards() {
        return false;
    }

    @Override
    public Side deepCopy(Board board) {
        return new CommanderCornerCaptureKingTestAttackers(board, getStartingTaflmen());
    }

    @Override
    public List<TaflmanHolder> generateTaflmen() {
        List<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(13);

        // Right side
        taflmen.add(new Commander((byte) 0, Coord.get(2, 1), this, getBoard(), getBoard().getRules()));

        // Top
        taflmen.add(new Commander((byte) 1, Coord.get(5, 3), this, getBoard(), getBoard().getRules()));

        // Left side
        taflmen.add(new Commander((byte) 2, Coord.get(2, 5), this, getBoard(), getBoard().getRules()));

        // Bottom
        taflmen.add(new Commander((byte) 3, Coord.get(6, 5), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 4, Taflman.TYPE_TAFLMAN, Coord.get(4, 0), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }
}
