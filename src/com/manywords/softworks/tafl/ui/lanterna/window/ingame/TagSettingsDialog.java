package com.manywords.softworks.tafl.ui.lanterna.window.ingame;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.manywords.softworks.tafl.engine.replay.MoveAddress;
import com.manywords.softworks.tafl.engine.replay.ReplayGame;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by jay on 8/28/16.
 */
public class TagSettingsDialog extends DialogWindow {
    private TextBox mCompilerTextBox, mAnnotatorTextBox, mPuzzlePrestartTextBox, mPuzzleStartTextBox;
    private RadioBoxList<String> mPuzzleModeRadio;
    private Map<String, String> mTagMap;
    private ReplayGame mReplay;

    public TagSettingsDialog(ReplayGame game) {
        super("Tag settings");
        mTagMap = game.getGame().getTagMap();
        mReplay = game;

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        mainPanel.addComponent(TerminalUtils.newSpacer());

        // Compiler label ---------------------------------------
        Label compilerLabel = new Label("Compiler");
        mCompilerTextBox = new TextBox();
        mCompilerTextBox.setText(mTagMap.getOrDefault("compiler", "OpenTafl"));

        mainPanel.addComponent(compilerLabel);
        mainPanel.addComponent(mCompilerTextBox);
        mainPanel.addComponent(TerminalUtils.newSpacer());

        // Annotator label ---------------------------------------
        Label annotatorLabel = new Label("Annotator");
        mAnnotatorTextBox = new TextBox();
        mAnnotatorTextBox.setText(mTagMap.getOrDefault("annotator", ""));

        mainPanel.addComponent(annotatorLabel);
        mainPanel.addComponent(mAnnotatorTextBox);
        mainPanel.addComponent(TerminalUtils.newSpacer());

        // Puzzle mode label ---------------------------------------
        Label puzzleModeLabel = new Label("Puzzle mode");
        mPuzzleModeRadio = new RadioBoxList<>();
        mPuzzleModeRadio.addItem("None");
        mPuzzleModeRadio.addItem("Loose");
        mPuzzleModeRadio.addItem("Strict");

        String puzzleMode = mTagMap.getOrDefault("puzzle-mode", "none");

        mPuzzleModeRadio.setCheckedItem("None");
        if(puzzleMode.toLowerCase().equals("loose")) mPuzzleModeRadio.setCheckedItem("Loose");
        if(puzzleMode.toLowerCase().equals("strict")) mPuzzleModeRadio.setCheckedItem("Strict");

        mainPanel.addComponent(puzzleModeLabel);
        mainPanel.addComponent(mPuzzleModeRadio);
        mainPanel.addComponent(TerminalUtils.newSpacer());

        // Prestart label ---------------------------------------
        Label puzzlePrestartLabel = new Label("Puzzle prestart");
        mPuzzlePrestartTextBox = new TextBox();
        mPuzzlePrestartTextBox.setText(mTagMap.getOrDefault("puzzle-prestart", ""));

        mainPanel.addComponent(puzzlePrestartLabel);
        mainPanel.addComponent(mPuzzlePrestartTextBox);
        mainPanel.addComponent(TerminalUtils.newSpacer());

        // Start label ---------------------------------------
        Label puzzleStartLabel = new Label("Puzzle start");
        mPuzzleStartTextBox = new TextBox();
        mPuzzleStartTextBox.setText(mTagMap.getOrDefault("puzzle-start", "0a"));

        mainPanel.addComponent(puzzleStartLabel);
        mainPanel.addComponent(mPuzzleStartTextBox);
        mainPanel.addComponent(TerminalUtils.newSpacer());

        // Buttons label ---------------------------------------
        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Button cancelButton = new Button("Cancel", this::close);
        Button saveButton = new Button("Save", () -> {
            List<String> errors = checkData();

            if(errors.size() == 0) {
                saveData();
                this.close();
            }
            else {
                showErrorDialog(errors);
            }
        });

        buttonPanel.addComponent(saveButton);
        buttonPanel.addComponent(cancelButton);
        mainPanel.addComponent(TerminalUtils.newSpacer());

        mainPanel.addComponent(buttonPanel);

        setComponent(mainPanel);
    }

    private List<String> checkData() {
        List<String> errors = new ArrayList<>();
        if(!mPuzzleModeRadio.getCheckedItem().equals("None")) {
            if(!mPuzzlePrestartTextBox.getText().isEmpty()) {
                MoveAddress prestart = MoveAddress.parseAddress(mPuzzlePrestartTextBox.getText());
                if(prestart == null) errors.add("Prestart move address is invalid.");
                else if(mReplay.getStateByAddress(prestart) == null) errors.add("Prestart address doesn't address a state.");
            }

            if(!mPuzzleStartTextBox.getText().isEmpty()) {
                MoveAddress start = MoveAddress.parseAddress(mPuzzleStartTextBox.getText());
                if(start == null) errors.add("Start move address is invalid.");
                else if(mReplay.getStateByAddress(start) == null) errors.add("Start address doesn't address a state.");
            }
        }

        return errors;
    }

    private void saveData() {
        if(!mCompilerTextBox.getText().isEmpty()) mTagMap.put("compiler", mCompilerTextBox.getText());
        if(!mAnnotatorTextBox.getText().isEmpty()) mTagMap.put("annotator", mAnnotatorTextBox.getText());

        mTagMap.put("puzzle-mode", mPuzzleModeRadio.getCheckedItem().toLowerCase());

        if(mPuzzlePrestartTextBox.getText().isEmpty()) mTagMap.remove("puzzle-prestart");
        else mTagMap.put("puzzle-prestart", mPuzzlePrestartTextBox.getText());

        if(mPuzzleStartTextBox.getText().isEmpty()) mTagMap.remove("puzzle-start");
        else mTagMap.put("puzzle-start", mPuzzleStartTextBox.getText());

        mReplay.markDirty();
    }

    private void showErrorDialog(List<String> errors) {
        MessageDialogBuilder b = new MessageDialogBuilder();
        b.setTitle("Errors detected");

        String errorString = "";
        for(String error : errors) {
            errorString += error + "\n";
        }

        b.setText(errorString);

        b.addButton(MessageDialogButton.OK);
        b.build().showDialog(getTextGUI());
    }
}
