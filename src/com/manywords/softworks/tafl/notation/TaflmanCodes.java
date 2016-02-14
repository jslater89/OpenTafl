package com.manywords.softworks.tafl.notation;

import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;

/**
 * Created by jay on 2/13/16.
 */
public class TaflmanCodes {
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

    public static boolean isCodeAttackingSide(char code) {
        boolean moverAttacker = true;
        if(Character.isUpperCase(code)) {
            moverAttacker = false;
        }

        return moverAttacker;
    }

    public static char getTaflmanTypeForCode(char code) {
        char typeChar = Character.toUpperCase(code);
        char typeFlag;
        switch(typeChar) {
            case 'T': typeFlag = Taflman.TYPE_TAFLMAN; break;
            case 'C': typeFlag = Taflman.TYPE_COMMANDER; break;
            case 'N': typeFlag = Taflman.TYPE_KNIGHT; break;
            case 'K': typeFlag = Taflman.TYPE_KING; break;
            default: typeFlag = Taflman.TYPE_TAFLMAN;
        }

        return typeFlag;
    }
}
