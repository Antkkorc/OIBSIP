package bw.oibsip.chrono;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Stopwatch screen. Elapsed time comes from SystemClock.elapsedRealtime() and is saved
 * on every state change, so leaving the screen, locking the phone or losing the process
 * cannot lose time. The Handler only repaints; it is never the source of truth.
 */
public class MainActivity extends AppCompatActivity {

    private static final long TICK_MS = 40L;
    private static final String PREFS = "chrono_state";
    private static final String KEY_ACCUMULATED = "accumulated";
    private static final String KEY_STARTED_AT = "started_at";
    private static final String KEY_RUNNING = "running";
    private static final String KEY_LAPS = "laps";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Long> lapTotals = new ArrayList<>();
    private final LapAdapter adapter = new LapAdapter();

    private Stopwatch stopwatch;

    private TextView timeDisplay;
    private TextView stateLabel;
    private TextView lastLap;
    private TextView lapCount;
    private View runDot;
    private View emptyLaps;
    private Button startButton;
    private Button stopButton;
    private Button lapButton;
    private Button resetButton;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            renderTime();
            if (stopwatch.isRunning()) {
                handler.postDelayed(this, TICK_MS);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlassUi.install(this, R.layout.activity_main);

        stopwatch = new Stopwatch(new Stopwatch.Ticker() {
            @Override
            public long millis() {
                return SystemClock.elapsedRealtime();
            }
        });

        timeDisplay = findViewById(R.id.timeDisplay);
        stateLabel = findViewById(R.id.stateLabel);
        lastLap = findViewById(R.id.lastLap);
        lapCount = findViewById(R.id.lapCount);
        runDot = findViewById(R.id.runDot);
        emptyLaps = findViewById(R.id.emptyLaps);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        lapButton = findViewById(R.id.lapButton);
        resetButton = findViewById(R.id.resetButton);

        RecyclerView list = findViewById(R.id.lapList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        lapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordLap();
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        GlassUi.pressAll(startButton, stopButton, lapButton, resetButton);

        GlassUi.enter(findViewById(R.id.header), 0L);
        GlassUi.enter(findViewById(R.id.cardReadout), 70L);
        GlassUi.enter(findViewById(R.id.lapHeader), 130L);
        GlassUi.enter(findViewById(R.id.dock), 190L);

        restore();
        renderAll();
    }

    // ---------------------------------------------------------------- controls

    private void start() {
        if (stopwatch.isRunning()) {
            return;
        }
        stopwatch.start();
        persist();
        renderAll();
        startTicking();
    }

    private void stop() {
        if (!stopwatch.isRunning()) {
            return;
        }
        stopwatch.stop();
        persist();
        stopTicking();
        renderAll();
    }

    private void reset() {
        stopTicking();
        stopwatch.reset();
        lapTotals.clear();
        persist();
        renderAll();
    }

    private void recordLap() {
        if (!stopwatch.isRunning()) {
            return;
        }
        lapTotals.add(stopwatch.elapsed());
        persist();
        renderLaps();
        GlassUi.refresh(lapCount);
    }

    // ---------------------------------------------------------------- rendering

    private void renderAll() {
        renderTime();
        renderLaps();
        updateControls();
        updateKeepScreenOn();
    }

    private void renderTime() {
        long elapsed = stopwatch.elapsed();
        String main = ChronoFormat.main(elapsed);
        String fraction = ChronoFormat.fraction(elapsed);

        SpannableString readout = new SpannableString(main + fraction);
        int start = main.length();
        readout.setSpan(new RelativeSizeSpan(0.42f), start, readout.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        readout.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.text_tertiary)),
                start, readout.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        boolean sameLength = timeDisplay.length() == readout.length();
        timeDisplay.setText(readout);
        if (!sameLength) {
            GlassUi.refresh(timeDisplay);
        }

        stateLabel.setText(stopwatch.isRunning()
                ? R.string.state_running
                : (elapsed > 0L ? R.string.state_paused : R.string.state_idle));
    }

    private void renderLaps() {
        List<Lap> chronological = Lap.build(lapTotals);
        adapter.setLaps(Lap.newestFirst(chronological));

        lapCount.setText(getResources().getQuantityString(
                R.plurals.laps_count, lapTotals.size(), lapTotals.size()));
        emptyLaps.setVisibility(lapTotals.isEmpty() ? View.VISIBLE : View.GONE);

        if (chronological.isEmpty()) {
            lastLap.setText(R.string.last_lap_none);
        } else {
            Lap last = chronological.get(chronological.size() - 1);
            lastLap.setText(getString(R.string.last_lap,
                    ChronoFormat.full(last.split), ChronoFormat.full(last.total)));
        }
    }

    /** Every control dims when it has nothing to do, so the next tap is obvious. */
    private void updateControls() {
        boolean running = stopwatch.isRunning();
        long elapsed = stopwatch.elapsed();

        startButton.setEnabled(!running);
        startButton.setAlpha(running ? 0.35f : 1f);
        startButton.setText(elapsed > 0L ? R.string.resume : R.string.start);
        stopButton.setEnabled(running);
        stopButton.setAlpha(running ? 1f : 0.35f);
        lapButton.setEnabled(running);
        lapButton.setAlpha(running ? 1f : 0.35f);
        resetButton.setEnabled(elapsed > 0L || !lapTotals.isEmpty());
        resetButton.setAlpha(resetButton.isEnabled() ? 1f : 0.35f);

        runDot.setVisibility(running ? View.VISIBLE : View.INVISIBLE);
        if (running) {
            runDot.animate().cancel();
            runDot.setAlpha(1f);
            runDot.animate().alpha(0.35f).setDuration(600).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (runDot.getVisibility() == View.VISIBLE) {
                        runDot.animate().alpha(1f).setDuration(600).start();
                    }
                }
            }).start();
        }
    }

    private void updateKeepScreenOn() {
        if (stopwatch.isRunning()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void startTicking() {
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    private void stopTicking() {
        handler.removeCallbacks(ticker);
    }

    // ---------------------------------------------------------------- persistence

    private void persist() {
        StringBuilder laps = new StringBuilder();
        for (int i = 0; i < lapTotals.size(); i++) {
            if (i > 0) {
                laps.append(',');
            }
            laps.append(lapTotals.get(i));
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putLong(KEY_ACCUMULATED, stopwatch.accumulated())
                .putLong(KEY_STARTED_AT, stopwatch.startedAt())
                .putBoolean(KEY_RUNNING, stopwatch.isRunning())
                .putString(KEY_LAPS, laps.toString())
                .apply();
    }

    private void restore() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        long accumulated = prefs.getLong(KEY_ACCUMULATED, 0L);
        long startedAt = prefs.getLong(KEY_STARTED_AT, 0L);
        boolean running = prefs.getBoolean(KEY_RUNNING, false);

        stopwatch.restore(accumulated, startedAt, running);

        lapTotals.clear();
        String saved = prefs.getString(KEY_LAPS, "");
        if (saved != null && !saved.isEmpty()) {
            for (String part : saved.split(",")) {
                if (!part.isEmpty()) {
                    try {
                        lapTotals.add(Long.parseLong(part));
                    } catch (NumberFormatException ignored) {
                        // A corrupt entry is dropped rather than crashing the clock.
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------- lifecycle

    @Override
    protected void onResume() {
        super.onResume();
        // Catch up on any time spent away, then keep repainting if still running.
        renderAll();
        if (stopwatch.isRunning()) {
            startTicking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTicking();
        persist();
    }
}
