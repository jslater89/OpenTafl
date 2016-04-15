package com.manywords.softworks.tafl.ui.lanterna.settings;

import com.manywords.softworks.tafl.engine.GameClock;
import com.manywords.softworks.tafl.ui.player.Player;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

/**
 * Created by jay on 2/15/16.
 */
public class TerminalSettings {
    public static final String SETTINGS_PATH = "settings.ini";

    public static final int HUMAN = 0;
    public static final int AI = 1;
    public static final int NETWORK = 2;
    public static final int ENGINE = 3;

    public static int attackers = AI;
    public static int defenders = HUMAN;

    public static File attackerEngineFile = null;
    public static File defenderEngineFile = null;

    public static boolean analysisEngine = false;
    public static File analysisEngineFile = null;

    public static int aiThinkTime = 10;

    public static int variant = 0;

    public static GameClock.TimeSpec timeSpec = new GameClock.TimeSpec(0, 0, 0, 0);

    public static String labelForPlayerType(int i) {
        switch(i) {
            case HUMAN:
                return "Human";
            case AI:
                return "OpenTafl AI";
            case NETWORK:
                return "Network";
            default:
                return "External Engine";
        }
    }

    public static Player getNewPlayer(int type) {
        switch(type) {
            case HUMAN:
                return Player.getNewPlayer(Player.Type.HUMAN);
            case AI:
                return Player.getNewPlayer(Player.Type.AI);
            case NETWORK:
                return Player.getNewPlayer(Player.Type.HUMAN);
            case ENGINE:
                return Player.getNewPlayer(Player.Type.ENGINE);
        }

        return null;
    }

    public static File saveToFile() {
        File f = new File(SETTINGS_PATH);

        try {
            if(!f.exists()) {
                f.createNewFile();
            }
            Wini ini = new Wini(f);
            ini.put("config", "attacker", attackers + 1);
            ini.put("config", "attackerfile", (attackerEngineFile != null ? attackerEngineFile.getCanonicalPath() : ""));
            ini.put("config", "defender", defenders + 1);
            ini.put("config", "defenderfile", (defenderEngineFile != null ? defenderEngineFile.getCanonicalPath() : ""));
            ini.put("config", "variant", variant+ 1);
            ini.put("config", "thinktime", aiThinkTime);
            ini.put("config", "analysis", analysisEngine);
            ini.put("config", "analysisfile", (analysisEngineFile != null ? analysisEngineFile.getCanonicalPath() : ""));
            ini.put("clock", "maintime", timeSpec.mainTime);
            ini.put("clock", "overtime", timeSpec.overtimeTime);
            ini.put("clock", "otcount", timeSpec.overtimeCount);
            ini.put("clock", "increment", timeSpec.incrementTime);
            ini.store();
        }
        catch(IOException e) {
            // Unable to save everything, so delete the file and return null;
            System.out.println("IOException: " + e);
            f.delete();
            return null;
        }

        return f;
    }

    public static void loadFromFile() {
        File f = new File(SETTINGS_PATH);
        try {
            if(!f.exists()) {
                return;
            }
            Wini ini = new Wini(f);
            int attacker = ini.get("config", "attacker", int.class);
            if(attacker != 0) attackers = attacker - 1;

            String attackerFile = ini.get("config", "attackerfile", String.class);
            if(attackerFile != null && !attackerFile.equals("")) attackerEngineFile = new File(attackerFile);

            int defender = ini.get("config", "defender", int.class);
            if(defender != 0) defenders = defender - 1;

            String defenderFile = ini.get("config", "defenderfile", String.class);
            if(defenderFile != null && !defenderFile.equals("")) defenderEngineFile = new File(defenderFile);

            String analysisFile = ini.get("config", "analysisfile", String.class);
            if(analysisFile != null && !analysisFile.equals("")) analysisEngineFile = new File(analysisFile);

            analysisEngine = ini.get("config", "analysis", boolean.class);

            int v = ini.get("config", "variant", int.class);
            if(v != 0) variant = v - 1;

            int time = ini.get("config", "thinktime", int.class);
            if(time != 0) aiThinkTime = time;

            long mainTime = ini.get("clock", "maintime", long.class);
            long overtimeTime = ini.get("clock", "overtime", long.class);
            int overtimeCount = ini.get("clock", "otcount", int.class);
            long incrementTime = ini.get("clock", "increment", long.class);

            GameClock.TimeSpec ts = new GameClock.TimeSpec(mainTime, overtimeTime, overtimeCount, incrementTime);
            timeSpec = ts;
        }
        catch(IOException e) {
            System.out.println("IOException: " + e);
            // Don't really care if we can't load everything.
            // Use defaults otherwise.
        }
    }
}
