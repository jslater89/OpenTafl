package com.manywords.softworks.tafl.notation.playtaflonline;

/**
 * Created by jay on 9/15/16.
 */
public class PTOConstants {
    static final String BRANDUB_LAYOUT = "start:/3t3/3t3/3T3/ttTKTtt/3T3/3t3/3t3/";
    static final String ARD_RI_LAYOUT = "start:/2ttt2/3t3/t2T2t/ttTKTtt/t2T2t/3t3/2ttt2/";
    static final String TABLUT_LAYOUT = "start:/3ttt3/4t4/4T4/t3T3t/ttTTKTTtt/t3T3t/4T4/4t4/3ttt3/";
    static final String JARLSHOF_LAYOUT = "start:/3ttt3/1t5t1/4T4/t3T3t/t1TTKTT1t/t3T3t/4T4/1t5t1/3ttt3/";
    static final String PAPILLON_LAYOUT = "start:/3ttt3/4t4/9/t2TTT2t/tt1TKT1tt/t2TTT2t/9/4t4/3ttt3/";
    static final String TAWLBWRDD_LAYOUT = "start:/4ttt4/4t1t4/5t5/5T5/tt2TTT2tt/t1tTTKTTt1t/tt2TTT2tt/5T5/5t5/4t1t4/4ttt4/";
    static final String COPENHAGEN_LAYOUT = "start:/3ttttt3/5t5/11/t4T4t/t3TTT3t/tt1TTKTT1tt/t3TTT3t/t4T4t/11/5t5/3ttttt3/";
    static final String SERIF_CROSS_11_LAYOUT = "start:/3ttttt3/5t5/5T5/t4T4t/t4T4t/ttTTTKTTTtt/t4T4t/t4T4t/5T5/5t5/3ttttt3/";
    static final String SERIF_CROSS_13_LAYOUT = "start:/3ttttttt3/6t6/6T6/t5T5t/t5T5t/t5T5t/ttTTTTKTTTTtt/t5T5t/t5T5t/t5T5t/6T6/6t6/3ttttttt3/";
    static final String PARLETT_LAYOUT = "start:/4ttttt4/5t1t5/6t6/6T6/t3T3T3t/tt3TTT3tt/t1tT1TKT1Tt1t/tt3TTT3tt/t3T3T3t/6T6/6t6/5t1t5/4ttttt4/";
    static final String SERIF_CROSS_15_LAYOUT = "/3ttttttttt3/7t7/7T7/t6T6t/t6T6t/t6T6t/t6T6t/ttTTTTTKTTTTTtt/t6T6t/t6T6t/t6T6t/t6T6t/7T7/7t7/3ttttttttt3/";
    static final String COPPERGATE_II_LAYOUT = "start:/5ttttt5/6ttt6/7t7/7t7/7T7/t5TTT5t/tt3T1T1T3tt/ttttTTTKTTTtttt/tt3T1T1T3tt/t5TTT5t/7T7/7t7/7t7/6ttt6/5ttttt5/";

    static final String KEY_MOVES = "Move";
    static final String KEY_MOVE_X_FROM = "MoveX";
    static final String KEY_MOVE_Y_FROM = "MoveY";
    static final String KEY_MOVE_X_TO = "MoveXTo";
    static final String KEY_MOVE_Y_TO = "MoveYTo";
    static final String KEY_LAYOUT = "Layout";
    static final String KEY_ATTACKER = "Attacker";
    static final String KEY_DEFENDER = "Defender";
    static final String KEY_START_DATE = "StartDate";

    static final String KEY_OBJECTIVE = "Objective";
    static final int OBJECTIVE_EDGE = 0;
    static final int OBJECTIVE_CORNER = 1;

    static final String KEY_KING_CAPTURE = "KingCapture";
    static final int KING_CUSTODIAN = 0;
    static final int KING_FLEXIBLE = 1;
    static final int KING_ENCLOSED = 2;
    static final int KING_CONFINED = 3;

    static final String KEY_KING_STRENGTH = "KingStrength";
    static final int KING_ARMED = 0;
    static final int KING_WEAPONLESS = 1;
    static final int KING_HAMMER = 2;
    static final int KING_ANVIL = 3;

    static final String KEY_THRONE = "Throne";
    static final int THRONE_NONE = 0;
    static final int THRONE_EXCLUSIVE = 1;
    static final int THRONE_FORBIDDEN = 2;
    static final int THRONE_BLOCK_PAWN = 3;
    static final int THRONE_BLOCK_ALL = 4;

    static final String KEY_HOSTILE = "Hostile";
    static final int HOSTILE_NONE = 0;
    static final int HOSTILE_THRONE = 1;

    static final String KEY_SPEED = "Speed";
    static final int SPEED_UNLIMITED = 0;
    static final int SPEED_KING = 1;
    static final int SPEED_PAWN = 2;
    static final int SPEED_ALL = 3;

    static final String KEY_SURROUND = "Surround";
    static final int SURROUND_ENABLED = 0;
    static final int SURROUND_DISABLED = 1;

    static final String KEY_EXIT_FORT = "ExitFort";
    static final int EXIT_FORT_DISABLED = 0;
    static final int EXIT_FORT_ENABLED = 1;

    static final String KEY_SHIELDWALL = "ShieldWall";
    static final int SHIELDWALL_DISABLED = 0;
    static final int SHIELDWALL_ENABLED = 1;
}
