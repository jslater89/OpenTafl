package com.manywords.softworks.tafl.notation.playtaflonline;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.notation.NotationParseException;
import com.manywords.softworks.tafl.notation.PositionSerializer;
import com.manywords.softworks.tafl.notation.RulesSerializer;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jay on 9/15/16.
 */
public class PlayTaflOnlineJsonTranslator {

    public static Game readJsonFile(File f) {
        if(f == null) return null;
        if(!f.exists()) return null;

        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(f));
            return readJsonInputStream(bis);
        }
        catch (FileNotFoundException e) {
            Log.print(Log.Level.NORMAL, "Failed to read file: " + f);
        }
        finally {
            if(bis != null) try {
                bis.close();
            }
            catch (IOException e) {
                // best effort
            }
        }

        return null;
    }

    public static Game readJsonInputStream(InputStream stream) {
        JsonReader reader = Json.createReader(stream);
        JsonObject gameObject = reader.readObject();

        JsonArray moveArray = gameObject.getJsonArray(PTOConstants.KEY_MOVES);
        List<MoveRecord> moves = parseMoveArray(moveArray);

        // Get starting layout
        String openTaflLayout = getLayoutForName(gameObject.getString(PTOConstants.KEY_LAYOUT));
        if(openTaflLayout == null) {
            Log.println(Log.Level.NORMAL, "Unknown layout: " + gameObject.getString(PTOConstants.KEY_LAYOUT));
        }
        else {
            openTaflLayout = openTaflLayout.replaceFirst("start\\:", "");
            Log.println(Log.Level.CHATTY, openTaflLayout);
            openTaflLayout = "starti:" + PositionSerializer.invertRecord(openTaflLayout);
            Log.println(Log.Level.CHATTY, openTaflLayout);
        }

        // Translate rules to OTNR
        String openTaflRules = "dim:" + getDimensionForName(gameObject.getString(PTOConstants.KEY_LAYOUT)) + " ";
        openTaflRules += "name:" + getNotationNameForName(gameObject.getString(PTOConstants.KEY_LAYOUT)) + " ";

        switch(gameObject.getInt(PTOConstants.KEY_OBJECTIVE)) {
            case PTOConstants.OBJECTIVE_CORNER: openTaflRules += "esc:c "; break;
            case PTOConstants.OBJECTIVE_EDGE: openTaflRules += "esc:e cor: "; break;
        }

        switch(gameObject.getInt((PTOConstants.KEY_KING_CAPTURE))) {
            case PTOConstants.KING_CUSTODIAN: openTaflRules += "ks:w "; break;
            case PTOConstants.KING_ENCLOSED: openTaflRules += "ks:s "; break;
            case PTOConstants.KING_CONFINED: openTaflRules += "ks:m "; break;
            case PTOConstants.KING_FLEXIBLE: openTaflRules += "ks:c "; break;
        }

        switch(gameObject.getInt((PTOConstants.KEY_KING_STRENGTH))) {
            case PTOConstants.KING_WEAPONLESS: openTaflRules += "ka:n "; break;
            case PTOConstants.KING_ARMED: openTaflRules += "ka:y "; break;
            case PTOConstants.KING_HAMMER: openTaflRules += "ka:h "; break;
            case PTOConstants.KING_ANVIL: openTaflRules += "ka:a "; break;
        }

        switch(gameObject.getInt((PTOConstants.KEY_THRONE))) {
            case PTOConstants.THRONE_EXCLUSIVE: openTaflRules += "cens:K cenp:tcnkTCNK "; break;
            case PTOConstants.THRONE_FORBIDDEN: openTaflRules += "cens: cenp:tcnkTCNK "; break;
            case PTOConstants.THRONE_BLOCK_PAWN: openTaflRules += "cens: cenp:K "; break;
            case PTOConstants.THRONE_BLOCK_ALL: openTaflRules += "cens: cenp: "; break;
            case PTOConstants.THRONE_NONE: openTaflRules += "cen: cens:tcnkTCNK cenp:tcnkTCNK "; break;
        }

        switch(gameObject.getInt((PTOConstants.KEY_HOSTILE))) {
            case PTOConstants.HOSTILE_NONE: openTaflRules += "cenh: cenhe: "; break;
            case PTOConstants.HOSTILE_THRONE: openTaflRules += "cenh:tcnk cenhe:tcnkTCNK "; break;
        }

        switch(gameObject.getInt((PTOConstants.KEY_SPEED))) {
            case PTOConstants.SPEED_KING: openTaflRules += "spd:-1,-1,-1,-1,-1,-1,-1,1 "; break;
            case PTOConstants.SPEED_PAWN: openTaflRules += "spd:1,1,1,1,1,1,1,-1 "; break;
            case PTOConstants.SPEED_ALL: openTaflRules += "spd:1 "; break;
        }

        switch(gameObject.getInt((PTOConstants.KEY_SURROUND), PTOConstants.SURROUND_ENABLED)) {
            case PTOConstants.SURROUND_ENABLED: openTaflRules += "surf:y "; break;
            case PTOConstants.SURROUND_DISABLED: openTaflRules += "surf:n "; break;
        }

        switch(gameObject.getInt((PTOConstants.KEY_EXIT_FORT))) {
            case PTOConstants.EXIT_FORT_ENABLED: openTaflRules += "efe:y "; break;
            case PTOConstants.EXIT_FORT_DISABLED: openTaflRules += "efen: n "; break;
        }

        switch(gameObject.getInt((PTOConstants.KEY_SHIELDWALL))) {
            case PTOConstants.SHIELDWALL_ENABLED: openTaflRules += "sw:s swf:y "; break;
            case PTOConstants.SHIELDWALL_DISABLED: openTaflRules += "sw:n "; break;
        }

        // Load rules, create game, set up some default tag values
        String otnrString = openTaflRules + openTaflLayout;
        Log.println(Log.Level.NORMAL, "Generated rules from JSON: " + otnrString);

        Rules r = null;
        try {
            r = RulesSerializer.loadRulesRecord(otnrString);
        }
        catch(NotationParseException e) {
            Log.println(Log.Level.NORMAL, "Failed to load rules string: " + e.toString());
            return null;
        }

        Log.println(Log.Level.CHATTY, "Generated rules re-serialized: " + RulesSerializer.getRulesRecord(r, true));
        Game g = new Game(r, null);

        // Apply moves
        for(MoveRecord m : moves) {
            int moveResult = g.getCurrentState().makeMove(m);
            Log.println(Log.Level.CHATTY, "Move: " + m + " Result: " + GameState.getStringForMoveResult(moveResult));
        }

        g.setDefaultTags();
        g.getTagMap().put(Game.Tag.ATTACKERS, gameObject.getString(PTOConstants.KEY_ATTACKER, ""));
        g.getTagMap().put(Game.Tag.DEFENDERS, gameObject.getString(PTOConstants.KEY_DEFENDER, ""));
        g.getTagMap().put(Game.Tag.SITE, "playtaflonline.com");

        if(gameObject.getInt(PTOConstants.KEY_START_DATE, -1) != -1) {
            long timestamp = gameObject.getJsonNumber(PTOConstants.KEY_START_DATE).longValue() * 1000;
            g.getTagMap().put(Game.Tag.DATE, new SimpleDateFormat("yyyy.MM.dd").format(new Date(timestamp)));
        }

        JsonObject lastMove = moveArray.getJsonObject(0);
        int endState = lastMove.getInt(PTOConstants.KEY_END_STATE);
        g.getTagMap().put(Game.Tag.RESULT, getResultCode(endState));
        g.getTagMap().put(Game.Tag.TERMINATION, getTerminationString(endState));

        return g;
    }

    private static List<MoveRecord> parseMoveArray(JsonArray moveArray) {
        List<MoveRecord> moves = new ArrayList<>(moveArray.size());

        // Backwards in the game specification
        for(int i = moveArray.size() - 1; i >= 0; i--) {
            JsonObject moveObject = moveArray.getJsonObject(i);
            int fromX, fromY, toX, toY;

            fromX = moveObject.getInt(PTOConstants.KEY_MOVE_X_FROM);
            fromY = moveObject.getInt(PTOConstants.KEY_MOVE_Y_FROM);

            toX = moveObject.getInt(PTOConstants.KEY_MOVE_X_TO);
            toY = moveObject.getInt(PTOConstants.KEY_MOVE_Y_TO);

            if(fromX == -1 || fromY == -1 || toX == -1 || toY == -1) continue;

            Coord startCoord = Coord.get(
                    fromX,
                    fromY);

            Coord endCoord = Coord.get(
                    toX,
                    toY);

            moves.add(new MoveRecord(startCoord, endCoord));
        }

        return moves;
    }

    private static int getDimensionForName(String name) {
        name = name.toLowerCase();
        if(name.equals("hhtablut") || name.equals("tablut") || name.equals("seabattletablut")) return 9;
        else if(name.equals("hhgokstad") || name.equals("seabattlegokstad") || name.equals("gokstad2")) return 13;
        else if(name.equals("hhcoppergate") || name.equals("coppergate2")) return 15;
        else if(name.equals("hhbrandubh") || name.equals("brandubh") || name.equals("hhardri") || name.equals("magpie")) return 7;
        else if(name.equals("hhtawlbwrdd") || name.equals("tawlbwrdd")) return 11;
        else if(name.equals("hhlewiscross") || name.equals("lewiscross")) return 11;
        else if(name.equals("ardri")) return 7;
        else if(name.equals("ballinderry2")) return 7;
        else if(name.equals("ballinderry3")) return 7;
        else if(name.equals("ballinderry4")) return 7;
        else if(name.equals("ballinderry6")) return 7;
        else if(name.equals("ballinderry7")) return 7;
        else if(name.equals("ballinderry8")) return 7;
        else if(name.equals("ballinderry9")) return 7;
        else if(name.equals("fetlar") || name.equals("copenhagen") || name.equals("hnefatafl")) return 11;
        else if(name.equals("seabattlecircle") || name.equals("jarlshofcircle")) return 9;
        else if(name.equals("jarlshof3")) return 9;
        else if(name.equals("seabattlecross") || name.equals("trondheimcross")) return 11;
        else if(name.equals("toftanes1")) return 13;
        else if(name.equals("toftanes2")) return 13;
        else if(name.equals("gokstad1")) return 13;
        else if(name.equals("gokstad3")) return 13;
        else if(name.equals("gokstad4")) return 13;
        else if(name.equals("coppergate1")) return 15;
        else if(name.equals("papillon")) return 9;
        else if(name.equals("alfheim1")) return 17;
        else if(name.equals("alfheim2")) return 17;
        else if(name.equals("aleaevangelii")) return 19;
        else return -1;
    }

    private static String getLayoutForName(String name) {
        name = name.toLowerCase();
        if(name.equals("hhtablut") || name.equals("tablut") || name.equals("seabattletablut")) return PTOConstants.TABLUT_LAYOUT;
        else if(name.equals("hhgokstad") || name.equals("seabattlegokstad") || name.equals("gokstad2")) return PTOConstants.PARLETT_LAYOUT;
        else if(name.equals("hhcoppergate") || name.equals("coppergate2")) return PTOConstants.COPPERGATE_II_LAYOUT;
        else if(name.equals("hhbrandubh") || name.equals("brandubh") || name.equals("hhardri") || name.equals("magpie")) return PTOConstants.BRANDUB_LAYOUT;
        else if(name.equals("hhtawlbwrdd") || name.equals("tawlbwrdd")) return PTOConstants.TAWLBWRDD_LAYOUT;
        else if(name.equals("hhlewiscross") || name.equals("lewiscross")) return PTOConstants.LEWIS_CROSS_LAYOUT;
        else if(name.equals("ardri")) return PTOConstants.ARD_RI_LAYOUT;
        else if(name.equals("ballinderry2")) return PTOConstants.BALLINDERRY_2_LAYOUT;
        else if(name.equals("ballinderry3")) return PTOConstants.BALLINDERRY_3_LAYOUT;
        else if(name.equals("ballinderry4")) return PTOConstants.BALLINDERRY_4_LAYOUT;
        else if(name.equals("ballinderry6")) return PTOConstants.BALLINDERRY_6_LAYOUT;
        else if(name.equals("ballinderry7")) return PTOConstants.BALLINDERRY_7_LAYOUT;
        else if(name.equals("ballinderry8")) return PTOConstants.BALLINDERRY_8_LAYOUT;
        else if(name.equals("ballinderry9")) return PTOConstants.BALLINDERRY_9_LAYOUT;
        else if(name.equals("fetlar") || name.equals("copenhagen") || name.equals("hnefatafl")) return PTOConstants.COPENHAGEN_LAYOUT;
        else if(name.equals("seabattlecircle") || name.equals("jarlshofcircle")) return PTOConstants.JARLSHOF_LAYOUT;
        else if(name.equals("jarlshof3")) return PTOConstants.JARLSHOF_3_LAYOUT;
        else if(name.equals("seabattlecross") || name.equals("trondheimcross")) return PTOConstants.SERIF_CROSS_11_LAYOUT;
        else if(name.equals("toftanes1")) return PTOConstants.TOFTANES_1_LAYOUT;
        else if(name.equals("toftanes2")) return PTOConstants.TOFTANES_2_LAYOUT;
        else if(name.equals("gokstad1")) return PTOConstants.SERIF_CROSS_13_LAYOUT;
        else if(name.equals("gokstad3")) return PTOConstants.GOKSTAD_3_LAYOUT;
        else if(name.equals("gokstad4")) return PTOConstants.GOKSTAD_4_LAYOUT;
        else if(name.equals("coppergate1")) return PTOConstants.SERIF_CROSS_15_LAYOUT;
        else if(name.equals("papillon")) return PTOConstants.PAPILLON_LAYOUT;
        else if(name.equals("alfheim1")) return PTOConstants.ALFHEIM_LAYOUT;
        else if(name.equals("alfheim2")) return PTOConstants.ALFHEIM_II_LAYOUT;
        else if(name.equals("aleaevangelii")) return PTOConstants.ALEA_EVANGELII_LAYOUT;
        else return null;
    }

    private static String getNotationNameForName(String name) {
        name = name.toLowerCase();
        if(name.equals("hhtablut")) return "H.H_Tablut";
        else if(name.equals("tablut")) return "Custom_Tablut";
        else if(name.equals("hhgokstad")) return "H.H._Gokstad";
        else if(name.equals("toftanes1") || name.equals("toftanes2")) return "Custom_Toftanes";
        else if(name.equals("gokstad1") || name.equals("gokstad2") || name.equals("gokstad3") || name.equals("gokstad4")) return "Custom_Gokstad";
        else if(name.equals("seabattlegokstad") || name.equals("seabattlecircle") || name.equals("seabattlecross") || name.equals("seabattletablut")) return "Sea_Battle";
        else if(name.equals("hhcoppergate")) return "H.H._Coppergate";
        else if(name.equals("coppergate1") || name.equals("coppergate2")) return "Custom_Coppergate";
        else if(name.equals("hhbrandubh")) return "H.H_Brandubh";
        else if(name.equals("brandubh")) return "Custom_Brandubh";
        else if(name.equals("hhtawlbwrdd")) return "H.H._Tawlbwrdd";
        else if(name.equals("tawlbwrdd")) return "Custom_Tawlbwrdd";
        else if(name.equals("hhlewiscross")) return "H.H._Lewis_Cross";
        else if(name.equals("lewiscross")) return "Custom_Lewis_Cross";
        else if(name.equals("hhardri")) return "H.H._Ard_Ri";
        else if(name.equals("ardri")) return "Custom_Ard_Ri";
        else if(name.equals("ballinderry2") || name.equals("ballinderry3") || name.equals("ballinderry4") || name.equals("ballinderry6") || name.equals("ballinderry7") || name.equals("ballinderry8") || name.equals("ballinderry9")) return "Custom_Ballinderry";
        else if(name.equals("magpie")) return "Magpie";
        else if(name.equals("fetlar")) return "Fetlar";
        else if(name.equals("copenhagen")) return "Copenhagen";
        else if(name.equals("hnefatafl")) return "Custom_Hnefatafl";
        else if(name.equals("jarlshofcircle")) return "Custom_Jarlshof";
        else if(name.equals("trondheimcross")) return "Custom_Trondheim";
        else if(name.equals("papillon")) return "Custom_Papillon's_Escape";
        else if(name.equals("alfheim1") || name.equals("alfheim2")) return "Custom_Alfheim";
        else if(name.equals("aleaevangelii")) return "Custom_Alea_Evangelii";
        else return name;
    }

    private static String getResultCode(int endState) {
        switch(endState) {
            case PTOConstants.END_STATE_ATTACKER_WIN:
            case PTOConstants.END_STATE_DEFENDER_RESIGN:
            case PTOConstants.END_STATE_DEFENDER_TIME:
                return "1";
            case PTOConstants.END_STATE_DEFENDER_WIN:
            case PTOConstants.END_STATE_ATTACKER_RESIGN:
            case PTOConstants.END_STATE_ATTACKER_TIME:
                return "-1";
            case PTOConstants.END_STATE_DRAW:
            case PTOConstants.END_STATE_GAME_DECLINED:
                return "0";
            default:
                return "?";
        }
    }

    private static String getTerminationString(int endState) {
        switch(endState) {
            case PTOConstants.END_STATE_ATTACKER_WIN:
                return "Attacker wins";
            case PTOConstants.END_STATE_DEFENDER_RESIGN:
                return "Defender resigns";
            case PTOConstants.END_STATE_DEFENDER_TIME:
                return "Defender times out";
            case PTOConstants.END_STATE_DEFENDER_WIN:
                return "Defender wins";
            case PTOConstants.END_STATE_ATTACKER_RESIGN:
                return "Attacker resigns";
            case PTOConstants.END_STATE_ATTACKER_TIME:
                return "Attacker times out";
            case PTOConstants.END_STATE_DRAW:
                return "Draw";
            case PTOConstants.END_STATE_GAME_DECLINED:
                return "Game declined";
            default:
                return "";
        }
    }
}
