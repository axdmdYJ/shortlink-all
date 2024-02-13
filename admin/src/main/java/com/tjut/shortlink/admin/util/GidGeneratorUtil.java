package com.tjut.shortlink.admin.util;

import java.util.Random;

public class GidGeneratorUtil {
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String generateRandomString() {
        StringBuilder stringBuilder = new StringBuilder(6);
        Random rnd = new Random();
        while (stringBuilder.length() < 6) { 
            int index = (int) (rnd.nextFloat() * CHAR_POOL.length());
            stringBuilder.append(CHAR_POOL.charAt(index));
        }
        return stringBuilder.toString();
    }
}