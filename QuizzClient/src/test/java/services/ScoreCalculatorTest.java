package services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import model.Answer;
import model.Answer_Select;
import model.Question;

class ScoreCalculatorTest {

    @Test
    void calculateReturnsFullScoreWhenAllQuestionsAreCorrect() {
        ArrayList<Question> questions = new ArrayList<>();
        questions.add(question(1, true, false));
        questions.add(question(2, false, true));

        List<Answer_Select> selectedAnswers = List.of(
                selected(questions.get(0).getAnswers().get(0), true),
                selected(questions.get(0).getAnswers().get(1), false),
                selected(questions.get(1).getAnswers().get(0), false),
                selected(questions.get(1).getAnswers().get(1), true));

        assertEquals(10, ScoreCalculator.calculate(questions, selectedAnswers, 5));
    }

    @Test
    void calculateCountsQuestionAsWrongWhenAnyAnswerDoesNotMatch() {
        ArrayList<Question> questions = new ArrayList<>();
        questions.add(question(1, true, false));
        questions.add(question(2, false, true));

        List<Answer_Select> selectedAnswers = List.of(
                selected(questions.get(0).getAnswers().get(0), true),
                selected(questions.get(0).getAnswers().get(1), true),
                selected(questions.get(1).getAnswers().get(0), false),
                selected(questions.get(1).getAnswers().get(1), true));

        assertEquals(5, ScoreCalculator.calculate(questions, selectedAnswers, 5));
    }

    private Question question(int questionId, boolean firstCorrect, boolean secondCorrect) {
        ArrayList<Answer> answers = new ArrayList<>();
        answers.add(new Answer(1, questionId, "A", firstCorrect));
        answers.add(new Answer(2, questionId, "B", secondCorrect));
        return new Question(questionId, 1, "Chapter", 1, "Question", answers, false);
    }

    private Answer_Select selected(Answer answer, boolean isChoice) {
        return new Answer_Select(answer, isChoice);
    }
}
