package bw.oibsip.tasks;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Remembers which account is signed in. Clearing it is what "Log out" does — no
 * plain-text credentials are ever written to disk.
 */
public final class SessionStore {

    private static final String FILE = "tasks_session";
    private static final String KEY_USER = "user_id";
    private static final long NOBODY = -1L;

    private final SharedPreferences prefs;

    public SessionStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public void signIn(long userId) {
        prefs.edit().putLong(KEY_USER, userId).apply();
    }

    public long userId() {
        return prefs.getLong(KEY_USER, NOBODY);
    }

    public boolean hasSession() {
        return userId() > 0;
    }

    public void signOut() {
        prefs.edit().clear().apply();
    }
}
