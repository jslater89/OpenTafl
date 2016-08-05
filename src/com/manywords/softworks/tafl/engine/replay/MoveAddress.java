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

        public Element(int rootAddress, int moveAddress) {
            rootIndex = rootAddress;
            moveIndex = moveAddress;
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

    public MoveAddress(List<Element> rootElements, Element newTip) {
        mElements.addAll(rootElements);
        mElements.add(newTip);
    }

    public MoveAddress(List<Element> nonRootElements) {
        mElements.addAll(nonRootElements);
    }

    private List<Element> mElements = new ArrayList<>();

    public List<Element> getElements() {
        List<Element> elements = new ArrayList<>(mElements.size());
        elements.addAll(mElements);
        return elements;
    }

    public Element getRootElement() {
        return mElements.get(0);
    }

    public Element getLastElement() {
        return mElements.get(mElements.size() - 1);
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

    public List<Element> getAllRootElements() {
        return getElementsBefore(mElements.size() - 1);
    }

    public List<Element> getElementsBefore(int end) {
        List<Element> elements = new ArrayList<>();
        for(int i = 0; i < Math.min(end, mElements.size()); i++) {
            elements.add(mElements.get(i));
        }

        return elements;
    }

    public MoveAddress changePrefix(MoveAddress currentPrefix, MoveAddress newPrefix) {
        if(currentPrefix.mElements.size() > mElements.size()) {
            throw new IllegalArgumentException("Couldn't replace " + currentPrefix + " with " + newPrefix + " in " + this + ": prefix too long");
        }

        for(int i = 0; i < currentPrefix.mElements.size(); i++) {
            Element thisElement = mElements.get(i);
            Element otherElement = currentPrefix.mElements.get(i);

            if(!thisElement.equals(otherElement)) {
                throw new IllegalArgumentException("Couldn't replace " + currentPrefix + " with " + newPrefix + " in " + this + ": prefix doesn't match");
            }
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

        boolean otherSideWent = false;
        boolean thisSideWentPreviously = false;

        // We should increment the turn if, between the start of the turn and now, the other side has gone
        // and we are the starting side. Since we're deep in a tree, we have to follow the tree back up to
        // know what the history is so we can know if we should increment. Hooray for tracking parents!

        int lengthOfTurn = inQuestion.moveIndex;
        List<ReplayGameState> backwardStates = new ArrayList<>(lengthOfTurn);

        backwardStates.add(current);
        ReplayGameState s = current;
        while((s = s.getParent()) != null && backwardStates.size() <= lengthOfTurn) {
            backwardStates.add(s);
        }

        List<ReplayGameState> states = new ArrayList<>(backwardStates.size());

        for(int i = backwardStates.size() - 1; i >= 0; i--) {
            states.add(backwardStates.get(i));
        }

        for (ReplayGameState rgs : states) {
            if(rgs.getCurrentSide().isAttackingSide() != startingSideAttackers) {
                otherSideWent = true;
            }
            else {
                thisSideWentPreviously = true;
            }
        }


        if(otherSideWent && thisSideWentPreviously) {
            other = increment(true);
        }
        else {
            other = increment(false);
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

    public MoveAddress nextVariation(int index) {
        MoveAddress other = new MoveAddress(this);
        Element newVariation = new Element();
        newVariation.rootIndex = index;
        newVariation.moveIndex = -1;

        other.mElements.add(newVariation);
        return other;
    }

    // TODO: change both branches to increment to make state numbering absolute in variations
    // e.g. 1a.1.1b.1.2a.1.2b...
    public MoveAddress firstChild(ReplayGame game, ReplayGameState state) {
        MoveAddress address = state.getParent().getMoveAddress();
        if(address.getElements().size() == 1) {
            return firstChild(1, address.getLastElement().moveIndex);
        }
        else {
            MoveAddress lastElement = new MoveAddress();
            lastElement.mElements.add(address.getLastElement());
            MoveAddress incremented = lastElement.increment(game, state);
            return firstChild(1, incremented.getLastElement().moveIndex);
        }
    }

    public MoveAddress firstChild(int rootIndex, int moveIndex) {
        MoveAddress other = new MoveAddress(this);
        Element newChild = new Element();
        newChild.rootIndex = rootIndex;
        newChild.moveIndex = moveIndex;

        other.mElements.add(newChild);

        return other;
    }

    // Add a new variation: add a new variation node and a new child node, set to 1 and 1a
    public MoveAddress nextVariationFirstState() {
        MoveAddress other = nextVariation(1);
        return other.firstChild(1, 0);
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
                    return null;
                    //throw new IllegalArgumentException("Invalid address part: " + element);
                }
            }
            else {
                e.moveIndex = -1;
            }

            try {
                e.rootIndex = Integer.parseInt(rootIndex);
            }
            catch(NumberFormatException ex) {
                return null;
            }

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
