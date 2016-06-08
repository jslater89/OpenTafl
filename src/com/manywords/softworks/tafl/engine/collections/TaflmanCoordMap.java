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

    private final int mDimension;
    private final short mSize;
    private final byte mAttackers;
    private final byte mDefenders;

    public TaflmanCoordMap(int dimension, byte attackers, byte defenders) {
        this.mSize = (byte)(attackers + defenders);
        this.mAttackers = attackers;
        this.mDefenders = defenders;
        this.mDimension = dimension;
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
        this.mDimension = other.mDimension;
        mTaflmen = new char[mSize];
        mCoords = new char[mSize];

        for(int i = 0; i < mSize; i++) {
            mTaflmen[i] = other.mTaflmen[i];
            mCoords[i] = other.mCoords[i];
        }
    }

    public short size() { return mSize; }

    public Coord getCoord(char taflman) {
        if(taflman == Taflman.EMPTY) return null;

        char taflmanSide = Taflman.getPackedSide(taflman);
        byte taflmanId = Taflman.getPackedId(taflman);

        // Index: 0 to mDefenders - 1 for defenders, mDefenders - size for attackers;
        int index = taflmanId + (taflmanSide > 0 ? mDefenders : 0);
        char entry = mTaflmen[index];
        char coord = mCoords[index];

        char entrySide = Taflman.getPackedSide(entry);
        byte entryId = Taflman.getPackedId(entry);

        if(coord != (char) -1 && taflmanSide == entrySide && taflmanId == entryId) {
            return Coord.getCoordForIndex(mDimension, coord);
        }
        else {
            return null;
        }
    }

    public char getTaflman(Coord c) {
        return getTaflman(Coord.getIndex(mDimension, c));
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
        char coord = (char) Coord.getIndex(mDimension, space);

        mCoords[index] = coord;
        mTaflmen[index] = taflman;
    }

    public char[] getTaflmen() {
        return mTaflmen;
    }
    public List<Coord> getOccupiedSpaces() { return null; }

    public String toString() {
        String s = "";
        for(int i = 0; i < mSize; i++) {
            s += Taflman.getStringSymbol(mTaflmen[i]) + " id " + Taflman.getPackedId(mTaflmen[i]) + "@" + Coord.getCoordForIndex(mDimension, mCoords[i]) + ", ";
        }
        s += "\n";

        return s;
    }
}
