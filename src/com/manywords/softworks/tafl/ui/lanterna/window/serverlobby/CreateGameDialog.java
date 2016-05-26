package com.manywords.softworks.tafl.ui.lanterna.window.serverlobby;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialog;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;

import java.util.regex.Pattern;

/**
 * Created by jay on 5/23/16.
 */
public class CreateGameDialog extends DialogWindow {
    
    public String hashedPassword;
    public Rules rules;
    public boolean attackingSide;
    public boolean canceled;

    public CreateGameDialog(String title) {
        super(title);

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        final Label rulesLabel = new Label(BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant));
        final Button rulesButton = new Button("Rules", new Runnable() {
            @Override
            public void run() {
                String[] rulesTextArray = new String[BuiltInVariants.rulesDescriptions.size()];

                for(int i = 0; i < rulesTextArray.length; i++) {
                    rulesTextArray[i] = BuiltInVariants.rulesDescriptions.get(i);
                }

                rules = BuiltInVariants.rulesForDescription(ListSelectDialog.showDialog(getTextGUI(), "Rules", "Select a rules variant", rulesTextArray));
                rulesLabel.setText(rules.toString());
            }
        });
        rules = BuiltInVariants.availableRules.get(TerminalSettings.variant);

        final Label passwordLabel = new Label("Password");
        final TextBox passwordInput = new TextBox();
        passwordInput.setMask('*');
        passwordInput.setValidationPattern(Pattern.compile("([[a-z][A-Z][0-9]\\-_\\.\\*])+"));

        final Label sideLabel = new Label("Side");
        final RadioBoxList<String> sideChooser = new RadioBoxList<>();
        sideChooser.addItem("Attackers");
        sideChooser.addItem("Defenders");
        sideChooser.setCheckedItemIndex(0);

        final Button finishButton = new Button("Create", () -> {
            if(sideChooser.getCheckedItem().equals("Attackers")) attackingSide = true;
            hashedPassword = String.valueOf(passwordInput.getText().hashCode()); //TODO: implement this
            canceled = false;

            CreateGameDialog.this.close();
        });

        final Button cancelButton = new Button("Cancel", () -> {
            canceled = true;
            CreateGameDialog.this.close();
        });

        p.addComponent(rulesButton);
        p.addComponent(rulesLabel);

        p.addComponent(passwordLabel);
        p.addComponent(passwordInput);

        p.addComponent(sideLabel);
        p.addComponent(sideChooser);

        Panel buttonPanel = new Panel();
        buttonPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        buttonPanel.addComponent(finishButton);
        buttonPanel.addComponent(cancelButton);

        p.addComponent(buttonPanel);

        setComponent(p);
    }
}
