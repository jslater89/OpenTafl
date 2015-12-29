package com.manywords.softworks.tafl.rules.brandub.seven.test;

import com.manywords.softworks.tafl.rules.*;

import java.util.ArrayList;
import java.util.List;


public class AITwoCornerEscapeTestAttackers extends Side {
    public AITwoCornerEscapeTestAttackers(Board board) {
        super(board);
    }

    public AITwoCornerEscapeTestAttackers(Board board, List<TaflmanHolder> taflmen) {
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

        taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(2, 3), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(4, 3), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new AITwoCornerEscapeTestAttackers(board, getStartingTaflmen());
    }
}
