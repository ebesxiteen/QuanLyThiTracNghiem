package services;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import data.StudentDAO;
import data.SubmissionDAO;
import model.HostExam;
import model.Question;
import model.Student;
import model.Submission;

public class StartServer {

    private final Map<String, String> clients;
    private ServerSocket serverSocket;
    private final int port;

    public StartServer(HostExam hostExam, int port) throws IOException {
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
        try {
            this.serverSocket = new ServerSocket(port);
            new Thread() {
                @Override
                public void run() {
                    try {
                        while (!serverSocket.isClosed()) {
                            new ThreadServer(serverSocket.accept(), clients, hostExam).start();
                        }
                    } catch (SocketException e) {
                        if (serverSocket.isClosed()) {
                            System.out.println("Connection Closed.");
                        }
                    } catch (IOException e) {
                        System.err.println("Accept failed.");
                    }
                }
            }.start();
        } catch (IOException e) {
            throw new IOException("Could not listen on port: " + port);
        }
    }

    public void shutdownServer() throws IOException {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new IOException("Could not close port: " + port);
        }
    }

    public static String getIPAddress() {
        try (final DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.connect(InetAddress.getByName("8.8.8.8"), 12345);
            return datagramSocket.getLocalAddress().getHostAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getConnectedClients() {
        return new ArrayList<>(clients.values());
    }
}

class ThreadServer extends Thread {

    private static final int SOCKET_TIMEOUT_MILLIS = 300_000;
    private static final int MAX_SERIAL_DEPTH = 20;
    private static final long MAX_SERIAL_REFERENCES = 10_000;
    private static final long MAX_SERIAL_BYTES = 1_000_000;
    private static final String STUDENT_CODE_PATTERN = "[A-Za-z0-9_-]{1,20}";

    private final Socket socket;
    private final Map<String, String> clients;
    private final HostExam hostExam;

    public ThreadServer(Socket socket, Map<String, String> clients, HostExam hostExam) {
        this.socket = socket;
        this.clients = clients;
        this.hostExam = hostExam;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }

        try (
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            inputStream.setObjectInputFilter(ThreadServer::serverInputFilter);

            Object studentPayload = inputStream.readObject();
            if (!(studentPayload instanceof String)) {
                return;
            }

            String studentCode = ((String) studentPayload).trim();
            if (!studentCode.matches(STUDENT_CODE_PATTERN)) {
                outputStream.writeObject(null);
                outputStream.flush();
                return;
            }

            Student student = new StudentDAO().getByStudentIdfromGroup(studentCode, hostExam.getGroupId());
            if (student == null) {
                outputStream.writeObject(null);
                outputStream.flush();
                return;
            }

            clients.put(student.getStudentId(), formatClientStatus(student, 0, 0));
            outputStream.writeObject(hostExam);
            outputStream.flush();

            Object submissionPayload = inputStream.readObject();
            if (!(submissionPayload instanceof Submission)) {
                return;
            }

            Submission submission = (Submission) submissionPayload;
            submission.setStudentId(student.getUid());
            if (!isValidSubmission(submission)) {
                return;
            }

            if (new SubmissionDAO().create(submission)) {
                clients.put(student.getStudentId(),
                        formatClientStatus(student, submission.getTimeTaken(), submission.getScore()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String formatClientStatus(Student student, int timeTaken, float score) {
        return student.getStudentId() + "-" + student.getFirstName() + " " + student.getLastName() + "-"
                + timeTaken + "-" + score;
    }

    private boolean isValidSubmission(Submission submission) {
        if (submission == null || submission.getAnswerSelectedMap() == null) {
            return false;
        }
        if (submission.getHostExamId() != hostExam.getHostExamId()) {
            return false;
        }
        if (submission.getTimeTaken() < 0 || submission.getScore() < 0 || submission.getScore() > hostExam.getMaxScore()) {
            return false;
        }
        if (submission.getAnswerSelectedMap().size() != hostExam.getExamQuestions().size()) {
            return false;
        }

        for (Question question : hostExam.getExamQuestions()) {
            List<Integer> selectedAnswers = submission.getAnswerSelectedMap().get(question.getQuestionId());
            if (selectedAnswers == null || selectedAnswers.size() != question.getAnswers().size()) {
                return false;
            }
            for (Integer selectedAnswer : selectedAnswers) {
                if (selectedAnswer == null || (selectedAnswer != 0 && selectedAnswer != 1)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static ObjectInputFilter.Status serverInputFilter(ObjectInputFilter.FilterInfo info) {
        if (info.depth() > MAX_SERIAL_DEPTH || info.references() > MAX_SERIAL_REFERENCES
                || info.streamBytes() > MAX_SERIAL_BYTES) {
            return ObjectInputFilter.Status.REJECTED;
        }

        Class<?> serialClass = info.serialClass();
        if (serialClass == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        if (serialClass.isArray()) {
            return ObjectInputFilter.Status.ALLOWED;
        }

        String className = serialClass.getName();
        if (className.startsWith("model.") || className.startsWith("java.lang.")
                || className.startsWith("java.util.")) {
            return ObjectInputFilter.Status.ALLOWED;
        }

        return ObjectInputFilter.Status.REJECTED;
    }
}
