package bw.oibsip.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Scorecard: the number first, then the correct, incorrect and accuracy breakdown. */
public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_CORRECT = "correct";
    public static final String EXTRA_TOTAL = "total";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlassUi.install(this, R.layout.activity_result);

        int correct = Math.max(0, getIntent().getIntExtra(EXTRA_CORRECT, 0));
        int total = Math.max(1, getIntent().getIntExtra(EXTRA_TOTAL, 12));
        int incorrect = Math.max(0, total - correct);
        int percent = Math.round(correct * 100f / total);

        TextView scoreValue = findViewById(R.id.scoreValue);
        TextView scoreTotal = findViewById(R.id.scoreTotal);
        TextView verdict = findViewById(R.id.verdict);
        TextView correctValue = findViewById(R.id.correctValue);
        TextView incorrectValue = findViewById(R.id.incorrectValue);
        TextView percentValue = findViewById(R.id.percentValue);

        scoreValue.setText(String.valueOf(correct));
        scoreTotal.setText(getString(R.string.result_of, total));
        correctValue.setText(String.valueOf(correct));
        incorrectValue.setText(String.valueOf(incorrect));
        percentValue.setText(percent + "%");
        verdict.setText(verdictFor(correct, total));

        View restart = findViewById(R.id.restartButton);
        GlassUi.press(restart);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, QuizActivity.class);
                intent.putExtra(QuizActivity.EXTRA_SEED, System.nanoTime());
                startActivity(intent);
                finish();
            }
        });

        View close = findViewById(R.id.closeButton);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        GlassUi.enter(findViewById(R.id.resultTile), 0L);
        GlassUi.enter(findViewById(R.id.scoreRow), 80L);
        GlassUi.enter(findViewById(R.id.verdict), 130L);
        GlassUi.enter(findViewById(R.id.breakdownCard), 180L);
        GlassUi.enter(restart, 240L);
        GlassUi.press(close);
    }

    private String verdictFor(int correct, int total) {
        if (correct == total) {
            return getString(R.string.verdict_perfect);
        }
        double fraction = total == 0 ? 0d : (double) correct / total;
        if (fraction >= 0.8d) {
            return getString(R.string.verdict_strong);
        }
        if (fraction >= 0.55d) {
            return getString(R.string.verdict_mid);
        }
        if (fraction >= 0.3d) {
            return getString(R.string.verdict_weak);
        }
        return getString(R.string.verdict_poor);
    }
}
