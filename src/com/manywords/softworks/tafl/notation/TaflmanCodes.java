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
    public static final char[] inverse = {'t', 'c', 'n', 'k', 'T', 'C', 'N', 'K'};
    public static final int t = 0;
    public static final int c = 1;
    public static final int n = 2;
    public static final int k = 3;
    public static final int T = 4;
    public static final int C = 5;
    public static final int N = 6;
    public static final int K = 7;

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
            case 'T':
                return T;
            case 'C':
                return C;
            case 'N':
                return N;
            case 'K':
                return K;
            default:
                throw new IllegalArgumentException("Bad character code");
        }
    }

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
