package services;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Answer;
import model.Answer_Select;
import model.HostExam;
import model.Question;
import model.Submission;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class StartClient {

    private Data submission = new Data();

    private HostExam hostExam = null;

    private String studentID = null;

    public StartClient(String id, String host, int port) {

        new Thread() {
            @Override
            public void run() {
                try (
                        Socket socket = new Socket(host, port);

                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

                        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

                    String clientInfo = id;

                    studentID = id;

                    outputStream.writeObject(clientInfo);

                    hostExam = (HostExam) inputStream.readObject();
                    if (hostExam == null) {
                        return;
                    }

                    Submission receivedSubmission = submission.receive();

                    outputStream.writeObject(receivedSubmission);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public HostExam getHostExam() {
        HostExam hostExam_Test = this.hostExam;

        return hostExam_Test;
    }

    public HostExam HostExamCorrect() {
        return hostExam;
    }

    public double submit(ArrayList<Question> questionSelecteds, List<Answer_Select> answer_selects, long time) {

        long timeTaken = time;

        double scorePerQuestion = hostExam.getMaxScore() / (hostExam.getExamQuestions().size() * 1.0);
        float score = (float) ScoreCalculator.calculate(questionSelecteds, answer_selects, scorePerQuestion);
    
        int studentID_Sub = Integer.parseInt(this.studentID.substring(2));

        Map<Integer, List<Integer>> map = convertListMap(questionSelecteds);

        Submission submission = new Submission(0, hostExam.getHostExamId(), studentID_Sub, (int) timeTaken, score,
                map);

        this.submission.send(submission);

        return (float) (Math.round(score * 100.0) / 100.0);

    }

    private Map<Integer, List<Integer>> convertListMap(List<Question> list) {
        Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>(list.size());
        for (Question i : list) {
            int questionId = i.getQuestionId();
            ArrayList<Answer> selectedAnswer = i.getAnswers();
            List<Integer> answerChosen = new ArrayList<Integer>(selectedAnswer.size());
            for (Answer j : selectedAnswer) {
                answerChosen.add(j.isCorrect() ? 1 : 0);
            }
            map.put(questionId, answerChosen);

        }
        return map;
    }

}

class Data {
    private Submission packet;

    private boolean transfer = true;

    public synchronized Submission receive() {
        while (transfer) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread Interrupted");
            }
        }
        transfer = true;
        Submission returnPacket = packet;
        notifyAll();
        return returnPacket;
    }

    public synchronized void send(Submission packet) {
        while (!transfer) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread Interrupted");
            }
        }
        transfer = false;
        this.packet = packet;
        notifyAll();
    }
}
