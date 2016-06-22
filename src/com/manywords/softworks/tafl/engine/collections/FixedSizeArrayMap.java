package com.manywords.softworks.tafl.engine.collections;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Created by jay on 6/22/16.
 */
public class FixedSizeArrayMap<K, V> implements Map<K, V> {
    private K[] mKeys;
    private V[] mValues;

    private int mMaxSize = 0;
    private int mSize = 0;

    public FixedSizeArrayMap(int size) {
        mKeys = (K[]) new Object[size];
        mValues = (V[]) new Object[size];

        for(int i = 0; i < size; i++) {
            mKeys[i] = null;
            mValues[i] = null;
        }

        mMaxSize = size;
    }

    @Override
    public int size() {
        return mSize;
    }

    @Override
    public boolean isEmpty() {
        return mSize == 0;
    }

    private int getKeyIndex(Object key) {
        /* This optimization is not needed by means of better structuring of data in Coord
        if(key instanceof Integer && mKeys instanceof Integer[]) {
            int index = (Integer) key;
            if(index >= 0 && index < mKeys.length) {
                if((Integer) mKeys[index] == index) {
                    // Short-circuit optimization for a map containing a 0-n list of coords
                    return index;
                }
            }
        }
        */
        for(int i = 0; i < mKeys.length; i++) {
            if(key.equals(mKeys[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean containsKey(Object key) {
        return getKeyIndex(key) != -1;
    }

    @Override
    public boolean containsValue(Object value) {
        for(V val : mValues) {
            if(value.equals(val)) return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        int index = getKeyIndex(key);
        if(index == -1) {
            return null;
        }
        else {
            return mValues[index];
        }
    }

    @Override
    public V put(K key, V value) {
        int index = getKeyIndex(key);
        if(index != -1) {
            V prev = mValues[index];
            mValues[index] = value;

            return prev;
        }
        else {
            for(int i = 0; i < mKeys.length; i++) {
                if(mKeys[i] == null) {
                    mKeys[i] = key;
                    mValues[i] = value;
                    mSize++;
                    return null;
                }
            }
        }

        // Error handling
        /*
        System.out.print("{");
        for(int i = 0; i < mKeys.length; i++) {
            System.out.print(mKeys[i] + "=" + mValues[i] + ",");
        }
        System.out.println("}");
        */
        throw new IllegalStateException("Overfilled fixed-size map");
    }

    @Override
    public V remove(Object key) {
        int index = getKeyIndex(key);
        if(index != -1) {
            V prev = mValues[index];
            mValues[index] = null;
            mSize--;

            return prev;
        }
        else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        for(int i = 0; i < mKeys.length; i++) {
            mKeys[i] = null;
        }
        mSize = 0;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>(mSize);
        for(K key : mKeys) {
            if(key != null) set.add(key);
        }
        return set;
    }

    @Override
    public Collection<V> values() {
        Collection<V> set = new ArrayList<>(mSize);
        for(int i = 0; i < mKeys.length; i++) {
            if(mKeys[i] != null) set.add(mValues[i]);
        }
        return set;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> set = new HashSet<>();
        for(K key : keySet()) {
            set.add(new FixedSizeMapEntry<K, V>(key, get(key)));
        }

        return set;
    }

    public static class FixedSizeMapEntry<K, V> implements Entry<K, V> {
        private K key;
        private V value;

        public FixedSizeMapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            return null;
        }
    }
}
