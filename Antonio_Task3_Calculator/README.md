# 03 Calculator

A four function calculator with precedence, a tape line and two error states. Keys fill
the lower half of the screen, the readout sits above them, and nothing crashes on a fast
double tap.

Part of the [OIBSIP set](../README.md). Design: [docs/DESIGN.md](../docs/DESIGN.md).
Build: [docs/BUILD_AND_RUN.md](../docs/BUILD_AND_RUN.md).

| | |
| --- | --- |
| Launcher label | Calculator |
| Package | `bw.oibsip.calc` |
| Screens | `MainActivity` |
| Logic | `Evaluator`, no Android imports |
| Tests | 9 in `EvaluatorTest` |

## What it does

Digits 0 to 9 and a decimal point, plus, minus, times and divide, equals, C and a
backspace. There is also a sign key and a percent key, because a calculator without them
feels incomplete.

The tape above the readout shows the expression as it is built, then the finished
expression with an equals sign after evaluation. Pressing an operator after a result
continues from that result.

Divide by zero puts `Error` in the readout and `Cannot divide by zero` on the tape. The
next digit starts a fresh expression. A result too large for a double puts `Error` and
`Number is out of range` instead.

## Requirements

| Brief line | Where |
| --- | --- |
| Digits 0 to 9 and a decimal | `key0` to `key9`, `keyDot` in `activity_main.xml` |
| Plus, minus, times, divide | `keyAdd`, `keySubtract`, `keyMultiply`, `keyDivide` |
| Equals | `keyEquals`, `pressEquals()` |
| C clears | `keyClear`, `pressClear()` |
| Backspace | `keyDelete` with `ic_backspace`, `pressDelete()` |
| Divide by zero shows Error | `Evaluator` throws, `enterError()` paints the state |
| Grid layout | five rows of four weighted keys |
| No crash on rapid taps | the tape is a token list, so an invalid chain cannot be built |

## How the tape works

The expression is a `List<String>` that alternates number, operator, number. Typing a
digit appends to the current operand. Pressing an operator commits the operand and
appends the operator, or replaces an operator that is already last. Pressing equals drops
a trailing operator first.

That structure is why a fast double tap on `+` cannot produce `12 + + 3`. The second tap
overwrites the last entry instead of appending. There is no string being parsed at the
end, so there is no parse step to fail.

Evaluation runs in two passes. Times and divide fold into the number on their left.
Then plus and minus run left to right. No expression parser, no recursion, no stack.

## Files

```
app/src/main/java/bw/oibsip/calc/
  Evaluator.java     token parsing, two pass evaluation, formatting
  MainActivity.java  keys, tape state, error states, saved state
  GlassUi.java
  GlassApp.java
app/src/main/res/
  layout/activity_main.xml   header, readout card, five key rows
  drawable/                  key shapes, gradient key, icons
  values/, values-night/     tokens, styles, strings
app/src/test/java/.../EvaluatorTest.java
```

## Details worth knowing

**Grouping.** The readout inserts thousands separators as you type, so `1234567` reads
`1,234,567`. Grouping applies to the integer part only.

**Scientific notation.** Above 1e12 or below 1e-9 the readout switches to six figure
scientific notation. `Evaluator.format` handles it in one place so the tape and the
readout agree.

**Backspace.** It deletes from the operand when there is one. When the operand is empty
it removes the last token, and if that token was a number it starts editing that number
instead of throwing it away.

**Errors are a state, not a toast.** An error owns the readout until the next key. That
is easier to read at arm's length than a message that disappears after two seconds.

**Rotation.** The whole tape, the operand, the result and the error all survive.

## Run it

```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Tests:

```
./gradlew test
```
