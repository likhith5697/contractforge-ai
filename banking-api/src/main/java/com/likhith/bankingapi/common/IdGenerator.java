package com.likhith.bankingapi.common;

import java.util.UUID;

/**
 * Generates realistic, synthetic, human-readable domain identifiers such as
 * CUS-8F3A1C2B90. Never derived from or resembling any real-world identifier scheme.
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    public static String generate(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return prefix + "-" + suffix;
    }

    public static String token() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
