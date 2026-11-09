# Design system

One surface treatment across five apps. The idea is a dark room with a few coloured
lights behind a pane of glass. Cards are frosted, controls are pills, and the only
saturated colour comes from a single mint to sky gradient.

Everything below is in the repo. Tokens are in `res/values/colors.xml` and
`res/values-night/colors.xml`, shapes are drawables, and the two Java helpers are
`GlassUi.java` and `GlassApp.java`.

## Dark first

The app starts in dark mode. `GlassApp` calls `AppCompatDelegate.setDefaultNightMode`
with `MODE_NIGHT_YES`, so the first launch matches the design and the system setting
cannot surprise the user with a half styled screen.

Light mode exists and uses the same token names. `values/colors.xml` holds the light
palette, `values-night/colors.xml` holds the dark one. Delete the line in `GlassApp`
if you would rather follow the phone setting.

## Colour

Dark palette, the one that ships by default:

| Token | Value | Used for |
| ----- | ----- | -------- |
| `bg_base` | `#070B12` | page behind everything |
| `bg_base_alt` | `#0B1220` | second layer, dialogs |
| `glass_fill` | `#1AFFFFFF` | card fill, 10 percent white |
| `glass_fill_strong` | `#26FFFFFF` | raised or pressed card, 15 percent |
| `glass_top` | `#33FFFFFF` | top edge of a gradient fill |
| `glass_bottom` | `#0FFFFFFF` | bottom edge of a gradient fill |
| `glass_border` | `#33FFFFFF` | the 1dp border |
| `glass_highlight` | `#59FFFFFF` | inner top light |
| `text_primary` | `#F4F8F7` | headings and values |
| `text_secondary` | `#A7B8BE` | body copy |
| `text_tertiary` | `#6F828B` | labels, captions |
| `accent_start` | `#6EF2C0` | mint, start of the gradient |
| `accent_end` | `#37B6FF` | sky, end of the gradient |
| `on_accent` | `#05110E` | text and glyphs on the gradient |
| `success` | `#63E6A0` | right answer, best lap |
| `error` | `#FF8A7A` | wrong answer, invalid input |
| `dialog_bg` | `#F20B1220` | bottom sheet background |

Light mode inverts the same names. Backgrounds become `#EDF2F1`, glass fill becomes
white at 85 percent, text becomes `#0A1418`, and the gradient darkens to `#2FBF8F`
and `#1E8FD6` so it still passes on a pale background.

There is one gradient, `angle="0"`, mint to sky. It fills primary buttons, the floating
action button, selected chips, the equals key and completion bars. Nothing else in the
app is saturated.

## Surfaces

Cards are 24dp radius with a 12dp elevation, a translucent fill and a 1dp border drawn
as a foreground overlay. The border is what sells the glass, so it sits above the
content rather than behind it.

The drawables do the work:

- `bg_backdrop.xml` is the page. It lays down `bg_base` and three soft radial glows so
  the blur has something to bend.
- `bg_pill_primary.xml` is the gradient pill, with a pressed state that lifts a lighter
  gradient inside a rim.
- `bg_pill_glass.xml` and `bg_pill_ghost.xml` are secondary and tertiary actions.
- `bg_field.xml` is the input well, 18dp radius.
- `bg_key.xml`, `bg_key_action.xml` and `bg_key_accent.xml` are the calculator keys.
- `bg_icon_tile.xml` is the rounded square behind an app icon or a leading glyph.
- `bg_chip.xml` has three states: unselected glass, pressed rim, selected gradient.

Pills are fully rounded, `999dp`. Cards use 24dp, keys 24dp, fields 18dp, icon tiles
15dp. Radii never mix inside one control.

## Type

Poppins, bundled as five weights in `res/font`. The theme sets `Poppins` as the app
font, so nothing needs a font attribute unless it wants a heavier weight.

| Style | Size | Weight | Where |
| ----- | ---- | ------ | ----- |
| `Text.Display` | 26sp | SemiBold | screen titles |
| `Text.Title` | 19sp | SemiBold | card headings, questions |
| `Text.Value` | 15sp | Medium | row titles and numbers |
| `Text.Body` | 14sp | Regular | paragraphs, notes |
| `Text.Subtitle` | 13sp | Regular | the line under a title |
| `Text.Option` | 13.5sp | Regular | quiz answers |
| `Text.Label` | 11sp | Medium, 0.04em | field labels and captions |
| `Text.Readout` | 42sp | SemiBold, tabular | the big number |

The readout uses tabular figures and autosizes down to 20sp, so a long conversion or a
large calculator result shrinks instead of clipping. Labels are small and tracked out.
They never carry a value, only a name for the field below them.

## Motion

Three rules:

- A press dips to 0.97 over 90ms, then releases to 1.0 over 220ms with an overshoot of
  1.6. That is the spring.
- Content fades in and rises 10dp at once, 220ms, staggered by 60 to 70ms per block.
- A changed value fades from 35 percent to full over 180ms. Numbers never swap instantly.

`GlassUi.press(view)` installs the spring on a view without swallowing the click, which
is why it can be applied to buttons, chips, list rows and quiz options alike.
`GlassUi.enter(view, delay)` handles the entrance, and `GlassUi.refresh(view)` the value
change. Call `GlassUi.install(activity, layout)` before touching any view: it sets edge
to edge drawing and pads the root for the status bar, navigation bar and cutout.

## Layout

Mobile first, one hand. Screens use 20dp side padding, content is a column of rounded
blocks, and the main action sits at the bottom where a thumb reaches it.

- The converter keeps Convert in a bottom dock.
- The task list puts a 58dp floating action button at the bottom right.
- The calculator fills the lower half with a five row keypad.
- The quiz keeps Next under the options.
- The stopwatch puts four controls in two rows at the bottom.

Lists scroll, headers do not. Empty states are centred with an icon tile and one line
of copy, because an empty screen with no explanation looks broken.

## Dialogs

`GlassUi.dialog(activity, layout)` builds a bottom sheet: transparent window, dim at
55 percent, background blur where the platform supports it, 12dp of air around the
sheet so the rounded corners are visible.

The add task sheet has a title field, an optional note and two pills. The delete sheet
repeats the task name and asks once. Both use the same spring on their buttons.

## Icons

Every glyph is a vector drawable, 24dp, drawn on a 1.8dp round stroke in the accent
colour. They are named for what they are, not for where they go: `ic_converter`,
`ic_tasks`, `ic_calculator`, `ic_quiz`, `ic_chrono`, plus the small set used inside
cards. Launcher icons are adaptive: a background colour plus a foreground mark.

## What not to do

Do not add a second accent colour. One gradient is the whole system.

Do not put a shadow under a pill. The pressed rim does that job.

Do not use an em dash or a slash to separate labels. Plain words and a full stop.

Do not use a font weight that is not in `res/font`. Falling back to a system font is
visible at 15sp.
