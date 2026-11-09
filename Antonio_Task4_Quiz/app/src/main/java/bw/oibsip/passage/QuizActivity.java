package bw.oibsip.passage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * One question at a time. The answer locks on the first tap: no changing it after the
 * green rule appears, which is the only honest way to keep a score.
 */
public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_SEED = "seed";
    private static final String STATE_SEED = "state_seed";
    private static final String STATE_INDEX = "state_index";
    private static final String STATE_ANSWERS = "state_answers";

    private List<QuizDeck.Card> deck;
    private final List<Integer> answers = new ArrayList<>();
    private long seed;
    private int index;
    private boolean answered;

    private TextView counter;
    private TextView questionText;
    private TextView hintText;
    private TextView noteText;
    private Button nextButton;
    private TextView[] options;
    private View progressDone;
    private View progressRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        counter = findViewById(R.id.counter);
        questionText = findViewById(R.id.questionText);
        hintText = findViewById(R.id.hintText);
        noteText = findViewById(R.id.noteText);
        nextButton = findViewById(R.id.nextButton);
        progressDone = findViewById(R.id.progressDone);
        progressRest = findViewById(R.id.progressRest);
        options = new TextView[]{
                findViewById(R.id.option0),
                findViewById(R.id.option1),
                findViewById(R.id.option2),
                findViewById(R.id.option3)
        };

        seed = getIntent().getLongExtra(EXTRA_SEED, 0L);
        if (seed == 0L) {
            seed = System.nanoTime();
        }

        int restoredIndex = 0;
        if (savedInstanceState != null) {
            seed = savedInstanceState.getLong(STATE_SEED, seed);
            restoredIndex = savedInstanceState.getInt(STATE_INDEX, 0);
            ArrayList<Integer> saved = savedInstanceState.getIntegerArrayList(STATE_ANSWERS);
            if (saved != null) {
                answers.clear();
                answers.addAll(saved);
            }
        }

        deck = QuizDeck.deal(QuestionBank.all(), seed);
        index = Math.max(0, Math.min(restoredIndex, deck.size() - 1));
        while (answers.size() < deck.size()) {
            answers.add(-1);
        }

        for (int i = 0; i < options.length; i++) {
            final int position = i;
            options[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answer(position);
                }
            });
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                advance();
            }
        });

        showQuestion();
    }

    private void showQuestion() {
        QuizDeck.Card card = deck.get(index);
        counter.setText(getString(R.string.question_counter, index + 1, deck.size()));
        questionText.setText(card.question.prompt);

        int recorded = answers.get(index);
        answered = recorded >= 0;

        for (int i = 0; i < options.length; i++) {
            options[i].setText(card.options.get(i));
            options[i].setBackgroundResource(R.drawable.bg_option);
            options[i].setTextColor(ContextCompat.getColor(this, R.color.ink));
        }

        if (answered) {
            paintFeedback(card, recorded);
        } else {
            hintText.setVisibility(View.VISIBLE);
            noteText.setVisibility(View.GONE);
        }

        nextButton.setEnabled(answered);
        nextButton.setText(index == deck.size() - 1
                ? R.string.see_results
                : R.string.next_question);

        updateProgress();
    }

    private void answer(int position) {
        if (answered) {
            return;
        }
        QuizDeck.Card card = deck.get(index);
        answered = true;
        answers.set(index, position);
        paintFeedback(card, position);
        nextButton.setEnabled(true);
        updateProgress();
    }

    /** Green on the right answer, oxide on the wrong pick, grey on everything else. */
    private void paintFeedback(QuizDeck.Card card, int chosen) {
        for (int i = 0; i < options.length; i++) {
            if (i == card.correctIndex) {
                options[i].setBackgroundResource(R.drawable.bg_option_correct);
                options[i].setTextColor(ContextCompat.getColor(this, R.color.moss));
            } else if (i == chosen) {
                options[i].setBackgroundResource(R.drawable.bg_option_wrong);
                options[i].setTextColor(ContextCompat.getColor(this, R.color.oxide));
            } else {
                options[i].setBackgroundResource(R.drawable.bg_option);
                options[i].setTextColor(ContextCompat.getColor(this, R.color.ink_muted));
            }
        }

        hintText.setVisibility(View.GONE);
        noteText.setText(getString(R.string.note_label).toUpperCase(java.util.Locale.US)
                + " · " + card.question.note);
        noteText.setVisibility(View.VISIBLE);
        fadeOptions();
    }

    private void updateProgress() {
        int done = index + (answered ? 1 : 0);
        int rest = Math.max(1, deck.size() - done);
        LinearLayout.LayoutParams doneParams =
                (LinearLayout.LayoutParams) progressDone.getLayoutParams();
        doneParams.weight = Math.max(0.001f, done);
        progressDone.setLayoutParams(doneParams);

        LinearLayout.LayoutParams restParams =
                (LinearLayout.LayoutParams) progressRest.getLayoutParams();
        restParams.weight = rest;
        progressRest.setLayoutParams(restParams);
    }

    private void advance() {
        if (!answered) {
            return;
        }
        if (index < deck.size() - 1) {
            index++;
            showQuestion();
            return;
        }
        int correct = QuizDeck.score(deck, answers);
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_CORRECT, correct);
        intent.putExtra(ResultActivity.EXTRA_TOTAL, deck.size());
        startActivity(intent);
        finish();
    }

    /** 140ms alpha on the option block — motion because the state just changed. */
    private void fadeOptions() {
        for (TextView option : options) {
            option.animate().cancel();
            option.setAlpha(0.55f);
            option.animate().alpha(1f).setDuration(140).start();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_SEED, seed);
        outState.putInt(STATE_INDEX, index);
        outState.putIntegerArrayList(STATE_ANSWERS, new ArrayList<>(answers));
    }
}
