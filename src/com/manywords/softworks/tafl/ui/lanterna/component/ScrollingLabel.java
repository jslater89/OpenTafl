package com.manywords.softworks.tafl.ui.lanterna.component;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class ScrollingLabel extends Label {
    // Queue
    /*
     * mStringBuffer holds up to 512 strings, which are broken up for display
     * by the line buffer.
     */

    private List<String> mStringBuffer;
    private List<String> mLineBuffer;
    private int mHeight;
    private int mWidth;
    private int mStartPosition = 0;

    public ScrollingLabel(String text) {
        super(text);
        mStringBuffer = new ArrayList<String>(512);
        mLineBuffer = new ArrayList<String>(512);
        clearAndAddLine(text);
    }

    @Override
    public synchronized Label setSize(TerminalSize size) {
        Label result = super.setSize(size);
        mHeight = size.getRows();
        mWidth = size.getColumns();

        resize();

        return result;
    }

    public synchronized void clearAndAddLine(String text) {
        addLine(text, true);
    }

    public synchronized void resize() {
        mHeight = getSize().getRows();
        mWidth = getSize().getColumns();

        mLineBuffer.clear();

        for(int i = mStringBuffer.size() - 1; i >= 0; i--) {
            String s = mStringBuffer.get(i);
            List<String> lines = TerminalTextUtils.getWordWrappedText(mWidth, s);

            for(String line : lines) {
                mLineBuffer.add(0, line);
            }

            if(mLineBuffer.size() > 512) break;
        }
    }

    public synchronized void addLine(String text) {
        addLine(text, false);
    }

    private synchronized void addLine(String text, boolean clear) {
        if(mLineBuffer == null || mStringBuffer == null) return; //during init
        if(text == null || text.equals("")) return; // crashes TTU.getWordWrappedText

        if (clear) {
            mLineBuffer.clear();
            mStringBuffer.clear();
        }

        mStringBuffer.add(0, text);

        List<String> wrappedLine = TerminalTextUtils.getWordWrappedText(mWidth, text);
        for (String s : wrappedLine) {
            mLineBuffer.add(0, s);
        }

        while (mStringBuffer.size() >= 512) mStringBuffer.remove(511);
        while (mLineBuffer.size() >= 512) mLineBuffer.remove(511);

        if(mStartPosition != 0) {
            mStartPosition = Math.min(mStartPosition + wrappedLine.size(), mLineBuffer.size() - mHeight);
        }

        String out = lineBufferToString(mStartPosition);
        setText(out);
    }

    public void handleScroll(boolean up) {
        if(up) {
            // Always leave enough in the line buffer so that we fill the message window when we scroll up.
            mStartPosition = Math.min(mStartPosition + mHeight - 4, mLineBuffer.size() - mHeight);
        }
        else {
            // Never look into the future.
            mStartPosition = Math.max(mStartPosition - mHeight + 4, 0);
        }

        String out = lineBufferToString(mStartPosition);
        setText(out);
    }

    private String lineBufferToString(int offset) {
        StringBuilder out = new StringBuilder();
        int lines = getSize().getRows() + offset;
        int start = Math.min(lines, mLineBuffer.size()) - 1;

        for(int i = start; i >= offset; i--) {
            if(offset > 0 && i == offset) {
                out.append("[Scrolled at ");
                out.append(mStartPosition);
                out.append("/");
                out.append(mLineBuffer.size());
                out.append("]\n");
            }
            else {
                out.append(mLineBuffer.get(i));
                out.append("\n");
            }
        }

        return out.toString();
    }
}
