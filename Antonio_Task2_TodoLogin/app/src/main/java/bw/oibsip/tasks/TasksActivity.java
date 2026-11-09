package bw.oibsip.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Dialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The task list. Everything shown here belongs to the signed-in account; the store
 * filters on user id for every query.
 */
public class TasksActivity extends AppCompatActivity implements TaskAdapter.Listener {

    private static final String STATE_FILTER = "filter";

    private TaskStore store;
    private SessionStore session;
    private UserStore users;
    private UserStore.Account account;

    private TaskAdapter adapter;
    private RecyclerView list;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptyBody;
    private TextView countLine;
    private TextView[] chips;

    private TaskStore.Filter filter = TaskStore.Filter.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlassUi.install(this, R.layout.activity_tasks);

        session = new SessionStore(this);
        users = new UserStore(this);
        if (!session.hasSession()) {
            goToLogin();
            return;
        }
        account = users.byId(session.userId());
        if (account == null) {
            session.signOut();
            goToLogin();
            return;
        }
        store = new TaskStore(this);

        if (savedInstanceState != null) {
            filter = TaskStore.Filter.values()[savedInstanceState.getInt(STATE_FILTER, 0)];
        }

        list = findViewById(R.id.taskList);
        emptyState = findViewById(R.id.emptyState);
        emptyTitle = findViewById(R.id.emptyTitle);
        emptyBody = findViewById(R.id.emptyBody);
        countLine = findViewById(R.id.countLine);

        chips = new TextView[]{
                findViewById(R.id.filterAll),
                findViewById(R.id.filterOpen),
                findViewById(R.id.filterDone)
        };
        for (int i = 0; i < chips.length; i++) {
            final TaskStore.Filter target = TaskStore.Filter.values()[i];
            chips[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (target != filter) {
                        filter = target;
                        refresh();
                    }
                }
            });
        }
        GlassUi.pressAll(chips);

        adapter = new TaskAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        ImageButton fab = findViewById(R.id.addFab);
        GlassUi.press(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        Button logout = findViewById(R.id.logoutButton);
        GlassUi.press(logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.signOut();
                Toast.makeText(TasksActivity.this, R.string.signed_out, Toast.LENGTH_SHORT).show();
                goToLogin();
            }
        });

        GlassUi.enter(findViewById(R.id.header), 0L);
        GlassUi.enter(findViewById(R.id.filterRow), 70L);
        GlassUi.enter(list, 130L);
        GlassUi.enter(fab, 190L);

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (store == null || account == null) {
            return;
        }
        if (!session.hasSession()) {
            goToLogin();
            return;
        }
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < chips.length; i++) {
            chips[i].setSelected(i == filter.ordinal());
        }

        adapter.submitList(store.list(account.id, filter), new Runnable() {
            @Override
            public void run() {
                updateEmptyState();
            }
        });

        TaskStore.Counts counts = store.counts(account.id);
        countLine.setText(getString(R.string.counts, counts.open, counts.done));
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) {
            return;
        }
        if (filter == TaskStore.Filter.OPEN) {
            emptyTitle.setText(R.string.empty_open_title);
            emptyBody.setText(R.string.empty_open_body);
        } else if (filter == TaskStore.Filter.DONE) {
            emptyTitle.setText(R.string.empty_done_title);
            emptyBody.setText(R.string.empty_done_body);
        } else {
            emptyTitle.setText(R.string.empty_all_title);
            emptyBody.setText(R.string.empty_all_body);
        }
        GlassUi.refresh(emptyState);
    }

    private void showAddDialog() {
        final Dialog dialog = GlassUi.dialog(this, R.layout.dialog_add_task);
        final EditText titleInput = dialog.findViewById(R.id.taskTitleInput);
        final EditText notesInput = dialog.findViewById(R.id.taskNotesInput);
        final TextView error = dialog.findViewById(R.id.dialogError);
        Button cancel = dialog.findViewById(R.id.cancelButton);
        Button add = dialog.findViewById(R.id.addButton);
        GlassUi.press(cancel);
        GlassUi.press(add);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleInput.getText().toString().trim();
                if (title.isEmpty()) {
                    error.setText(R.string.err_title_empty);
                    error.setVisibility(View.VISIBLE);
                    GlassUi.refresh(error);
                    return;
                }
                store.add(account.id, title, notesInput.getText().toString().trim());
                dialog.dismiss();
                Toast.makeText(TasksActivity.this, R.string.task_added, Toast.LENGTH_SHORT).show();
                refresh();
            }
        });

        dialog.show();
        titleInput.requestFocus();
    }

    @Override
    public void onToggle(Task task) {
        store.setDone(task.id, account.id, !task.done);
        Toast.makeText(this,
                task.done ? R.string.task_reopened : R.string.task_checked,
                Toast.LENGTH_SHORT).show();
        refresh();
    }

    @Override
    public void onDelete(Task task) {
        final Dialog dialog = GlassUi.dialog(this, R.layout.dialog_confirm);
        TextView message = dialog.findViewById(R.id.confirmMessage);
        Button keep = dialog.findViewById(R.id.keepButton);
        Button delete = dialog.findViewById(R.id.deleteButton);
        GlassUi.press(keep);
        GlassUi.press(delete);

        message.setText(task.title);
        keep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                store.delete(task.id, account.id);
                dialog.dismiss();
                Toast.makeText(TasksActivity.this, R.string.task_deleted, Toast.LENGTH_SHORT).show();
                refresh();
            }
        });
        dialog.show();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_FILTER, filter.ordinal());
    }
}
