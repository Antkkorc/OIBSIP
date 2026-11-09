package bw.oibsip.ledger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * The ledger itself. The open count is the dominant object: it is the one number the
 * user came for, so it is set in serif at 60sp while everything else is mono.
 */
public class TasksActivity extends AppCompatActivity implements TaskAdapter.Listener {

    private UserStore users;
    private TaskStore store;
    private SessionStore session;
    private TaskAdapter adapter;

    private TextView openCount;
    private TextView doneCount;
    private TextView signedInName;
    private TextView emptyTitle;
    private TextView emptyHint;
    private TextView emptyLegend;
    private TextView[] filters;
    private View emptyState;

    private long userId;
    private TaskStore.Filter filter = TaskStore.Filter.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        users = new UserStore(this);
        store = new TaskStore(this);
        session = new SessionStore(this);

        userId = session.userId();
        UserStore.Account account = userId > 0 ? users.byId(userId) : null;
        if (account == null) {
            session.signOut();
            goToLogin(getString(R.string.error_session));
            return;
        }

        openCount = findViewById(R.id.openCount);
        doneCount = findViewById(R.id.doneCount);
        signedInName = findViewById(R.id.signedInName);
        emptyTitle = findViewById(R.id.emptyTitle);
        emptyHint = findViewById(R.id.emptyHint);
        emptyLegend = findViewById(R.id.emptyLegend);
        emptyState = findViewById(R.id.emptyState);

        signedInName.setText(account.name);

        RecyclerView list = findViewById(R.id.taskList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setItemAnimator(new DefaultItemAnimator() {
            // Rows arrive and leave instantly; only a changed row fades.
            @Override
            public boolean animateAdd(RecyclerView.ViewHolder holder) {
                dispatchAddFinished(holder);
                return false;
            }

            @Override
            public boolean animateRemove(RecyclerView.ViewHolder holder) {
                dispatchRemoveFinished(holder);
                return false;
            }
        });
        adapter = new TaskAdapter(this);
        list.setAdapter(adapter);

        filters = new TextView[]{
                findViewById(R.id.filterAll),
                findViewById(R.id.filterOpen),
                findViewById(R.id.filterDone)
        };
        for (int i = 0; i < filters.length; i++) {
            final TaskStore.Filter target = TaskStore.Filter.values()[i];
            filters[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filter = target;
                    refresh();
                }
            });
        }

        findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        findViewById(R.id.logoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        refresh();
    }

    private void refresh() {
        List<Task> tasks = store.list(userId, filter);
        adapter.submitList(tasks);

        TaskStore.Counts counts = store.counts(userId);
        openCount.setText(counts.open > 99
                ? String.valueOf(counts.open)
                : String.format(Locale.US, "%02d", counts.open));
        doneCount.setText(getString(R.string.count_done).toUpperCase(Locale.US)
                + " " + counts.done);

        for (int i = 0; i < filters.length; i++) {
            filters[i].setSelected(i == filter.ordinal());
        }

        boolean empty = tasks.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            switch (filter) {
                case OPEN:
                    emptyLegend.setText("00 / no open entries");
                    emptyTitle.setText(R.string.empty_open_title);
                    emptyHint.setText(R.string.empty_open_hint);
                    break;
                case DONE:
                    emptyLegend.setText("00 / no finished entries");
                    emptyTitle.setText(R.string.empty_done_title);
                    emptyHint.setText(R.string.empty_done_hint);
                    break;
                case ALL:
                default:
                    emptyLegend.setText("00 / no entries");
                    emptyTitle.setText(R.string.empty_all_title);
                    emptyHint.setText(R.string.empty_all_hint);
                    break;
            }
        }
    }

    @Override
    public void onToggle(Task task) {
        store.setDone(task.id, userId, !task.done);
        refresh();
    }

    @Override
    public void onDelete(Task task) {
        showDeleteDialog(task);
    }

    private void showAddDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_task);

        final EditText titleInput = dialog.findViewById(R.id.titleInput);
        final EditText notesInput = dialog.findViewById(R.id.notesInput);

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = tidy(titleInput.getText().toString());
                if (TextUtils.isEmpty(title)) {
                    titleInput.requestFocus();
                    Toast.makeText(TasksActivity.this,
                            R.string.error_task_title, Toast.LENGTH_SHORT).show();
                    return;
                }
                store.add(userId, title, notesInput.getText().toString().trim());
                if (filter == TaskStore.Filter.DONE) {
                    // A new entry is open by definition; do not leave the user staring at an empty DONE page.
                    filter = TaskStore.Filter.ALL;
                }
                dialog.dismiss();
                refresh();
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.paper);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        titleInput.requestFocus();
    }

    private void showDeleteDialog(final Task task) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm);

        TextView entry = dialog.findViewById(R.id.confirmEntry);
        entry.setText(task.title);

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                store.delete(task.id, userId);
                dialog.dismiss();
                refresh();
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.paper);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /** Collapses runs of whitespace so a pasted title cannot wreck the row rhythm. */
    private static String tidy(String raw) {
        return raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
    }

    private void logOut() {
        session.signOut();
        goToLogin(null);
    }

    private void goToLogin(String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
