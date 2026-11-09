package bw.oibsip.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/** Start card: what the quiz is, then one button to begin. */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlassUi.install(this, R.layout.activity_welcome);

        View start = findViewById(R.id.startButton);
        GlassUi.press(start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, QuizActivity.class);
                intent.putExtra(QuizActivity.EXTRA_SEED, System.nanoTime());
                startActivity(intent);
            }
        });

        GlassUi.enter(findViewById(R.id.heroTile), 0L);
        GlassUi.enter(findViewById(R.id.factsCard), 90L);
        GlassUi.enter(start, 160L);
    }
}
