package com.manywords.softworks.tafl.rules.brandub.seven.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.taflmen.King;

import java.util.ArrayList;
import java.util.List;

public class AITwoCornerEscapeTestDefenders extends Side {
    public AITwoCornerEscapeTestDefenders(Board board) {
        super(board);
    }

    public AITwoCornerEscapeTestDefenders(Board board, List<TaflmanHolder> taflmen) {
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

    @Override
    public List<TaflmanHolder> generateTaflmen() {
        List<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(9);

        int x = 3;
        int y = 3;

        taflmen.add(new King((byte) 0, Coord.get(3, 0), this, getBoard(), getBoard().getRules()));

        // Give it another piece to faff about with
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(x, y + 1), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new AITwoCornerEscapeTestDefenders(board, getStartingTaflmen());
    }
}
