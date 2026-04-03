package org.polyfrost.example.utils;

public class TimeUtils {

    public static String formatMillis(long ms) {
        long minutes = ms / 60_000;
        long seconds = (ms % 60_000) / 1_000;
        long millis = ms % 1_000;
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }

    public static String formatDelta(long deltaMs) {
        String sign = deltaMs < 0 ? "-" : "+";
        long abs = Math.abs(deltaMs);
        long seconds = abs / 1_000;
        long millis = abs % 1_000;
        return String.format("%s%d.%03d", sign, seconds, millis);
    }

}
