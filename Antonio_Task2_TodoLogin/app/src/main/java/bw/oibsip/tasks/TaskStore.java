package bw.oibsip.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * All task queries are scoped by user_id. That is the only thing standing between two
 * accounts on the same phone, so it is enforced here rather than in the Activity.
 */
public final class TaskStore {

    public enum Filter { ALL, OPEN, DONE }

    public static final class Counts {
        public final int open;
        public final int done;

        Counts(int open, int done) {
            this.open = open;
            this.done = done;
        }
    }

    private static final String ORDER =
            "done ASC, COALESCE(completed_at, created_at) DESC";

    private final TaskDb db;

    public TaskStore(Context context) {
        this.db = TaskDb.get(context);
    }

    public List<Task> list(long userId, Filter filter) {
        StringBuilder selection = new StringBuilder("user_id = ?");
        if (filter == Filter.OPEN) {
            selection.append(" AND done = 0");
        } else if (filter == Filter.DONE) {
            selection.append(" AND done = 1");
        }
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = db.getReadableDatabase().query(
                "tasks",
                new String[]{"_id", "title", "notes", "done", "COALESCE(completed_at, created_at)"},
                selection.toString(),
                new String[]{String.valueOf(userId)},
                null, null, ORDER);
        try {
            while (cursor.moveToNext()) {
                tasks.add(new Task(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3) == 1,
                        cursor.getLong(4)));
            }
        } finally {
            cursor.close();
        }
        return tasks;
    }

    public long add(long userId, String title, String notes) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("notes", notes == null ? "" : notes);
        values.put("done", 0);
        values.put("created_at", System.currentTimeMillis());
        return db.getWritableDatabase().insert("tasks", null, values);
    }

    public void setDone(long taskId, long userId, boolean done) {
        ContentValues values = new ContentValues();
        values.put("done", done ? 1 : 0);
        if (done) {
            values.put("completed_at", System.currentTimeMillis());
        } else {
            values.putNull("completed_at");
        }
        db.getWritableDatabase().update(
                "tasks", values, "_id = ? AND user_id = ?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)});
    }

    public void delete(long taskId, long userId) {
        db.getWritableDatabase().delete(
                "tasks", "_id = ? AND user_id = ?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)});
    }

    public Counts counts(long userId) {
        int open = count(userId, 0);
        int done = count(userId, 1);
        return new Counts(open, done);
    }

    private int count(long userId, int done) {
        Cursor cursor = db.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM tasks WHERE user_id = ? AND done = ?",
                new String[]{String.valueOf(userId), String.valueOf(done)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }
}
