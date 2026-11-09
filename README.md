# OIBSIP Android internship

Five Android apps built for the Oasis Infobyte internship, task one to task five.
Every app is plain Java and XML. There is no UI library, no Compose, no third party
kit, and no image assets beyond vector drawables that live in the repo.

The five apps share one design system. It is a dark glass surface with a single mint
to sky gradient, frosted cards, pill controls and a short spring on every press. The
details are in [docs/DESIGN.md](docs/DESIGN.md).

## The apps

| Task | Folder | Package | App |
| ---- | ------ | ------- | --- |
| 1 | `Antonio_Task1_UnitConverter` | `bw.oibsip.converter` | Unit converter, four categories and 26 units |
| 2 | `Antonio_Task2_TodoLogin` | `bw.oibsip.tasks` | Task list with register, sign in and per user tasks |
| 3 | `Antonio_Task3_Calculator` | `bw.oibsip.calc` | Calculator with precedence and error states |
| 4 | `Antonio_Task4_Quiz` | `bw.oibsip.quiz` | Ten question quiz with instant feedback and a score |
| 5 | `Antonio_Task5_Stopwatch` | `bw.oibsip.chrono` | Stopwatch with hundredths, laps and lifecycle safety |

Each folder is a standalone Gradle project. You can open one at a time in Android
Studio, or open the repository root and let Studio pick a folder when you run.

## Build one app

1. Open Android Studio. Use Hedgehog or newer.
2. Choose File, then Open, then pick one of the five folders. For example
   `Antonio_Task3_Calculator`.
3. Wait for Gradle to sync. Studio downloads the AndroidX libraries on the first sync,
   so this step needs internet.
4. Start an emulator from Device Manager, or plug in a phone with USB debugging on.
5. Press the green Run button, or Shift + F10.

The full walkthrough, including the VS Code only route and where each file goes if you
rebuild the project from scratch, is in [docs/BUILD_AND_RUN.md](docs/BUILD_AND_RUN.md).

## Preview without building

Open `preview/index.html` in a browser. It is a working copy of all five screens with
the same layout, colours and timing as the Android build. Type in the fields, tap the
keys, finish a quiz, run the stopwatch. The button in the top right switches the page
to the light palette.

The preview needs nothing installed. It loads Poppins from `preview/fonts` and runs
from `preview/apps.js`, so it works offline.

## Tests

There are 54 JVM unit tests across seven classes. They cover the maths and the state
machines, not the views, so they run without an emulator.

```
cd Antonio_Task1_UnitConverter
./gradlew test
```

Swap in any other folder for the other four suites. On Windows use `gradlew.bat test`.
The test names and what each one protects are listed in [docs/TESTING.md](docs/TESTING.md).

## Design choices worth knowing

Passwords are hashed with PBKDF2-HMAC-SHA256 at 120,000 iterations with a salt per
account. A single SHA or MD5 digest would have been shorter to write and much weaker,
which is why it is not used here.

Stopwatch time comes from `SystemClock.elapsedRealtime()`, not from counting ticks.
Ticks drift when the app is paused, and a stopwatch that loses time in the background
is not a stopwatch.

The calculator keeps a list of numbers and operators instead of a string of text. Fast
or repeated taps can only build a valid chain, so there is no parser to break and no
crash path from a double operator.

The quiz deals ten cards from a bank of twelve using a seed. Shuffling happens in one
pure class, which keeps the order testable.

## Repository layout

```
README.md
docs/
  BUILD_AND_RUN.md     step by step setup for both editors
  DESIGN.md            the glass design system
  SUBMISSION.md        what to upload and what the demo should show
  TESTING.md           unit tests and manual passes
preview/
  index.html           browser prototype of all five apps
  styles.css
  apps.js
  fonts/
Antonio_Task1_UnitConverter/
  README.md
  app/src/main/java/...       source
  app/src/main/res/...        layouts, drawables, fonts
  app/src/test/java/...       unit tests
Antonio_Task2_TodoLogin/
Antonio_Task3_Calculator/
Antonio_Task4_Quiz/
Antonio_Task5_Stopwatch/
```

## Built with

Android Studio Hedgehog or newer, Gradle 8.7, Android Gradle plugin 8.6.1, compileSdk 34,
minSdk 26, Java 17. The only libraries are AndroidX AppCompat, RecyclerView, CardView
and ConstraintLayout. Fonts are Poppins, bundled in each project so the app looks the
same on every phone.

## Submission

The internship asks for a GitHub link and a short demo. [docs/SUBMISSION.md](docs/SUBMISSION.md)
has the checklist, a demo script that fits in two minutes, and the points an evaluator
usually checks.

Repository: https://github.com/Antkkorc/OIBSIP
