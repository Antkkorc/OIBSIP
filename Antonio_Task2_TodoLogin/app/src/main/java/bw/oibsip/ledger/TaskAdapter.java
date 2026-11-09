package bw.oibsip.ledger;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Ledger rows. Open entries keep a 3dp ink marker in the gutter; finished entries lose
 * the marker, take the strikethrough and drop to muted ink.
 */
public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskHolder> {

    public interface Listener {
        void onToggle(Task task);

        void onDelete(Task task);
    }

    private static final DiffUtil.ItemCallback<Task> DIFF = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task a, @NonNull Task b) {
            return a.id == b.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task a, @NonNull Task b) {
            return a.id == b.id
                    && a.done == b.done
                    && a.updatedAt == b.updatedAt
                    && a.title.equals(b.title)
                    && a.notes.equals(b.notes);
        }
    };

    private final Listener listener;
    private final SimpleDateFormat stamp =
            new SimpleDateFormat("dd MMM · HH:mm", Locale.US);

    public TaskAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TaskHolder extends RecyclerView.ViewHolder {

        private final View marker;
        private final CheckBox check;
        private final TextView title;
        private final TextView notes;
        private final TextView stampView;
        private final View delete;

        TaskHolder(@NonNull View itemView) {
            super(itemView);
            marker = itemView.findViewById(R.id.openMarker);
            check = itemView.findViewById(R.id.doneCheck);
            title = itemView.findViewById(R.id.taskTitle);
            notes = itemView.findViewById(R.id.taskNotes);
            stampView = itemView.findViewById(R.id.taskStamp);
            delete = itemView.findViewById(R.id.deleteButton);
        }

        void bind(final Task task) {
            title.setText(task.title);
            if (task.done) {
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.ink_muted));
                itemView.setAlpha(0.72f);
                marker.setVisibility(View.INVISIBLE);
            } else {
                title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.ink));
                itemView.setAlpha(1f);
                marker.setVisibility(View.VISIBLE);
            }

            check.setChecked(task.done);

            if (task.notes.isEmpty()) {
                notes.setVisibility(View.GONE);
            } else {
                notes.setVisibility(View.VISIBLE);
                notes.setText(task.notes);
            }

            String prefix = task.done
                    ? itemView.getContext().getString(R.string.stamp_done)
                    : itemView.getContext().getString(R.string.stamp_added);
            stampView.setText((prefix + " " + stamp.format(new Date(task.updatedAt)))
                    .toUpperCase(Locale.US));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onToggle(task);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDelete(task);
                }
            });
        }
    }
}
