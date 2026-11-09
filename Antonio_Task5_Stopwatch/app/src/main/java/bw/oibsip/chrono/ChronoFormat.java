package bw.oibsip.chrono;

import java.util.Locale;

/**
 * Time formatting for the readout. Under an hour it shows MM:SS, over an hour it grows
 * an hours column, and the hundredths always sit after a dot — so the digits never
 * jump sideways as the clock passes a boundary.
 */
public final class ChronoFormat {

    private ChronoFormat() {
    }

    public static String main(long millis) {
        long safe = Math.max(0L, millis);
        long totalSeconds = safe / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    public static String fraction(long millis) {
        long safe = Math.max(0L, millis);
        long hundredths = (safe % 1000L) / 10L;
        return String.format(Locale.US, ".%02d", hundredths);
    }

    public static String full(long millis) {
        return main(millis) + fraction(millis);
    }
}
