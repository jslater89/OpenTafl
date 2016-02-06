package com.manywords.softworks.tafl.rules;

import java.util.ArrayList;
import java.util.List;

public abstract class Side {
    public static class TaflmanHolder {
        public final char packed;
        public final Coord coord;

        public TaflmanHolder(char packed, Coord coord) {
            this.packed = packed;
            this.coord = coord;
        }

        public String toString() {
            return "TaflmanHolder: " + packed + "@" + coord;
        }
    }

    public Side(Board board) {
        mBoard = board;
        mStartingTaflmen = new ArrayList<TaflmanHolder>();

        setStartingTaflmen(generateTaflmen());
    }

    public Side(Board board, List<TaflmanHolder> taflmen) {
        mBoard = board;
        // No need to copy. Starting taflmen are final.
        setStartingTaflmen(taflmen);
    }

    public abstract boolean isAttackingSide();

    public abstract boolean hasCommanders();

    public abstract boolean hasKnights();

    public abstract Side deepCopy(Board board);

    public abstract List<TaflmanHolder> generateTaflmen();

    private char[] mTaflmen;
    protected Board mBoard;
    private List<TaflmanHolder> mStartingTaflmen;

    public final Board getBoard() {
        return mBoard;
    }

    public final void onTaflmanMoved(char taflman, Coord destination) {

    }

    public final void setStartingTaflmen(List<TaflmanHolder> taflmen) {
        mStartingTaflmen = taflmen;
    }

    public final List<TaflmanHolder> getStartingTaflmen() {
        return mStartingTaflmen;
    }

    public final List<Character> getTaflmen() {
        return mBoard.getTaflmenWithMask(Taflman.SIDE_MASK, getSideChar());
    }

    public final List<TaflmanHolder> createHolderListFromTaflmanList(List<TaflmanImpl> taflmen) {
        List<TaflmanHolder> map = new ArrayList<TaflmanHolder>(taflmen.size());
        for (TaflmanImpl t : taflmen) {
            map.add(t.getImplId(), new TaflmanHolder(Taflman.encode(t), t.getStartingSpace()));
        }

        if (taflmen.size() != map.size()) {
            throw new IllegalArgumentException("Taflman ID collision!");
        }

        return map;
    }

    public final char getSideChar() {
        return (isAttackingSide() ? Taflman.SIDE_ATTACKERS : 0);
    }

    /**
     * Filter a list of taflmen so that it only contains
     * our taflmen.
     *
     * @return
     */
    public final List<Character> onlyOurTaflmen(List<Character> unsorted) {
        List<Character> ours = new ArrayList<Character>(12);
        char thisSide = getSideChar();
        for (char taflman : unsorted) {
            if ((taflman & Taflman.SIDE_MASK) == thisSide) {
                ours.add(taflman);
            }
        }

        return ours;
    }
}
