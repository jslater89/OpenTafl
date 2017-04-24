package com.manywords.softworks.tafl.engine.clock;

/**
 * Created by jay on 5/28/16.
 */
public class TimeSpec {
    public long mainTime = 0;
    public long overtimeTime = 0;
    public int overtimeCount = 0;
    public long incrementTime = 0;

    public TimeSpec(long mainTime, long overtimeTime, int overtimeCount, long incrementTime) {
        this.mainTime = mainTime;
        this.overtimeTime = overtimeTime;
        this.overtimeCount = overtimeCount;
        this.incrementTime = incrementTime;
    }

    public static TimeSpec parseMachineReadableString(String timeSpec) {
        return parseMachineReadableString(timeSpec, "\\|");
    }

    public static TimeSpec parseMachineReadableString(String timeSpec, String componentSeparator) {
        return parseMachineReadableString(timeSpec, componentSeparator, 1);
    }

    public static TimeSpec parseMachineReadableString(String timeSpec, String componentSeparator, long multiplier) {
        String[] parts = timeSpec.split(componentSeparator);
        long mainTime = Long.parseLong(parts[0]) * multiplier;

        String[] overtimeParts = parts[1].split("/");
        long overtimeTime = Long.parseLong(overtimeParts[0]) * multiplier;
        int overtimeCount = Integer.parseInt(overtimeParts[1]);

        long incrementTime = Long.parseLong(parts[2]) * multiplier;

        return new TimeSpec(mainTime, overtimeTime, overtimeCount, incrementTime);
    }


    public boolean isEnabled() {
        // Clock is enabled if we have either main time or overtime time.
        return !(mainTime == 0 && (overtimeTime == 0 || overtimeCount == 0));
    }

    public String toString() {
        return mainTime / 1000 + " " + overtimeTime / 1000 + "/" + overtimeCount + " " + incrementTime / 1000 + "i";
    }

    public String toGameNotationString() {
        return mainTime / 1000 + " " + overtimeTime / 1000 + "/" + overtimeCount;
    }

    public String toHumanString() {
        return toHumanString(false);
    }

    public String toHumanString(boolean increment) {
        int mainTimeSeconds = (int) mainTime / 1000;
        int overtimeSeconds = (int) overtimeTime / 1000;
        int incrementSeconds = (int) incrementTime / 1000;

        int hours = mainTimeSeconds / 3600;
        int minutes = (mainTimeSeconds % 3600) / 60;
        int seconds = (mainTimeSeconds % 3600) % 60;
        String m = (minutes >= 10 ? "" + minutes : "0" + minutes);
        String s = (seconds >= 10 ? "" + seconds : "0" + seconds);
        String mainTime = hours + ":" + m + ":" + s;

        hours = overtimeSeconds / 3600;
        minutes = (overtimeSeconds % 3600) / 60;
        seconds = (overtimeSeconds % 3600) % 60;
        m = (minutes >= 10 ? "" + minutes : "0" + minutes);
        s = (seconds >= 10 ? "" + seconds : "0" + seconds);
        String overtimeTime = hours + ":" + m + ":" + s;

        String incrementTime = "";
        if(increment) {
            hours = incrementSeconds / 3600;
            minutes = (incrementSeconds % 3600) / 60;
            seconds = (incrementSeconds % 3600) % 60;
            m = (minutes >= 10 ? "" + minutes : "0" + minutes);
            s = (seconds >= 10 ? "" + seconds : "0" + seconds);

            if(incrementSeconds > 0) incrementTime += " ";
            if(hours > 0) incrementTime += hours + ":";
            if(minutes > 0 || hours > 0) incrementTime += m + ":";
            if(seconds > 0 || minutes > 0 || hours > 0) incrementTime += s;
            if(!incrementTime.isEmpty()) incrementTime += "i";
        }

        String result = mainTime + " " + overtimeTime + "/" + overtimeCount + incrementTime;
        return result;
    }

    public String toMillisString() {
        return mainTime + " " + overtimeTime + "/" + overtimeCount + " " + incrementTime + "i";
    }

    public String toMachineReadableString() {
        return mainTime + "|" + overtimeTime + "/" + overtimeCount + "|" + incrementTime;
    }

    private static final TimeSpec EMPTY = new TimeSpec(-1, -1, 0, -1);
    public static TimeSpec emptyTimeSpec() {
        return EMPTY;
    }

    /**
     * A clock equals another clock if main time, overtime time, and overtime count are the same.
     * @param timeSpec
     * @return
     */
    public boolean clockEquals(TimeSpec timeSpec) {
        return this.mainTime == timeSpec.mainTime && this.overtimeTime == timeSpec.overtimeTime && this.overtimeCount == timeSpec.overtimeCount;
    }

    /**
     * A setting equals another setting if their clocks are the same, and increment time is also the same.
     * @param timeSpec
     * @return
     */
    public boolean settingEquals(TimeSpec timeSpec) {
        return clockEquals(timeSpec) && this.incrementTime == timeSpec.incrementTime;
    }
}
