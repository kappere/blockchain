package com.wataru.blockchain.core.util;

public class StringUtil {
    public static String dup(String c, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
}
