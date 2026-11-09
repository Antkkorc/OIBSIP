# 01 Unit converter

Turns one unit into another across length, weight, volume and temperature. Four
categories, twenty-six units, one screen.

Part of the [OIBSIP set](../README.md). Design: [docs/DESIGN.md](../docs/DESIGN.md).
Build: [docs/BUILD_AND_RUN.md](../docs/BUILD_AND_RUN.md).

| | |
| --- | --- |
| Launcher label | Converter |
| Package | `bw.oibsip.converter` |
| Screens | `MainActivity` |
| Logic | `ConverterEngine`, no Android imports |
| Tests | 13 in `ConverterEngineTest` |

## What it does

The screen opens on length with centimetre to inch already selected. Four chips at the
top switch the category, and switching clears the old answer so no stale number can be
mistaken for a new one.

Two pill spinners pick the units, with a swap button between them. The value field takes
decimals, a leading minus sign and a comma as a decimal separator, which is what a lot
of people type on a phone.

The result sits in a glass card at the bottom of the content, in a readout that shrinks
when the number is long. Under it there is one plain sentence: `250 cm = 98.425197 in`.
Long press the number to copy it.

Convert is pinned to the bottom of the screen, inside thumb reach.

## Requirements

| Brief line | Where |
| --- | --- |
| Three or more categories including length and weight | `ConverterEngine.Category`, four values |
| Spinners for units | two `Spinner`s in `activity_main.xml` |
| Switching category resets the units | `selectCategory()` rebuilds both adapters and clears the result |
| Toast on empty input | `convert(true)` shows `toast_empty` and an inline line |
| Toast on non numeric input | same path with `toast_nan` |

## The units

Length has eight: millimetre, centimetre, metre, kilometre, inch, foot, yard and mile.
Weight has seven: milligram, gram, kilogram, ounce, pound, stone and tonne. Volume has
eight: millilitre, litre, cubic metre, teaspoon, tablespoon, fluid ounce, cup and US
gallon. Temperature has three: Celsius, Fahrenheit and Kelvin.

Length, weight and volume each store a factor to one base unit, the metre, the gram and
the litre. Converting is then a multiplication and a division. Temperature cannot work
that way because the scales have offsets, so it round trips through Celsius with three
small functions.

## Files

```
app/src/main/java/bw/oibsip/converter/
  ConverterEngine.java    unit table, parsing, conversion, formatting
  MainActivity.java       screen wiring, validation, saved state
  GlassUi.java            insets, press spring, content entrance
  GlassApp.java           dark mode default
app/src/main/res/
  layout/activity_main.xml     header, chips, input card, result card, dock
  layout/item_unit_selected.xml   spinner closed state
  layout/item_unit_dropdown.xml   spinner open state
  drawable/                glass shapes, borders, 16 icons
  values/                  light tokens, styles, dimensions, strings
  values-night/            dark tokens
  font/                    five Poppins weights
app/src/test/java/.../ConverterEngineTest.java
```

## Details worth knowing

**Number parsing.** A comma with no dot is read as a decimal separator, so `1,5` becomes
1.5. A comma mixed with a dot is read as a thousands separator, so `1,234.5` becomes
1234.5. Anything else throws and the screen shows a toast.

**Absolute zero.** A temperature below -273.15 °C is refused with a message that names
the limit. The check runs before the conversion, not after, so nothing overflows the
display.

**Formatting.** Results group thousands, keep up to six decimals and switch to
scientific notation below 1e-6 or above 1e12. The readout autosizes between 20sp and
40sp so a long value never clips.

**State.** Rotation keeps the category, both units, the typed value and the last result.
The chip row and the dock re-enter with the same stagger they had on first launch.

## Run it

Open this folder in Android Studio and press Run. Or from a terminal:

```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Tests:

```
./gradlew test
```
