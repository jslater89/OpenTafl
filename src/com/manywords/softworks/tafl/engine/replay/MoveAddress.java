package com.manywords.softworks.tafl.engine.replay;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jay on 7/31/16.
 */
public class MoveAddress {
    static class Element {
        public int rootIndex;
        public int moveIndex;

        public Element() { }

        public Element(Element other) {
            rootIndex = other.rootIndex;
            moveIndex = other.moveIndex;
        }

        public boolean isVariation() {
            return moveIndex == -1;
        }

        public String toString() {
            StringBuilder name = new StringBuilder();
            name.append(rootIndex);
            if(moveIndex >= 0) {
                name.append((char) ('a' + moveIndex));
            }

            return name.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Element)) return false;
            Element other = (Element) obj;

            return rootIndex == other.rootIndex && moveIndex == other.moveIndex;
        }
    }

    public MoveAddress() { }

    public MoveAddress(MoveAddress other) {
        for(Element e : other.mElements) {
            mElements.add(new Element(e));
        }
    }

    public MoveAddress(List<Element> nonRootElements) {
        mElements.addAll(nonRootElements);
    }

    private List<Element> mElements = new ArrayList<>();

    public Element getRootElement() {
        return mElements.get(0);
    }

    public List<Element> getNonRootElements() {
        return getElementsStartingAt(1);
    }

    public List<Element> getElementsStartingAt(int start) {
        List<Element> elements = new ArrayList<>();
        for(int i = start; i < mElements.size(); i++) {
            elements.add(mElements.get(i));
        }

        return elements;
    }

    public MoveAddress changePrefix(MoveAddress currentPrefix, MoveAddress newPrefix) {
        if(currentPrefix.mElements.size() > mElements.size()) return null;

        for(int i = 0; i < currentPrefix.mElements.size(); i++) {
            Element thisElement = mElements.get(i);
            Element otherElement = currentPrefix.mElements.get(i);

            if(!thisElement.equals(otherElement)) return null;
        }

        List<Element> postPrefixElements = getElementsStartingAt(currentPrefix.mElements.size());
        MoveAddress newAddress = new MoveAddress();
        newAddress.mElements.addAll(newPrefix.mElements);
        newAddress.mElements.addAll(postPrefixElements);

        return newAddress;
    }

    public MoveAddress increment(ReplayGame game, ReplayGameState current) {
        MoveAddress other;
        Element inQuestion = mElements.get(mElements.size() - 1);

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
            int lengthOfTurn = inQuestion.moveIndex;
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
        Element inQuestion = other.mElements.get(other.mElements.size() - 1);

        if(newTurn) {
            inQuestion.rootIndex++;
            inQuestion.moveIndex = 0;
        }
        else {
            inQuestion.moveIndex++;
        }

        return other;
    }

    // Add a sibling: increment the variation node (size - 2), set the leaf node to 1a
    public MoveAddress nextSibling() {
        MoveAddress newSibling = new MoveAddress(this);
        if(newSibling.mElements.size() < 2) throw new IllegalArgumentException("No variations to add a sibling to! " + this);

        Element variationNode = newSibling.mElements.get(newSibling.mElements.size() - 2);
        variationNode.rootIndex++;

        Element leafNode = newSibling.mElements.get(newSibling.mElements.size() - 1);
        leafNode.rootIndex = 1;
        leafNode.moveIndex = 0;

        return newSibling;
    }

    // Add a new variation: add a new variation node and a new child node, set to 1 and 1a
    public MoveAddress nextVariation() {
        MoveAddress other = new MoveAddress(this);
        Element newVariation = new Element();
        newVariation.rootIndex = 1;
        newVariation.moveIndex = -1;

        Element newChild = new Element();
        newChild.rootIndex = 1;
        newChild.moveIndex = 0;

        other.mElements.add(newVariation);
        other.mElements.add(newChild);

        return other;
    }

    public static MoveAddress newRootAddress() {
        Element e = new Element();
        e.rootIndex = 1;
        e.moveIndex = 0;

        MoveAddress a = new MoveAddress();
        a.mElements.add(e);

        return a;
    }

    public static MoveAddress parseAddress(String address) {
        String[] elements = address.split("\\.");
        Pattern moveIndexPattern = Pattern.compile("([a-z])");

        MoveAddress moveAddress = new MoveAddress();

        for(String element : elements) {
            Element e = new Element();

            String moveIndex = "";
            Matcher matcher = moveIndexPattern.matcher(element);
            if(matcher.find()) moveIndex = matcher.group();
            String rootIndex = element.replaceAll("[a-z]", "");

            if(!moveIndex.isEmpty()) {
                if(moveIndex.length() == 1) {
                    e.moveIndex = moveIndex.charAt(0) - 'a';
                }
                else {
                    throw new IllegalArgumentException("Invalid address part: " + element);
                }
            }
            else {
                e.moveIndex = -1;
            }

            e.rootIndex = Integer.parseInt(rootIndex);

            moveAddress.mElements.add(e);
        }

        return moveAddress;
    }

    public String toString() {
        StringBuilder name = new StringBuilder();
        for(Element element : mElements) {
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
