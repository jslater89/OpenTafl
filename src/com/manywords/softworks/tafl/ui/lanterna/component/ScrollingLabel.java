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
    public enum Direction {
        BACKSCROLL, // Count up when scrolling up the screen
        SCROLL // Count up when scrolling down the screen
    }

    // Queue
    /*
     * mStringBuffer holds up to 512 strings, which are broken up for display
     * by the line buffer.
     */

    private boolean mForceScrollCount;
    private Direction mDirection = Direction.BACKSCROLL;
    private List<String> mStringBuffer;
    private List<String> mLineBuffer;
    private int mHeight;
    private int mWidth;
    private int mStartPosition = 0;

    public ScrollingLabel() {
        this("");
    }

    public ScrollingLabel(String text) {
        super(text);
        mStringBuffer = new ArrayList<String>(512);
        mLineBuffer = new ArrayList<String>(1024);
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
        mLineBuffer.clear();

        if(mStringBuffer.size() == 0) return;

        for(int i = mStringBuffer.size() - 1; i >= 0; i--) {
            String s = mStringBuffer.get(i);
            List<String> lines = TerminalTextUtils.getWordWrappedText(mWidth, s);

            for(String line : lines) {
                mLineBuffer.add(0, line);
            }
        }

        if(mStartPosition > (mLineBuffer.size() - mHeight)) {
            mStartPosition = Math.max(mLineBuffer.size() - mHeight, 0);
        }

        String out = lineBufferToString(mStartPosition);
        setText(out);
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

        String[] lines = text.split("\n");
        for(String line : lines) {
            if(line.equals("")) line = " ";

            mStringBuffer.add(0, line);
        }

        // We'll get the string next time we get a setSize.
        if(mWidth == 0) return;

        List<String> wrappedLine = TerminalTextUtils.getWordWrappedText(mWidth, text);
        for (String s : wrappedLine) {
            mLineBuffer.add(0, s);
        }

        while (mStringBuffer.size() > 512) {
            String s = mStringBuffer.remove(512);
            List<String> wrappedRemoved = TerminalTextUtils.getWordWrappedText(mWidth, s);

            for(int i = 0; i < wrappedRemoved.size(); i++) {
                mLineBuffer.remove(mLineBuffer.size() - 1);
            }
        }

        if(mStartPosition != 0) {
            mStartPosition = Math.min(mStartPosition + wrappedLine.size(), mLineBuffer.size() - mHeight);
        }

        String out = lineBufferToString(mStartPosition);
        setText(out);
    }

    public void handleScroll(boolean page, boolean up) {
        int distance = (page ? mHeight - 4 : 1);
        if(up) {
            // Always leave enough in the line buffer so that we fill the message window when we scroll up.
            // If the line buffer is smaller than the height, start at zero.
            mStartPosition = Math.min(mStartPosition + distance, Math.max(mLineBuffer.size() - mHeight, 0));
        }
        else {
            // Never look into the future.
            mStartPosition = Math.max(mStartPosition - distance, 0);
        }

        handleScroll();
    }

    private void handleScroll() {
        String out = lineBufferToString(mStartPosition);
        setText(out);
    }

    private String lineBufferToString(int offset) {
        StringBuilder out = new StringBuilder();
        int lines = getSize().getRows() + offset;
        int start = Math.min(lines, mLineBuffer.size()) - 1;

        boolean singleScreen = mLineBuffer.size() < lines;

        if(mForceScrollCount && !singleScreen) {
            if(start < mLineBuffer.size() - 1) {
                start -= 1;
            }
            else {
                offset += 1;
            }
        }

        for(int i = start; i >= offset; i--) {
            if(offset > 0 && i == offset && !mForceScrollCount) {
                out.append(getScrollCount());
            }
            else {
                out.append(mLineBuffer.get(i));
                out.append("\n");
            }
        }

        if(mForceScrollCount && !singleScreen) {
            out.append(getScrollCount());
        }

        return out.toString();
    }

    private String getScrollCount() {
        String scrollCount = "[Scrolled at ";

        if(mDirection == Direction.BACKSCROLL) {
            scrollCount += mStartPosition + "/" + mLineBuffer.size();
        }
        else {
            scrollCount += ((mLineBuffer.size() - mStartPosition) - getSize().getRows()) + "/" + (mLineBuffer.size() - getSize().getRows());
        }
        scrollCount += "]\n";

        return scrollCount;
    }

    public void scrollToStart() {
        if(mHeight == 0) {
            mStartPosition = 1024;
        }
        else {
            mStartPosition = Math.max(mLineBuffer.size() - mHeight, 0);
        }
        handleScroll();
    }

    public void scrollToEnd() {
        mStartPosition = 0;
        handleScroll();
    }

    public void forceScrollCount(boolean force) {
        mForceScrollCount = force;
    }

    public void setScrollCountDirection(Direction d) {
        mDirection = d;
    }
}
