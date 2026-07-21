package com.inawulot.wallet.service;

import org.springframework.stereotype.Service;

@Service
public class InputSanitizer {
    public String clean(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String cleaned = value
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .trim();
        if (cleaned.length() > maxLength) {
            return cleaned.substring(0, maxLength);
        }
        return cleaned;
    }

    public String currency(String value) {
        String cleaned = clean(value, 12).toUpperCase();
        if (!cleaned.matches("[A-Z0-9]{3,8}")) {
            throw new IllegalArgumentException("Currency must use a 3 to 8 character code");
        }
        return cleaned;
    }
}
