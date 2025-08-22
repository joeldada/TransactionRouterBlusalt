package com.blusalt.common.utils;

public final class PanUtils {

    private PanUtils() {
    }

    public static String detectScheme(String pan) {
        if (pan == null || pan.length() < 1) return "UNKNOWN";
        if (pan.startsWith("4")) return "VISA";
        if (pan.matches("^5[1-5].*")) return "MASTERCARD";
        if (pan.startsWith("506")) return "VERVE";
        return "UNKNOWN";
    }

    public static String bin6(String pan) {
        return pan != null && pan.length() >= 6 ? pan.substring(0, 6) : null;
    }
}
