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

import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Taflman;

/**
 * Created by jay on 2/13/16.
 */
public class TaflmanCodes {
    public static final int count = Rules.TAFLMAN_TYPE_COUNT;
    public static final char[] inverse = {'t', 'c', 'n', 'k', 'm', 'g', 'T', 'C', 'N', 'K', 'M', 'G'};
    public static final int t = 0;
    public static final int c = 1;
    public static final int n = 2;
    public static final int k = 3;
    public static final int m = 4;
    public static final int g = 5;
    public static final int T = 6;
    public static final int C = 7;
    public static final int N = 8;
    public static final int K = 9;
    public static final int M = 10;
    public static final int G = 11;

    public static int getIndexForChar(char ch) {
        switch(ch) {
            case 't':
                return t;
            case 'c':
                return c;
            case 'n':
                return n;
            case 'k':
                return k;
            case 'm':
                return m;
            case 'T':
                return T;
            case 'C':
                return C;
            case 'N':
                return N;
            case 'K':
                return K;
            case 'M':
                return M;
            default:
                throw new IllegalArgumentException("Bad character code");
        }
    }

    public static boolean isCodeAttackingSide(char code) {
        boolean isAttacker = true;
        if(Character.isUpperCase(code)) {
            isAttacker = false;
        }

        return isAttacker;
    }

    public static char getTaflmanTypeForCode(char code) {
        char typeChar = Character.toUpperCase(code);
        char typeFlag;
        switch(typeChar) {
            case 'T': typeFlag = Taflman.TYPE_TAFLMAN; break;
            case 'C': typeFlag = Taflman.TYPE_COMMANDER; break;
            case 'N': typeFlag = Taflman.TYPE_KNIGHT; break;
            case 'K': typeFlag = Taflman.TYPE_KING; break;
            case 'M': typeFlag = Taflman.TYPE_MERCENARY; break;
            case 'G': typeFlag = Taflman.TYPE_GUARD; break;
            default: typeFlag = Taflman.TYPE_TAFLMAN;
        }

        return typeFlag;
    }

    public static int getIndexForTaflmanChar(char taflman) {
        boolean attacker = Taflman.getPackedSide(taflman) == Taflman.SIDE_ATTACKERS;
        int index = (attacker ? 0 : Taflman.TYPES_BY_PIECE);

        switch(Taflman.getPackedType(taflman)) {
            // TYPE_TAFLMAN: index += 0
            case Taflman.TYPE_COMMANDER:
                index += 1;
                break;
            case Taflman.TYPE_KNIGHT:
                index += 2;
                break;
            case Taflman.TYPE_KING:
                index += 3;
                break;
            case Taflman.TYPE_MERCENARY:
                index += 4;
                break;
            case Taflman.TYPE_GUARD:
                index += 5;
                break;
        }

        return index;
    }

    public static String getTaflmanNameForTaflman(char taflman, boolean plural) {
        String taflmanType = "taflman";

        switch(Taflman.getPackedType(taflman)) {
            // TYPE_TAFLMAN: index += 0
            case Taflman.TYPE_COMMANDER:
                taflmanType = "commander";
                break;
            case Taflman.TYPE_KNIGHT:
                taflmanType = "knight";
                break;
            case Taflman.TYPE_KING:
                taflmanType = "king";
                break;
            case Taflman.TYPE_MERCENARY:
                taflmanType = "mercenary";
                break;
            case Taflman.TYPE_GUARD:
                taflmanType = "guard";
        }

        if(plural) {
            taflmanType += "s";

            if(taflmanType.equals("taflmans")) taflmanType = "taflmen";
            if(taflmanType.equals("mercenarys")) taflmanType = "mercenaries";
        }

        return taflmanType;
    }
}
