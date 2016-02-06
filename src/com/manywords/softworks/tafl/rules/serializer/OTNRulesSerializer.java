package com.manywords.softworks.tafl.rules.serializer;

import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;

import java.util.*;

/**
 * Created by jay on 2/5/16.
 */
public class OTNRulesSerializer {
    public static class TaflmanTypeIndex {
        public static final int count = Rules.TAFLMAN_TYPE_COUNT;
        public static final char[] inverse = {'t', 'c', 'n', 'k', 'T', 'C', 'N', 'K'};
        public static final int t = 0;
        public static final int c = 1;
        public static final int n = 2;
        public static final int k = 3;
        public static final int T = 4;
        public static final int C = 5;
        public static final int N = 6;
        public static final int K = 7;
    }

    public static final Map<String, String> defaults;
    static {
        HashMap<String, String> map = new HashMap<>();
        map.put("esc", "c");
        map.put("surf", "y");
        map.put("atkf", "y");
        map.put("ka", "y");
        map.put("ks", "y");
        map.put("kj", "n");
        map.put("nj", "c");
        map.put("cj", "j");
        map.put("cor", "default");
        map.put("cen", "default");
        map.put("afor", "");
        map.put("dfor", "");

        map.put("corh", "tcnkTCNK");
        map.put("cenh", "tcnk");
        map.put("cenhe", "tcnkTCNK");
        map.put("aforh", "TCNK");
        map.put("dforh", "tcnk");

        map.put("corp", "K");
        map.put("cenp", "tcnkTCNK");
        map.put("aforp", "tcnkTCNK");
        map.put("dforp", "TCNK");

        map.put("cors", "K");
        map.put("cens", "tcnkTCNK");
        map.put("afors", "tcnkTCNK");
        map.put("dfors", "TCNK");

        map.put("sw", "n");
        map.put("swf", "y");
        map.put("efe", "n");
        map.put("ber", "n");

        defaults = map;
    }


    public static String getOTNRulesString(Rules rules) {
        String otnString = "";

        otnString += "dim:";
        otnString += rules.boardSize;
        otnString += " ";

        if(rules.getEscapeType() == Rules.EDGES) {
            otnString += "esc:e ";
        }

        if(!rules.isSurroundingFatal()) {
            otnString += "surf:n ";
        }

        if(!rules.getStartingSide().isAttackingSide()) {
            otnString += "atkf:n ";
        }

        if(!rules.isKingArmed()) {
            otnString += "ka:n ";
        }

        if(!rules.isKingStrong()) {
            otnString += "ks:n ";
        }

        if(rules.getKingJumpMode() != Taflman.JUMP_NONE) {
            otnString += "kj:" + getStringForJumpMode(rules.getKingJumpMode()) + " ";
        }

        if(rules.getKnightJumpMode() != Taflman.JUMP_CAPTURE) {
            otnString += "nj:" + getStringForJumpMode(rules.getKnightJumpMode()) + " ";
        }

        if(rules.getCommanderJumpMode() != Taflman.JUMP_STANDARD) {
            otnString += "cj:" + getStringForJumpMode(rules.getCommanderJumpMode()) + " ";
        }

        Set<Coord> corners = rules.getCornerSpaces();
        if(!corners.equals(getDefaultCorners(rules.boardSize))) {
            String cornersString = "cor:";
            for(Coord corner : corners) {
                cornersString += corner.toString() + ",";
            }

            otnString += cornersString + " ";
        }

        Set<Coord> centers = rules.getCenterSpaces();
        if(!centers.equals(getDefaultCenter(rules.boardSize))) {
            String centerString = "cen:";
            for(Coord center : centers) {
                centerString += center.toString() + ",";
            }

            otnString += centerString + " ";
        }

        Set<Coord> attackerForts = rules.getAttackerForts();
        if(attackerForts.size() > 0) {
            String fortString = "afor:";
            for(Coord fort : attackerForts) {
                fortString += fort.toString() + ",";
            }

            otnString += fortString + " ";
        }

        Set<Coord> defenderForts = rules.getDefenderForts();
        if(defenderForts.size() > 0) {
            String fortString = "dfor:";
            for(Coord fort : attackerForts) {
                fortString += fort.toString() + ",";
            }

            otnString += fortString + " ";
        }

        String corp = getStringForTaflmanTypeList(rules.cornerPassableFor);
        if(!corp.equals(defaults.get("corp"))) otnString += "corp:" + corp + " ";

        String cenp = getStringForTaflmanTypeList(rules.centerPassableFor);
        if(!cenp.equals(defaults.get("cenp"))) otnString += "cenp:" + cenp + " ";

        String aforp = getStringForTaflmanTypeList(rules.attackerFortPassableFor);
        if(!aforp.equals(defaults.get("aforp"))) otnString += "aforp:" + aforp + " ";

        String dforp = getStringForTaflmanTypeList(rules.defenderFortPassableFor);
        if(!dforp.equals(defaults.get("dforp"))) otnString += "dforp:" + dforp + " ";

        String cors = getStringForTaflmanTypeList(rules.cornerStoppableFor);
        if(!cors.equals(defaults.get("cors"))) otnString += "cors:" + cors + " ";

        String cens = getStringForTaflmanTypeList(rules.centerStoppableFor);
        if(!cens.equals(defaults.get("cens"))) otnString += "cens:" + cens + " ";

        String afors = getStringForTaflmanTypeList(rules.attackerFortStoppableFor);
        if(!afors.equals(defaults.get("afors"))) otnString += "afors:" + afors + " ";

        String dfors = getStringForTaflmanTypeList(rules.defenderFortStoppableFor);
        if(!dfors.equals(defaults.get("dfors"))) otnString += "dfors:" + afors + " ";

        String corh = getStringForTaflmanTypeList(rules.cornerHostileTo);
        if(!corh.equals(defaults.get("corh"))) otnString += "corh:" + corh + " ";

        String cenh = getStringForTaflmanTypeList(rules.centerHostileTo);
        if(!cenh.equals(defaults.get("cenh"))) otnString += "cenh:" + cenh + " ";

        String cenhe = getStringForTaflmanTypeList(rules.emptyCenterHostileTo);
        if(!cenhe.equals(defaults.get("cenhe"))) otnString += "cenhe:" + cenhe + " ";

        String aforh = getStringForTaflmanTypeList(rules.attackerFortHostileTo);
        if(!aforh.equals(defaults.get("aforh"))) otnString += "aforh:" + aforh + " ";

        String dforh = getStringForTaflmanTypeList(rules.defenderFortHostileTo);
        if(!dforh.equals(defaults.get("dforh"))) otnString += "dforh:" + dforh + " ";

        if(rules.allowShieldWallCaptures() != Rules.NO_SHIELDWALL) {
            otnString += "sw:" + getStringForShieldwallMode(rules.allowShieldWallCaptures()) + " ";
        }

        if(!rules.allowFlankingShieldwallCapturesOnly()) {
            otnString += "swf:n ";
        }

        if(rules.allowShieldFortEscapes()) {
            otnString += "efe:y ";
        }

        if(rules.getBerserkMode() != Rules.BERSERK_NONE) {
            otnString += "ber:" + getStringForBerserkMode(rules.getBerserkMode()) + " ";
        }

        otnString += "start:" + rules.getBoard().getOTNPositionString();

        return otnString;
    }

    private static String getStringForJumpMode(int jumpMode) {
        switch(jumpMode) {
            case Taflman.JUMP_CAPTURE: return "c";
            case Taflman.JUMP_STANDARD: return "j";
            case Taflman.JUMP_RESTRICTED: return "r";
            default: return "n";
        }
    }

    private static int getJumpModeForString(String jumpMode) {
        if(jumpMode.equals("c")) return Taflman.JUMP_CAPTURE;
        if(jumpMode.equals("j")) return Taflman.JUMP_STANDARD;
        if(jumpMode.equals("r")) return Taflman.JUMP_RESTRICTED;

        return Taflman.JUMP_NONE;
    }

    private static Set<Coord> getDefaultCorners(int boardSize) {
        Coord c1 = Coord.get(0, 0);
        Coord c2 = Coord.get(boardSize - 1, 0);
        Coord c3 = Coord.get(0, boardSize - 1);
        Coord c4 = Coord.get(boardSize - 1, boardSize - 1);

        Set<Coord> defaultCorners = new HashSet<Coord>(6);
        defaultCorners.add(c1);
        defaultCorners.add(c2);
        defaultCorners.add(c3);
        defaultCorners.add(c4);

        return defaultCorners;
    }

    private static Set<Coord> getDefaultCenter(int boardSize) {
        int center = (boardSize - 1) / 2;

        Set<Coord> defaultCenter = new HashSet<Coord>(2);
        defaultCenter.add(Coord.get(center, center));
        return defaultCenter;
    }

    private static String getStringForTaflmanTypeList(boolean[] typeList) {
        String typeString = "";
        for(int i = 0; i < TaflmanTypeIndex.count; i++) {
            if(typeList[i]) typeString += TaflmanTypeIndex.inverse[i];
        }

        return typeString;
    }

    public static boolean[] getTaflmanTypeListForString(String typeString) {
        boolean[] typeList = new boolean[TaflmanTypeIndex.count];
        for(int i = 0; i < typeString.length(); i++) {
            char c = typeString.charAt(i);
            int index = indexOf(TaflmanTypeIndex.inverse, c);

            typeList[index] = true;
        }

        return typeList;
    }

    private static int indexOf(char[] array, char item) {
        for(int i = 0; i < array.length; i++) {
            if(array[i] == item) return i;
        }

        return -1;
    }

    private static String getStringForShieldwallMode(int shieldwallMode) {
        switch(shieldwallMode) {
            case Rules.STRONG_SHIELDWALL: return "s";
            case Rules.WEAK_SHIELDWALL: return "w";
            default: return "n";
        }
    }

    private static int getShieldwallModeForString(String shieldwallString) {
        if(shieldwallString.equals("s")) return Rules.STRONG_SHIELDWALL;
        if(shieldwallString.equals("w")) return Rules.WEAK_SHIELDWALL;

        return Rules.NO_SHIELDWALL;
    }

    private static String getStringForBerserkMode(int berserkMode) {
        switch(berserkMode) {
            case Rules.BERSERK_ANY_MOVE: return "m";
            case Rules.BERSERK_CAPTURE_ONLY: return "c";
            default: return "n";
        }
    }

    private static int getBerserkModeForString(String berserkString) {
        if(berserkString.equals("m")) return Rules.BERSERK_ANY_MOVE;
        if(berserkString.equals("c")) return Rules.BERSERK_CAPTURE_ONLY;

        return Rules.BERSERK_NONE;
    }
}
