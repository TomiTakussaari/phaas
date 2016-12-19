package com.github.tomitakussaari.phaas.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringHelper {

    /**
     * From BCrypt.java
     */
    public static boolean equalsNoEarlyReturn(String a, String b) {
        char[] caa = a.toCharArray();
        char[] cab = b.toCharArray();
        if(caa.length != cab.length) {
            return false;
        } else {
            byte ret = 0;

            for(int i = 0; i < caa.length; ++i) {
                ret = (byte)(ret | caa[i] ^ cab[i]);
            }

            return ret == 0;
        }
    }
}
