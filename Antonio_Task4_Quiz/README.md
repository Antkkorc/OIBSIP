# 04 · Passage — quiz application

**OIBSIP · Android App Development · 2026**
Part of the [five-instrument set](../README.md). Design language: [Gutter & Rule](../docs/STUDIO.md).
Build instructions: [BUILD_AND_RUN.md](../docs/BUILD_AND_RUN.md).

A twelve-question multiple-choice quiz on **coastal navigation** — latitude, lights, soundings,
projections. One question at a time, feedback the instant you answer, a note explaining every
answer, then a scorecard.

| | |
|---|---|
| Launcher label | `04 Passage` |
| Package | `bw.oibsip.passage` |
| Screens | `WelcomeActivity` → `QuizActivity` → `ResultActivity` |
| Testable core | `QuizDeck` + `QuestionBank` (no Android imports) |
| Tests | 7 (`QuizDeckTest`) |

## Screens

```
WELCOME                            QUESTION                       RESULT
Passage                            QUESTION 03 / 12               RESULT
Twelve questions on latitude …     ▬▬▬▬▬▬▬▬▬░░░░░░░░░░              09 of 12
────────────────────────────       What is the period of …        Within a cable’s length …
01 / Questions        12           ─────────────────────────      ─────────────────────────
02 / Order      Shuffled each run  A  One nautical mile            CORRECT            09
03 / Scoring    One point, first   B  One kilometre               INCORRECT          03
04 / Topic      Coastal navigation C  Three flashes / 20 s        SCORE              75%
────────────────────────────       D  Three separate lights       ─────────────────────────
[      START THE QUIZ      ]       NOTE · …                       [    RESTART QUIZ    ]
                                   [      NEXT QUESTION     ]      [       CLOSE       ]
```

## Why coastal navigation

The brief allows any topic. A quiz about a real discipline with checkable facts is easier to defend
than ten general-knowledge questions copied from a tutorial, and the notes give the app something to
say after the answer is locked. Every question has a one-paragraph explanation with a number in it.

## Requirements → implementation

| Brief line | Implementation |
|---|---|
| Welcome screen + Start | `activity_welcome.xml`, `WelcomeActivity` |
| Question text, 4 options, counter | `questionText`, `option0…3`, `question_counter` (`%1$02d / %2$02d`) |
| 10+ questions, hardcoded or JSON | `QuestionBank.all()` — 12 `Question` objects in Java |
| Immediate feedback, green/red | `paintFeedback()` → `bg_option_correct` (moss + 3 dp moss rule) / `bg_option_wrong` (oxide) |
| Next button | `advance()`; disabled until the question is answered |
| Score tracking | `QuizDeck.score(deck, answers)` — recomputed from the answer list, no drifting counter |
| Results: score, correct, incorrect, restart | `activity_result.xml`, `ResultActivity` with `EXTRA_CORRECT` / `EXTRA_TOTAL` |
| Shuffled each time | `QuizDeck.deal(bank, seed)` shuffles questions *and* options |

## The seed decision

`deal()` takes a `long seed` and uses `new Random(seed)`. The Activity stores that seed in
`onSaveInstanceState` along with the index and the answers.

- Same seed → exactly the same deck, so **rotating the phone cannot reshuffle the exam** mid-run.
- New run → `System.nanoTime()` → a genuinely different order.
- It also makes the shuffler testable: `sameSeedDealsSameOrder`, `differentSeedsDealDifferentOrders`.

Shuffling options is done by text (`options.indexOf(correctText)`), which is unambiguous because the
bank test asserts four *unique* options per question.

## Accessibility note

Feedback is not colour alone: the correct option gets a 3 dp moss rule on its left edge, the wrong
pick gets an oxide rule, and a written note below states the correct fact. Colour is the fastest
signal, not the only one.

## Edge cases handled

| Case | Behaviour |
|---|---|
| Answer changed after tapping | Impossible — `answered` guards every option; the first tap is the only tap |
| Last question | Button label becomes “See results” |
| Long question / option text | Question wraps in serif, options wrap in mono, the whole option block scrolls |
| Long note | Scrolls inside the option area, never clipped |
| Rotation mid-quiz | Seed + index + answers restored; identical deck |
| Back from result | `QuizActivity` is finished, so back returns to the welcome card |
| Exactly 0% or 100% | Distinct verdicts; `correct == total` is checked before the fractions |
| 12 correct, 0 incorrect | Incorrect row shows `00`, not a blank |

## Files

```
app/src/main/java/bw/oibsip/passage/
├── Question.java        immutable question (prompt, options, correct index, note)
├── QuestionBank.java    12 questions, correct answers spread over A–D
├── QuizDeck.java        seeded deal + scoring (JVM-testable)
├── WelcomeActivity.java
├── QuizActivity.java    options, feedback, progress, state
└── ResultActivity.java  scorecard + verdict + restart
app/src/test/java/bw/oibsip/passage/QuizDeckTest.java
```

## Run and test

```bash
cd Antonio_Task4_Quiz
./gradlew testDebugUnitTest      # 7 tests
./gradlew installDebug
```

## Demo in 40 seconds

1. **Start the quiz** → “Question 01 / 12”.
2. Answer one **wrong** on purpose → correct option green with a rule, yours oxide, note appears.
3. **Next** → the 2 dp progress rule advances.
4. Finish → `09 of 12`, correct/incorrect/percentage, verdict.
5. **Restart quiz** → the first question changes (new seed).
6. Rotate the phone mid-quiz → the same question and the same option order.

## Viva

**Why three activities rather than one with fragments?**
The brief asks for `Intent` and `Bundle` navigation, and the three states are genuinely different
screens with different back behaviour. Passing `correct`/`total` as Intent extras keeps the result
screen stateless.

**How does scoring stay correct?**
It is derived, not accumulated: `QuizDeck.score(deck, answers)` walks the answer list each time.
An accumulated counter drifts the moment a screen is restored; a derived one cannot.

**What if a question had two identical options?**
`options.indexOf(correctText)` would pick the first. The bank test forbids duplicates
(`4 == new HashSet<>(question.options).size()`), so this is checked rather than assumed.

**Why hardcode 12 questions instead of a JSON asset?**
Twelve questions are 12 lines of Java and no parser, no asset loading, no malformed-JSON path. If
the bank grew past thirty, a JSON asset or a Room table would be the right move.

**How would you add a timer?**
A `CountDownTimer` in `QuizActivity`, paused/resumed with the activity lifecycle, writing the
remaining seconds into the same saved-instance-state bundle as the index.
