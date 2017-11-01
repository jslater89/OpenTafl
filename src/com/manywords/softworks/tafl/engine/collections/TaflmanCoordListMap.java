package com.manywords.softworks.tafl.engine.collections;

import com.manywords.softworks.tafl.engine.Utilities;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 1/7/16.
 */
public class TaflmanCoordListMap {
    private char[] mTaflmen;
    private TableEntry[] mEntries;
    private boolean[] mDirty;

    private final int mDimension;
    private final short mSize;
    private final short mAttackers;
    private final short mDefenders;

    public TaflmanCoordListMap(int dimension, int attackers, int defenders) {
        this.mSize = (short)(attackers + defenders);
        this.mAttackers = (short) attackers;
        this.mDefenders = (short) defenders;
        mDimension = dimension;
        mTaflmen = new char[mSize];
        mEntries = new TableEntry[mSize];
        mDirty = new boolean[mSize];

        for(int i = 0; i < mSize; i++) {
            mEntries[i] = new TableEntry();
        }
    }

    public TaflmanCoordListMap(TaflmanCoordListMap other) {
        mSize = other.size();
        this.mAttackers = other.mAttackers;
        this.mDefenders = other.mDefenders;
        this.mDimension = other.mDimension;
        mTaflmen = new char[mSize];
        mEntries = new TableEntry[mSize];
        mDirty = new boolean[mSize];

        for(int i = 0; i < mSize; i++) {
            mTaflmen[i] = other.mTaflmen[i];
            mDirty[i] = other.mDirty[i];

            mEntries[i].coords = new char[other.mEntries[i].coords.length];
            for(int j = 0; j < other.mEntries[i].coords.length; j++) {
                mEntries[i].coords[j] = other.mEntries[i].coords[j];
            }
        }
    }

    public short size() { return mSize; }

    public List<Coord> get(char taflman) {
        int index = Taflman.getPackedId(taflman);
        if(index < 0) index += 256;
        if(mDirty[index]) return null;

        char[] coords = mEntries[index].coords;
        if(coords != null && coords.length > 0) {
            ArrayList<Coord> list = new ArrayList<Coord>(coords.length);
            for(int i = 0; i < coords.length; i++) {
                list.add(Coord.getCoordForIndex(mDimension, coords[i]));
            }
            return list;
        }

        return null;
    }

    public void reset() {
        Utilities.fillArray(mTaflmen, Taflman.EMPTY);
        Utilities.fillArray(mDirty, true);
    }

    public void remove(char taflman) {
        int index = Taflman.getPackedId(taflman);
        if(index < 0) index += 256;

        mEntries[index].coords = new char[0];
        mTaflmen[index] = Taflman.EMPTY;
        mDirty[index] = false;
    }

    public void put(char taflman, List<Coord> spaces) {
        int index = Taflman.getPackedId(taflman);
        if(index < 0) index += 256;

        char[] coords = new char[spaces.size()];

        int i = 0;
        for(Coord c : spaces) {
            coords[i++] = (char) Coord.getIndex(mDimension, c);
        }

        mEntries[index].coords = coords;
        mTaflmen[index] = taflman;
        mDirty[index] = false;
    }

    public char[] getTaflmen() {
        return mTaflmen;
    }

    private class TableEntry {
        char[] coords;
    }
}
