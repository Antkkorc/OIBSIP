# Submission pack — what to send, when, and exactly what to say

> ## Read this first: the brief contains two different deadlines
> - “**Final Submission Deadline: 15 November 2026**” (Important Deadline block)
> - “**Submission Deadline: 15 December 2026**” (Internship Schedule table)
>
> **Treat 15 November 2026 as the real deadline.** If everything is ready in November, the
> December date never matters. If you need the extra month, ask in the Telegram group
> (<https://t.me/oasisinfobyte>) and get it in writing before you rely on it.
>
> Commencement date on the brief: 5 November 2026 — so the window between start and the earlier
> deadline is ten days. Ship early.

---

## 1. Before you submit (tick every line)

- [ ] The repository is **public** and named **OIBSIP**: <https://github.com/Antkkorc/OIBSIP>
- [ ] Root `README.md` renders on GitHub with the task table and the requirement map
- [ ] Each of the five task folders has its own `README.md`
- [ ] The five projects open and run in Android Studio on your machine (not just mine)
- [ ] `./gradlew testDebugUnitTest` passes in each project
- [ ] `preview/index.html` opens and every one of the five panels responds to taps
- [ ] Folder names match `YourName_TaskNumber…` (rename if your evaluator wants the bare form)
- [ ] You can explain every file in your own words (see “Viva” below — plagiarism is instant termination)
- [ ] A demo video exists, 3–5 minutes, screen recording with your voice
- [ ] Your LinkedIn profile lists the internship, and one post per milestone

---

## 2. The submission form

The brief says **all tasks must be submitted through the official submission form only**. When it
opens, have these ready:

| Field | What to put |
|---|---|
| Full name | as registered with Oasis Infobyte |
| Internship domain | Android App Development |
| GitHub repository | `https://github.com/Antkkorc/OIBSIP` |
| Live demo | `https://github.com/Antkkorc/OIBSIP/blob/main/preview/index.html` — or host the `preview/` folder (GitHub Pages: Settings → Pages → Deploy from branch `main` → `/preview`) and paste that URL |
| Tasks completed | 01 Converter, 02 Ledger, 03 Calculator, 04 Passage, 05 Chronograph |
| Documentation | root `README.md` + `docs/STUDIO.md` + per-task READMEs |
| Video link | unlisted YouTube / Drive link, “anyone with the link can view” |

Do **not** send repeated emails about the same issue; the Telegram group is the faster channel.

---

## 3. File naming

The brief: *“File naming format: YourName_TaskNumber.”*

The repository uses `Antonio_Task1_UnitConverter` … `Antonio_Task5_Stopwatch` — the required
`Antonio_TaskN` prefix plus a descriptor, which is what most evaluators expect because it tells
them what they are opening.

If your evaluator insists on the literal bare form, rename the folders and nothing else:

```bash
git mv Antonio_Task1_UnitConverter Antonio_Task1
git mv Antonio_Task2_TodoLogin      Antonio_Task2
git mv Antonio_Task3_Calculator     Antonio_Task3
git mv Antonio_Task4_Quiz           Antonio_Task4
git mv Antonio_Task5_Stopwatch      Antonio_Task5
```

Nothing in the Gradle files or the Java depends on the folder name (the Gradle project names are
set in each `settings.gradle`). Update the table in `README.md` afterwards.

---

## 4. Demo video script (4 minutes)

Record at 1080p, phone in portrait, system bar visible. Talk while you tap — silence reads as
"I copied this".

**0:00 – 0:25 · Opening**
> "This is my OIBSIP Android submission: five apps built as one instrument set — converter, ledger,
> calculator, quiz, chronograph. Java and XML throughout, SQLite for the ledger, no third-party UI
> library. The design language is called Gutter & Rule: a numbered legend in a left gutter, ruled
> rows, one readout anchored at the bottom."

**0:25 – 1:10 · Converter**
1. Type `250`, From `Centimetre`, To `Inch`, press **Convert** → `98.425197`.
2. Tap **SWAP** → the readout re-converts without pressing Convert again.
3. Switch to **Temp**, enter `-300` → toast: "Below absolute zero. −273.15 °C is the floor."
4. Clear the field, press Convert → "Enter a value to convert."
5. Type `123456789` and convert → grouping / scientific notation keeps the readout readable.

**1:10 – 2:10 · Ledger (the biggest one)**
1. Register: name, email, an 8-character password → lands in the empty ledger ("The ledger is
   clean.").
2. **Add entry** with a title and notes → appears at the top, open count `01`.
3. Tap the row → strikethrough, marker disappears, count drops to `00`.
4. Filter **Done** → the finished entry; filter **Open** → the empty state for that filter.
5. Add a second entry, then **Del** → confirmation dialog → delete is permanent.
6. **Log out** → back to the sign-in screen. Sign in again → tasks are still there.
7. Say the security line: "Passwords are PBKDF2-HMAC-SHA256, 120,000 iterations, a fresh 16-byte
   salt per account — the database stores a hash, never the password. Session state is in Shared
   Preferences and cleared on logout. Every task query is scoped by user id, so two accounts on the
   same phone cannot see each other's lists."

**2:10 – 2:40 · Calculator**
1. `2 + 3 × 4 =` → `14` (precedence, not left-to-right).
2. `8 ÷ 0 =` → `ERROR` on the tape with "division by zero".
3. `12345` then **Del** → `1,234`.
4. Hammer ten keys fast → no crash, no double-tap.

**2:40 – 3:20 · Passage (quiz)**
1. **Start the quiz** → "Question 01 / 12".
2. Answer one wrong on purpose → green on the correct option, oxide on yours, note appears.
3. **Next** → progress rule advances.
4. Finish → scorecard: score, correct, incorrect, percentage, verdict.
5. **Restart quiz** → a different first question (shuffled). Mention: "The deck is dealt from a
   saved seed, so rotating the phone cannot reshuffle the exam mid-run."

**3:20 – 4:00 · Chronograph**
1. **Start** → Start greys out, Stop and Lap activate, the fraction ticks in hundredths.
2. **Lap** twice → splits with BEST/WORST tags.
3. **Stop** → freezes; **Start** becomes **Resume**.
4. Lock the phone for ten seconds, unlock → the clock kept running. "Elapsed time comes from
   `SystemClock.elapsedRealtime()` and is persisted, so screen lock, backgrounding, or process death
   cannot lose time."
5. **Reset** → `00:00.00`, laps cleared.

**Closing line:** "Design thesis and tokens are in `docs/STUDIO.md`; all conversion, parsing,
hashing, deck and clock logic is unit-tested — 54 tests, no emulator needed."

---

## 5. LinkedIn

The brief asks for a professional presence and milestones. One post per milestone beats one
all-at-once post at the end.

**Post 1 — day you start (screenshot: the shelf/preview page)**
> Starting a ten-day Android build sprint with Oasis Infobyte: five apps — unit converter, a
> to-do list with real login, a calculator, a quiz, and a stopwatch — built as one set of
> instruments instead of five unrelated demos. Java, XML, SQLite. Repo: github.com/Antkkorc/OIBSIP
> #OIBSIP #Android #Java

**Post 2 — mid-build (screenshot: the ledger with tasks)**
> Ledger is done: SQLite with a foreign key from tasks to accounts, PBKDF2-HMAC-SHA256 password
> hashing with a per-account salt, session cleared on logout, and an empty state that actually
> says something. Next: calculator precedence and a quiz that cannot reshuffle itself on rotation.
> #OIBSIP #AndroidDevelopment

**Post 3 — submission (screenshot: results screen of the quiz, or the five launcher icons)**
> Submitted my OIBSIP Android track: five apps, one design language I called Gutter & Rule —
> numbered legends, hairlines instead of cards, one oxide accent, zero corner radius. 54 unit tests
> on the logic that matters (conversion, parsing, hashing, scoring, timing). Thanks to @Oasis
> Infobyte. Repo: github.com/Antkkorc/OIBSIP
> #OIBSIP #Android #Java #OasisInfobyte

Rules that keep it professional: no emojis in the first line, no "excited to share", tag the
organisation once, attach one image, no hashtag walls.

---

## 6. Viva: questions they will actually ask

Each task README has its own Q&A. These five come up every time:

1. **"Why not store the password as MD5?"** — A fast digest is a checksum, not a password hash. One
   leaked table reverses common passwords in seconds. PBKDF2-HMAC-SHA256 with 120,000 iterations and
   a per-account salt makes each guess cost real work, and the salt stops precomputed tables.
2. **"Why does the calculator give 14 for 2+3×4?"** — Because the tape is a token list evaluated in
   two passes: × and ÷ fold first, then + and −. `Evaluator.evaluate()` and its unit test.
3. **"What happens if I rotate the phone mid-quiz?"** — `onSaveInstanceState` keeps the seed, the
   index and the answers; the deck is re-dealt from the same seed, so the order is identical.
4. **"How does the stopwatch survive a screen lock?"** — It never counts ticks. Elapsed time is
   `accumulated + (elapsedRealtime() - startedAt)`; the Handler only repaints, and the three numbers
   are persisted in Shared Preferences on every state change.
5. **"Where is the empty state?"** — Task 2, and it changes with the filter: "The ledger is clean.",
   "Nothing open.", "Nothing finished yet."

---

## 7. After you press submit

- Keep the repository public until the certificate is issued.
- Don't force-push or rename the repo after submitting; the evaluator's link must keep working.
- If the evaluator asks for an APK, build `assembleDebug` and attach it — never commit a keystore.
