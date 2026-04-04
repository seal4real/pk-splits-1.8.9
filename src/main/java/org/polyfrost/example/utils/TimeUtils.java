package org.polyfrost.example.utils;

public class TimeUtils {

    public static String formatTicks(int ticks) {
        int minutes = ticks / 1200;
        int seconds = (ticks % 1200) / 20;
        int centis  = (ticks % 20) * 5;
        return String.format("%d:%02d.%02d", minutes, seconds, centis);
    }

    public static String formatDeltaTicks(int deltaTicks) {
        String sign = deltaTicks < 0 ? "-" : "+";
        int abs = Math.abs(deltaTicks);
        int seconds = abs / 20;
        int centis  = (abs % 20) * 5;
        return String.format("%s%d.%02d", sign, seconds, centis);
    }

}
