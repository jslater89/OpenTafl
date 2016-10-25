package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.manywords.softworks.tafl.engine.DetailedMoveRecord;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.notation.GameSerializer;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.window.mainmenu.TimeEntryDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by jay on 5/23/16.
 */
public class CreateGameDialog extends DialogWindow {
    
    public String hashedPassword;
    public Rules rules;
    public List<DetailedMoveRecord> history;
    private int settingsRulesIndex = -1;
    public boolean attackingSide;
    public boolean canceled;
    public boolean allowReplay;
    public boolean combineChat;
    public TimeSpec timeSpec = new TimeSpec(0, 0, 0, 0);

    // Used for loading games: time remaining
    public TimeSpec attackerClock = null;
    public TimeSpec defenderClock = null;

    private TerminalSize mCachedSize = new TerminalSize(0, 0);
    private TextBox mPasswordInput;

    public CreateGameDialog(String title) {
        super(title);

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        final Label rulesLabel = new Label("Rules: " + BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant));
        final Button rulesButton = new Button("Rules", () -> {
            String[] rulesTextArray = new String[BuiltInVariants.rulesDescriptions.size()];

            for(int i = 0; i < rulesTextArray.length; i++) {
                rulesTextArray[i] = BuiltInVariants.rulesDescriptions.get(i);
            }

            String rulesDescription = ListSelectDialog.showDialog(getTextGUI(), "Rules", "Select a rules variant", rulesTextArray);
            if(rulesDescription != null) {
                settingsRulesIndex = BuiltInVariants.indexForDescription(rulesDescription);
                rules = BuiltInVariants.availableRules.get(settingsRulesIndex);
                rulesLabel.setText("Rules: " + rules.toString());
            }
        });

        final Label timeLabel = new Label("Untimed");

        final Button loadButton = new Button("Load game", () -> {
            File gameFile = TerminalUtils.showFileChooserDialog(getTextGUI(), "Select saved game", "Open", new File("saved-games"));

            if(gameFile == null) {
                return;
            }

            GameSerializer.GameContainer g = null;
            try {
                 g = GameSerializer.loadGameRecordFile(gameFile);
            }
            catch(NotationParseException e) {
                MessageDialogBuilder builder = new MessageDialogBuilder();
                builder.setTitle("Failed to load game record");
                builder.setText("Game record parsing failed at index: " + e.index + "\n" +
                        "With context: " + e.context);
                builder.addButton(MessageDialogButton.OK);
                builder.build().showDialog(getTextGUI());
                return;
            }

            rules = g.game.getRules();
            history = new ArrayList<>(g.moves);

            Map<String, String> tagMap = g.game.getTagMap();

            if(tagMap.containsKey("time-control")) {
                String timeControl = tagMap.get("time-control");

                // TODO: refactor this block to a method in GameSerializer
                timeControl = timeControl.trim();
                if(timeControl.split(" ").length == 2) timeControl += " 0";
                else timeControl = timeControl.replaceAll("i", "");

                timeSpec = TimeSpec.parseMachineReadableString(timeControl, " ", 1000);

                if(timeSpec != null && timeSpec.isEnabled()) {
                    timeLabel.setText(timeSpec.toHumanString());
                }
            }

            if(tagMap.containsKey("time-remaining")) {
                String[] clockRecords = tagMap.get("time-remaining").split(",");

                clockRecords[0] = clockRecords[0].trim();
                clockRecords[1] = clockRecords[1].trim();

                if(clockRecords[0].split(" ").length == 2) clockRecords[0] += " 0";
                else clockRecords[0] = clockRecords[0].replaceAll("i", "");

                if(clockRecords[1].split(" ").length == 2) clockRecords[1] += " 0";
                else clockRecords[1] = clockRecords[1].replaceAll("i", "");

                attackerClock = TimeSpec.parseMachineReadableString(clockRecords[0], " ", 1000);
                defenderClock = TimeSpec.parseMachineReadableString(clockRecords[1], " ", 1000);
            }

            rulesLabel.setText("Rules: loaded " + rules.toString());
        });

        rules = BuiltInVariants.availableRules.get(TerminalSettings.variant);

        timeSpec = TerminalSettings.timeSpec;
        if(timeSpec != null && timeSpec.isEnabled()) {
            timeLabel.setText(timeSpec.toHumanString());
        }
        final Button timeButton = new Button("Clock settings", () -> {
            TimeEntryDialog d = new TimeEntryDialog("Clock settings");
            d.showDialog(getTextGUI());

            if(!d.timeSpec.isEnabled()) {
                timeSpec = new TimeSpec(0, 0, 0, 0);
                timeLabel.setText("Untimed");
            }
            else {
                timeSpec = d.timeSpec;
                timeLabel.setText(timeSpec.toHumanString());
            }
        });

        final Label passwordLabel = new Label("Password");
        mPasswordInput = new TextBox();
        mPasswordInput.setMask('*');
        mPasswordInput.setValidationPattern(Pattern.compile("([[a-z][A-Z][0-9]\\-_\\.\\*])+"));

        final Label sideLabel = new Label("Side");
        final RadioBoxList<String> sideChooser = new RadioBoxList<>();
        sideChooser.addItem("Attackers");
        sideChooser.addItem("Defenders");
        sideChooser.setCheckedItemIndex(0);

        final Label otherOptionsLabel = new Label("Other options");
        final CheckBoxList<String> optionsChooser = new CheckBoxList<>();
        optionsChooser.addItem("Combine spectator+player chat", true);
        optionsChooser.addItem("Allow replays and analysis", true);

        final Button finishButton = new Button("Create", () -> {
            if(sideChooser.getCheckedItem().equals("Attackers")) attackingSide = true;
            if(optionsChooser.isChecked(0)) combineChat = true;
            if(optionsChooser.isChecked(1)) allowReplay = true;
            hashedPassword = mPasswordInput.getText();
            hashedPassword = (hashedPassword.isEmpty() ? PasswordHasher.NO_PASSWORD : hashedPassword);
            if(!hashedPassword.equals(PasswordHasher.NO_PASSWORD)) {
                hashedPassword = PasswordHasher.hashPassword("", hashedPassword);
            }
            canceled = false;

            if(settingsRulesIndex >= 0) {
                TerminalSettings.variant = settingsRulesIndex;
            }
            TerminalSettings.timeSpec = timeSpec;

            CreateGameDialog.this.close();
        });

        final Button cancelButton = new Button("Cancel", () -> {
            canceled = true;
            CreateGameDialog.this.close();
        });

        Panel topGridPanel = new Panel();
        topGridPanel.setLayoutManager(new GridLayout(3));

        p.addComponent(rulesLabel);

        Panel rulesButtonPanel = new Panel();
        rulesButtonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        rulesButtonPanel.addComponent(rulesButton);
        rulesButtonPanel.addComponent(loadButton);

        p.addComponent(rulesButtonPanel);

        p.addComponent(TerminalUtils.newSpacer());

        topGridPanel.addComponent(timeButton);
        topGridPanel.addComponent(TerminalUtils.newSpacer());
        topGridPanel.addComponent(timeLabel);

        p.addComponent(topGridPanel);

        p.addComponent(TerminalUtils.newSpacer());

        p.addComponent(passwordLabel);
        p.addComponent(mPasswordInput);

        p.addComponent(TerminalUtils.newSpacer());

        p.addComponent(sideLabel);
        p.addComponent(sideChooser);

        p.addComponent(TerminalUtils.newSpacer());

        p.addComponent(otherOptionsLabel);
        p.addComponent(optionsChooser);

        Panel bottomGridPanel = new Panel();
        bottomGridPanel.setLayoutManager(new GridLayout(3));

        bottomGridPanel.addComponent(finishButton);
        bottomGridPanel.addComponent(cancelButton);
        bottomGridPanel.addComponent(TerminalUtils.newSpacer());

        p.addComponent(bottomGridPanel);

        setComponent(p);
    }

    @Override
    public Object showDialog(WindowBasedTextGUI textGUI) {
        return super.showDialog(textGUI);
    }

    @Override
    public TerminalSize getPreferredSize() {
        TerminalSize preferredSize = super.getPreferredSize();
        if(!preferredSize.equals(mCachedSize)) {
            mPasswordInput.setPreferredSize(new TerminalSize(1, 1));
            preferredSize = super.getPreferredSize();
            mCachedSize = preferredSize;
            mPasswordInput.setPreferredSize(new TerminalSize(preferredSize.getColumns() - 1, 1));
        }
        return preferredSize;
    }
}
