package bw.oibsip.chrono;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LapTest {

    @Test
    public void splitsAreDifferencesOfTheRunningTotal() {
        List<Lap> laps = Lap.build(Arrays.asList(1_000L, 3_500L, 4_000L));
        assertEquals(3, laps.size());
        assertEquals(1, laps.get(0).number);
        assertEquals(1_000L, laps.get(0).split);
        assertEquals(1_000L, laps.get(0).total);
        assertEquals(2_500L, laps.get(1).split);
        assertEquals(3_500L, laps.get(1).total);
        assertEquals(500L, laps.get(2).split);
        assertEquals(4_000L, laps.get(2).total);
    }

    @Test
    public void bestAndWorstIgnoreASingleLap() {
        List<Lap> one = Lap.build(Collections.singletonList(1_000L));
        assertEquals(-1, Lap.bestIndex(one));
        assertEquals(-1, Lap.worstIndex(one));
        assertTrue(Lap.build(null).isEmpty());
    }

    @Test
    public void bestIsTheShortestSplitAndWorstTheLongest() {
        List<Lap> laps = Lap.build(Arrays.asList(1_000L, 3_500L, 4_000L));
        assertEquals(2, Lap.bestIndex(laps));
        assertEquals(1, Lap.worstIndex(laps));
    }

    @Test
    public void equalSplitsProduceNoTags() {
        List<Lap> laps = Lap.build(Arrays.asList(2_000L, 4_000L, 6_000L));
        assertEquals(-1, Lap.worstIndex(laps));
        assertEquals(0, Lap.bestIndex(laps));
    }

    @Test
    public void newestFirstReversesWithoutMutatingTheOriginal() {
        List<Lap> laps = Lap.build(Arrays.asList(1_000L, 3_500L, 4_000L));
        List<Lap> reversed = Lap.newestFirst(laps);
        assertEquals(3, reversed.get(0).number);
        assertEquals(1, reversed.get(2).number);
        assertEquals(1, laps.get(0).number);
    }

    @Test
    public void nonMonotonicTotalsCannotProduceNegativeSplits() {
        List<Lap> laps = Lap.build(Arrays.asList(3_000L, 1_000L));
        assertEquals(3_000L, laps.get(1).total);
        assertEquals(0L, laps.get(1).split);
    }
}
