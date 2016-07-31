package com.manywords.softworks.tafl.engine.replay;

import com.manywords.softworks.tafl.engine.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jay on 7/31/16.
 */
public class MoveAddress {
    private static class AddressElement {
        public int rootIndex;
        public int moveIndexInRoot;

        public AddressElement() { }

        public AddressElement(AddressElement other) {
            rootIndex = other.rootIndex;
            moveIndexInRoot = other.moveIndexInRoot;
        }

        public boolean isVariation() {
            return moveIndexInRoot == -1;
        }

        public String toString() {
            StringBuilder name = new StringBuilder();
            name.append(rootIndex);
            if(moveIndexInRoot >= 0) {
                name.append((char) ('a' + moveIndexInRoot));
            }

            return name.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof AddressElement)) return false;
            AddressElement other = (AddressElement) obj;

            return rootIndex == other.rootIndex && moveIndexInRoot == other.moveIndexInRoot;
        }
    }

    public MoveAddress() { }

    public MoveAddress(MoveAddress other) {
        for(AddressElement e : other.mElements) {
            mElements.add(new AddressElement(e));
        }
    }

    private List<AddressElement> mElements = new ArrayList<>();

    public MoveAddress increment(ReplayGame game, ReplayGameState current) {
        MoveAddress other;
        AddressElement inQuestion = mElements.get(mElements.size() - 1);

        boolean startingSideAttackers = game.getGame().getRules().getStartingSide().isAttackingSide();

        // The turn number never increments if the
        if(current.getCurrentSide().isAttackingSide() != startingSideAttackers) {
            other = increment(false);
        }
        else {
            boolean otherSideWent = false;

            // We should increment the turn if, between the start of the turn and now, the other side has gone
            // and we are the starting side.

            int historySize = game.getGame().getHistory().size();
            int lengthOfTurn = inQuestion.moveIndexInRoot;
            for (int i = historySize - 1 - lengthOfTurn; i < historySize; i++) {
                ReplayGameState rgs = (ReplayGameState) game.getGame().getHistory().get(i);
                if(rgs.getCurrentSide().isAttackingSide() != startingSideAttackers) {
                    otherSideWent = true;
                }
            }

            if(otherSideWent) {
                other = increment(true);
            }
            else {
                other = increment(false);
            }
        }

        return other;
    }

    public MoveAddress increment(boolean newTurn) {
        MoveAddress other = new MoveAddress(this);
        AddressElement inQuestion = other.mElements.get(other.mElements.size() - 1);

        if(newTurn) {
            inQuestion.rootIndex++;
            inQuestion.moveIndexInRoot = 0;
        }
        else {
            inQuestion.moveIndexInRoot++;
        }

        return other;
    }

    // Add a sibling: increment the variation node (size - 2), set the leaf node to 1a
    public MoveAddress addSibling() {
        MoveAddress newSibling = new MoveAddress(this);
        if(newSibling.mElements.size() < 2) throw new IllegalArgumentException("No variations to add a sibling to! " + this);

        AddressElement variationNode = newSibling.mElements.get(newSibling.mElements.size() - 2);
        variationNode.rootIndex++;

        AddressElement leafNode = newSibling.mElements.get(newSibling.mElements.size() - 1);
        leafNode.rootIndex = 1;
        leafNode.moveIndexInRoot = 0;

        return newSibling;
    }

    // Add a new variation: add a new variation node and a new child node, set to 1 and 1a
    public MoveAddress addVariation() {
        MoveAddress other = new MoveAddress(this);
        AddressElement newVariation = new AddressElement();
        newVariation.rootIndex = 1;
        newVariation.moveIndexInRoot = -1;

        AddressElement newChild = new AddressElement();
        newChild.rootIndex = 1;
        newChild.moveIndexInRoot = 0;

        other.mElements.add(newVariation);
        other.mElements.add(newChild);

        return other;
    }

    public static MoveAddress newRootAddress() {
        AddressElement e = new AddressElement();
        e.rootIndex = 1;
        e.moveIndexInRoot = 0;

        MoveAddress a = new MoveAddress();
        a.mElements.add(e);

        return a;
    }

    public static MoveAddress parseAddress(String address) {
        String[] elements = address.split("\\.");
        Pattern moveIndexPattern = Pattern.compile("([a-z])");

        MoveAddress moveAddress = new MoveAddress();

        for(String element : elements) {
            AddressElement e = new AddressElement();

            String moveIndex = "";
            Matcher matcher = moveIndexPattern.matcher(element);
            if(matcher.find()) moveIndex = matcher.group();
            String rootIndex = element.replaceAll("[a-z]", "");

            if(!moveIndex.isEmpty()) {
                if(moveIndex.length() == 1) {
                    e.moveIndexInRoot = moveIndex.charAt(0) - 'a';
                }
                else {
                    throw new IllegalArgumentException("Invalid address part: " + element);
                }
            }
            else {
                e.moveIndexInRoot = -1;
            }

            e.rootIndex = Integer.parseInt(rootIndex);

            moveAddress.mElements.add(e);
        }

        return moveAddress;
    }

    public String toString() {
        StringBuilder name = new StringBuilder();
        for(AddressElement element : mElements) {
            name.append(element.toString());
            name.append(".");
        }

        return name.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MoveAddress)) return false;
        MoveAddress other = (MoveAddress) obj;

        if(mElements.size() != other.mElements.size()) return false;

        boolean equal = true;
        for(int i = 0; i < mElements.size(); i++) {
            if(!mElements.get(i).equals(other.mElements.get(i))) {
                equal = false;
                break;
            }
        }

        return equal;
    }
}
