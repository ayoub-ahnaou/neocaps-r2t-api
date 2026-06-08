package com.neocaps.api.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

public class KeyCompressor {
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String compressUUID(UUID uuid) {
        // Convert UUID to a 128-bit positive BigInteger
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        BigInteger number = new BigInteger(1, bb.array());

        // Encode to Base62
        StringBuilder sb = new StringBuilder();
        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divideAndRemainder = number.divideAndRemainder(BigInteger.valueOf(62));
            sb.append(BASE62.charAt(divideAndRemainder[1].intValue()));
            number = divideAndRemainder[0];
        }
        return sb.reverse().toString();
    }
}
