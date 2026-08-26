package com.inawulot.wallet.service;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class TotpService {
    private static final char[] BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private final SecureRandom random = new SecureRandom();

    public String newSecret() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        StringBuilder encoded = new StringBuilder(32);
        int buffer = 0, bits = 0;
        for (byte value : bytes) { buffer = (buffer << 8) | (value & 255); bits += 8; while (bits >= 5) { encoded.append(BASE32[(buffer >> (bits - 5)) & 31]); bits -= 5; } }
        if (bits > 0) encoded.append(BASE32[(buffer << (5 - bits)) & 31]);
        return encoded.toString();
    }

    public boolean matches(String secret, String code) {
        if (secret == null || !code.matches("^[0-9]{6}$")) return false;
        long step = System.currentTimeMillis() / 30_000L;
        for (long offset = -1; offset <= 1; offset++) if (code.equals(code(secret, step + offset))) return true;
        return false;
    }

    private String code(String secret, long step) {
        try {
            byte[] key = decode(secret);
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(step).array());
            int offset = hash[hash.length - 1] & 15;
            int value = ((hash[offset] & 127) << 24) | ((hash[offset + 1] & 255) << 16) | ((hash[offset + 2] & 255) << 8) | (hash[offset + 3] & 255);
            return "%06d".formatted(value % 1_000_000);
        } catch (Exception exception) { return ""; }
    }

    private byte[] decode(String secret) {
        int buffer = 0, bits = 0, size = 0;
        byte[] output = new byte[secret.length() * 5 / 8];
        for (char character : secret.toUpperCase().toCharArray()) {
            int value = new String(BASE32).indexOf(character); if (value < 0) throw new IllegalArgumentException();
            buffer = (buffer << 5) | value; bits += 5;
            if (bits >= 8) { output[size++] = (byte) (buffer >> (bits - 8)); bits -= 8; }
        }
        return output;
    }
}
