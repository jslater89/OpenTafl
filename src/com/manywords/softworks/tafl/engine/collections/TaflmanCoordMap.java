package com.manywords.softworks.tafl.engine.collections;

import com.manywords.softworks.tafl.engine.Utilities;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

/**
 * Created by jay on 1/7/16.
 */
public class TaflmanCoordMap {
    private char[] mTaflmen;
    private char[] mCoords;
    private byte[] mTaflmanIndexByCoord;

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
        mTaflmanIndexByCoord = new byte[mDimension * mDimension];

        Utilities.fillArray(mCoords, (char) -1);
        Utilities.fillArray(mTaflmanIndexByCoord, (byte) -1);
    }

    public TaflmanCoordMap(TaflmanCoordMap other) {
        mSize = other.size();
        this.mAttackers = other.mAttackers;
        this.mDefenders = other.mDefenders;
        this.mDimension = other.mDimension;
        mTaflmen = new char[mSize];
        mCoords = new char[mSize];
        mTaflmanIndexByCoord = new byte[mDimension * mDimension];

        for(int i = 0; i < mTaflmanIndexByCoord.length; i++) {
            if(i < mSize) {
                mTaflmen[i] = other.mTaflmen[i];
                mCoords[i] = other.mCoords[i];
            }
            mTaflmanIndexByCoord[i] = other.mTaflmanIndexByCoord[i];
        }
    }

    public short size() { return mSize; }

    public Coord getCoord(char taflman) {
        if(taflman == Taflman.EMPTY) return null;

        char taflmanSide = Taflman.getPackedSide(taflman);
        byte taflmanId = Taflman.getPackedId(taflman);

        char entry = mTaflmen[taflmanId];
        char coord = mCoords[taflmanId];

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
        byte index = mTaflmanIndexByCoord[c];
        if(index != -1) return mTaflmen[index];
        else return Taflman.EMPTY;
    }

    public void remove(char taflman) {
        byte taflmanId = Taflman.getPackedId(taflman);

        char coord = mCoords[taflmanId];

        mCoords[taflmanId] = (char) -1;
        mTaflmen[taflmanId] = Taflman.EMPTY;
        mTaflmanIndexByCoord[coord] = (byte) -1;
    }

    public void put(char taflman, Coord space) {
        char taflmanSide = Taflman.getPackedSide(taflman);
        byte taflmanId = Taflman.getPackedId(taflman);

        char coord = (char) Coord.getIndex(mDimension, space);
        char oldCoord = mCoords[taflmanId];
        mCoords[taflmanId] = coord;
        mTaflmen[taflmanId] = taflman;
        mTaflmanIndexByCoord[coord] = (byte) taflmanId;

        if(oldCoord != (char) -1) {
            mTaflmanIndexByCoord[oldCoord] = (byte) -1;
        }
    }

    public char[] getTaflmen() {
        return mTaflmen;
    }

    public String toString() {
        String s = "";
        for(int i = 0; i < mSize; i++) {
            s += Taflman.getStringSymbol(mTaflmen[i]) + " id " + Taflman.getPackedId(mTaflmen[i]) + "@" + Coord.getCoordForIndex(mDimension, mCoords[i]) + "i" + mTaflmanIndexByCoord[mCoords[i]]+ "/" + i + ", ";
        }
        s += "\n";

        return s;
    }
}
