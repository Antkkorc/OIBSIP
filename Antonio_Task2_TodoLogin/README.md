# 02 Task list with login

A task list where every task belongs to an account. Register, sign in, add tasks, tick
them off, delete them, log out. Data lives in SQLite on the phone and passwords are
stored as PBKDF2 hashes.

Part of the [OIBSIP set](../README.md). Design: [docs/DESIGN.md](../docs/DESIGN.md).
Build: [docs/BUILD_AND_RUN.md](../docs/BUILD_AND_RUN.md).

| | |
| --- | --- |
| Launcher label | Tasks |
| Package | `bw.oibsip.tasks` |
| Screens | `LoginActivity`, `RegisterActivity`, `TasksActivity` |
| Logic | `PasswordHasher`, no Android imports |
| Tests | 6 in `PasswordHasherTest` |

## What it does

The first screen is sign in. A saved session skips it and goes straight to the list.
New users create an account with a name, an email and a password of at least eight
characters, then land on the task list already signed in.

The list has a count line, three filters and one floating button. Adding a task opens a
bottom sheet with a title and an optional note. Tapping a row marks it done, which
strikes the title through and fills the check tile with the gradient. The delete button
asks once before removing anything.

Log out clears the session and returns to sign in. The next account sees only its own
tasks.

## Requirements

| Brief line | Where |
| --- | --- |
| Register and login screens | `activity_register.xml`, `activity_login.xml` |
| Passwords hashed, not plain text | `PasswordHasher.hash()`, PBKDF2-HMAC-SHA256, 120,000 rounds, salt per account |
| Storage in SQLite | `TaskDb` with a `users` table and a `tasks` table |
| Logout clears the session | `SessionStore.signOut()`, then back to login with `CLEAR_TASK` |
| Tasks per user | every query in `TaskStore` and `UserStore` filters on the owner |
| Add task with an optional note | `dialog_add_task.xml` and `TasksActivity.showAddDialog()` |
| Complete with strikethrough | `TaskAdapter.bind()` sets `STRIKE_THRU_TEXT_FLAG` |
| Delete | `dialog_confirm.xml`, then `TaskStore.delete()` |
| Empty state | centred card with an icon and one line per filter |

## Database

```
users
  _id            integer primary key
  name           text
  email          text, unique, case insensitive
  password_hash  text, Base64 of the PBKDF2 output
  salt           text, Base64, sixteen bytes, one per account
  created_at     integer

tasks
  _id           integer primary key
  user_id       integer, foreign key to users
  title         text
  notes         text, may be empty
  done          integer, 0 or 1
  created_at    integer
  completed_at  integer, null while open

index idx_tasks_owner on tasks(user_id, done)
```

Foreign keys are switched on in `onConfigure()`. SQLite ignores them by default, and
that default is how orphaned rows end up in a database.

## Files

```
app/src/main/java/bw/oibsip/tasks/
  TaskDb.java            schema, versioning, foreign keys
  UserStore.java         register, login, lookup by id
  SessionStore.java      which account is signed in
  PasswordHasher.java    PBKDF2, salt generation, constant time compare
  TaskStore.java         list, add, toggle, delete, counts
  Task.java              one row, immutable
  TaskAdapter.java       RecyclerView adapter with the glass row
  LoginActivity.java
  RegisterActivity.java
  TasksActivity.java
  GlassUi.java
  GlassApp.java
app/src/main/res/layout/
  activity_login.xml  activity_register.xml  activity_tasks.xml
  item_task.xml  dialog_add_task.xml  dialog_confirm.xml
app/src/test/java/.../PasswordHasherTest.java
```

## Details worth knowing

**Hashing.** The brief asks for basic hashing. A single SHA-256 digest would satisfy that
sentence and fail the point of it, because a leaked table can be reversed with a
dictionary in minutes. This uses PBKDF2-HMAC-SHA256 with a per account salt and 120,000
iterations, and compares with `MessageDigest.isEqual` so the comparison does not leak
timing.

**Login timing.** A wrong email returns immediately if the code only checks the password
hash for accounts that exist. That tells an attacker which emails are registered. The
login path runs the same PBKDF2 work against a dummy hash when the account is missing,
so both answers take the same time.

**Session.** Only the user id is written to `SharedPreferences`. No email, no password,
no token. Signing out clears the file.

**Ownership.** `TaskStore` never takes a task id alone. Every call takes the user id too
and puts it in the `where` clause, so a stale adapter cannot touch another account's
rows.

**Rotation.** The current filter and the list survive rotation. The add sheet closes,
which is the normal behaviour for a dialog and avoids a half typed task appearing twice.

## Run it

```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Tests:

```
./gradlew test
```

To see it with data, register one account and add two tasks, then log out and register a
second account. Neither list should show the other account's rows.
