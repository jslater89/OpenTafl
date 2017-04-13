package com.manywords.softworks.tafl.rules;

import java.util.List;

/**
 * Created by jay on 2/6/16.
 */
public class GenericSide extends Side {
    private boolean mAttackingSide;
    private List<TaflmanHolder> mStartingTaflmen;
    private boolean mCommanders = false;
    private boolean mKnights = false;

    public GenericSide(Board b, boolean isAttackingSide) {
        super(b);
        mAttackingSide = isAttackingSide;
    }

    public GenericSide(Board b, boolean isAttackingSide, List<TaflmanHolder> taflmen) {
        super(b, taflmen);
        this.mAttackingSide = isAttackingSide;
        mStartingTaflmen = taflmen;

        for(TaflmanHolder holder : taflmen) {
            char taflman = holder.packed;
            if(Taflman.getPackedType(taflman) == Taflman.TYPE_COMMANDER) {
                mCommanders = true;
            }
            if(Taflman.getPackedType(taflman) == Taflman.TYPE_KNIGHT) {
                mKnights = true;
            }
        }
    }

    @Override
    public boolean isAttackingSide() {
        return mAttackingSide;
    }

    @Override
    public boolean hasCommanders() {
        return mCommanders;
    }

    @Override
    public boolean hasKnights() {
        return mKnights;
    }

    @Override
    public Side deepCopy(Board board) {
        return new GenericSide(board, isAttackingSide(), getStartingTaflmen());
    }

    @Override
    public List<TaflmanHolder> generateTaflmen() {
        return mStartingTaflmen;
    }
}
