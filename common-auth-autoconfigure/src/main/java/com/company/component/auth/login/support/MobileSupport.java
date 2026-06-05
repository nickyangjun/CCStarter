package com.company.component.auth.login.support;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public final class MobileSupport {

    private static final Pattern CHINA_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

    private MobileSupport() {
    }

    public static void requireValid(String mobile) {
        if (!StringUtils.hasText(mobile) || !CHINA_MOBILE.matcher(mobile.trim()).matches()) {
            throw new IllegalArgumentException("invalid mobile");
        }
    }

    public static String normalize(String mobile) {
        return mobile.trim();
    }

    public static String mask(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return "***";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    public static String mobileSuffix(String mobile, int smsLength) {
        String normalized = normalize(mobile);
        if (normalized.length() < smsLength) {
            return normalized;
        }
        return normalized.substring(normalized.length() - smsLength);
    }
}
