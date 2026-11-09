# Gutter & Rule — the studio behind the five instruments

> Five Android apps, one workshop. Every screen is a measuring instrument: a numbered
> legend in the left gutter, values right-aligned onto hairlines, one readout you cannot
> miss. No cards. No shadows. No purple.

---

## (a) Design thesis — four lines

1. **User** — an engineering student on a mid-range Android phone, in daylight, one hand, doing lab or timetable work between other tasks.
2. **Context** — the app is opened for twenty seconds, put down, and reopened later; the surrounding noise is a lecture hall, not a mood board.
3. **The one job** — read one number, trust it, act on it. (Convert, count, calculate, answer, time.)
4. **Mood words** — *calibrated*, *deadpan*, *engraved*.

**References, and exactly what is stolen from each**

| Reference | What we take | What we refuse |
|---|---|---|
| **Braun ET66 calculator** (Dieter Rams, 1987) | The legend-in-the-gutter layout: every control is labelled in small type beside it, one accent colour, a single surface per function. Colour is information, never decoration. | The retro nostalgia. We are not making a 1987 prop; the type is contemporary and the grid is 2026-tight. |
| **Swiss Federal Railways (SBB) timetable** | Right-aligned tabular numerals sitting *on* a hairline, dense information with zero boxes, hierarchy made by size and weight rather than by containers. | The blue-on-white corporate palette and the timetable's uniform row rhythm — we vary the rhythm deliberately. |

Also acknowledged, not copied: the printed lab notebook (numbered steps, correction marks) and the architect's drawing sheet (title block in the corner, revision stamp).

---

## (b) The structural point of view

**Every screen is one instrument column: legend gutter → ruled rows → one dominant readout anchored at the bottom edge. Nothing is centred. Nothing floats. Nothing is in a card.**

Concretely:

- A fixed **76dp legend gutter** on the left holds numbered micro-labels (`01 / VALUE`, `02 / FROM`). Content starts at that gutter and runs to the right edge.
- Rows are separated by **1dp hairlines in ink**, not by gaps and elevation. There is no `CardView` anywhere in the repository.
- The **readout** is the largest object on screen (44–84sp) and sits low, near the thumb, either on paper or inverted on an ink block.
- The **title block** (masthead) lives in the top-left corner like a drawing sheet: `OIBSIP / INSTRUMENT 01` + `REV 1.0`.

This would be *wrong* for a social app or a shopping feed, because it is label-forward, dense, and refuses to be browsed: you cannot scroll past an instrument, you have to read it. It also fails for marketing: there is nowhere to put a hero.

---

## (c) Three signature details, repeated in all five apps

1. **The engraved legend** — 10sp IBM Plex Mono, all caps, +0.16em tracking, `NN / NAME` numbering, ink at 55%. It labels every input, every section, every screen. No icon is ever used where a word will do.
2. **The hairline, never the card** — 1dp rules (`#D8D2C2` on paper, `#3A382E` on ink) close every row and every section; 2dp is reserved for one thing only: the *active* state (selected category tab, correct/wrong answer rule, progress bar).
3. **One accent, one action** — oxide red `#C8452B` appears exactly once per screen, on the primary action or on the live state that matters. Everything else is paper, ink, and grey. The result readout is *ink*, not accent: scale is the emphasis, not colour.

Supporting habits: tabular numerals for anything that changes; text buttons (`DEL`, `SWAP`, `LOG OUT`) instead of glyph icons; motion only when state changes (a 120ms readout fade on a new result, a 160ms alpha settle when a task is struck through) — **no entrance animations, no springs, no confetti.**

---

## (d) Type pairing and accent

| Role | Face | Size / tracking | Used for |
|---|---|---|---|
| Display | **Instrument Serif** (Regular + Italic) | 30–38sp, leading 0.95 | Screen titles, the one editorial line per screen, the score, the big empty-state line |
| Interface | **IBM Plex Mono** (Regular / Medium / SemiBold) | 10sp legends (+0.16em) · 11–12sp captions · 14–16sp values · 44–84sp readouts (−0.02em) | Legends, buttons, fields, every numeral in the app |

No Roboto body copy, no Inter, no display/body of the same family. Serif carries voice; mono carries data.

**Accent:** oxide red `#C8452B` (paper `#F3F0E9`, ink `#17160F`, rule `#D8D2C2`, moss `#2E6B4E` is a *signal*, not an accent — it only ever means "correct").

Why oxide: it is the colour of rust, red oxide primer and surveyor's marks. It reads as a mark made on paper, not as a brand gradient, and it is the one warm colour that survives daylight on a cheap LCD.

---

## Copy rules

- Deadpan, specific, no exclamation marks, no "Boost your productivity".
- Numbers are stated with their units and their year: `26 UNITS · SI ↔ IMPERIAL · 2026 EDITION`.
- Empty states are written like a lab note, not a greeting: *"The ledger is clean. Add the first entry — it stays on this phone."*
- Errors say what happened and what the limit is: *"Below absolute zero. −273.15 °C is the floor."*
- Every app ends with a colophon in the corner (`SI BASE UNITS · 1 IN = 2.54 CM EXACT`), like a printed sheet.

## Motion policy

Only state change, only short, only once:

| Event | Motion |
|---|---|
| New conversion result | 120ms alpha crossfade on the readout |
| Task marked done | 160ms alpha on the row, strike drawn instantly |
| Quiz answer locked | 140ms alpha on the option rules |
| Stopwatch lap recorded | new row appears at list top, 120ms alpha |
| Everything else | instant |

No screen-entry transitions, no scaling buttons, no ripples beyond AppCompat's own press shade (which we keep because touch needs it).

## Accessibility notes

- Minimum touch target 48dp on every control, including the small text buttons.
- Legends are 10sp but never carry meaning alone — the value is always next to them.
- Contrast: ink on paper ≈ 14:1; muted `#6F6C60` on paper ≈ 4.9:1; on-ink muted `#A8A493` ≈ 4.8:1.
- All readouts are `singleLine`-safe and use autosize or wrapping so long values never clip.

## Token table (identical in all five projects)

| Token | Value | Use |
|---|---|---|
| `paper` | `#F3F0E9` | App background |
| `paper_shade` | `#E7E2D4` | Pressed rows, input focus background |
| `ink` | `#17160F` | Text, rules at full strength, readout blocks |
| `ink_muted` | `#6F6C60` | Legends, captions |
| `ink_faint` | `#A9A496` | Hints, disabled text |
| `rule` | `#D8D2C2` | Hairlines on paper |
| `oxide` | `#C8452B` | The single accent |
| `moss` | `#2E6B4E` | Correct answer only |
| `paper_on_ink` | `#F3F0E9` / `#A8A493` | Text on inverted readouts |
| Screen padding | 20dp horizontal, 18dp vertical | All screens |
| Gutter | 76dp | Legend column |
| Row height | 52dp min, 56dp for primary actions | All rows |
| Corner radius | **0dp everywhere** | Rectangles only |
| Elevation | **0 everywhere** | No shadows |

---

## Self-audit: what still looked generated, and what we changed

Written after the first pass, because a design is only finished when you can name its
weaknesses.

| Still looked machine-made | What we changed |
|---|---|
| Five apps that share a palette but not a *structure* | Gave every screen the same spine: title block, legend gutter, ruled rows, readout at the bottom edge. The converter, calculator and ledger now look like three instruments from the same bench, not three tutorials. |
| Centred readouts with a big rounded card behind them | Killed the card. The readout is an ink rectangle that runs edge to edge inside the padding, with the value pushed to the right margin, and the label left in the gutter. Asymmetry does the work that a drop shadow was doing. |
| Material's default dialog (white, rounded, coloured text buttons) | Custom `Dialog` on paper with a ruled legend, a serif title and two rectangular actions. The delete confirmation is a page, not a popup. |
| Icons borrowed from a set (`ic_add`, `ic_delete`) | Replaced with words: `ADD ENTRY`, `DEL`, `SWAP FROM / TO`, `LOG OUT`. One vector remains — the 9 × 6 dp spinner caret — because a written “choose” label at that size is noise. |
| Colour used to say three things at once | One accent per screen. In the quiz, moss and oxide are *signals* with a 3 dp rule and a written note, so the meaning survives greyscale. |
| Emoji, gradients, “Start your journey” copy | Replaced with deadpan instrument copy: “The ledger is clean.”, “Division by zero · the tape stops here”, “Hard aground. Start with the definitions, then come back.” |
| Uniform 16 dp padding everywhere | Rows are 52 dp with a 76 dp gutter and 20 dp page margins; the readout block drops to 18/16 dp to feel tighter than the list. Uneven by intent. |
| Entrance animations on every screen | Removed. Only 120–160 ms alpha, only on state change. The quiz's green/red rule appears instantly, then the block fades in — the rule is the fact, the fade is the acknowledgement. |
| An English-only assumption in the demo | Number parsing accepts a decimal comma; the demo copy still says “enter a number”, which is the honest limit of a 10-day build. |

**Still not perfect, and known:** the 60 sp open-task count can crowd a small 320 dp screen
with a three-digit count; the collection widget lazily builds items (fine for 500 rows, not for
5,000); dark mode is deliberately absent — paper is the product, and a half-hearted dark theme
would be worse than none.
