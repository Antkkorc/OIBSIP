package bw.oibsip.chrono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A recorded lap: its split (this lap alone) and the running total when it was taken. */
public final class Lap {

    public final int number;
    public final long split;
    public final long total;

    private Lap(int number, long split, long total) {
        this.number = number;
        this.split = split;
        this.total = total;
    }

    /** Builds laps from cumulative totals, oldest first. */
    public static List<Lap> build(List<Long> totals) {
        List<Lap> laps = new ArrayList<>();
        if (totals == null) {
            return laps;
        }
        long previous = 0L;
        for (int i = 0; i < totals.size(); i++) {
            long total = Math.max(previous, totals.get(i) == null ? previous : totals.get(i));
            laps.add(new Lap(i + 1, total - previous, total));
            previous = total;
        }
        return laps;
    }

    /** Index of the shortest split, or -1 until there are two laps to compare. */
    public static int bestIndex(List<Lap> laps) {
        if (laps == null || laps.size() < 2) {
            return -1;
        }
        int best = 0;
        for (int i = 1; i < laps.size(); i++) {
            if (laps.get(i).split < laps.get(best).split) {
                best = i;
            }
        }
        return best;
    }

    /** Index of the longest split, or -1 when the list is too short or all splits match. */
    public static int worstIndex(List<Lap> laps) {
        if (laps == null || laps.size() < 2) {
            return -1;
        }
        int worst = 0;
        for (int i = 1; i < laps.size(); i++) {
            if (laps.get(i).split > laps.get(worst).split) {
                worst = i;
            }
        }
        int best = bestIndex(laps);
        return worst == best ? -1 : worst;
    }

    /** Newest first, which is where a thumb looks after pressing LAP. */
    public static List<Lap> newestFirst(List<Lap> laps) {
        List<Lap> copy = new ArrayList<>(laps);
        Collections.reverse(copy);
        return copy;
    }
}
