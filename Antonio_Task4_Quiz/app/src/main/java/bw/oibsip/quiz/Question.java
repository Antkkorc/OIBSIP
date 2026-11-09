package bw.oibsip.quiz;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One question plus the note that is shown after it is answered. */
public final class Question {

    public final String prompt;
    public final List<String> options;
    public final int correctIndex;
    public final String note;

    public Question(String prompt, String[] options, int correctIndex, String note) {
        this.prompt = prompt;
        this.options = Collections.unmodifiableList(Arrays.asList(options));
        this.correctIndex = correctIndex;
        this.note = note;
    }

    public String correctOption() {
        return options.get(correctIndex);
    }
}
