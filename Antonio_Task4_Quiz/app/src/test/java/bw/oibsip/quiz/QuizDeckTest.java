package bw.oibsip.quiz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuizDeckTest {

    @Test
    public void bankHasAtLeastTenQuestionsWithFourOptionsEach() {
        List<Question> bank = QuestionBank.all();
        assertTrue("The assignment needs at least ten questions", bank.size() >= 10);
        for (Question question : bank) {
            assertEquals(question.prompt, 4, question.options.size());
            assertEquals(question.prompt, 4, new HashSet<>(question.options).size());
            assertTrue(question.prompt, question.correctIndex >= 0 && question.correctIndex < 4);
            assertTrue(question.prompt, question.note.length() > 20);
        }
    }

    @Test
    public void correctAnswersAreSpreadAcrossAllFourPositions() {
        Set<Integer> positions = new HashSet<>();
        for (Question question : QuestionBank.all()) {
            positions.add(question.correctIndex);
        }
        assertEquals(4, positions.size());
    }

    @Test
    public void dealingKeepsEveryQuestionAndItsCorrectAnswer() {
        List<QuizDeck.Card> deck = QuizDeck.deal(QuestionBank.all(), 42L);
        assertEquals(QuestionBank.all().size(), deck.size());

        Set<String> prompts = new HashSet<>();
        for (QuizDeck.Card card : deck) {
            prompts.add(card.question.prompt);
            assertEquals(4, card.options.size());
            assertTrue(card.options.contains(card.question.correctOption()));
            assertEquals(card.question.correctOption(), card.options.get(card.correctIndex));
        }
        assertEquals(QuestionBank.all().size(), prompts.size());
    }

    @Test
    public void sameSeedDealsSameOrder() {
        List<QuizDeck.Card> first = QuizDeck.deal(QuestionBank.all(), 7L);
        List<QuizDeck.Card> second = QuizDeck.deal(QuestionBank.all(), 7L);
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).question.prompt, second.get(i).question.prompt);
            assertEquals(first.get(i).options, second.get(i).options);
            assertEquals(first.get(i).correctIndex, second.get(i).correctIndex);
        }
    }

    @Test
    public void differentSeedsDealDifferentOrders() {
        List<QuizDeck.Card> first = QuizDeck.deal(QuestionBank.all(), 1L);
        List<QuizDeck.Card> second = QuizDeck.deal(QuestionBank.all(), 2L);
        boolean differs = false;
        for (int i = 0; i < first.size(); i++) {
            if (!first.get(i).question.prompt.equals(second.get(i).question.prompt)) {
                differs = true;
                break;
            }
        }
        assertTrue("Two seeds should not produce the identical deck", differs);
    }

    @Test
    public void scoringCountsOnlyMatchingAnswers() {
        List<QuizDeck.Card> deck = QuizDeck.deal(QuestionBank.all(), 99L);
        List<Integer> answers = new ArrayList<>();
        for (QuizDeck.Card card : deck) {
            answers.add(card.correctIndex);
        }
        assertEquals(deck.size(), QuizDeck.score(deck, answers));

        answers.set(0, -1);
        assertEquals(deck.size() - 1, QuizDeck.score(deck, answers));

        answers.set(0, deck.get(0).correctIndex);
        answers.set(1, (deck.get(1).correctIndex + 1) % 4);
        assertEquals(deck.size() - 1, QuizDeck.score(deck, answers));

        assertEquals(0, QuizDeck.score(deck, Arrays.asList(-1, -1, -1)));
    }

    @Test
    public void shuffledOptionsStayWithinTheirQuestion() {
        List<QuizDeck.Card> deck = QuizDeck.deal(QuestionBank.all(), 123456L);
        for (QuizDeck.Card card : deck) {
            assertEquals(new HashSet<>(card.question.options), new HashSet<>(card.options));
        }
        // At least one question must not be in its original option order.
        boolean reshuffled = false;
        for (QuizDeck.Card card : deck) {
            if (!card.options.equals(card.question.options)) {
                reshuffled = true;
                break;
            }
        }
        assertTrue(reshuffled);
        assertNotEquals(0, deck.size());
    }
}
