package com.manywords.softworks.tafl.ui.lanterna.window;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.ui.lanterna.settings.TerminalSettings;

import java.util.regex.Pattern;

/**
 * Created by jay on 2/21/16.
 */
public class TimeEntryDialog extends DialogWindow {
    protected TimeEntryDialog(String title) {
        super(title);

        Panel p = new Panel();
        p.setLayoutManager(new LinearLayout());

        final Label mainTimeLabel = new Label("Main time (seconds)");
        final TextBox mainTimeInput = new TextBox();
        mainTimeInput.setText("" + TerminalSettings.timeSpec.mainTime / 1000);
        mainTimeInput.setValidationPattern(Pattern.compile("[0-9]+"));

        final Label overtimeTimeLabel = new Label("Overtime period length (seconds)");
        final TextBox overtimeInput = new TextBox();
        overtimeInput.setText("" + TerminalSettings.timeSpec.overtimeTime / 1000);
        overtimeInput.setValidationPattern(Pattern.compile("[0-9]+"));

        final Label overtimeCountLabel = new Label("Overtime period count");
        final TextBox overtimeCountInput = new TextBox();
        overtimeCountInput.setText("" + TerminalSettings.timeSpec.overtimeCount);
        overtimeCountInput.setValidationPattern(Pattern.compile("[0-9]+"));

        final Label incrementTimeLabel = new Label("Increment time (seconds)");
        final TextBox incrementTimeInput = new TextBox();
        incrementTimeInput.setText("" + TerminalSettings.timeSpec.incrementTime / 1000);
        incrementTimeInput.setValidationPattern(Pattern.compile("[0-9]+"));

        final Button finishButton = new Button("OK", new Runnable() {
            @Override
            public void run() {
                long mainTimeMillis = Integer.parseInt(mainTimeInput.getText()) * 1000;
                long overtimeTimeMillis = Integer.parseInt(overtimeInput.getText()) * 1000;
                int overtimeCount = Integer.parseInt(overtimeCountInput.getText());
                long incrementTimeMillis = Integer.parseInt(incrementTimeInput.getText()) * 1000;

                GameClock.TimeSpec ts = new GameClock.TimeSpec(mainTimeMillis, overtimeTimeMillis, overtimeCount, incrementTimeMillis);
                TerminalSettings.timeSpec = ts;

                TimeEntryDialog.this.close();
            }
        });

        p.addComponent(mainTimeLabel);
        p.addComponent(mainTimeInput);

        p.addComponent(overtimeTimeLabel);
        p.addComponent(overtimeInput);

        p.addComponent(overtimeCountLabel);
        p.addComponent(overtimeCountInput);

        p.addComponent(incrementTimeLabel);
        p.addComponent(incrementTimeInput);

        p.addComponent(finishButton);

        setComponent(p);
    }
}
