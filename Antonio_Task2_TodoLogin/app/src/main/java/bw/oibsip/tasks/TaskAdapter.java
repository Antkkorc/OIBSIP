package bw.oibsip.tasks;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
 * Glass rows for the task list. A finished task gets the strikethrough, a filled check
 * tile and a softer title colour. Everything else stays bright.
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
            new SimpleDateFormat("d MMM, HH:mm", Locale.getDefault());

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

        private final View checkTile;
        private final ImageView checkMark;
        private final TextView title;
        private final TextView notes;
        private final TextView stampView;
        private final ImageButton delete;

        TaskHolder(@NonNull View itemView) {
            super(itemView);
            checkTile = itemView.findViewById(R.id.doneCheckTile);
            checkMark = itemView.findViewById(R.id.checkMark);
            title = itemView.findViewById(R.id.taskTitle);
            notes = itemView.findViewById(R.id.taskNotes);
            stampView = itemView.findViewById(R.id.taskStamp);
            delete = itemView.findViewById(R.id.deleteButton);
        }

        void bind(final Task task) {
            title.setText(task.title);

            if (task.done) {
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_tertiary));
                checkTile.setBackgroundResource(R.drawable.bg_check_on);
                checkMark.setVisibility(View.VISIBLE);
            } else {
                title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
                checkTile.setBackgroundResource(R.drawable.bg_check_off);
                checkMark.setVisibility(View.INVISIBLE);
            }

            if (task.notes == null || task.notes.isEmpty()) {
                notes.setVisibility(View.GONE);
            } else {
                notes.setVisibility(View.VISIBLE);
                notes.setText(task.notes);
            }

            String prefix = itemView.getContext()
                    .getString(task.done ? R.string.stamp_done : R.string.stamp_added);
            stampView.setText(prefix + " " + stamp.format(new Date(task.updatedAt)));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onToggle(task);
                }
            });

            checkTile.setOnClickListener(new View.OnClickListener() {
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
