package com.company.component.auth.login.support;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public final class EmailSupport {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private EmailSupport() {
    }

    public static void requireValid(String email) {
        if (!StringUtils.hasText(email) || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("invalid email");
        }
    }

    public static String normalize(String email) {
        return email.trim().toLowerCase();
    }

    public static String mask(String email) {
        if (!StringUtils.hasText(email)) {
            return "***";
        }
        String normalized = normalize(email);
        int at = normalized.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return normalized.charAt(0) + "***" + normalized.substring(at);
    }
}
