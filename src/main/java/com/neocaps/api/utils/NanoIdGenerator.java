package com.neocaps.api.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class NanoIdGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    // 12 characters is the sweet spot for your label size
    public static String generateShortId() {
        char[] idBuilder = new char[12];
        for (int i = 0; i < 12; i++) {
            idBuilder[i] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(idBuilder);
    }
}
