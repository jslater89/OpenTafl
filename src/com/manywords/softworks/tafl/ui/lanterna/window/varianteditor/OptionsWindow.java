package com.manywords.softworks.tafl.ui.lanterna.window.varianteditor;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.ui.HumanReadableRulesPrinter;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.component.FocusableBasicWindow;
import com.manywords.softworks.tafl.ui.lanterna.component.ScrollingMessageDialog;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;
import com.manywords.softworks.tafl.ui.lanterna.theme.TerminalThemeConstants;

import java.io.*;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by jay on 4/13/17.
 */
public class OptionsWindow extends FocusableBasicWindow {
    private Label mStatus;
    private OptionsWindowHost mHost;

    public OptionsWindow(LogicalScreen.TerminalCallback callback, OptionsWindowHost host) {
        super("Options", callback);
        mHost = host;
        buildWindow();
    }

    private void buildWindow() {
        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        Panel firstRow = new Panel();
        firstRow.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        mStatus = new Label("Status: ");
        firstRow.addComponent(mStatus);
        p.addComponent(firstRow);

//        Panel rowSpacer = new Panel();
//        rowSpacer.addComponent(TerminalUtils.newSpacer());
//        p.addComponent(rowSpacer);

        Panel secondRow = new Panel();
        secondRow.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button newLayout = new Button("New rules", () -> {
            BigInteger newSize = TextInputDialog.showNumberDialog(
                    getTextGUI(),
                    "New board size",
                    "Enter the side length of the new board.",
                    "9"
            );

            if(newSize != null) {
                int size = newSize.intValue();
                if(size % 2 != 1) size += 1;
                size = Math.max(5, Math.min(17, size));
                mHost.newLayout(size);
            }
        });
        secondRow.addComponent(newLayout);

        Button loadStatic = new Button("Load static", () -> {
            List<Rules> builtins = BuiltInVariants.availableRules;
            Rules[] r = new Rules[builtins.size()];

            ListSelectDialogBuilder<Rules> builder = new ListSelectDialogBuilder<>();
            builder.addListItems(builtins.toArray(r));
            builder.setTitle("Select variant");
            builder.setCanCancel(true);
            builder.setListBoxSize(new TerminalSize(40, 20));

            Rules selected = builder.build().showDialog(getTextGUI());
            mHost.loadRules(selected);
        });
        secondRow.addComponent(loadStatic);

        Button loadUser = new Button("Load user", () -> {
            File f = TerminalUtils.showFileChooserDialog(getTextGUI(), "Load rule set", "Load", new File("user-rules"));

            if(f != null) {
                if(f.exists()) {
                    try {
                        BufferedReader r = new BufferedReader(new FileReader(f));
                        String rulesString = r.readLine();

                        Rules rules = RulesSerializer.loadRulesRecord(rulesString);
                        mHost.loadRules(rules);
                    }
                    catch (FileNotFoundException e) {
                        // Already caught below
                    }
                    catch (IOException e) {
                        MessageDialog.showMessageDialog(getTextGUI(), "Error", "Failed to read file");
                        OpenTafl.logStackTrace(OpenTafl.LogLevel.NORMAL, e);
                    }
                    catch (NotationParseException e) {
                        MessageDialog.showMessageDialog(getTextGUI(), "Error", "Failed to parse file");
                        OpenTafl.logStackTrace(OpenTafl.LogLevel.NORMAL, e);
                    }
                }
                else {
                    MessageDialog.showMessageDialog(getTextGUI(), "Error", "File not found");
                }
            }
        });
        secondRow.addComponent(loadUser);

        p.addComponent(secondRow);

        Panel thirdRow = new Panel();
        thirdRow.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button save = new Button("Save rules", () -> mHost.saveRules());
        thirdRow.addComponent(save);

        Button help = new Button("Help", () -> {
            // TODO: handle help
        });
        thirdRow.addComponent(help);

        Button quit = new Button("Quit", () -> mHost.quit());
        thirdRow.addComponent(quit);

        p.addComponent(thirdRow);

        Panel fourthRow = new Panel();
        fourthRow.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        Button readableRules = new Button("Human-readable rules", () -> {
            ScrollingMessageDialog d = new ScrollingMessageDialog(
                    "Rules",
                    HumanReadableRulesPrinter.getHumanReadableRules(mHost.getRules()),
                    MessageDialogButton.Close);
            d.setSize(new TerminalSize(50, 25));
            d.setHints(TerminalThemeConstants.CENTERED_MODAL);
            d.showDialog(getTextGUI());
        });
        fourthRow.addComponent(readableRules);

        p.addComponent(fourthRow);


        setComponent(p);
    }

    public void updateStatus(Coord space, SpaceType spaceType, char taflman) {
        String status = "Status: ";
        status += space;

        if(spaceType != SpaceType.NONE || taflman != Taflman.EMPTY) {
            status += " ";

            if(spaceType != SpaceType.NONE) {
                status += "(" + spaceType + ") ";
            }

            if(taflman != Taflman.EMPTY) {
                status += (Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS ? "atk " : "def ");

                switch(Taflman.getPackedType(taflman)) {
                    case Taflman.TYPE_KING:
                        status += "king";
                        break;
                    case Taflman.TYPE_KNIGHT:
                        status += "knight";
                        break;
                    case Taflman.TYPE_COMMANDER:
                        status += "commander";
                        break;
                    default:
                        status += "taflman";
                }
            }
        }

        mStatus.setText(status);
    }

    public interface OptionsWindowHost {
        void newLayout(int dimension);
        void loadRules(Rules r);
        void saveRules();
        void quit();
        Rules getRules();
    }
}
