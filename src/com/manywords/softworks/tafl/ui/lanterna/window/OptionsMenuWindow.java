package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialog;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.manywords.softworks.tafl.rules.BuiltInVariants;
import com.manywords.softworks.tafl.ui.AdvancedTerminal;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by jay on 2/15/16.
 */
public class OptionsMenuWindow extends BasicWindow {
    private AdvancedTerminal.TerminalCallback mTerminalCallback;
    public OptionsMenuWindow(AdvancedTerminal.TerminalCallback callback) {
        mTerminalCallback = callback;

        refreshSettings();
    }

    public void refreshSettings() {
        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        Label l1 = new Label("OpenTafl Options");
        p.addComponent(l1);

        Label l2 = new Label("The old Norse board game,");
        p.addComponent(l2);

        Label l3 = new Label("In an old computer style.");
        p.addComponent(l3);

        Panel optionsPanel = new Panel();
        optionsPanel.setLayoutManager(new GridLayout(3));

        Button variantSelect = new Button("Variant", new Runnable() {
            @Override
            public void run() {
                showVariantSelectDialog();
            }
        });
        Label variantName = new Label(BuiltInVariants.rulesDescriptions.get(TerminalSettings.variant));
        optionsPanel.addComponent(variantSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(variantName);

        Button attackerSelect = new Button("Attackers", new Runnable() {
            @Override
            public void run() {
                showPlayerSelectDialog(true);
            }
        });
        Label attackerType = new Label(TerminalSettings.labelForPlayerType(TerminalSettings.attackers));
        optionsPanel.addComponent(attackerSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(attackerType);

        Button defenderSelect = new Button("Defenders", new Runnable() {
            @Override
            public void run() {
                showPlayerSelectDialog(false);
            }
        });
        Label defenderType = new Label(TerminalSettings.labelForPlayerType(TerminalSettings.defenders));
        optionsPanel.addComponent(defenderSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(defenderType);

        Button aiDepthSelect = new Button("AI think time", new Runnable() {
            @Override
            public void run() {
                showAiDepthEntryDialog();
            }
        });
        Label aiDepth = new Label("" + TerminalSettings.aiThinkTime);
        optionsPanel.addComponent(aiDepthSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(aiDepth);

        Button clockSettingSelect = new Button("Clock setting", new Runnable() {
            @Override
            public void run() {
                showTimeSpecDialog();
            }
        });
        Label clockSettingLabel = new Label(TerminalSettings.timeSpec.toString());
        optionsPanel.addComponent(clockSettingSelect);
        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(clockSettingLabel);

        Button backButton = new Button("Back", new Runnable() {
            @Override
            public void run() {
                mTerminalCallback.onMenuNavigation(new MainMenuWindow(mTerminalCallback));
            }
        });
        optionsPanel.addComponent(backButton);

        optionsPanel.addComponent(newSpacer());
        optionsPanel.addComponent(newSpacer());

        p.addComponent(optionsPanel);

        this.setComponent(p);
    }

    private EmptySpace newSpacer() {
        return new EmptySpace(new TerminalSize(4, 0));
    }

    private void showTimeSpecDialog() {
        new TimeEntryDialog("Clock settings").showDialog(getTextGUI());

        refreshSettings();
    }

    private void showAiDepthEntryDialog() {
        List<String> lines = TerminalTextUtils.getWordWrappedText(50,
                "The maximum time in seconds per AI move, or 0 to let the AI" +
                        " decide its timing based on the game clock.");
        String descriptionString = "";
        for(String s : lines) {
            descriptionString += s + "\n";
        }
        BigInteger searchdepth = TextInputDialog.showNumberDialog(
                getTextGUI(),
                "AI think time",
                descriptionString,
                "" + TerminalSettings.aiThinkTime);
        int intDepth = searchdepth.intValue();
        TerminalSettings.aiThinkTime = intDepth;

        refreshSettings();
    }

    private void showVariantSelectDialog() {
        Runnable[] variants = new Runnable[BuiltInVariants.rulesDescriptions.size()];
        for(int i = 0; i < variants.length; i++) {
            final String name = BuiltInVariants.rulesDescriptions.get(i);
            final int index = i;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    TerminalSettings.variant = index;

                    refreshSettings();
                }

                @Override
                public String toString() {
                    return name;
                }
            };

            variants[i] = r;
        }

        ActionListDialog.showDialog(this.getTextGUI(), "Select variant", "", variants);
    }

    private void showPlayerSelectDialog(final boolean attackers) {
        ActionListDialog.showDialog(this.getTextGUI(), "Select player type", "",
                new Runnable() {

                    @Override
                    public void run() {
                        if(attackers) TerminalSettings.attackers = TerminalSettings.HUMAN;
                        else TerminalSettings.defenders = TerminalSettings.HUMAN;

                        refreshSettings();
                    }

                    @Override
                    public String toString() {
                        return "Human";
                    }
                },

                new Runnable() {

                    @Override
                    public void run() {
                        if(attackers) TerminalSettings.attackers = TerminalSettings.AI;
                        else TerminalSettings.defenders = TerminalSettings.AI;

                        refreshSettings();
                    }

                    @Override
                    public String toString() {
                        return "AI";
                    }
                },

                new Runnable() {

                    @Override
                    public void run() {
                        if(attackers) TerminalSettings.attackers = TerminalSettings.HUMAN;
                        else TerminalSettings.defenders = TerminalSettings.HUMAN;

                        refreshSettings();
                    }

                    @Override
                    public String toString() {
                        return "Network";
                    }
                },

                new Runnable() {

                    @Override
                    public void run() {
                        if(attackers) TerminalSettings.attackers = TerminalSettings.ENGINE;
                        else TerminalSettings.defenders = TerminalSettings.ENGINE;

                        refreshSettings();
                    }

                    @Override
                    public String toString() {
                        return "External Engine";
                    }
                }
        );
    }
}
