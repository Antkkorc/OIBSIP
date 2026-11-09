# 05 Stopwatch

A stopwatch with hundredths, laps and a clock that survives rotation, backgrounding and
process death.

Part of the [OIBSIP set](../README.md). Design: [docs/DESIGN.md](../docs/DESIGN.md).
Build: [docs/BUILD_AND_RUN.md](../docs/BUILD_AND_RUN.md).

| | |
| --- | --- |
| Launcher label | Stopwatch |
| Package | `bw.oibsip.chrono` |
| Screens | `MainActivity` |
| Logic | `Stopwatch`, `Lap`, `ChronoFormat`, no Android imports |
| Tests | 19 across three classes |

## What it does

Start, pause, resume and reset, plus a lap key. The readout counts in hundredths and
grows an hours column after sixty minutes. Each control dims when it has nothing to do,
so Start is pale while the clock runs and Pause is pale while it is stopped.

Laps list newest first with the split, the running total and a Best or Slowest tag. The
tags recalculate on every lap, because a new lap can take either title away.

The status pill reads Ready, Running or Paused, and the dot beside it breathes while the
clock runs. The screen stays awake for as long as it does.

## Requirements

| Brief line | Where |
| --- | --- |
| HH:MM:SS or millisecond display | `ChronoFormat.main()` and `fraction()` |
| Start, pause, reset | four buttons in `activity_main.xml` |
| Button states change | `updateControls()` dims and relabels the row |
| Lifecycle safe | time from `SystemClock.elapsedRealtime()`, state in `SharedPreferences` |
| Lap bonus | lap key, `Lap` model, best and slowest tags |

## Why the clock is not a counter

A `Handler` that adds 40ms every tick looks fine and drifts. It drifts more when the app
is paused, because the callbacks stop and the missed time never comes back.

This stopwatch stores when the current run started in `SystemClock.elapsedRealtime()`
milliseconds, which counts from boot and cannot be moved by the user setting the time.
Elapsed time is the stored total plus the difference between now and the start. The
handler only repaints the number 25 times a second; it never decides what the number is.

Every state change writes four values to `SharedPreferences`: the accumulated total, the
start stamp, whether it is running, and the lap list. A restored running clock counts the
time away. A restored clock where the start stamp is later than now, which happens after
a reboot clears `elapsedRealtime`, pauses instead of going negative.

## Files

```
app/src/main/java/bw/oibsip/chrono/
  Stopwatch.java     elapsed time, start, stop, reset, restore
  Lap.java           splits, totals, best and worst index, newest first
  ChronoFormat.java  readout formatting
  LapAdapter.java    the glass lap row
  MainActivity.java  controls, ticking, persistence, keep screen on
  GlassUi.java
  GlassApp.java
app/src/main/res/layout/
  activity_main.xml  item_lap.xml
app/src/test/java/bw/oibsip/chrono/
  StopwatchTest.java  ChronoFormatTest.java  LapTest.java
```

## Details worth knowing

**Repaint rate.** 40ms, which is 25 frames a second. Hundredths change three or four
times between repaints, and the eye cannot tell. The clock reads correctly because the
value is computed, not incremented.

**Keep screen on.** The window flag is set while running and cleared when the clock stops
or the screen is left.

**Laps.** The stored list holds cumulative totals. Splits are differences of consecutive
totals, so a lap cannot be out of step with the total beside it.

**Pause on rotation.** Rotation does not stop the clock. Nothing in `onPause` stops it
either, because a phone call or a notification shade should not end a timing run.

## Run it

```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Tests:

```
./gradlew test
```

For the lifecycle check, start the clock, take one lap, then press Home for ten seconds
and reopen. The readout includes the time away.
