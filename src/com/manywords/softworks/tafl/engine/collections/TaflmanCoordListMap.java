package com.manywords.softworks.tafl.engine.collections;

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

    private final short mSize;
    private final byte mAttackers;
    private final byte mDefenders;

    public TaflmanCoordListMap(byte attackers, byte defenders) {
        this.mSize = (byte)(attackers + defenders);
        this.mAttackers = attackers;
        this.mDefenders = defenders;
        mTaflmen = new char[mSize];
        mEntries = new TableEntry[mSize];

        for(int i = 0; i < mSize; i++) {
            mEntries[i] = new TableEntry();
        }
    }

    public TaflmanCoordListMap(TaflmanCoordListMap other) {
        mSize = other.size();
        this.mAttackers = other.mAttackers;
        this.mDefenders = other.mDefenders;
        mTaflmen = new char[mSize];
        mEntries = new TableEntry[mSize];

        for(int i = 0; i < mSize; i++) {
            mTaflmen[i] = other.mTaflmen[i];

            mEntries[i].coords = new char[other.mEntries[i].coords.length];
            for(int j = 0; j < other.mEntries[i].coords.length; j++) {
                mEntries[i].coords[j] = other.mEntries[i].coords[j];
            }
        }
    }

    public short size() { return mSize; }

    public List<Coord> get(char taflman) {
        char taflmanSide = Taflman.getPackedSide(taflman);
        byte taflmanId = Taflman.getPackedId(taflman);

        // Index: 0 to mDefenders - 1 for defenders, mDefenders - size for attackers;
        int index = taflmanId + (taflmanSide > 0 ? mDefenders : 0);
        char[] coords = mEntries[index].coords;

        if(coords.length > 0) {
            ArrayList<Coord> list = new ArrayList<Coord>(coords.length);
            for(int i = 0; i < coords.length; i++) {
                list.add(Coord.getCoordForIndex(coords[i]));
            }
            return list;
        }
        else {
            return null;
        }
    }

    public void reset() {
        for(int i = 0; i < mSize; i++) {
            mEntries[i].coords = new char[0];
            mTaflmen[i] = Taflman.EMPTY;
        }
    }

    public void remove(char taflman) {
        char taflmanSide = Taflman.getPackedSide(taflman);
        byte taflmanId = Taflman.getPackedId(taflman);

        // Index: 0 to mDefenders - 1 for defenders, mDefenders - size for attackers;
        int index = taflmanId + (taflmanSide > 0 ? mDefenders : 0);
        mEntries[index].coords = new char[0];
        mTaflmen[index] = Taflman.EMPTY;
    }

    public void put(char taflman, List<Coord> spaces) {
        char taflmanSide = Taflman.getPackedSide(taflman);
        byte taflmanId = Taflman.getPackedId(taflman);

        // Index: 0 to mDefenders - 1 for defenders, mDefenders - size for attackers;
        int index = taflmanId + (taflmanSide > 0 ? mDefenders : 0);
        char[] coords = new char[spaces.size()];

        int i = 0;
        for(Coord c : spaces) {
            coords[i++] = (char) Coord.getIndex(c);
        }

        mEntries[index].coords = coords;
        mTaflmen[index] = taflman;
    }

    public char[] getTaflmen() {
        return mTaflmen;
    }

    private class TableEntry {
        char[] coords = {};
    }
}
