package bw.oibsip.ledger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Locale;

/** Reads and writes accounts. Passwords only ever leave this class as hashes. */
public final class UserStore {

    /** The only fields the UI is allowed to see — no hash, no salt. */
    public static final class Account {
        public final long id;
        public final String name;
        public final String email;

        Account(long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    /** Used to burn the same amount of time when no account exists, so timing leaks nothing. */
    private static final String DUMMY_SALT = "Z3V0dGVyLWFuZC1ydWxl";
    private static volatile String dummyHash;

    private final LedgerDb db;

    public UserStore(Context context) {
        this.db = LedgerDb.get(context);
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }

    /**
     * @return the new account id, or -1 when the email already exists
     */
    public long register(String name, String email, String password) {
        String salt = PasswordHasher.newSalt();
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("email", normalizeEmail(email));
        values.put("password_hash", PasswordHasher.hash(password, salt));
        values.put("salt", salt);
        values.put("created_at", System.currentTimeMillis());
        return db.getWritableDatabase()
                .insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /** @return the account, or null when the email/password pair does not match */
    public Account login(String email, String password) {
        Cursor cursor = db.getReadableDatabase().query(
                "users",
                new String[]{"_id", "name", "email", "password_hash", "salt"},
                "email = ?",
                new String[]{normalizeEmail(email)},
                null, null, null, "1");
        try {
            if (!cursor.moveToFirst()) {
                burnSameTime(password);
                return null;
            }
            String hash = cursor.getString(3);
            String salt = cursor.getString(4);
            if (!PasswordHasher.verify(password, salt, hash)) {
                return null;
            }
            return new Account(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
        } finally {
            cursor.close();
        }
    }

    public Account byId(long id) {
        Cursor cursor = db.getReadableDatabase().query(
                "users",
                new String[]{"_id", "name", "email"},
                "_id = ?",
                new String[]{String.valueOf(id)},
                null, null, null, "1");
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return new Account(cursor.getLong(0), cursor.getString(1), cursor.getString(2));
        } finally {
            cursor.close();
        }
    }

    private static void burnSameTime(String password) {
        if (dummyHash == null) {
            dummyHash = PasswordHasher.hash("the-ledger-dummy-password", DUMMY_SALT);
        }
        PasswordHasher.verify(password == null ? "" : password, DUMMY_SALT, dummyHash);
    }
}
