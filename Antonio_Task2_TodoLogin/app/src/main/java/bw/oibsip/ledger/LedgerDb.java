package bw.oibsip.ledger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * One database file for the whole app: accounts and their tasks.
 *
 * Foreign keys are enabled explicitly because SQLite ignores them by default, and the
 * task table is declared with ON DELETE CASCADE so a removed account cannot leave
 * orphaned entries behind.
 */
public class LedgerDb extends SQLiteOpenHelper {

    private static final String DB_NAME = "ledger.db";
    private static final int DB_VERSION = 1;

    private static volatile LedgerDb instance;

    public static LedgerDb get(Context context) {
        if (instance == null) {
            synchronized (LedgerDb.class) {
                if (instance == null) {
                    instance = new LedgerDb(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private LedgerDb(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "email TEXT NOT NULL COLLATE NOCASE UNIQUE,"
                + "password_hash TEXT NOT NULL,"
                + "salt TEXT NOT NULL,"
                + "created_at INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE tasks ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL REFERENCES users(_id) ON DELETE CASCADE,"
                + "title TEXT NOT NULL,"
                + "notes TEXT NOT NULL DEFAULT '',"
                + "done INTEGER NOT NULL DEFAULT 0,"
                + "created_at INTEGER NOT NULL DEFAULT 0,"
                + "completed_at INTEGER)");

        db.execSQL("CREATE INDEX idx_tasks_owner ON tasks(user_id, done)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the only shipped schema. A real product would migrate here.
    }
}
