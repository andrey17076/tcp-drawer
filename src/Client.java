import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Application {

    protected static PrintWriter out;
    protected static TextArea log;

    public Client() throws IOException{
        Socket socket = new Socket("10.211.55.21", 5555);
        out = new PrintWriter(socket.getOutputStream(), true);
        log = new TextArea();
        new Handler(socket).start();
    }

    @Override
    public void start(Stage primaryStage) throws IOException{
        Button button = new Button("Click me");
        button.setOnAction(event -> out.println("Clicked"));

        VBox layout = new VBox();
        layout.setSpacing(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(button, log);

        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    private static class Handler extends Thread{

        protected static BufferedReader in;

        public Handler(Socket socket) throws IOException{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            log.appendText("Ready\n");
        }

        public void run() {
            while (true) {
                try {
                    String input = in.readLine();
                    System.out.println(input);
                    log.appendText(input + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

