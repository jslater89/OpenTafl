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
        String[] parts = timeSpec.split(componentSeparator);
        long mainTime = Long.parseLong(parts[0]);

        String[] overtimeParts = parts[1].split("/");
        long overtimeTime = Long.parseLong(overtimeParts[0]);
        int overtimeCount = Integer.parseInt(overtimeParts[1]);

        long incrementTime = Long.parseLong(parts[2]);

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
        int mainTimeSeconds = (int) mainTime / 1000;
        int overtimeSeconds = (int) overtimeTime / 1000;

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

        String result = mainTime + " " + overtimeTime + "/" + overtimeCount;
        return result;
    }

    public String toMillisString() {
        return mainTime + " " + overtimeTime + "/" + overtimeCount + " " + incrementTime + "i";
    }

    public String toMachineReadableString() {
        return mainTime + "|" + overtimeTime + "/" + overtimeCount + "|" + incrementTime;
    }
}
