# 05 · Chronograph — stopwatch

**OIBSIP · Android App Development · 2026**
Part of the [five-instrument set](../README.md). Design language: [Gutter & Rule](../docs/STUDIO.md).
Build instructions: [BUILD_AND_RUN.md](../docs/BUILD_AND_RUN.md).

A stopwatch that keeps time across screen locks, backgrounding and process death, because it never
counts ticks: elapsed time is always derived from the boot clock. Start, stop, resume, reset, and
record laps with best/worst splits.

| | |
|---|---|
| Launcher label | `05 Chronograph` |
| Package | `bw.oibsip.chrono` |
| Screen | `MainActivity` (single activity) |
| Testable core | `Stopwatch`, `ChronoFormat`, `Lap` (no Android imports) |
| Tests | 19 (three classes) |

## Screen

```
OIBSIP / INSTRUMENT 05                            REV 1.0
──────────────────────────────────────────────────────────
Chronograph
Forty-millisecond resolution, measured from the boot clock …
┌────────────────────────────────────────────────────────┐
│ ELAPSED                               ● RUNNING        │
│ 04:12.37                                               │  ← 66 sp mono, autosized
│ ─────────────────────────────────────────────────────  │
│ Last lap 00:58.12 · total 04:12.37                     │
└────────────────────────────────────────────────────────┘
LAPS                                        03 recorded
  #                SPLIT      TOTAL
  L03             00:41.06   00:41.06   BEST
  L02             00:58.12   00:58.12
  L01             01:12.40   01:12.40   WORST
[   START   ][    STOP    ]
[    LAP    ][   RESET    ]
```

## The clock model

```java
elapsed = running ? accumulated + max(0, now - startedAt) : accumulated;
```

- `now` is `SystemClock.elapsedRealtime()` — milliseconds since boot, immune to the user changing
  the clock, the timezone or an NTP correction. `System.currentTimeMillis()` would not be.
- The `Handler` posts a repaint every **40 ms**. It is a *view* of the model, never the source of
  truth: if the screen freezes for two seconds, the next frame shows the right number.
- `accumulated`, `startedAt` and `running` are written to `SharedPreferences` on every state change
  and in `onPause()`. Reopening the app reconstructs the model, and the elapsed total includes the
  time away.
- **Reboot guard:** `elapsedRealtime` restarts at zero on reboot, so a stored `startedAt` in the
  future means the phone restarted. `restore()` detects this (`startedAt > now`) and pauses the
  clock at the banked total instead of showing a negative or absurd time.

## Requirements → implementation

| Brief line | Implementation |
|---|---|
| Large HH:MM:SS / MM:SS:ms readout | `timeDisplay` — `MM:SS.cc`, growing to `H:MM:SS.cc` after an hour; the hundredths are a `RelativeSizeSpan` at 0.42× |
| Start | `startButton` → `stopwatch.start()` |
| Stop / pause | `stopButton` → `stopwatch.stop()` |
| Reset to zero | `resetButton` → `stopwatch.reset()` and laps cleared |
| Buttons change state | `updateControls()`: Start greys out and becomes “Resume” when paused; Stop/Lap only while running |
| Survives navigating away / `onPause`+`onResume` | Persisted model + `onResume()` catch-up render + `SystemClock.elapsedRealtime()` |
| (Bonus) Lap list in a scrollable list | `RecyclerView` + `LapAdapter`; splits, totals, BEST/WORST tags |

Extra behaviour that is not in the brief but belongs: `FLAG_KEEP_SCREEN_ON` while running, a 7 dp
oxide run dot, and `L%02d` lap numbering that matches the readout's typography.

## Edge cases handled

| Case | Behaviour |
|---|---|
| Screen locked for a minute | Elapsed includes it (boot clock, not ticks) |
| Process killed, app reopened | Restored from prefs; still running |
| Phone rebooted mid-run | Paused at the banked total, no negative time |
| Double-tap Start | `start()` is a no-op while running, so no time is added |
| Lap before start | Impossible: LAP is disabled unless running |
| 100+ laps | RecyclerView scrolls; splits are computed from totals, so no quadratic work |
| All laps equal | BEST only, no WORST (equal splits would tag one row twice) |
| Non-monotonic stored totals | `Lap.build` clamps so a split can never be negative |
| Rotation | The readout and buttons re-render from the same model; nothing is lost |
| Reset while running | Stops the clock first, then zeroes it |

## Files

```
app/src/main/java/bw/oibsip/chrono/
├── Stopwatch.java     ticker-injected clock model (JVM-testable)
├── ChronoFormat.java  MM:SS / H:MM:SS / hundredths (JVM-testable)
├── Lap.java           splits, best/worst, ordering (JVM-testable)
├── LapAdapter.java    RecyclerView rows with BEST/WORST tags
└── MainActivity.java  persistence, Handler ticks, controls, keep-screen-on
app/src/test/java/bw/oibsip/chrono/
├── StopwatchTest.java   (8)
├── ChronoFormatTest.java (5)
└── LapTest.java          (6)
```

## Run and test

```bash
cd Antonio_Task5_Stopwatch
./gradlew testDebugUnitTest      # 19 tests
./gradlew installDebug
```

## Demo in 40 seconds

1. **Start** → Start greys out, Stop and Lap activate, hundredths tick.
2. **Lap** twice → newest lap at the top with BEST/WORST tags.
3. **Stop** → frozen; Start now reads **Resume**.
4. Lock the phone, wait ten seconds, unlock → the clock moved on by ten seconds.
5. **Reset** → `00:00.00`, lap ledger empty with “No laps yet.”

## Viva

**Why `elapsedRealtime()` and not `currentTimeMillis()`?**
`currentTimeMillis()` is the wall clock and can jump backwards (NTP, timezone, a manual change) —
a stopwatch that runs backwards is a bug report. `elapsedRealtime()` is monotonic since boot and
keeps counting while the device sleeps.

**Why a Handler instead of a Timer or a thread?**
The UI can only be touched on the main thread. `Handler.postDelayed` gives a main-thread callback
that is trivial to cancel in `onPause`, and it avoids a background thread whose only job is to
post to the main thread anyway.

**What happens if the app is killed?**
Nothing is lost: the three numbers are in `SharedPreferences`. On reopen, `elapsed()` recomputes
from the stored start, so the stopwatch catches up instantly.

**Why can't `elapsed()` be negative?**
`Math.max(0, now - startedAt)` plus the reboot guard in `restore()`. The unit test
`restoredClockAfterRebootPausesInsteadOfGoingNegative` pins that behaviour.

**Is 40 ms accurate enough?**
The display refreshes at 25 fps, but the *value* is exact — it is computed from timestamps, not
accumulated from ticks. Sub-centisecond drift on screen is cosmetic; the elapsed total never drifts.
