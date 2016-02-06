package com.manywords.softworks.tafl.rules.serializer;

import com.manywords.softworks.tafl.rules.*;

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

        otnrString += "start:" + rules.getBoard().getOTNPositionString();

        return otnrString;
    }

    public static Rules getRulesForString(String otnrString) {
        int boardSize = 0;
        boolean attackerCommanders = false;
        boolean defenderCommanders = false;
        boolean attackerKnights = false;
        boolean defenderKnights = false;
        List<List<Side.TaflmanHolder>> startingTaflmen;

        Map<String, String> config = new HashMap<String, String>();
        String[] components = otnrString.split(" ");
        for(String component : components) {
            String[] keyValue = component.split(":");
            if(keyValue.length == 1) {
                config.put(keyValue[0], "");
            }
            else {
                config.put(keyValue[0], keyValue[1]);
            }
        }

        System.out.println(config);

        int boardDimension = Integer.parseInt(config.get("dim"));
        String startPosition = config.get("start");
        startingTaflmen = parseTaflmenFromPosition(startPosition);

        Board board = new GenericBoard(boardDimension);
        Side attackers = new GenericSide(board, true, startingTaflmen.get(0));
        //System.out.println(attackers.getStartingTaflmen());
        Side defenders = new GenericSide(board, false, startingTaflmen.get(1));
        //System.out.println(defenders.getStartingTaflmen());
        Rules rules = new GenericRules(board, attackers, defenders);

        return rules;
    }

    private static List<List<Side.TaflmanHolder>> parseTaflmenFromPosition(String startPosition) {
        List<Side.TaflmanHolder> attackers = new ArrayList<Side.TaflmanHolder>();
        List<Side.TaflmanHolder> defenders = new ArrayList<Side.TaflmanHolder>();
        List<List<Side.TaflmanHolder>> taflmen = new ArrayList<>();
        taflmen.add(attackers);
        taflmen.add(defenders);

        String[] rawRows = startPosition.split("/");
        List<String> rows = new ArrayList<String>();
        for(String row : rawRows) {
            if(row.length() > 0) rows.add(row);
        }

        int currentAttackerId = 0;
        int currentDefenderId = 0;

        int currentRow = 0;
        for(String row : rows) {
            boolean inNumber = false;
            String numberSoFar = "";

            int currentCol = 0;
            for(int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);

                // Catch multi-number digits
                if(Character.isDigit(c)) {
                    inNumber = true;
                    numberSoFar += c;
                    continue;
                }
                else if(inNumber && !Character.isDigit(c)) {
                    currentCol += Integer.parseInt(numberSoFar);
                    numberSoFar = "";
                    inNumber = false;
                }

                char side = 0;
                char id = 0;
                char type = 0;

                if(Character.isUpperCase(c)) {
                    side = Taflman.SIDE_DEFENDERS;
                    id = (char) currentDefenderId++;
                }
                else {
                    side = Taflman.SIDE_ATTACKERS;
                    id = (char) currentAttackerId++;
                }

                char typeChar = Character.toUpperCase(c);
                switch(typeChar) {
                    case 'T': type = Taflman.TYPE_TAFLMAN; break;
                    case 'C': type = Taflman.TYPE_COMMANDER; break;
                    case 'N': type = Taflman.TYPE_KNIGHT; break;
                    case 'K': type = Taflman.TYPE_KING; break;
                    default: type = Taflman.TYPE_TAFLMAN;
                }

                char taflman = Taflman.encode(id, type, side);
                Coord coord = Coord.get(currentCol, currentRow);

                Side.TaflmanHolder th = new Side.TaflmanHolder(taflman, coord);
                if(side == Taflman.SIDE_ATTACKERS) {
                    attackers.add(th);
                }
                else {
                    defenders.add(th);
                }
                currentCol++;
            }

            currentRow++;
        }


        return taflmen;
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
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
