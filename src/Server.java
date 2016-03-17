import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;

public class Server {

    private static HashSet<OutputStream> clients = new HashSet<>();

    public static void main(String[] args) throws IOException, NumberFormatException {
        int port = Integer.parseInt(args[0]);
        ServerSocket listener = new ServerSocket(port);
        System.out.println("Started!");
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
        private InputStream in;
        private OutputStream out;

        public Handler(Socket socket) {
            this.socket = socket;
            System.out.println(socket.getInetAddress().toString());
        }

        public void run() {
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();

                clients.add(out);

                while (true) {
                    byte[] message = new byte[4];
                    int length = in.read(message);

                    if (length == 0) return;

                    System.out.print("From: " + socket.getInetAddress().getHostAddress() + ": ");

                    if (message[0] >= 0) {
                        int x = ((message[0] & 0xFF) << 8) + (message[1] & 0xFF);
                        int y = ((message[2] & 0xFF) << 8) + (message[3] & 0xFF);
                        System.out.println(x + " " + y);
                    } else {
                        System.out.println(Arrays.toString(message));
                    }

                    for (OutputStream client : clients) {
                        client.write(message);
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