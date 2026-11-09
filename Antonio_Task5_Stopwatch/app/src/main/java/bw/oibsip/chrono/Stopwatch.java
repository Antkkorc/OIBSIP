package bw.oibsip.chrono;

/**
 * The clock model. Time comes from an injected ticker, which is what makes it testable:
 * production passes SystemClock.elapsedRealtime(), tests pass a counter.
 *
 * Uses elapsed time since boot rather than wall-clock time, so changing the phone's
 * clock — or the user's timezone — cannot make the stopwatch jump or run backwards.
 */
public final class Stopwatch {

    public interface Ticker {
        long millis();
    }

    private final Ticker ticker;

    private long accumulated;
    private long startedAt;
    private boolean running;

    public Stopwatch(Ticker ticker) {
        this.ticker = ticker;
    }

    public long elapsed() {
        if (!running) {
            return accumulated;
        }
        long delta = ticker.millis() - startedAt;
        return accumulated + Math.max(0L, delta);
    }

    public boolean isRunning() {
        return running;
    }

    public long accumulated() {
        return accumulated;
    }

    public long startedAt() {
        return startedAt;
    }

    public void start() {
        if (running) {
            return;
        }
        startedAt = ticker.millis();
        running = true;
    }

    public void stop() {
        if (!running) {
            return;
        }
        accumulated = elapsed();
        running = false;
        startedAt = 0L;
    }

    public void reset() {
        accumulated = 0L;
        startedAt = 0L;
        running = false;
    }

    /**
     * Rehydrates a stopwatch from storage. If the stored start is in the future the
     * device has rebooted since it was written (elapsedRealtime restarts at zero), so
     * the clock is paused at the time that was banked instead of showing nonsense.
     */
    public void restore(long accumulated, long startedAt, boolean running) {
        this.accumulated = Math.max(0L, accumulated);
        if (running && startedAt > 0L && startedAt <= ticker.millis()) {
            this.startedAt = startedAt;
            this.running = true;
        } else {
            this.startedAt = 0L;
            this.running = false;
        }
    }
}
