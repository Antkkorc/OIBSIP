package bw.oibsip.chrono;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lap rows, newest first. The fastest and slowest tags are recomputed on every update
 * because a new lap can take either title from an older one.
 */
public class LapAdapter extends RecyclerView.Adapter<LapAdapter.LapHolder> {

    private List<Lap> laps = new ArrayList<>();
    private int bestIndex = -1;
    private int worstIndex = -1;

    public void setLaps(List<Lap> newLaps) {
        this.laps = newLaps == null ? new ArrayList<Lap>() : newLaps;
        this.bestIndex = Lap.bestIndex(this.laps);
        this.worstIndex = Lap.worstIndex(this.laps);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LapHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lap, parent, false);
        return new LapHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LapHolder holder, int position) {
        holder.bind(laps.get(position), position == bestIndex, position == worstIndex);
    }

    @Override
    public int getItemCount() {
        return laps.size();
    }

    static class LapHolder extends RecyclerView.ViewHolder {

        private final TextView number;
        private final TextView split;
        private final TextView tag;
        private final TextView total;

        LapHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.lapNumber);
            split = itemView.findViewById(R.id.lapSplit);
            tag = itemView.findViewById(R.id.lapTag);
            total = itemView.findViewById(R.id.lapTotal);
        }

        void bind(Lap lap, boolean best, boolean worst) {
            number.setText(String.format(Locale.US, "L%d", lap.number));
            split.setText(ChronoFormat.full(lap.split));
            total.setText(ChronoFormat.full(lap.total));

            if (best) {
                tag.setVisibility(View.VISIBLE);
                tag.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success));
                tag.setText(R.string.best);
            } else if (worst) {
                tag.setVisibility(View.VISIBLE);
                tag.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error));
                tag.setText(R.string.worst);
            } else {
                tag.setVisibility(View.INVISIBLE);
                tag.setText("");
            }
        }
    }
}
