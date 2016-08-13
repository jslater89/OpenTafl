package com.manywords.softworks.tafl.ui.lanterna.window.ingame;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;

import java.util.List;

/**
 * Created by jay on 8/9/16.
 */
public class AnnotationDialog extends DialogWindow {
    public AnnotationDialog(String title, TerminalSize screenSize, ReplayGame game) {
        super(title);

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        String comment;
        if(game.getCurrentState().getEnteringMove() != null) {
            comment = ((DetailedMoveRecord) game.getCurrentState().getEnteringMove()).getComment();
        }
        else {
            comment = game.getGame().getTagMap().get("start-comment");
        }

        int width = 60;
        int height = 30;

        if(screenSize.getColumns() < width) {
            width = screenSize.getColumns() - 6;
        }

        if(screenSize.getRows() < height) {
            height = screenSize.getRows() - 4;
        }

        TextBox box = new TextBox(new TerminalSize(width, height), TextBox.Style.MULTI_LINE);
        box.setPreferredSize(new TerminalSize(width, height));
        if(comment != null && !comment.isEmpty()) {
            List<String> lines = TerminalUtils.wrapTextPreservingNewlines(width, comment);
            box.setText(TerminalUtils.linesToString(lines));
        }

        p.addComponent(box);

        Panel buttons = new Panel();
        buttons.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button dismissButton = new Button("Cancel", this::close);
        Button okayButton = new Button("OK", () -> {
            String newComment = box.getText();

            newComment = newComment.replaceAll("\n\n","XXXXX");
            newComment = newComment.replaceAll("\n", " ");
            newComment = newComment.replaceAll("XXXXX", "\n\n");

            if(game.getCurrentState().getEnteringMove() != null) {
                ((DetailedMoveRecord) game.getCurrentState().getEnteringMove()).setComment(newComment);
            }
            else {
                game.getGame().getTagMap().put("start-comment", newComment);
            }

            close();
        });

        buttons.addComponent(dismissButton);
        buttons.addComponent(okayButton);

        p.addComponent(buttons);

        setComponent(p);
    }
}
