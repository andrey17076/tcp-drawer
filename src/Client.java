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

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Client extends Application {

    private static TextArea log;
    private static Socket socket;
    private static InputStream in;
    private static OutputStream out;

    public Client() throws SocketException{
        log = new TextArea();
    }

    @Override
    public void start(Stage primaryStage) {
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
        Button connectButton = new Button("Open");
        connectButton.setOnAction(event -> {
            try {
                InetAddress address = InetAddress.getByName(ipField.getText());
                try {
                    int port = Integer.parseInt(portField.getText());
                    new Handler(address, port).start();
                } catch (NumberFormatException e) {
                    log.appendText(portField.getText() + ": " + "Incorrect port\n");
                }
            } catch (UnknownHostException e) {
                log.appendText(ipField.getText() + ": " + "Unknown hostname\n");
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
        canvas.setCursor(Cursor.CROSSHAIR);
        canvas.setDisable(false);
        canvas.setOnMouseDragged(event -> {
            byte[] message = new byte[4];
            int x = (int) event.getX();
            int y = (int) event.getY();

            message[0] = (byte) (x >> 8);
            message[1] = (byte) (x & 0xFF);

            message[2] = (byte) (y >> 8);
            message[3] = (byte) (y & 0xFF);

            x = ((message[0] & 0xFF) << 8) + (message[1] & 0xFF);
            y = ((message[2] & 0xFF) << 8) + (message[3] & 0xFF);
            //log.appendText(Arrays.toString(message) + "\n");

            try {
                out.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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
        primaryStage.setOnCloseRequest(event -> {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class Handler extends Thread{

        public Handler(InetAddress address, int port) {
            try {
                socket = new Socket(address, port);
                in = socket.getInputStream();
                out = socket.getOutputStream();
                log.appendText("Connected!\n");
            } catch (IOException e) {
                log.appendText(address.getHostAddress() + ":" + Integer.toString(port) + ": " + "Can't connect\n");
            }
        }

        public void run() {
            try {
                while (true) {
                    try {
                        byte[] message = new byte[4];
                        int length = in.read(message);

                        if (length == 0) return;

                        if (length == 4) {
                            int x = ((message[0] & 0xFF) << 8) + (message[1] & 0xFF);
                            int y = ((message[2] & 0xFF) << 8) + (message[3] & 0xFF);
                            log.appendText(x + " " + y + "\n");
                        } else {
                            log.appendText(ByteBuffer.wrap(message, 0, length).toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

