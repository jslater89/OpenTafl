package com.manywords.softworks.tafl.engine.replay;

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
    }

    private List<AddressElement> mElements = new ArrayList<>();

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
            name.append(element.rootIndex);
            if(element.moveIndexInRoot >= 0) {
                name.append((char) ('a' + element.moveIndexInRoot));
            }
            name.append(".");
        }

        return name.toString();
    }
}
