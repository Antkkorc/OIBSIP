# 03 · Calculator

**OIBSIP · Android App Development · 2026**
Part of the [five-instrument set](../README.md). Design language: [Gutter & Rule](../docs/STUDIO.md).
Build instructions: [BUILD_AND_RUN.md](../docs/BUILD_AND_RUN.md).

A four-function calculator with a real expression model: precedence, chained results, sign toggle,
percent, delete, and a divide-by-zero state that says what happened instead of crashing.

| | |
|---|---|
| Launcher label | `03 Calculator` |
| Package | `bw.oibsip.calc` |
| Screen | `MainActivity` (single activity, `GridLayout` keys) |
| Testable core | `Evaluator` (no Android imports) |
| Tests | 9 (`EvaluatorTest`) |

## Screen

```
OIBSIP / INSTRUMENT 03                            REV 1.0
──────────────────────────────────────────────────────────
Calculator
Four functions, twenty keys. No history, nothing saved.
┌────────────────────────────────────────────────────────┐
│ 03 / DISPLAY                       2 + 3 × 4           │
│                                          14            │  ← the one dominant object
└────────────────────────────────────────────────────────┘
  AC    ±     %     ÷
  7     8     9     ×
  4     5     6     −        ← 4 × 5 GridLayout, weighted cells, 1 dp rules
  1     2     3     +
  0     .    DEL    =        ← oxide key, the only accent on screen
```

## The tape model (why it never crashes)

The expression is **not** a string that gets eval'd. It is a `List<String>` that alternates number,
operator, number, … plus the operand currently being typed:

```
tokens  = ["12", "×", "4"]      operand = "5"      → tape "12 × 4", readout 5
press × → tokens become ["12", "×", "4", "×"]       → operator replaced, never doubled
press = → evaluate(["12", "×", "4", "×"]) after dropping the trailing operator
```

That invariant is what makes rapid tapping harmless: a second consecutive operator *replaces* the
first, a leading operator starts from `0`, and an empty tape returns `0` instead of throwing.

## Requirements → implementation

| Brief line | Implementation |
|---|---|
| Display TextView for input and result | `tape` (expression, 12 sp mono) + `result` (autosize 56 → 20 sp) |
| Digits 0–9 and decimal | 10 digit keys → `pressDigit()`, dot → `pressDot()` (one dot per operand) |
| + − × ÷ | 4 operator keys → `pressOperator()` |
| Equals | `pressEquals()` → `Evaluator.evaluate()`, then the result is chained |
| Clear | `AC` → `pressClear()` |
| Backspace | `DEL` → `pressDelete()`: pops a digit, then a committed number, then an operator |
| Division by zero → “Error” | `Evaluator` throws `DIVIDE_BY_ZERO`; the readout shows `ERROR` and the tape says “division by zero” |
| GridLayout or ConstraintLayout | `GridLayout`, 4 columns × 5 rows, `layout_rowWeight` / `layout_columnWeight` |
| No crashes on rapid taps | Token guards, 24-token cap, 14-character operand cap, `try/catch` around evaluation, `AC` always available |

## Evaluation rules

1. Tokenise into alternating numbers and operators (the model guarantees this).
2. **Pass one:** fold `×` and `÷` into the number on their left.
3. **Pass two:** apply `+` and `−` left to right.
4. Reject division by exactly `0` (and `-0`) with `ArithmeticException("DIVIDE BY ZERO")`.
5. Reject `NaN` / `Infinity` results as `OUT OF RANGE`.

So `2 + 3 × 4 = 14`, not 20. And `10 − 6 ÷ 3 + 1 = 9`.

## Edge cases handled

| Case | Behaviour |
|---|---|
| `8 ÷ 0 =` | Readout `ERROR`, tape “Division by zero · the tape stops here”; any digit or `AC` recovers |
| 25th token | Further operators are ignored rather than overflowing the tape |
| `.` pressed twice | Ignored for that operand |
| `.` first | Becomes `0.` |
| `12345` then `DEL` | `1,234` — one character, grouping updates |
| `DEL` after `=` | Clears the finished calculation |
| `=` with only a number | Returns that number, no error |
| `=` with a trailing operator | The trailing operator is dropped, then evaluated |
| Result longer than the panel | Autosize to 20 sp, single line |
| Very large / very small results | Scientific notation at ≥ 10¹² or < 10⁻⁹ |
| Rotation | Tape, operand, result, error state all restored |
| Physical keyboard (desktop emulator) | Digits, `+ - * / x Enter Backspace Esc` are mapped |

## Files

```
app/src/main/java/bw/oibsip/calc/
├── Evaluator.java     tokenising, two-pass evaluation, formatting, grouping (JVM-testable)
└── MainActivity.java  tape model, key handling, state, autosized readout
app/src/main/res/layout/activity_main.xml     GridLayout + ink readout
app/src/test/java/bw/oibsip/calc/EvaluatorTest.java
```

## Run and test

```bash
cd Antonio_Task3_Calculator
./gradlew testDebugUnitTest     # 9 tests
./gradlew installDebug
```

## Demo in 20 seconds

1. `2 + 3 × 4 =` → **14** (prove precedence).
2. `8 ÷ 0 =` → **ERROR** with the reason on the tape; tap `7` and it recovers.
3. `1 2 3 4 5 DEL` → `1,234`.

## Viva

**Why not just use `ScriptEngine` or `eval`?**
Neither exists on Android, and string evaluation invites malformed input. A token list makes the
invalid states unrepresentable.

**Why two passes instead of a shunting-yard stack?**
There are no brackets, so two passes give identical results with a fraction of the code. A
shunting-yard would be the right call the moment parentheses or powers arrive.

**What does `%` do here?**
Divides the current operand (or the last result) by 100 — the same as most phone calculators. It is
not “percent of the previous operand”, which is a different, ambiguous convention.

**What happens on rotation mid-expression?**
`onSaveInstanceState` stores the token list, the operand, the result and the error flag;
`restore()` puts them back before the first render.

**Where is the memory?**
There is none, by design — the app has no history and nothing is written to disk. The readme copy
says so on screen, which is cheaper than a disclaimer.
