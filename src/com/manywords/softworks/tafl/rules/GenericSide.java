package com.manywords.softworks.tafl.rules;

import java.util.List;

/**
 * Created by jay on 2/6/16.
 */
public class GenericSide extends Side {
    private boolean mAttackingSide;

    public GenericSide(Board b) {
        super(b);
    }

    public GenericSide(Board b, boolean isAttackingSide, List<TaflmanHolder> taflmen) {
        super(b, taflmen);
        this.mAttackingSide = isAttackingSide;
    }

    @Override
    public boolean isAttackingSide() {
        return false;
    }

    @Override
    public boolean hasCommanders() {
        return false;
    }

    @Override
    public boolean hasKnights() {
        return false;
    }

    @Override
    public Side deepCopy(Board board) {
        return new GenericSide(board, isAttackingSide(), getStartingTaflmen());
    }

    @Override
    public List<TaflmanHolder> generateTaflmen() {
        return null;
    }
}
