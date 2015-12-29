package com.manywords.softworks.tafl.engine;

import com.manywords.softworks.tafl.rules.Coord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jay on 12/11/15.
 */
public class TaflmanMoveCache {
    private Map<Character, List<Coord>> mCachedAllowableMoves = new HashMap<Character, List<Coord>>();
    private Map<Character, List<Coord>> mCachedAllowableDestinations = new HashMap<Character, List<Coord>>();
    private Map<Character, List<Coord>> mCachedCapturingMoves = new HashMap<Character, List<Coord>>();
    private Map<Character, Set<Coord>> mCachedReachableSpaces = new HashMap<Character, Set<Coord>>();

    public void setCachedAllowableMovesForTaflman(char taflman, List<Coord> moves) {
        mCachedAllowableMoves.put(taflman, moves);
    }

    public void setCachedAllowableDestinationsForTaflman(char taflman, List<Coord> moves) {
        mCachedAllowableDestinations.put(taflman, moves);
    }

    public void setCachedCapturingMovesForTaflman(char taflman, List<Coord> moves) {
        mCachedCapturingMoves.put(taflman, moves);
    }

    public void setCachedReachableSpacesForTaflman(char taflman, Set<Coord> moves) {
        mCachedReachableSpaces.put(taflman, moves);
    }

    public List<Coord> getCachedAllowableMovesForTaflman(char taflman) {
        return mCachedAllowableMoves.get(taflman);
    }

    public List<Coord> getCachedAllowableDestinationsForTaflman(char taflman) {
        return mCachedAllowableDestinations.get(taflman);
    }

    public List<Coord> getCachedCapturingMovesForTaflman(char taflman) {
        return mCachedCapturingMoves.get(taflman);
    }

    public Set<Coord> getCachedReachableSpacesForTaflman(char taflman) {
        return mCachedReachableSpaces.get(taflman);
    }
}
