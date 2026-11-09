# 01 · Converter — unit converter

**OIBSIP · Android App Development · 2026**
Part of the [five-instrument set](../README.md). Design language: [Gutter & Rule](../docs/STUDIO.md).
Build instructions: [BUILD_AND_RUN.md](../docs/BUILD_AND_RUN.md).

A single-screen converter across **26 units in 4 categories** (length, weight, volume, temperature).
Pick a category, type a value, choose the two units, press convert. The result lands in an ink
readout at the bottom of the screen with the conversion spelled out underneath:
`250 CM  =  98.425197 IN  ·  via metre`.

| | |
|---|---|
| Launcher label | `01 Converter` |
| Package | `bw.oibsip.converter` |
| Screen | `MainActivity` (single activity) |
| Testable core | `ConverterEngine` (no Android imports) |
| Tests | 13 (`ConverterEngineTest`) |

## Screen structure

```
OIBSIP / INSTRUMENT 01                              REV 1.0
───────────────────────────────────────────────────────────
Converter
Length, weight, volume, temperature. Twenty-six units …
  LENGTH     WEIGHT     VOLUME     TEMP            ← 4 tabs, active one carries a 2 dp rule
  8 units · metric ↔ imperial · base: metre
  01 / Value                        [ enter a number ]
  ─────────────────────────────────────────────────────
  02 / From            [ Centimetre · cm        ▾ ]
  ─────────────────────────────────────────────────────
  03 / To              [ Inch · in              ▾ ]
  ─────────────────────────────────────────────────────
                                     Swap from / to
┌─────────────────────────────────────────────────────┐
│ CONVERTED VALUE                                     │  ← the one dominant object
│                                        98.425197    │
│ ─────────────────────────────────────────────────── │
│ 250 CM  =  98.425197 IN  ·  via metre               │
└─────────────────────────────────────────────────────┘
[                      CONVERT                      ]    ← the one oxide accent
```

## Requirements → implementation

| Brief line | Implementation |
|---|---|
| Input field for the numeric value | `EditText inputValue`, `inputType="numberDecimal\|numberSigned"` |
| Source unit spinner | `Spinner fromUnit`, `ArrayAdapter<String>` with custom `item_spinner_selected` / `item_spinner_dropdown` layouts (no boxed Material look) |
| Target unit spinner | `Spinner toUnit`, same adapter instance — cheap and keeps the two lists identical |
| Convert button | `convertButton` → `convert(true)` |
| Result TextView with unit label | `resultValue` + `resultSentence` (`ConverterEngine.sentence`) |
| 3+ categories | Length 8, Weight 7, Volume 8, Temperature 3 |
| Toast on empty / non-numeric | `toast_empty`, `toast_nan` |
| Category selector resets dropdowns | 4 tab TextViews → `selectCategory()` rebuilds both adapters and resets to index 0 → 1 |

## Units and factors

Every non-temperature unit stores one number: its factor to the category base unit. Converting is
`value × from.toBase ÷ to.toBase`, which is why new units are one line each and there is no
8 × 7 table of special cases.

| Category | Base | Units |
|---|---|---|
| Length | metre | mm, cm, m, km, in (0.0254), ft (0.3048), yd (0.9144), mi (1609.344) |
| Weight | gram | mg, g, kg, t, oz (28.349523125), lb (453.59237), st (6350.29318) |
| Volume | litre | mL, L, m³, US gal (3.785411784), US qt, US pt, US fl oz, US cup |
| Temperature | °C, via an explicit Celsius round-trip | °C, °F, K |

Offsets (`°F`, `K`) cannot be expressed as a multiplier, so temperature is special-cased with
`toCelsius()` / `fromCelsius()`.

## Edge cases handled

| Case | Behaviour |
|---|---|
| Empty input | Toast “Enter a value to convert.” and focus returns to the field |
| `abc`, `1.2.3`, `1e5`, `NaN` | Rejected by a strict regex before `Double.parseDouble` — note that `parseDouble` alone accepts `1e5`, `NaN` and `Infinity` |
| Decimal comma (`1,5`) | Read as `1.5`; a comma mixed with a dot is read as a thousands separator |
| Value above 1 × 10¹⁵ | Refused: “Too large to convert reliably.” |
| `-300 °C`, `-500 °F` | Refused: “Below absolute zero. −273.15 °C is the floor.” `-459.67 °F` is still accepted |
| Results ≥ 10¹² or < 10⁻⁶ | Scientific notation, so the readout never becomes an unreadable wall of digits |
| Long results | `AppCompatTextView` autosize 42 → 16 sp, one line |
| Units changed after a conversion | The readout re-converts silently — it never shows a stale number |
| Rotation | Category, both spinner positions, input and last result restored from `onSaveInstanceState` |
| Result long-press | Copies to the clipboard (the one thing people actually want from a converter) |

## Files

```
app/src/main/java/bw/oibsip/converter/
├── ConverterEngine.java   units, factors, temperature, parsing, formatting (JVM-testable)
└── MainActivity.java      tabs, spinners, validation, readout, clipboard, state
app/src/main/res/layout/
├── activity_main.xml
├── item_spinner_selected.xml    right-aligned mono, no box
└── item_spinner_dropdown.xml
app/src/test/java/bw/oibsip/converter/ConverterEngineTest.java
```

## Run and test

```bash
cd Antonio_Task1_UnitConverter
./gradlew testDebugUnitTest     # 13 tests
./gradlew installDebug          # or press Run ▶ in Android Studio
```

## Demo in 30 seconds

1. Type `250`, From `Centimetre`, To `Inch` → **Convert** → `98.425197`.
2. Tap **Swap from / to** → `250 IN = 635 CM`, no second tap on Convert.
3. Tap **Temp**, enter `-300`, **Convert** → the absolute-zero toast.
4. Clear the field, **Convert** → “Enter a value to convert.”

## Viva

**Why one factor per unit instead of a formula per pair?**
With 8 lengths you would need 56 pair formulas; with factors you need 8 numbers and one division.
Pair formulas also drift — the classic bug is `cm → in` dividing where it should multiply.

**How does temperature fit that model?**
It does not, so it is not forced to. `°C → °F` is `c × 9/5 + 32`, an offset plus a scale. `convert()`
branches on the category, sends the value through Celsius, and returns from there.

**Why refuse below absolute zero rather than converting it?**
Because `-300 °C` is not a temperature. Converting it would produce a confident, wrong number, which
is worse than a refusal. That is a domain rule, not input validation.

**How do you avoid floating-point noise on screen?**
`format()` rounds to six decimals through `DecimalFormat` with `Locale.US` fixed, strips trailing
zeros, and switches to scientific notation at the extremes. The underlying double is never mutated.

**What would you add next?**
A searchable unit list and a “recent pairs” row, both of which need persistence the screen does not
have yet. Also `Locale`-aware decimal separators for input rather than the current lenient parse.
