package net.miarma.mkernel.util;

public class ConversionUtil {
    public static long cooldownToMillis(String s) {
        if(s.contains("s"))
            return Long.parseLong(s.replace("s", "")) * 1000;
        if(s.contains("m"))
            return Long.parseLong(s.replace("m", "")) * 60 * 1000;
        if(s.contains("h"))
            return Long.parseLong(s.replace("h", "")) * 3600 * 1000;
        if(s.contains("d"))
            return Long.parseLong(s.replace("d", "")) * 86400 * 1000;
        return 0;
    }

    public static String millisToCooldown(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if(days > 0)
            return days + "d";
        if(hours > 0)
            return hours + "h";
        if(minutes > 0)
            return minutes + "m";
        return seconds + "s";
    }
}
