# 02 · Ledger — to-do list with login

**OIBSIP · Android App Development · 2026**
Part of the [five-instrument set](../README.md). Design language: [Gutter & Rule](../docs/STUDIO.md).
Build instructions: [BUILD_AND_RUN.md](../docs/BUILD_AND_RUN.md).

A task manager where the tasks belong to an account. Register, sign in, keep a list, strike entries
through, delete them for good, log out. Everything lives in a local **SQLite** database; passwords
are stored as **PBKDF2-HMAC-SHA256** hashes, never as text.

| | |
|---|---|
| Launcher label | `02 Ledger` |
| Package | `bw.oibsip.ledger` |
| Screens | `LoginActivity` · `RegisterActivity` · `TasksActivity` |
| Testable core | `PasswordHasher` (no Android imports) |
| Tests | 6 (`PasswordHasherTest`) |

## Screens

```
SIGN IN                                OPEN ACCOUNT                 LEDGER
The Ledger                             Open an account              The Ledger        Log out
…                                      …                            ─────────────────────────
01 / Email    [ you@example.com ]      01 / Name     [ … ]           04 ··· OPEN        Antonio
02 / Password [ •••••••••••••• ]       02 / Email    [ … ]              02 done
error line (oxide, only when needed)   03 / Password [ … ]           ALL   OPEN   DONE
[            SIGN IN            ]      [        CREATE ACCOUNT   ]   ─────────────────────────
No account? Register                   Already registered? Sign in  [ ] Task title          DEL
Use the demo account                                                 [x] Struck-through task
PBKDF2-SHA256 · 120,000 iterations                                   [        ADD ENTRY       ]
```

## Database

```sql
CREATE TABLE users (
  _id           INTEGER PRIMARY KEY AUTOINCREMENT,
  name          TEXT NOT NULL,
  email         TEXT NOT NULL COLLATE NOCASE UNIQUE,
  password_hash TEXT NOT NULL,          -- Base64 PBKDF2 output
  salt          TEXT NOT NULL,          -- Base64, unique per account
  created_at    INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE tasks (
  _id          INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id      INTEGER NOT NULL REFERENCES users(_id) ON DELETE CASCADE,
  title        TEXT NOT NULL,
  notes        TEXT NOT NULL DEFAULT '',
  done         INTEGER NOT NULL DEFAULT 0,
  created_at   INTEGER NOT NULL DEFAULT 0,
  completed_at INTEGER
);
CREATE INDEX idx_tasks_owner ON tasks(user_id, done);
```

Foreign keys are enabled explicitly in `onConfigure()` — SQLite ignores them by default, which is
how orphaned rows quietly pile up in student projects.

## Requirements → implementation

| Brief line | Implementation |
|---|---|
| Login screen with email + password | `activity_login.xml` → `LoginActivity.submit()` |
| Sign-up with name, email, password | `activity_register.xml` → `RegisterActivity.submit()` |
| Passwords hashed, not plaintext | `PasswordHasher.hash()`: PBKDF2-HMAC-SHA256, 120,000 iterations, 256-bit key, 16-byte salt per account, `MessageDigest.isEqual` for comparison |
| Logout clears the session | `SessionStore.signOut()` then `TasksActivity.goToLogin()` with `CLEAR_TASK` |
| Task list for the logged-in user | `TaskStore.list(userId, filter)` — the `user_id = ?` predicate is in every query |
| Add task dialog with notes | `dialog_task.xml` + `showAddDialog()` |
| Mark complete (strikethrough) | `TaskAdapter.bind()` sets `Paint.STRIKE_THRU_TEXT_FLAG`; the 3 dp open marker disappears |
| Delete permanently | `dialog_confirm.xml` → `TaskStore.delete(taskId, userId)` |
| Tasks linked per account | FK `tasks.user_id → users._id` with `ON DELETE CASCADE` |
| Empty state | `emptyState` block, copy changes per filter |

## Details worth defending

- **Why PBKDF2 and not MD5/SHA?** The brief says “basic hashing”; a bare digest is a checksum. 120k
  PBKDF2 iterations cost roughly 100–250 ms per attempt on a mid-range phone — invisible to a user
  signing in once, expensive to anyone trying a wordlist. The salt makes precomputed tables useless
  and stops two users with the same password from sharing a hash.
- **Same error for unknown email and wrong password.** The message never confirms which half was
  wrong, and `UserStore.burnSameTime()` performs a dummy PBKDF2 derivation when no account exists, so
  the response time does not leak account existence either.
- **Hashing never blocks the UI.** Login and registration run on a single-thread `ExecutorService`
  with the button disabled; results come back through `runOnUiThread`.
- **No plaintext anywhere.** `SessionStore` keeps only a user id in `SharedPreferences` — no email,
  no password, no token.
- **`ListAdapter` + `DiffUtil`** so toggling one row animates that row, not the whole list; add and
  remove are instant by policy (no entrance animations), only changes fade.

## Edge cases handled

| Case | Behaviour |
|---|---|
| Empty ledger | “The ledger is clean. Add the first entry. It stays on this phone.” |
| Empty filter | Different copy for Open and Done — never a blank panel |
| 120-character title | `maxLength=120`, two lines with ellipsis, row height stays stable |
| Long notes | Two lines, ellipsis; hidden entirely when a task has no notes |
| Pasted whitespace | Collapsed with `replaceAll("\\s+", " ")` before insert |
| Duplicate email | `UNIQUE` + `CONFLICT_IGNORE` → “An account already exists for that email.” |
| Short password | “Use at least 8 characters. Longer is better than clever.” |
| Expired session | A stored id whose account no longer exists signs out with a message |
| 100+ tasks | RecyclerView; the open count grows to three digits without breaking the serif block |
| Rotation / process death | Data is in SQLite; the session is in SharedPreferences; nothing is held in memory |

## Files

```
app/src/main/java/bw/oibsip/ledger/
├── LedgerDb.java          SQLiteOpenHelper, schema, foreign keys
├── UserStore.java         register / login / byId, email normalisation, timing equalisation
├── PasswordHasher.java    PBKDF2-HMAC-SHA256, salt, constant-time verify
├── TaskStore.java         user-scoped queries, add, toggle, delete, counts
├── SessionStore.java      SharedPreferences session
├── Task.java              immutable row model
├── TaskAdapter.java       ListAdapter + DiffUtil + strikethrough
├── LoginActivity.java
├── RegisterActivity.java
└── TasksActivity.java
app/src/test/java/bw/oibsip/ledger/PasswordHasherTest.java
```

## Run and test

```bash
cd Antonio_Task2_TodoLogin
./gradlew testDebugUnitTest      # 6 hashing tests
./gradlew installDebug
```

To inspect the database in a debug build: **View → Tool Windows → App Inspection → Database
Inspector** while the app is running. You should see `password_hash` and `salt` columns full of
Base64, and no password anywhere.

## Demo in 60 seconds

1. **Register** with any name, an email and 8+ characters → you land in an empty ledger.
2. **Add entry** → title + notes → it appears with an ink marker and `01` open.
3. Tap the row → strikethrough, marker gone, count `00`, entry moves under the finished section.
4. **Del** on a row → confirmation → gone permanently.
5. **Log out** → sign-in screen. Sign in again → the entries are still there.
6. Register a **second** account → its ledger is empty. That is the `user_id` scope working.

## Viva

**What happens if two people use the same phone?**
Two rows in `users`, and every task row carries a `user_id`. `TaskStore` always filters by the
signed-in id, and `delete`/`setDone` include `AND user_id = ?` so a task cannot be edited by
guessing an id.

**Why not store the salt secretly?**
A salt is not a secret, it is uniqueness. It stops precomputation and identical hashes. The secret
is the password; the iteration count buys the time.

**How would you rate-limit login attempts?**
Not implemented. Next step: a failed-attempt counter in `SharedPreferences` with a backoff, or
Android's `BiometricPrompt` for device-bound unlock.

**Why hand-rolled dialogs instead of `AlertDialog`?**
`AlertDialog` brings Material typography, rounded white sheets and coloured buttons — three things
this design language bans. A plain `Dialog` with a custom view keeps the paper surface and the ruled
rows.

**What is the biggest weakness?**
The database file is not encrypted. Anyone with a rooted device or a backup can read the task text
(not the passwords). SQLCipher or `EncryptedFile` is the honest next step.
