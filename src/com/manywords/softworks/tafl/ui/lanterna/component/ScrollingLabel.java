package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class ScrollingLabel extends Label {
    // Queue
    private List<String> mLineBuffer;

    public ScrollingLabel(String text) {
        super(text);
        mLineBuffer = new ArrayList<String>(512);
        clearAndAddLine(text);
    }

    public synchronized void clearAndAddLine(String text) {
        addLine(text, true);
    }

    public synchronized void resize() {
        addLine("", false, true);
    }

    public synchronized void addLine(String text) {
        addLine(text, false);
    }

    private synchronized void addLine(String text, boolean clear) {
        addLine(text, clear, false);
    }

    private synchronized void addLine(String text, boolean clear, boolean resize) {
        if(mLineBuffer == null) return; //during init
        if(text == null || text.equals("") || resize) return; // crashes TTU.getWordWrappedText

        if (clear || resize) mLineBuffer.clear();
        if(!resize) {
            List<String> wrappedLine = TerminalTextUtils.getWordWrappedText(getSize().getColumns(), text);
            for (String s : wrappedLine) {
                mLineBuffer.add(0, s);
            }

            while (mLineBuffer.size() > 512) mLineBuffer.remove(511);
        }
        String out = lineBufferToString();
        setText(out);
    }

    private String lineBufferToString() {
        StringBuilder out = new StringBuilder();
        int lines = getSize().getRows();
        int start = Math.min(lines, mLineBuffer.size()) - 1;

        for(int i = start; i >= 0; i--) {
            out.append(mLineBuffer.get(i));
            out.append("\n");
        }

        return out.toString();
    }
}
