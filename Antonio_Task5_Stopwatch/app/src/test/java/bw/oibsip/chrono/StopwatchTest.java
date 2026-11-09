package bw.oibsip.chrono;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StopwatchTest {

    private static final class FakeTicker implements Stopwatch.Ticker {
        long now = 1000L;

        @Override
        public long millis() {
            return now;
        }

        void advance(long millis) {
            now += millis;
        }
    }

    @Test
    public void startsAtZero() {
        Stopwatch watch = new Stopwatch(new FakeTicker());
        assertEquals(0L, watch.elapsed());
        assertFalse(watch.isRunning());
    }

    @Test
    public void accumulatesWhileRunning() {
        FakeTicker ticker = new FakeTicker();
        Stopwatch watch = new Stopwatch(ticker);
        watch.start();
        ticker.advance(2500L);
        assertEquals(2500L, watch.elapsed());
        ticker.advance(500L);
        assertEquals(3000L, watch.elapsed());
    }

    @Test
    public void stopFreezesTheTotal() {
        FakeTicker ticker = new FakeTicker();
        Stopwatch watch = new Stopwatch(ticker);
        watch.start();
        ticker.advance(1200L);
        watch.stop();
        ticker.advance(9000L);
        assertEquals(1200L, watch.elapsed());
        assertFalse(watch.isRunning());
    }

    @Test
    public void restartResumesFromThePausedTotal() {
        FakeTicker ticker = new FakeTicker();
        Stopwatch watch = new Stopwatch(ticker);
        watch.start();
        ticker.advance(2000L);
        watch.stop();
        ticker.advance(5000L);
        watch.start();
        ticker.advance(500L);
        assertEquals(2500L, watch.elapsed());
        assertTrue(watch.isRunning());
    }

    @Test
    public void resetZeroesEverything() {
        FakeTicker ticker = new FakeTicker();
        Stopwatch watch = new Stopwatch(ticker);
        watch.start();
        ticker.advance(4000L);
        watch.reset();
        assertEquals(0L, watch.elapsed());
        assertFalse(watch.isRunning());
    }

    @Test
    public void restoredRunningClockCountsTimeAway() {
        FakeTicker ticker = new FakeTicker();
        ticker.now = 10_000L;
        Stopwatch watch = new Stopwatch(ticker);
        watch.restore(5_000L, 8_000L, true);
        assertTrue(watch.isRunning());
        assertEquals(7_000L, watch.elapsed());
    }

    @Test
    public void restoredClockAfterRebootPausesInsteadOfGoingNegative() {
        FakeTicker ticker = new FakeTicker();
        ticker.now = 1_000L;
        Stopwatch watch = new Stopwatch(ticker);
        watch.restore(5_000L, 900_000L, true);
        assertFalse(watch.isRunning());
        assertEquals(5_000L, watch.elapsed());
    }

    @Test
    public void doubleStartDoesNotAddTime() {
        FakeTicker ticker = new FakeTicker();
        Stopwatch watch = new Stopwatch(ticker);
        watch.start();
        ticker.advance(300L);
        watch.start();
        ticker.advance(200L);
        assertEquals(500L, watch.elapsed());
    }
}
