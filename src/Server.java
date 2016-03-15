import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;

public class Server {

    private static final int PORT = 5555;
    private static HashSet<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Working...");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
            System.out.println(socket.getInetAddress().toString());
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                writers.add(out);

                while (true) {
                    String input = in.readLine();
                    System.out.println(input);

                    if (input == null) {
                        return;
                    }

                    for (PrintWriter writer : writers) {
                        writer.println(input);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    writers.remove(out);
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}