package bw.oibsip.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Deals a quiz from a seed, so the same seed always produces the same order.
 *
 * That one decision is what makes rotation safe: the Activity saves the seed and the
 * answers, and a re-created screen re-deals the identical deck instead of reshuffling
 * the exam under the user's thumb.
 */
public final class QuizDeck {

    public static final class Card {
        public final Question question;
        public final List<String> options;
        public final int correctIndex;

        Card(Question question, List<String> options, int correctIndex) {
            this.question = question;
            this.options = Collections.unmodifiableList(options);
            this.correctIndex = correctIndex;
        }

        public boolean isCorrect(int optionIndex) {
            return optionIndex == correctIndex;
        }
    }

    private QuizDeck() {
    }

    public static List<Card> deal(List<Question> bank, long seed) {
        Random random = new Random(seed);
        List<Question> questions = new ArrayList<>(bank);
        Collections.shuffle(questions, random);

        List<Card> cards = new ArrayList<>(questions.size());
        for (Question question : questions) {
            List<String> options = new ArrayList<>(question.options);
            String correct = question.correctOption();
            Collections.shuffle(options, random);
            // Option texts inside one question are unique, so indexOf is unambiguous here.
            cards.add(new Card(question, options, options.indexOf(correct)));
        }
        return cards;
    }

    /** Counts answers that match the dealt deck; -1 (unanswered) never scores. */
    public static int score(List<Card> cards, List<Integer> answers) {
        int score = 0;
        for (int i = 0; i < cards.size() && i < answers.size(); i++) {
            int answer = answers.get(i);
            if (answer >= 0 && cards.get(i).isCorrect(answer)) {
                score++;
            }
        }
        return score;
    }
}
