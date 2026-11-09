package bw.oibsip.ledger;

/** A single ledger entry. Immutable: edits go back through TaskStore. */
public final class Task {

    public final long id;
    public final String title;
    public final String notes;
    public final boolean done;
    /** created_at while open, completed_at once done — used for ordering and the stamp. */
    public final long updatedAt;

    public Task(long id, String title, String notes, boolean done, long updatedAt) {
        this.id = id;
        this.title = title;
        this.notes = notes;
        this.done = done;
        this.updatedAt = updatedAt;
    }
}
