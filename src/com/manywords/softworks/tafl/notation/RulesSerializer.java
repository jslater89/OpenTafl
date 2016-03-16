/*
Under section 3(b)(ii) of the Free-As-In-Beer License, this file
is exempted from all terms and conditions of the license. It is
released instead under the Apache license 2.0:

Copyright 2015 Jay Slater

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.manywords.softworks.tafl.notation;

import com.manywords.softworks.tafl.rules.*;

import java.util.*;

/**
 * Created by jay on 2/5/16.
 */
public class RulesSerializer {
    public static final Map<String, String> defaults;
    static {
        HashMap<String, String> map = new HashMap<>();
        map.put("esc", "c");
        map.put("surf", "y");
        map.put("atkf", "y");
        map.put("tfr", "d");
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
        map.put("cens", "K");
        map.put("afors", "tcnkTCNK");
        map.put("dfors", "TCNK");

        map.put("sw", "n");
        map.put("swf", "y");
        map.put("efe", "n");
        map.put("ber", "n");

        defaults = map;
    }


    public static String getRulesRecord(Rules rules) {
        String otnrString = "";

        otnrString += "dim:";
        otnrString += rules.boardSize;
        otnrString += " ";

        if(rules.getEscapeType() == Rules.EDGES) {
            otnrString += "esc:e ";
        }

        if(!rules.isSurroundingFatal()) {
            otnrString += "surf:n ";
        }

        if(!rules.getStartingSide().isAttackingSide()) {
            otnrString += "atkf:n ";
        }

        if(rules.threefoldRepetitionResult() != Rules.DRAW) {
            otnrString += getStringForThreefoldResult(rules.threefoldRepetitionResult());
        }

        if(!rules.isKingArmed()) {
            otnrString += "ka:n ";
        }

        if(!rules.isKingStrong()) {
            otnrString += "ks:n ";
        }

        if(rules.getKingJumpMode() != Taflman.JUMP_NONE) {
            otnrString += "kj:" + getStringForJumpMode(rules.getKingJumpMode()) + " ";
        }

        if(rules.getKnightJumpMode() != Taflman.JUMP_CAPTURE) {
            otnrString += "nj:" + getStringForJumpMode(rules.getKnightJumpMode()) + " ";
        }

        if(rules.getCommanderJumpMode() != Taflman.JUMP_STANDARD) {
            otnrString += "cj:" + getStringForJumpMode(rules.getCommanderJumpMode()) + " ";
        }

        Set<Coord> corners = rules.getCornerSpaces();
        if(!corners.equals(getDefaultCorners(rules.boardSize))) {
            String cornersString = "cor:";
            for(Coord corner : corners) {
                cornersString += corner.toString() + ",";
            }

            otnrString += cornersString + " ";
        }

        Set<Coord> centers = rules.getCenterSpaces();
        if(!centers.equals(getDefaultCenter(rules.boardSize))) {
            String centerString = "cen:";
            for(Coord center : centers) {
                centerString += center.toString() + ",";
            }

            otnrString += centerString + " ";
        }

        Set<Coord> attackerForts = rules.getAttackerForts();
        if(attackerForts.size() > 0) {
            String fortString = "afor:";
            for(Coord fort : attackerForts) {
                fortString += fort.toString() + ",";
            }

            otnrString += fortString + " ";
        }

        Set<Coord> defenderForts = rules.getDefenderForts();
        if(defenderForts.size() > 0) {
            String fortString = "dfor:";
            for(Coord fort : attackerForts) {
                fortString += fort.toString() + ",";
            }

            otnrString += fortString + " ";
        }

        String corp = getStringForTaflmanTypeList(rules.cornerPassableFor);
        if(!corp.equals(defaults.get("corp"))) otnrString += "corp:" + corp + " ";

        String cenp = getStringForTaflmanTypeList(rules.centerPassableFor);
        if(!cenp.equals(defaults.get("cenp"))) otnrString += "cenp:" + cenp + " ";

        String aforp = getStringForTaflmanTypeList(rules.attackerFortPassableFor);
        if(!aforp.equals(defaults.get("aforp"))) otnrString += "aforp:" + aforp + " ";

        String dforp = getStringForTaflmanTypeList(rules.defenderFortPassableFor);
        if(!dforp.equals(defaults.get("dforp"))) otnrString += "dforp:" + dforp + " ";

        String cors = getStringForTaflmanTypeList(rules.cornerStoppableFor);
        if(!cors.equals(defaults.get("cors"))) otnrString += "cors:" + cors + " ";

        String cens = getStringForTaflmanTypeList(rules.centerStoppableFor);
        if(!cens.equals(defaults.get("cens"))) otnrString += "cens:" + cens + " ";

        String afors = getStringForTaflmanTypeList(rules.attackerFortStoppableFor);
        if(!afors.equals(defaults.get("afors"))) otnrString += "afors:" + afors + " ";

        String dfors = getStringForTaflmanTypeList(rules.defenderFortStoppableFor);
        if(!dfors.equals(defaults.get("dfors"))) otnrString += "dfors:" + afors + " ";

        String corh = getStringForTaflmanTypeList(rules.cornerHostileTo);
        if(!corh.equals(defaults.get("corh"))) otnrString += "corh:" + corh + " ";

        String cenh = getStringForTaflmanTypeList(rules.centerHostileTo);
        if(!cenh.equals(defaults.get("cenh"))) otnrString += "cenh:" + cenh + " ";

        String cenhe = getStringForTaflmanTypeList(rules.emptyCenterHostileTo);
        if(!cenhe.equals(defaults.get("cenhe"))) otnrString += "cenhe:" + cenhe + " ";

        String aforh = getStringForTaflmanTypeList(rules.attackerFortHostileTo);
        if(!aforh.equals(defaults.get("aforh"))) otnrString += "aforh:" + aforh + " ";

        String dforh = getStringForTaflmanTypeList(rules.defenderFortHostileTo);
        if(!dforh.equals(defaults.get("dforh"))) otnrString += "dforh:" + dforh + " ";

        if(rules.allowShieldWallCaptures() != Rules.NO_SHIELDWALL) {
            otnrString += "sw:" + getStringForShieldwallMode(rules.allowShieldWallCaptures()) + " ";
        }

        if(!rules.allowFlankingShieldwallCapturesOnly()) {
            otnrString += "swf:n ";
        }

        if(rules.allowShieldFortEscapes()) {
            otnrString += "efe:y ";
        }

        if(rules.getBerserkMode() != Rules.BERSERK_NONE) {
            otnrString += "ber:" + getStringForBerserkMode(rules.getBerserkMode()) + " ";
        }

        otnrString += "start:" + PositionSerializer.getPositionRecord(rules.getBoard());

        return otnrString;
    }

    public static Rules loadRulesRecord(String otnrString) {
        List<List<Side.TaflmanHolder>> startingTaflmen;

        Map<String, String> config = getRulesMap(otnrString);

        int boardDimension = Integer.parseInt(config.get("dim"));
        Coord.initialize(boardDimension);
        String startPosition = config.get("start");
        startingTaflmen = PositionSerializer.parseTaflmenFromPosition(startPosition);

        Board board = new GenericBoard(boardDimension);
        Side attackers = new GenericSide(board, true, startingTaflmen.get(0));
        //System.out.println(attackers.getStartingTaflmen());
        Side defenders = new GenericSide(board, false, startingTaflmen.get(1));
        //System.out.println(defenders.getStartingTaflmen());
        GenericRules rules = new GenericRules(board, attackers, defenders);

        if(config.containsKey("esc")) rules.setEscapeType(getEscapeTypeForString(config.get("esc")));
        if(config.containsKey("surf")) rules.setSurroundingFatal(getBooleanForString(config.get("surf")));
        if(config.containsKey("atkf")) rules.setAttackersFirst(getBooleanForString(config.get("atkf")));
        if(config.containsKey("tfr")) rules.setThreefoldResult(getThreefoldResultForString(config.get("tfr")));
        if(config.containsKey("ka")) rules.setKingArmed(getBooleanForString(config.get("ka")));
        if(config.containsKey("ks")) rules.setKingStrong(getBooleanForString(config.get("ks")));
        if(config.containsKey("kj")) rules.setKingJumpMode(getJumpModeForString(config.get("kj")));
        if(config.containsKey("nj")) rules.setKnightJumpMode(getJumpModeForString(config.get("nj")));
        if(config.containsKey("cj")) rules.setCommanderJumpMode(getJumpModeForString(config.get("cj")));
        if(config.containsKey("sw")) rules.setShieldwallMode(getShieldwallModeForString(config.get("sw")));
        if(config.containsKey("swf")) rules.setShieldwallFlankingRequired(getBooleanForString(config.get("swf")));
        if(config.containsKey("efe")) rules.setEdgeFortEscape(getBooleanForString(config.get("efe")));
        if(config.containsKey("ber")) rules.setBerserkMode(getBerserkModeForString(config.get("ber")));

        if(config.containsKey("cor")) rules.setCornerSpaces(getCoordListForString(config.get("cor")));
        if(config.containsKey("cen")) rules.setCenterSpaces(getCoordListForString(config.get("cen")));
        if(config.containsKey("afor")) rules.setAttackerForts(getCoordListForString(config.get("afor")));
        if(config.containsKey("dfor")) rules.setDefenderForts(getCoordListForString(config.get("dfor")));

        boolean[] passable, stoppable, hostile, emptyHostile;
        passable = stoppable = hostile = emptyHostile = null;

        // Center
        if(config.containsKey("cenp")) passable = getTaflmanTypeListForString(config.get("cenp"));
        if(config.containsKey("cens")) stoppable = getTaflmanTypeListForString(config.get("cens"));
        if(config.containsKey("cenh")) hostile = getTaflmanTypeListForString(config.get("cenh"));
        if(config.containsKey("cenhe")) emptyHostile = getTaflmanTypeListForString(config.get("cenhe"));
        rules.setCenterParameters(passable, stoppable, hostile, emptyHostile);

        passable = stoppable = hostile = emptyHostile = null;

        // Corner
        if(config.containsKey("corp")) passable = getTaflmanTypeListForString(config.get("corp"));
        if(config.containsKey("cors")) stoppable = getTaflmanTypeListForString(config.get("cors"));
        if(config.containsKey("corh")) hostile = getTaflmanTypeListForString(config.get("corh"));
        rules.setCornerParameters(passable, stoppable, hostile);

        passable = stoppable = hostile = emptyHostile = null;

        // Attacker fort
        if(config.containsKey("aforp")) passable = getTaflmanTypeListForString(config.get("aforp"));
        if(config.containsKey("afors")) stoppable = getTaflmanTypeListForString(config.get("afors"));
        if(config.containsKey("aforh")) hostile = getTaflmanTypeListForString(config.get("aforh"));
        rules.setAttackerFortParameters(passable, stoppable, hostile);

        passable = stoppable = hostile = emptyHostile = null;

        // Defender fort
        if(config.containsKey("dforp")) passable = getTaflmanTypeListForString(config.get("dforp"));
        if(config.containsKey("dfors")) stoppable = getTaflmanTypeListForString(config.get("dfors"));
        if(config.containsKey("dforh")) hostile = getTaflmanTypeListForString(config.get("dforh"));
        rules.setDefenderFortParameters(passable, stoppable, hostile);

        return rules;
    }

    private static Map<String, String> getRulesMap(String rulesString) {
        Map<String, String> config = new HashMap<String, String>();
        String[] components = rulesString.split(" ");
        for(String component : components) {
            String[] keyValue = component.split(":");
            if(keyValue.length == 1) {
                config.put(keyValue[0], "");
            }
            else {
                config.put(keyValue[0], keyValue[1]);
            }
        }

        return config;
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private static int getThreefoldResultForString(String threefoldResult) {
        if(threefoldResult.equals("w")) {
            return Rules.THIRD_REPETITION_WINS;
        }
        else if(threefoldResult.equals("l")) {
            return Rules.THIRD_REPETITION_LOSES;
        }
        else if(threefoldResult.equals("d")) {
            return Rules.DRAW;
        }
        else {
            return Rules.DRAW;
        }
    }

    private static String getStringForThreefoldResult(int threefoldResult) {
        if(threefoldResult == Rules.THIRD_REPETITION_WINS) {
            return "w";
        }
        else if(threefoldResult == Rules.THIRD_REPETITION_LOSES) {
            return "l";
        }
        else if(threefoldResult == Rules.DRAW) {
            return "d";
        }
        else return "i";
    }

    private static int getEscapeTypeForString(String escapeString) {
        if(escapeString.equals("c")) return Rules.CORNERS;
        else return Rules.EDGES;
    }

    private static boolean getBooleanForString(String yesNo) {
        if(yesNo.equals("y")) return true;
        else return false;
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
        for(int i = 0; i < TaflmanCodes.count; i++) {
            if(typeList[i]) typeString += TaflmanCodes.inverse[i];
        }

        return typeString;
    }

    public static boolean[] getTaflmanTypeListForString(String typeString) {
        boolean[] typeList = new boolean[TaflmanCodes.count];
        for(int i = 0; i < typeString.length(); i++) {
            char c = typeString.charAt(i);
            int index = indexOf(TaflmanCodes.inverse, c);

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

    private static List<Coord> getCoordListForString(String coordListString) {
        List<Coord> coordList = new ArrayList<Coord>();

        String[] coords = coordListString.split(",");

        for(String coordString : coords) {
            if(coordString.length() == 0) continue;
            Coord c = Board.getCoordFromChessNotation(coordString);
            coordList.add(c);
        }

        return coordList;
    }

    public static boolean rulesEqual(String r1, String r2) {
        Map<String, String> rules1 = getRulesMap(r1);
        Map<String, String> rules2 = getRulesMap(r2);

        for(Map.Entry<String, String> e : rules1.entrySet()) {
            if(!rules2.containsKey(e.getKey())) return false;

            if(!e.getValue().equals(rules2.get(e.getKey()))) return false;
        }

        return true;
    }
}
