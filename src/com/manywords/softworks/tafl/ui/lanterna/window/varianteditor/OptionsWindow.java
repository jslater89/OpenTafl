package com.manywords.softworks.tafl.ui.lanterna.window.varianteditor;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.manywords.softworks.tafl.rules.*;
import com.manywords.softworks.tafl.ui.lanterna.TerminalUtils;
import com.manywords.softworks.tafl.ui.lanterna.component.FocusableBasicWindow;
import com.manywords.softworks.tafl.ui.lanterna.screen.LogicalScreen;

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

        Panel rowSpacer = new Panel();
        rowSpacer.addComponent(TerminalUtils.newSpacer());
        p.addComponent(rowSpacer);

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

        });
        secondRow.addComponent(loadUser);

        p.addComponent(secondRow);

        Panel thirdRow = new Panel();
        thirdRow.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button save = new Button("Save rules", () -> mHost.saveRules());
        thirdRow.addComponent(save);

        Button help = new Button("Help", () -> {

        });
        thirdRow.addComponent(help);

        Button quit = new Button("Quit", () -> mHost.quit());
        thirdRow.addComponent(quit);

        p.addComponent(thirdRow);


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
    }
}
