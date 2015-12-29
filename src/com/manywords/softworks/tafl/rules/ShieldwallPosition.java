package com.manywords.softworks.tafl.rules;

import java.util.List;

public class ShieldwallPosition {
    public final List<Coord> surroundedSpaces;
    public final List<Character> surroundingTaflmen;

    public ShieldwallPosition(List<Coord> spaces, List<Character> taflmen) {
        surroundedSpaces = spaces;
        surroundingTaflmen = taflmen;
    }

    public String toString() {
        return "Shieldwall position: " + surroundedSpaces + " surrounded by " + surroundingTaflmen;
    }
}
