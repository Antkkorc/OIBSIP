# Submission

Everything that has to be in place before the form is filled in, plus what to say when
someone asks how it works.

## Dates

The brief carries two dates. The important deadline block says 15 November 2026. The
schedule table says 15 December 2026. Treat 15 November as the real one and the second
date never matters.

The internship starts on 5 November 2026, which leaves ten days. Work backwards from
there and record the demo early, because a video that fails to upload on the last night
is the usual way these submissions go wrong.

## Checklist

- [ ] Repository is public and named exactly `OIBSIP`
- [ ] Root README renders, with the task table and the build steps
- [ ] Each of the five folders has its own README
- [ ] All five projects open and run in Android Studio on a machine that is not mine
- [ ] `./gradlew test` passes in each of the five folders
- [ ] `preview/index.html` opens and all five phones respond to taps
- [ ] Folder names follow `Antonio_TaskN_Name`, matching the brief's naming rule
- [ ] Demo video recorded, three to five minutes, with your voice
- [ ] Form submitted through the official link, not by email
- [ ] LinkedIn updated with the internship and one post per task

## The form

The brief says every task goes through the official submission form. Have these ready
when it opens.

| Field | Value |
| ----- | ----- |
| Full name | as registered with Oasis Infobyte |
| Domain | Android App Development |
| GitHub repository | `https://github.com/Antkkorc/OIBSIP` |
| Live demo | GitHub Pages for the `preview` folder, or a link to the file on GitHub |
| Tasks completed | 01 Converter, 02 Task list, 03 Calculator, 04 Quiz, 05 Stopwatch |
| Documentation | root README plus `docs/DESIGN.md` and the five project READMEs |
| Video | unlisted YouTube or Drive link with view access for anyone holding it |

For GitHub Pages: open Settings, then Pages, choose Deploy from a branch, pick `main`
and the `/preview` folder. The URL will look like
`https://antkkorc.github.io/OIBSIP/`. A static page needs no build step, so it works on
the free tier.

## File naming

The brief asks for `YourName_TaskNumber`. The folders are `Antonio_Task1_UnitConverter`
through `Antonio_Task5_Stopwatch`, which keeps the required prefix and adds a word so an
examiner knows what they are opening without clicking. If an evaluator insists on the
bare form, rename the folders and nothing else. Nothing in the code refers to a folder
name except the Gradle settings file, which names the module, not the folder.

## Demo script

Aim for three minutes. Record the emulator with the phone frame hidden or the device
chrome visible, it does not matter as long as the screen is readable. Speak over it.

**0:00 to 0:20, the repository.** Show the README on GitHub, the five folders, and the
preview page. Say what the apps are and that the design is shared.

**0:20 to 0:50, converter.** Convert 250 centimetres to inches, switch to weight, then
to temperature, then type -300 to show the absolute zero guard. Switch units with the
swap button.

**0:50 to 1:35, task list.** Register an account, add a task with a note, tick it off,
filter to Done, delete it, then log out and sign in again to show the tasks returned.
Mention PBKDF2 and per user rows while you do it.

**1:35 to 2:00, calculator.** 12 + 7 x 3 to show precedence, then 8 / 0 to show the
error state, then press a digit to show the recovery.

**2:00 to 2:30, quiz.** Answer two questions, one right and one wrong, so both colours
appear. Jump to the results screen and press Play again.

**2:30 to 3:00, stopwatch.** Start, take two laps, pause, resume, then reset. Open the
notification shade or switch apps for a few seconds and come back to show the clock
kept counting.

Close on the preview page and the test report.

## Questions an evaluator may ask

**Why PBKDF2 instead of MD5?** A fast digest is cheap to brute force once the table
leaks. PBKDF2 with a salt per account and 120,000 rounds makes each guess expensive.
The app ships with it because the platform provides it, so it costs no dependency.

**How are tasks kept apart?** Every query in `TaskStore` includes `user_id = ?`, and
the user id comes from the session, never from the UI. Signing out clears the stored id.

**How does the stopwatch survive a rotation?** Time is derived from
`SystemClock.elapsedRealtime()` and saved with `SharedPreferences` on every change. The
handler only repaints.

**Why a token list in the calculator?** A string can end up as `12 + + 3` after a fast
double tap. A list cannot, because a second operator replaces the first. There is no
parse step to crash.

**Why no Compose?** The brief is Java and XML, and five apps sharing one set of
drawables is easier to keep consistent with XML.

**Is the design copied?** No. The token set, layouts and drawables are in this repo, and
`docs/DESIGN.md` explains each choice.

## Plagiarism

The brief is direct about it: copied work means termination, and marks are withheld.
Be ready to explain any file you submit, in your own words, without the code in front of
you. The safest test is to delete a class, rewrite it from memory, and compare.

## After submitting

Keep the repository public until the certificate is issued. An evaluator who opens a
404 cannot mark the work. If you keep working after submission, add commits rather than
force pushing over the submitted state.
