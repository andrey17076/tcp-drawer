import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Application {

    protected static PrintWriter out;
    protected static TextArea log;
    protected static InetAddress address;
    protected static int port;


    public Client() throws IOException{
        log = new TextArea();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Drawer");

        //Color Picker
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setMinHeight(27);
        colorPicker.setValue(Color.valueOf("Black"));

        //Text Field for IP address
        TextField ipField = new TextField();
        ipField.setPromptText("IP");

        //Text Field for PORT
        TextField portField = new TextField();
        portField.setPromptText("PORT");

        //Confirm button
        Button connectButton = new Button("Connect");
        connectButton.setOnAction(event -> {
            try {
                address = InetAddress.getByName(ipField.getText());
                port = Integer.parseInt(portField.getText());
                try {
                    Socket socket = new Socket(address, port);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    new Handler(socket).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        //Socket VBox
        VBox socketInfoLayout = new VBox();
        socketInfoLayout.setSpacing(10);
        socketInfoLayout.setPadding(new Insets(10));
        socketInfoLayout.getChildren().addAll(ipField, portField, connectButton);

        //Socket Info Pane
        TitledPane socketInfo = new TitledPane();
        socketInfo.setText("Socket Info");
        socketInfo.setCollapsible(false);
        socketInfo.setContent(socketInfoLayout);

        //Log Pane
        TitledPane logPane = new TitledPane();
        logPane.setMinHeight(360);
        logPane.setText("Log");
        logPane.setCollapsible(false);
        logPane.setContent(log);

        //Left Panel of Main Layout
        VBox controlPanel = new VBox();
        controlPanel.setSpacing(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.getChildren().addAll(colorPicker, socketInfo, logPane);

        //Canvas
        Canvas canvas = new Canvas();
        canvas.setHeight(600);
        canvas.setWidth(600);
        canvas.getGraphicsContext2D().fill();
        canvas.setCursor(Cursor.CROSSHAIR);

        //Main Layout of Scene
        SplitPane mainLayout = new SplitPane();
        mainLayout.setDividerPositions(Region.USE_COMPUTED_SIZE);
        mainLayout.getItems().addAll(controlPanel, canvas);

        //Main Scene
        Scene scene = new Scene(mainLayout, 850, 600);
        primaryStage.setScene(scene);
        primaryStage.setMaxWidth(850);
        primaryStage.setMinWidth(850);
        primaryStage.setMinHeight(600);
        primaryStage.setMaxHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    private static class Handler extends Thread{

        protected static BufferedReader in;

        public Handler(Socket socket) throws IOException{
            out.println("Hello!");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            log.appendText("Ready\n");
        }

        public void run() {
            while (true) {
                try {
                    String input = in.readLine();
                    log.appendText(input + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

