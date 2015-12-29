package com.manywords.softworks.tafl.rules.seabattle.nine.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.seabattle.nine.SeaBattle9Attackers;

import java.util.ArrayList;
import java.util.List;

public class AITwoEdgeEscapeTestAttackers extends SeaBattle9Attackers {
    public AITwoEdgeEscapeTestAttackers(Board board) {
        super(board);
    }

    public AITwoEdgeEscapeTestAttackers(Board board, List<TaflmanHolder> taflmen) {
        super(board, taflmen);
    }

    public List<TaflmanHolder> generateTaflmen() {
        ArrayList<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(9);

        int x = 4;
        int y = 4;

        taflmen.add(new TaflmanImpl((byte) 0, Taflman.TYPE_TAFLMAN, Coord.get(2, 3), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 1, Taflman.TYPE_TAFLMAN, Coord.get(3, 2), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(0, 0), this, getBoard(), getBoard().getRules()));

        return createHolderListFromTaflmanList(taflmen);
    }

    @Override
    public Side deepCopy(Board board) {
        return new AITwoEdgeEscapeTestAttackers(board, getStartingTaflmen());
    }
}
