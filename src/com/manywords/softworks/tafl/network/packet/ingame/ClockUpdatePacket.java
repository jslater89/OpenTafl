package com.manywords.softworks.tafl.network.packet.ingame;

import com.manywords.softworks.tafl.engine.clock.TimeSpec;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/28/16.
 */
public class ClockUpdatePacket extends NetworkPacket {
    public static final String PREFIX = "clock-update";
    public final TimeSpec attackerClock;
    public final TimeSpec defenderClock;

    public ClockUpdatePacket(TimeSpec attackerClock, TimeSpec defenderClock) {
        this.attackerClock = attackerClock;
        this.defenderClock = defenderClock;
    }

    public static ClockUpdatePacket parse(String data) {
        data = data.replaceFirst("clock-update", "").trim();
        String[] parts = data.split(" ");

        TimeSpec attackerTime = TimeSpec.parseMachineReadableString(parts[0]);
        TimeSpec defenderTime = TimeSpec.parseMachineReadableString(parts[1]);

        return new ClockUpdatePacket(attackerTime, defenderTime);
    }

    @Override
    public String toString() {
        return PREFIX + " " + attackerClock.toMachineReadableString() + " " + defenderClock.toMachineReadableString();
    }
}
