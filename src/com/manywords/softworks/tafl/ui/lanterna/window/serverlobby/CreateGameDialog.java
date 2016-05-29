package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialog;
import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.PasswordHasher;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;
import com.manywords.softworks.tafl.ui.lanterna.window.mainmenu.TimeEntryDialog;

import java.util.regex.Pattern;

/**
 * Created by jay on 5/23/16.
 */
public class CreateGameDialog extends DialogWindow {
    
    public String hashedPassword;
    public Rules rules;
    public boolean attackingSide;
    public boolean canceled;
    public TimeSpec timeSpec = new TimeSpec(0, 0, 0, 0);

    private TerminalSize mCachedSize = new TerminalSize(0, 0);
    private TextBox mPasswordInput;

    public CreateGameDialog(String title) {
        super(title);

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        final Label rulesLabel = new Label(BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant));
        final Button rulesButton = new Button("Rules", () -> {
            String[] rulesTextArray = new String[BuiltInVariants.rulesDescriptions.size()];

            for(int i = 0; i < rulesTextArray.length; i++) {
                rulesTextArray[i] = BuiltInVariants.rulesDescriptions.get(i);
            }

            String rulesDescription = ListSelectDialog.showDialog(getTextGUI(), "Rules", "Select a rules variant", rulesTextArray);
            if(rulesDescription != null) {
                rules = BuiltInVariants.rulesForDescription(rulesDescription);
                rulesLabel.setText(rules.toString());
            }
        });
        rules = BuiltInVariants.availableRules.get(TerminalSettings.variant);

        final Label timeLabel = new Label("Untimed");
        final Button timeButton = new Button("Clock settings", () -> {
            TimeEntryDialog d = new TimeEntryDialog("Clock settings");
            d.showDialog(getTextGUI());

            if(d.timeSpec.mainTime == 0 && (d.timeSpec.overtimeTime == 0 || d.timeSpec.overtimeCount == 0)) {
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

        final Button finishButton = new Button("Create", () -> {
            if(sideChooser.getCheckedItem().equals("Attackers")) attackingSide = true;
            hashedPassword = mPasswordInput.getText();
            hashedPassword = (hashedPassword.isEmpty() ? "none" : hashedPassword);
            if(!hashedPassword.equals("none")) {
                hashedPassword = PasswordHasher.hashPassword("", hashedPassword);
            }
            canceled = false;

            CreateGameDialog.this.close();
        });

        final Button cancelButton = new Button("Cancel", () -> {
            canceled = true;
            CreateGameDialog.this.close();
        });

        Panel topGridPanel = new Panel();
        topGridPanel.setLayoutManager(new GridLayout(3));

        topGridPanel.addComponent(rulesButton);
        topGridPanel.addComponent(newSpacer());
        topGridPanel.addComponent(rulesLabel);

        topGridPanel.addComponent(timeButton);
        topGridPanel.addComponent(newSpacer());
        topGridPanel.addComponent(timeLabel);

        p.addComponent(topGridPanel);

        p.addComponent(passwordLabel);
        p.addComponent(mPasswordInput);

        p.addComponent(sideLabel);
        p.addComponent(sideChooser);

        Panel bottomGridPanel = new Panel();
        bottomGridPanel.setLayoutManager(new GridLayout(3));

        bottomGridPanel.addComponent(finishButton);
        bottomGridPanel.addComponent(cancelButton);
        bottomGridPanel.addComponent(newSpacer());

        p.addComponent(bottomGridPanel);

        setComponent(p);
    }

    @Override
    public TerminalSize getPreferredSize() {
        TerminalSize preferredSize = super.getPreferredSize();
        if(!preferredSize.equals(mCachedSize)) {
            mCachedSize = preferredSize;
            mPasswordInput.setPreferredSize(new TerminalSize(preferredSize.getColumns() - 2, 1));
        }
        return super.getPreferredSize();
    }

    private EmptySpace newSpacer() {
        return new EmptySpace(new TerminalSize(4, 1));
    }
}
