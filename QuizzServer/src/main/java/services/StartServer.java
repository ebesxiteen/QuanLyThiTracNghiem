package services;

import java.io.IOException;
import java.io.ObjectInputStream;
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
        try (
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            Object studentPayload = inputStream.readObject();
            if (!(studentPayload instanceof String)) {
                return;
            }

            String studentCode = (String) studentPayload;
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
}
