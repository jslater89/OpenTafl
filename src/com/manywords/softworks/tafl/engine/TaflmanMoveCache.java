package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.engine.collections.TaflmanCoordListMap;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jay on 12/11/15.
 */
public class TaflmanMoveCache {
    private static int mDimension;
    private static TaflmanCoordListMap mCachedAllowableMoves;
    private static TaflmanCoordListMap mCachedAllowableDestinations;
    private static TaflmanCoordListMap mCachedCapturingMoves;
    private static TaflmanCoordListMap mCachedReachableSpaces;
    private static TaflmanCoordListMap mCachedJumps;

    private static byte mAttackers;
    private static byte mDefenders;
    private static long mValidZobrist;

    private static boolean usable = true;

    public static void reset(int dimension, long zobrist, byte attackers, byte defenders) {
        if(mAttackers != attackers || mDefenders != defenders || mDimension != dimension) {
            mDimension = dimension;
            mAttackers = attackers;
            mDefenders = defenders;

            mCachedAllowableMoves = new TaflmanCoordListMap(mDimension, attackers, defenders);
            mCachedAllowableDestinations = new TaflmanCoordListMap(mDimension, attackers, defenders);
            mCachedCapturingMoves = new TaflmanCoordListMap(mDimension, attackers, defenders);
            mCachedReachableSpaces = new TaflmanCoordListMap(mDimension, attackers, defenders);
            mCachedJumps = new TaflmanCoordListMap(mDimension, attackers, defenders);
        }

        mCachedAllowableMoves.reset();
        mCachedAllowableDestinations.reset();
        mCachedCapturingMoves.reset();
        mCachedReachableSpaces.reset();
        mCachedJumps.reset();
        usable = true;

        mValidZobrist = zobrist;
    }

    private static boolean check(long zobrist) {
        if(!usable) return false;

        if(mValidZobrist != zobrist) {
            mCachedAllowableMoves.reset();
            mCachedAllowableDestinations.reset();
            mCachedCapturingMoves.reset();
            mCachedReachableSpaces.reset();
            mCachedJumps.reset();
            mValidZobrist = zobrist;

            return false;
        }
        else return true;
    }

    public static void invalidate() {
        usable = false;
    }

    public static void setCachedAllowableMovesForTaflman(long zobrist, char taflman, List<Coord> moves) {
        if(!usable) return;
        check(zobrist);
        mCachedAllowableMoves.put(taflman, moves);
    }

    public static void setCachedAllowableDestinationsForTaflman(long zobrist, char taflman, List<Coord> moves) {
        if(!usable) return;
        check(zobrist);
        mCachedAllowableDestinations.put(taflman, moves);
    }

    public static void setCachedJumpsForTaflman(long zobrist, char taflman, List<Coord> jumps) {
        if(!usable) return;
        check(zobrist);
        mCachedJumps.put(taflman, jumps);
    }

    public static void setCachedCapturingMovesForTaflman(long zobrist, char taflman, List<Coord> moves) {
        if(!usable) return;
        check(zobrist);
        mCachedCapturingMoves.put(taflman, moves);
    }

    public static void setCachedReachableSpacesForTaflman(long zobrist, char taflman, List<Coord> moves) {
        if(!usable) return;
        check(zobrist);
        mCachedReachableSpaces.put(taflman, moves);
    }

    public static List<Coord> getCachedAllowableMovesForTaflman(long zobrist, char taflman) {
        if(!check(zobrist)) return null;
        return mCachedAllowableMoves.get(taflman);
    }

    public static List<Coord> getCachedAllowableDestinationsForTaflman(long zobrist, char taflman) {
        if(!check(zobrist)) return null;
        return mCachedAllowableDestinations.get(taflman);
    }

    public static List<Coord> getCachedJumpsForTaflman(long zobrist, char taflman) {
        if(!check(zobrist)) return null;
        return mCachedJumps.get(taflman);
    }

    public static List<Coord> getCachedCapturingMovesForTaflman(long zobrist, char taflman) {
        if(!check(zobrist)) return null;
        return mCachedCapturingMoves.get(taflman);
    }

    public static List<Coord> getCachedReachableSpacesForTaflman(long zobrist, char taflman) {
        if(!check(zobrist)) return null;
        return mCachedReachableSpaces.get(taflman);
    }
}
