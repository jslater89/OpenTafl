package com.manywords.softworks.tafl.engine.ai.tables;

import com.manywords.softworks.tafl.engine.ai.evaluators.Evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jay on 12/16/15.
 */
public class DeepeningTable {
    private List<HashMap<Long, Short>> mDeepeningTable;

    public DeepeningTable(int maxDepth) {
        mDeepeningTable = new ArrayList<HashMap<Long, Short>>(maxDepth);

        for (int i = 0; i < maxDepth; i++) {
            mDeepeningTable.add(i, new HashMap<Long, Short>());
        }
    }

    public void putEntry(int depth, long zobrist, short value) {
        //System.out.println("Added " + zobrist + " at depth " + depth);
        mDeepeningTable.get(depth).put(zobrist, value);
    }

    public boolean depthHasData(int depth) {
        return mDeepeningTable.get(depth).size() > 0;
    }

    public boolean entryExists(int zobrist) {
        return entryExistsDeeperThan(0, zobrist);
    }

    public boolean entryExistsAtDepth(int depth, int zobrist) {
        return mDeepeningTable.get(depth).containsKey(zobrist);
    }

    public boolean entryExistsDeeperThan(int depth, int zobrist) {
        for (int i = depth; i < mDeepeningTable.size(); i++) {
            if (entryExistsAtDepth(i, zobrist)) {
                return true;
            }
        }
        return false;
    }

    public short getEntry(long zobrist) {
        return getEntryDeeperThan(0, zobrist);
    }

    public short getEntryAtDepth(int depth, long zobrist) {
        Short entry = mDeepeningTable.get(depth).get(zobrist);
        //System.out.println("Looking for " + zobrist + " at depth " + depth);
        if (entry == null) return Evaluator.NO_VALUE;
        else {
            //System.out.println("Found " + zobrist + " entry " + entry);
            return entry;
        }
    }

    public short getEntryDeeperThan(int depth, long zobrist) {
        Short best = null;
        for (int i = depth; i < mDeepeningTable.size(); i++) {
            short entryAtDepth = getEntryAtDepth(i, zobrist);
            if (entryAtDepth != Evaluator.NO_VALUE) best = entryAtDepth;
        }

        if (best == null) return Evaluator.NO_VALUE;
        else return best;
    }
}
