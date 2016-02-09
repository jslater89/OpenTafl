package com.manywords.softworks.tafl.engine.collections;

import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.List;

/**
 * Created by jay on 1/7/16.
 */
public class TaflmanCoordMap {
    private char[] mTaflmen;
    private char[] mCoords;

    private final short mSize;
    private final byte mAttackers;
    private final byte mDefenders;

    public TaflmanCoordMap(byte attackers, byte defenders) {
        this.mSize = (byte)(attackers + defenders);
        this.mAttackers = attackers;
        this.mDefenders = defenders;
        mTaflmen = new char[mSize];
        mCoords = new char[mSize];

        for(int i = 0; i < mSize; i++) {
            mCoords[i] = (char) -1;
        }
    }

    public TaflmanCoordMap(TaflmanCoordMap other) {
        mSize = other.size();
        this.mAttackers = other.mAttackers;
        this.mDefenders = other.mDefenders;
        mTaflmen = new char[mSize];
        mCoords = new char[mSize];

        for(int i = 0; i < mSize; i++) {
            mTaflmen[i] = other.mTaflmen[i];
            mCoords[i] = other.mCoords[i];
        }
    }

    public short size() { return mSize; }

    public Coord get(char taflman) {
        char taflmanSide = Taflman.getPackedSide(taflman);
        byte taflmanId = Taflman.getPackedId(taflman);

        // Index: 0 to mDefenders - 1 for defenders, mDefenders - size for attackers;
        int index = taflmanId + (taflmanSide > 0 ? mDefenders : 0);
        char entry = mTaflmen[index];
        char coord = mCoords[index];

        if(coord != (char) -1 && taflman == entry) {
            return Coord.getCoordForIndex(coord);
        }
        else {
            return null;
        }
    }

    public char getTaflman(Coord c) {
        return getTaflman(Coord.getIndex(c));
    }

    public char getTaflman(int c) {
        for(int i = 0; i < mCoords.length; i++) {
            if(mCoords[i] == c) return mTaflmen[i];
        }
        return Taflman.EMPTY;
    }

    public void remove(char taflman) {
        char taflmanSide = Taflman.getPackedSide(taflman);
        byte taflmanId = Taflman.getPackedId(taflman);

        // Index: 0 to mDefenders - 1 for defenders, mDefenders - size for attackers;
        int index = taflmanId + (taflmanSide > 0 ? mDefenders : 0);
        mCoords[index] = (char) -1;
        mTaflmen[index] = Taflman.EMPTY;
    }

    public void put(char taflman, Coord space) {
        char taflmanSide = Taflman.getPackedSide(taflman);
        byte taflmanId = Taflman.getPackedId(taflman);

        // Index: 0 to mDefenders - 1 for defenders, mDefenders - size for attackers;
        int index = taflmanId + (taflmanSide > 0 ? mDefenders : 0);
        char coord = (char) Coord.getIndex(space);

        mCoords[index] = coord;
        mTaflmen[index] = taflman;
    }

    public char[] getTaflmen() {
        return mTaflmen;
    }
    public List<Coord> getOccupiedSpaces() { return null; }
}
