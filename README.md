# OIBSIP — five Android instruments

**Oasis Infobyte Android App Development internship · Track: Android · 2026**

Five small Android apps, built as one set of measuring instruments rather than five unrelated
demos: a **converter**, a **ledger** (to-do with login), a **calculator**, a **quiz** and a
**chronograph**. Java, XML, SQLite, no third-party UI kit.

The brief asked for at least three of five tasks. All five are here.

| # | Instrument | Task | Stack | Status |
|---|---|---|---|---|
| 01 | [Converter](Antonio_Task1_UnitConverter/) | Unit Converter | Java · XML · Spinner | Complete |
| 02 | [Ledger](Antonio_Task2_TodoLogin/) | To-Do with Login | Java · XML · SQLite · PBKDF2 | Complete |
| 03 | [Calculator](Antonio_Task3_Calculator/) | Calculator | Java · XML · GridLayout | Complete |
| 04 | [Passage](Antonio_Task4_Quiz/) | Quiz Application | Java · XML · 12 questions | Complete |
| 05 | [Chronograph](Antonio_Task5_Stopwatch/) | Stopwatch | Java · XML · Handler | Complete |

Design language: [`docs/STUDIO.md`](docs/STUDIO.md) · Build and run: [`docs/BUILD_AND_RUN.md`](docs/BUILD_AND_RUN.md) ·
Tests: [`docs/TESTING.md`](docs/TESTING.md) · Submission: [`docs/SUBMISSION.md`](docs/SUBMISSION.md)

---

## See the UI in 10 seconds

`preview/index.html` is a working browser prototype of all five screens — same tokens, same copy,
same maths as the Java. Open it, or serve it:

```bash
cd preview && python3 -m http.server 8000
# then open http://localhost:8000
```

It is a **prototype for demonstration**, not a replacement: the deliverables are the five Android
Studio projects in this repository.

---

## Quick start (Android Studio)

1. **Install what you need** — Android Studio (any 2023.1+ build) with Android SDK **34** and a JDK
   **17** runtime. Android Studio ships its own JBR, which is fine.
2. **Open one project at a time.** Each task is a self-contained Gradle project:
   `File → Open… →` pick `Antonio_Task1_UnitConverter` (not the repository root).
3. Let Gradle sync (first sync downloads the Android Gradle Plugin 8.6.1 and AndroidX).
4. Press **Run ▶** with an emulator (API 26+) or a USB device. `minSdk 26`, `targetSdk 34`.
5. Repeat for tasks 2–5. They are independent; you can also open them in separate windows.

Run the unit tests from the project root:

```bash
cd Antonio_Task3_Calculator
./gradlew testDebugUnitTest
```

Full instructions, including the common Gradle/JDK errors and how to export a signed APK, are in
[`docs/BUILD_AND_RUN.md`](docs/BUILD_AND_RUN.md).

---

## Repository map

```
OIBSIP/
├── README.md                        ← you are here
├── docs/
│   ├── STUDIO.md                    ← design thesis, tokens, motion policy
│   ├── BUILD_AND_RUN.md             ← Android Studio, step by step + troubleshooting
│   ├── TESTING.md                   ← what the unit tests prove
│   └── SUBMISSION.md                ← form checklist, naming, demo script, LinkedIn
├── preview/                         ← interactive browser prototype (design demo)
│   ├── index.html · styles.css · apps.js
│   └── fonts/                       ← Instrument Serif + IBM Plex Mono
├── Antonio_Task1_UnitConverter/     ← each folder is its own Android Studio project
├── Antonio_Task2_TodoLogin/
├── Antonio_Task3_Calculator/
├── Antonio_Task4_Quiz/
└── Antonio_Task5_Stopwatch/
```

Each project folder contains `README.md` (features, files, edge cases, viva answers),
`app/src/main/java/…`, `app/src/main/res/…`, `app/src/test/java/…` and the Gradle wrapper.

> **File naming:** the brief asks for `YourName_TaskNumber`. These folders are
> `Antonio_Task1_UnitConverter` … `Antonio_Task5_Stopwatch` — the required prefix plus a
> descriptor. If your evaluator wants the bare form, rename to `Antonio_Task1` … `Antonio_Task5`
> and update nothing else (the Gradle project names are independent of the folder name).

---

## The design (short version)

**Thesis.** Read one number, trust it, act on it. Every screen is one instrument column: a
numbered legend in a 76 dp gutter, values right-aligned onto 1 dp hairlines, and one dominant
readout anchored low on the screen. Nothing is centred, nothing floats, nothing is in a card.

- **Type:** Instrument Serif for voice (titles, verdicts, the open-task count) + IBM Plex Mono for
  every number and label. No Roboto body copy, no Inter.
- **Colour:** paper `#F3F0E9`, ink `#17160F`, one accent — **oxide red `#C8452B`** — used once per
  screen. Moss `#2E6B4E` means "correct" and nothing else.
- **Signatures:** the engraved `NN / NAME` legend; hairlines instead of cards (zero elevation,
  zero corner radius anywhere in the repo); text buttons (`DEL`, `SWAP`, `LOG OUT`) instead of
  icon glyphs.
- **Motion:** 120–160 ms alpha only, and only when state changes. No entrance animations.

Full reasoning, references and the token table: [`docs/STUDIO.md`](docs/STUDIO.md).

---

## Requirement map

Every line of the brief, with where it is implemented. Tick these off as you record your demo.

### Task 1 — Unit Converter

| Requirement | Where |
|---|---|
| Numeric input field | `activity_main.xml` → `inputValue`, validated in `MainActivity.convert()` |
| Source unit dropdown | `fromUnit` Spinner, populated from `ConverterEngine.units()` |
| Target unit dropdown | `toUnit` Spinner, same adapter |
| Convert button | `convertButton` → `MainActivity.convert(true)` |
| Result with unit label | Ink readout + `ConverterEngine.sentence()` ("250 CM = 98.425197 IN · via metre") |
| 3+ categories | Length (8), Weight (7), Volume (8), Temperature (3) = 26 units |
| Toast on empty/non-numeric | `toast_empty`, `toast_nan` |
| Category selector resets dropdowns | `categoryRow` tabs → `selectCategory()` |

### Task 2 — To-Do with Login

| Requirement | Where |
|---|---|
| Login screen + button | `activity_login.xml`, `LoginActivity` |
| Sign-up with name/email/password | `activity_register.xml`, `RegisterActivity` |
| Passwords hashed in SQLite | `PasswordHasher` — PBKDF2-HMAC-SHA256, 120k iterations, 16-byte salt per account |
| Logout clears session | `SessionStore.signOut()` + `TasksActivity.logOut()` |
| Task list for the logged-in user | `TaskStore.list(userId, filter)` — every query scoped by `user_id` |
| Add task dialog with notes | `dialog_task.xml`, `showAddDialog()` |
| Mark complete (strikethrough) | `TaskAdapter` → `Paint.STRIKE_THRU_TEXT_FLAG` |
| Delete permanently | `dialog_confirm.xml` → `TaskStore.delete()` |
| Tasks linked per account | `tasks.user_id` FK with `ON DELETE CASCADE` |
| Empty state | `activity_tasks.xml` → `emptyState`, copy per filter |

### Task 3 — Calculator

| Requirement | Where |
|---|---|
| Display TextView | Ink readout + expression tape (`tape`, `result`) |
| Digits 0–9 and decimal | 11 keys, `pressDigit()` / `pressDot()` |
| + − × ÷ | `operatorListener(...)`, `pressOperator()` |
| Equals | `pressEquals()` → `Evaluator.evaluate()` |
| Clear | `keyClear` → `pressClear()` |
| Backspace | `keyDelete` → `pressDelete()` |
| Division by zero → "Error" | `Evaluator` throws `DIVIDE_BY_ZERO`; readout shows `ERROR` |
| GridLayout key grid | `GridLayout` with 4 columns × 5 rows and row/column weights |
| No crashes on rapid taps | Token model + guards + `try/catch` around evaluation |

### Task 4 — Quiz

| Requirement | Where |
|---|---|
| Welcome screen + Start | `activity_welcome.xml`, `WelcomeActivity` |
| Question + 4 options + counter | `activity_quiz.xml`, `question_counter` string |
| 10+ questions | `QuestionBank` — 12 on coastal navigation |
| Green/red immediate feedback | `paintFeedback()` → `bg_option_correct` / `bg_option_wrong` |
| Next button | `advance()` |
| Score tracking | `QuizDeck.score(deck, answers)` |
| Results: score, correct, incorrect, restart | `activity_result.xml`, `ResultActivity` |
| Shuffled each run | `QuizDeck.deal(bank, seed)` — question order *and* option order |

### Task 5 — Stopwatch

| Requirement | Where |
|---|---|
| Large HH:MM:SS (or MM:SS:ms) readout | `timeDisplay`, `ChronoFormat` (MM:SS.cc, grows to H:MM:SS.cc) |
| Start | `startButton` → `stopwatch.start()` |
| Stop / pause | `stopButton` → `stopwatch.stop()` |
| Reset to 00:00:00 | `resetButton` → `stopwatch.reset()` |
| Buttons change state | `updateControls()` — Start greys out and relabels to Resume |
| Survives navigating away | State persisted in `SharedPreferences`, elapsed derived from `SystemClock.elapsedRealtime()` |
| (Bonus) Laps in a scrollable list | `LapAdapter`, `item_lap.xml`, BEST/WORST tags |

---

## Engineering notes

- **Testable cores.** Conversion maths (`ConverterEngine`), expression evaluation (`Evaluator`),
  password hashing (`PasswordHasher`), deck dealing (`QuizDeck`), the clock model (`Stopwatch`),
  lap maths (`Lap`) and time formatting (`ChronoFormat`) contain **no `android.*` imports**, so they
  run as plain JVM unit tests. 54 tests across all five projects: `docs/TESTING.md`.
- **Nothing on the network.** No app requests the INTERNET permission. The quiz bank is local, the
  ledger is local, the converter is local.
- **Hashing.** The brief asked for "basic hashing". A bare MD5/SHA digest is the usual shortcut and
  the wrong one; PBKDF2 with a per-account salt costs ~150 ms per login and is a real password
  hash. Login work runs off the UI thread.
- **Rotation.** Converter, calculator and quiz all survive rotation (`onSaveInstanceState`); the
  quiz re-deals from a saved seed so the exam cannot reshuffle under the user's thumb.
- **Fonts.** Instrument Serif and IBM Plex Mono are bundled in each project's `res/font`
  (SIL Open Font License, see `preview/fonts/` origin: Google Fonts).

## Credits

Built for the Oasis Infobyte internship (OIBSIP), 2026. Question content in Task 4 is factual
maritime-navigation material written for this project; no third-party question banks are used.
Instrument Serif and IBM Plex Mono © their authors, licensed under the SIL Open Font License 1.1.
