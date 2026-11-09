# 04 Quiz

Ten questions, four options each, instant feedback and a score at the end. Three screens
and no timer.

Part of the [OIBSIP set](../README.md). Design: [docs/DESIGN.md](../docs/DESIGN.md).
Build: [docs/BUILD_AND_RUN.md](../docs/BUILD_AND_RUN.md).

| | |
| --- | --- |
| Launcher label | Quiz |
| Package | `bw.oibsip.quiz` |
| Screens | `WelcomeActivity`, `QuizActivity`, `ResultActivity` |
| Logic | `QuizDeck`, no Android imports |
| Tests | 7 in `QuizDeckTest` |

## What it does

A start card explains the format and holds one button. The quiz screen shows the
question, the four options, a progress bar and `Question 3 of 10`. Picking an option
locks the answer: the correct one turns green, a wrong pick turns red, the rest fade, and
a line appears explaining the answer. Next turns into See results on the tenth question.

The results screen shows the score, the number correct, the number incorrect, the
accuracy and a short verdict, then deals a new run.

## Requirements

| Brief line | Where |
| --- | --- |
| Welcome screen and start button | `activity_welcome.xml`, `WelcomeActivity` |
| Four options per question | `option0` to `option3`, built by `QuizDeck` |
| Question N of 10 | `question_counter`, set in `QuizActivity.showQuestion()` |
| At least ten questions | `QuestionBank` holds twelve |
| Instant green and red feedback | `paintFeedback()` swaps the option drawables |
| Next button | disabled until an answer is locked |
| Score | `QuizDeck.score()` |
| Results with correct and incorrect counts | `ResultActivity` |
| Restart | Play again deals a new run |

## The deck

`QuestionBank` supplies twelve questions with four options, a correct index and a note.
`QuizDeck.deal(bank, seed)` shuffles the bank, takes ten, then shuffles the options
inside each card while tracking the new correct index. The whole thing is seeded, so the
same seed deals the same run and the tests can check it.

Answer locking is what keeps the score honest. An answered question cannot be answered
again, so a user cannot tap every option to find the green one and still score.

## Files

```
app/src/main/java/bw/oibsip/quiz/
  Question.java      prompt, options, correct index, note
  QuestionBank.java  the twelve questions
  QuizDeck.java      deal, score, card model
  WelcomeActivity.java
  QuizActivity.java  one question at a time, feedback, progress
  ResultActivity.java
  GlassUi.java
  GlassApp.java
app/src/main/res/layout/
  activity_welcome.xml  activity_quiz.xml  activity_result.xml
app/src/test/java/.../QuizDeckTest.java
```

## Details worth knowing

**Progress.** The bar is two weighted views. The used weight is the number of questions
finished, the remaining weight is the rest. It animates because the layout pass does the
interpolation.

**Colours.** Green and red are the only saturated colours on the screen, and they only
appear after an answer. Before that the four options are identical, which is what makes
the feedback readable at a glance.

**Saved state.** Rotation keeps the seed, the current question and every answer already
recorded, including the highlight on the options. The quiz does not restart.

**Result copy.** The verdict changes with the score rather than praising everyone.
Telling someone they did well at three out of ten is not useful.

## Run it

```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Tests:

```
./gradlew test
```

To check the dealing by hand, run the quiz twice and compare the order. The bank is
twelve long, so a repeat is possible but rare.
