import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class Server {

    private static HashSet<PrintWriter> clients = new HashSet<>();

    public static void main(String[] args) throws IOException, NumberFormatException {
        int port = Integer.parseInt(args[1]);
        ServerSocket listener = new ServerSocket(port);
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

                clients.add(out);

                while (true) {
                    String input = in.readLine();
                    System.out.println(input);

                    if (input == null) {
                        return;
                    }

                    for (PrintWriter client : clients) {
                        client.println(input);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (out != null) {
                    clients.remove(out);
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