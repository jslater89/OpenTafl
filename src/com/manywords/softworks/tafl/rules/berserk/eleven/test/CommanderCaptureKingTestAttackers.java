package com.manywords.softworks.tafl.rules.berserk.eleven.test;

import com.manywords.softworks.tafl.rules.Board;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.TaflmanImpl;
import com.manywords.softworks.tafl.rules.taflmen.Commander;

import java.util.ArrayList;
import java.util.List;

public class CommanderCaptureKingTestAttackers extends Side {
    public CommanderCaptureKingTestAttackers(Board board) {
        super(board);
        setStartingTaflmen(generateTaflmen());
    }

    public CommanderCaptureKingTestAttackers(Board board, List<TaflmanHolder> taflmen) {
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
        return true;
    }

    @Override
    public Side deepCopy(Board board) {
        return new CommanderCaptureKingTestAttackers(board, getStartingTaflmen());
    }

    public List<TaflmanHolder> generateTaflmen() {
        List<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(24);

        // Right side
        taflmen.add(new Commander((byte) 0, Coord.get(5, 6), this, getBoard(), getBoard().getRules()));

        // Top
        taflmen.add(new Commander((byte) 1, Coord.get(5, 3), this, getBoard(), getBoard().getRules()));

        // Left side
        taflmen.add(new Commander((byte) 2, Coord.get(2, 5), this, getBoard(), getBoard().getRules()));

        // Bottom
        taflmen.add(new Commander((byte) 3, Coord.get(6, 5), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }
}
