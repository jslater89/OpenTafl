package com.manywords.softworks.tafl.rules.berserk.eleven.test;

import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.rules.taflmen.King;
import com.manywords.softworks.tafl.rules.taflmen.Knight;

import java.util.ArrayList;
import java.util.List;

public class JumpCaptureTestDefenders extends Side {
    public JumpCaptureTestDefenders(Board board) {
        super(board);
    }

    public JumpCaptureTestDefenders(Board board, List<TaflmanHolder> taflmen) {
        super(board, taflmen);
    }

    @Override
    public boolean isAttackingSide() {
        return false;
    }

    @Override
    public boolean hasKnights() {
        return true;
    }

    @Override
    public boolean hasCommanders() {
        return false;
    }

    @Override
    public Side deepCopy(Board board) {
        return new JumpCaptureTestDefenders(board, getStartingTaflmen());
    }

    @Override
    public List<TaflmanHolder> generateTaflmen() {
        List<TaflmanImpl> taflmen = new ArrayList<TaflmanImpl>(13);

        // 5,5 is center
        taflmen.add(new King((byte) 0, Coord.get(5, 5), this, getBoard(), getBoard().getRules()));

        // Adjacent spaces to 5,5
        taflmen.add(new Knight((byte) 1, Coord.get(4, 4), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 2, Taflman.TYPE_TAFLMAN, Coord.get(0, 7), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 3, Taflman.TYPE_TAFLMAN, Coord.get(0, 9), this, getBoard(), getBoard().getRules()));
        taflmen.add(new TaflmanImpl((byte) 4, Taflman.TYPE_TAFLMAN, Coord.get(2, 9), this, getBoard(), getBoard().getRules()));

        taflmen.add(new TaflmanImpl((byte) 5, Taflman.TYPE_TAFLMAN, Coord.get(10, 9), this, getBoard(), getBoard().getRules()));

        // The 'point spaces'

        return createHolderListFromTaflmanList(taflmen);
    }
}
