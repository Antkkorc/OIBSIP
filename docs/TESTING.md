# Testing

Two layers. JVM unit tests for the maths and the state machines, and a manual pass for
each screen, because a converter that returns the right number in a broken layout still
fails the brief.

## Run the unit tests

From a task folder:

```
./gradlew test
```

Windows:

```
gradlew.bat test
```

In Android Studio you can right click `app/src/test` and choose Run tests, or press the
green arrow next to a class name. The report lands in
`app/build/reports/tests/testDebugUnitTest/index.html`.

The tests have no Android dependencies, so they run on a plain JVM in a second or two.
That is deliberate: anything worth testing was moved into a class with no `android.*`
imports.

## What is covered

54 tests across seven classes.

### Task 1, `ConverterEngineTest`, 13 tests

Conversion in both directions for length, weight and volume, identical units passing
straight through, temperature round trips through Celsius, absolute zero allowed and
one step below it refused, input parsing with commas, rejection of non numbers, readout
formatting including scientific notation at the extremes, and a check that every
category offers at least two units.

The kilogram to pound test pins the exact definition, 2.20462262185, rather than a
loose delta. If someone edits the factor the test fails immediately.

### Task 2, `PasswordHasherTest`, 6 tests

The hash never equals the password, the right password verifies, the wrong one does
not, salts are unique and sixteen bytes, the same password with two salts gives two
hashes, and a corrupt stored hash fails closed instead of throwing into the login
screen.

Sixty thousand iterations of PBKDF2 in a test would be slow, so the tests use the same
code path with a lower count and separate the count from the logic.

### Task 3, `EvaluatorTest`, 9 tests

Multiplication before addition, left to right within one precedence level, decimals and
the ASCII fallbacks `*` and `/`, division by zero throwing a reason, a malformed chain
throwing `IllegalArgumentException`, a sign after an operator staying part of the
number, grouping in the readout, and plain numbers chaining back into the tape.

### Task 4, `QuizDeckTest`, 7 tests

The bank has at least ten questions with four options each, the correct answer appears
in all four positions across the bank, dealing keeps every question and its correct
answer, the same seed deals the same order, a different seed deals a different order,
scoring counts only matching answers, and shuffled options stay with their question.

### Task 5, `ChronoFormatTest`, 5 tests

Minutes and seconds under an hour, hours appearing only when needed, zero padded
hundredths, the combined readout, and negative input clamped rather than printed as a
minus sign.

### Task 5, `LapTest`, 6 tests

Splits as differences of the running total, no best or worst tag until there are two
laps, best as the shortest split and worst as the longest, equal splits producing no
tags, newest first reversing without mutating the input list, and non monotonic totals
never producing a negative split.

### Task 5, `StopwatchTest`, 8 tests

Starts at zero, accumulates while running, freezes on stop, resumes from the paused
total, resets to zero, a restored running clock counts the time away, a restored clock
after a reboot pauses instead of going negative, and a double start does not add time.

## Manual pass

Run each list on an emulator or a phone before you record the demo. Each one takes a
minute.

### Converter

1. Open the app. Length is selected and the result reads 0.
2. Type 250 with centimetre to inch. Press Convert. The result is 98.425197.
3. Tap Weight. The unit lists change and the result clears.
4. Clear the value and press Convert. A toast says to enter a value.
5. Type `12abc` and press Convert. A toast says it is not a number.
6. Swap the units. The result updates without pressing Convert again.
7. Choose Fahrenheit and Celsius, type -300, press Convert. The absolute zero message
   appears.
8. Long press the result. It copies to the clipboard.

### Task list

1. Create an account with a short password. The form refuses it.
2. Create the account properly. You land on the task list with the empty state visible.
3. Add a task with a note. It appears at the top with today's time.
4. Tap the row. The title gets a strikethrough and the check tile fills with the
   gradient.
5. Press Done, then Open, then All. The counts and the list agree.
6. Delete a task and cancel the sheet. The task is still there. Delete again and
   confirm. It is gone.
7. Press Log out. You are back on sign in.
8. Sign in as a second account. The first account's tasks are not visible.
9. Rotate the phone. The list and the filter survive.

### Calculator

1. Type 12 + 7 x 3 =. The result is 33.
2. Press C. The readout returns to 0.
3. Type 8 / 0 =. The readout reads Error and the tape says Cannot divide by zero.
4. Press 5. The error clears and the new number starts.
5. Tap an operator twice quickly. The second replaces the first and nothing crashes.
6. Type a long number. The readout shrinks to fit instead of clipping.

### Quiz

1. Read the start card and press Start the quiz.
2. The counter reads Question 1 of 10 and the progress bar is empty.
3. Pick an answer. The right option turns green, a wrong pick turns red, and the note
   appears.
4. Press an option again. Nothing changes.
5. Work through all ten. Next becomes See results on the last one.
6. The results screen shows the score, correct, incorrect and accuracy.
7. Press Play again. A new order is dealt.
8. Rotate mid quiz. You stay on the same question with the same answer marked.

### Stopwatch

1. Press Start. The clock runs and the state pill reads Running.
2. Press Lap twice. Two laps appear, newest first.
3. Press Pause. The clock stops and Start becomes Resume.
4. Press Start again. The clock continues from where it stopped.
5. Press Reset. The clock reads 00:00.00 and the lap list is empty.
6. Start, take a lap, then send the app to the background for ten seconds. Come back.
   The elapsed time includes the time away.
7. Start, then rotate the phone. The clock keeps counting and the laps survive.
