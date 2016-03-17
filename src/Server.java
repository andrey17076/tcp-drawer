import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class Server {

    private static HashSet<OutputStream> clients = new HashSet<>();

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            try {
                ServerSocket listener = new ServerSocket(port);
                System.out.println("Started!");
                while (true)
                    new Handler(listener.accept()).start();
            } catch (IOException e) {
                System.out.println("Can't run server");
            }
        } catch (NumberFormatException e) {
            System.out.println("Incorrect port");
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private InputStream socketInStream;
        private OutputStream socketOutStream;

        public Handler(Socket socket) {
            this.socket = socket;
            System.out.println(socket.getInetAddress().toString());
        }

        public void run() {
            try {
                socketInStream = socket.getInputStream();
                socketOutStream = socket.getOutputStream();

                clients.add(socketOutStream);

                while (!this.isInterrupted()) {
                    byte[] buf = new byte[4];
                    int length = socketInStream.read(buf);

                    if (length == 0)
                        return;

                    System.out.print("From: " + socket.getInetAddress().getHostAddress() + ": ");

                    int x = ((buf[0] & 0xFF) << 8) + (buf[1] & 0xFF);
                    int y = ((buf[2] & 0xFF) << 8) + (buf[3] & 0xFF);

                    if (x != 65535) {
                        System.out.println(x + " " + y);
                    } else {
                        System.out.println("New line");
                    }

                    for (OutputStream client : clients) {
                        client.write(buf);
                    }
                }
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } finally {
                if (socketOutStream != null) {
                    clients.remove(socketOutStream);
                }
            }
        }
    }
}