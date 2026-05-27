package services;

import java.util.ArrayList;
import java.util.List;

import model.Answer;
import model.Answer_Select;
import model.Question;

public final class ScoreCalculator {

    private ScoreCalculator() {
    }

    public static double calculate(ArrayList<Question> questions, List<Answer_Select> selectedAnswers,
            double scorePerQuestion) {
        if (questions == null || selectedAnswers == null || questions.isEmpty()) {
            return 0;
        }

        int correctQuestionCount = 0;
        int selectedIndex = 0;

        for (Question question : questions) {
            ArrayList<Answer> answers = question.getAnswers();
            if (answers == null || selectedIndex + answers.size() > selectedAnswers.size()) {
                return correctQuestionCount * scorePerQuestion;
            }

            boolean isCorrectQuestion = true;
            for (Answer answer : answers) {
                Answer_Select selectedAnswer = selectedAnswers.get(selectedIndex++);
                if (answer.isCorrect() != selectedAnswer.isChoice()) {
                    isCorrectQuestion = false;
                }
            }

            if (isCorrectQuestion) {
                correctQuestionCount++;
            }
        }

        return correctQuestionCount * scorePerQuestion;
    }
}
