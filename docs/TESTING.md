# Testing

Every app has a testable core: the maths and the state machines live in classes with **no
`android.*` imports**, so they compile and run as ordinary JVM unit tests — no emulator, no
device, a couple of seconds per project.

**54 tests across five projects.**

| Project | Test class | Tests | Proves |
|---|---|---|---|
| 01 Converter | `ConverterEngineTest` | 13 | exact conversion factors, temperature offsets, absolute-zero guard, decimal-comma parsing, rejection of `1e5`/`NaN`/`1.2.3`, readout formatting, 26 units present |
| 02 Ledger | `PasswordHasherTest` | 6 | hash is never the password, verify accepts/rejects, salts are unique and 16 bytes, same password + new salt → new hash, malformed stored hash fails closed |
| 03 Calculator | `EvaluatorTest` | 9 | precedence (`2+3×4=14`), left-to-right within a precedence, divide-by-zero reason, malformed tape rejected, sign after operator, grouping, chaining |
| 04 Passage | `QuizDeckTest` | 7 | 12 questions × 4 unique options, correct answers spread over A–D, correct answer survives option shuffling, same seed = same deck, different seed ≠ same deck, scoring ignores unanswered |
| 05 Chronograph | `StopwatchTest` | 8 | accumulate while running, freeze on stop, resume from paused total, reset, restored clock counts time away, reboot guard, double-start adds nothing |
| 05 Chronograph | `ChronoFormatTest` | 5 | MM:SS under an hour, hours appear at 1:00:00, zero-padded hundredths, negative input clamped |
| 05 Chronograph | `LapTest` | 6 | splits are differences of the total, best/worst only with 2+ laps, equal splits → no tags, newest-first without mutating, non-monotonic totals cannot go negative |

## Run them

Per project (Gradle downloads dependencies on the first run):

```bash
cd Antonio_Task1_UnitConverter && ./gradlew testDebugUnitTest
cd ../Antonio_Task2_TodoLogin  && ./gradlew testDebugUnitTest
cd ../Antonio_Task3_Calculator && ./gradlew testDebugUnitTest
cd ../Antonio_Task4_Quiz       && ./gradlew testDebugUnitTest
cd ../Antonio_Task5_Stopwatch  && ./gradlew testDebugUnitTest
```

Or in Android Studio: right-click a test class → **Run**, or the green gutter arrow beside a
`@Test`. HTML reports land in `app/build/reports/tests/testDebugUnitTest/index.html`.

## What is *not* covered by unit tests (and why)

| Area | Why | How to verify by hand |
|---|---|---|
| Layouts, spinners, RecyclerView | needs a device; Robolectric would add a dependency the brief does not need | run the app; the demo script in `docs/SUBMISSION.md` lists the taps |
| SQLite schema and queries | `android.database` classes are stubs on the JVM | register two accounts, add tasks in each, confirm each list only shows its own |
| Hashing cost on a real device | device-specific | first login takes ~100–250 ms; the UI stays responsive because it runs on a background thread |
| Stopwatch lifecycle | `SystemClock` is an Android class | start it, lock the screen for a minute, unlock: the elapsed time includes the lock |
| Rotation | configuration changes | rotate mid-quiz: the deck order must not change |

## If a test fails

- `Unresolved reference: junit` → the project synced before dependencies downloaded; run
  `./gradlew testDebugUnitTest` again.
- `PBKDF2 is unavailable on this device` → only possible below API 26; `minSdk` is 26.
- A formatting assertion failing on a non-English machine → the tests pin `Locale.US`
  deliberately; if you changed the formatter to use the default locale, put it back.
