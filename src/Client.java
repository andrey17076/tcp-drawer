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
import java.net.UnknownHostException;

public class Client extends Application {

    private static TextArea log;
    private static Canvas canvas;
    private static ColorPicker colorPicker;

    private static Socket socket;
    private static OutputStream socketOutStream;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Drawer");

        //Color Picker
        colorPicker = new ColorPicker();
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
                    putLog(portField.getText(), "Incorrect port");
                }
            } catch (UnknownHostException e) {
                putLog(ipField.getText(), "Unknown hostname");
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
        logPane.setMinHeight(210);
        logPane.setText("Log");
        logPane.setCollapsible(false);
        log = new TextArea();
        logPane.setContent(log);

        //Left Panel of Main Layout
        VBox controlPanel = new VBox();
        controlPanel.setSpacing(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.getChildren().addAll(colorPicker, socketInfo, logPane);

        //Canvas
        canvas = new Canvas();
        canvas.setHeight(450);
        canvas.setWidth(450);
        canvas.setCursor(Cursor.CROSSHAIR);
        canvas.getGraphicsContext2D().setLineWidth(5);
        canvas.getGraphicsContext2D().setFill(Color.WHITE);
        canvas.setDisable(false);
        canvas.setOnMousePressed(event -> {
            sendPointInfo();
            canvas.setOnMouseDragged(event1 -> sendPointInfo(event1.getX(), event1.getY()));
        });

        //Main Layout of Scene
        SplitPane mainLayout = new SplitPane();
        mainLayout.setDividerPositions(Region.USE_COMPUTED_SIZE);
        mainLayout.getItems().addAll(controlPanel, canvas);

        //Main Scene
        Scene scene = new Scene(mainLayout, 700, 450);
        primaryStage.setScene(scene);
        primaryStage.setMaxWidth(700);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(450);
        primaryStage.setMaxHeight(450);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> System.exit(0));
    }

    public static void sendPointInfo() {
        byte[] buf = new byte[7];
        buf[4] = 127;

        try {
            socketOutStream.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendPointInfo(double x, double y) {
        byte[] buf = new byte[7];
        int intX = (int) x;
        int intY = (int) y;

        buf[0] = (byte) (intX >> 8);
        buf[1] = (byte) (intX & 0xFF);
        buf[2] = (byte) (intY >> 8);
        buf[3] = (byte) (intY & 0xFF);
        buf[4] = (byte) (100 * colorPicker.getValue().getRed());
        buf[5] = (byte) (100 * colorPicker.getValue().getGreen());
        buf[6] = (byte) (100 * colorPicker.getValue().getBlue());

        try {
            socketOutStream.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void putLog(String title, String buf) {
        log.appendText(title + ": " + buf + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class Handler extends Thread {

        private static InputStream socketInStream;

        public Handler(InetAddress address, int port) {
            try {
                socket = new Socket(address, port);
                socketInStream = socket.getInputStream();
                socketOutStream = socket.getOutputStream();
                putLog("Client", "Connected");
            } catch (IOException e) {
                putLog(address.getHostAddress() + ":" + Integer.toString(port), "Can't connect");
            }
        }

        public void run() {
            int prevX = 0, prevY = 0, prevLineFlag = 127;
            while (!this.isInterrupted()) {
                try {
                    byte[] buf = new byte[7];
                    int length = socketInStream.read(buf);

                    if (length == 0)
                        return;

                    int x = ((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF);
                    int y = ((buf[2] & 0xFF) << 8) | (buf[3] & 0xFF);

                    if (buf[4] != 127) {
                        if (prevLineFlag != 127) {
                            Color color = new Color(
                                    (double) buf[4] / 100,
                                    (double) buf[5] / 100,
                                    (double) buf[6] / 100,
                                    1);
                            if (x < 450 && y < 450 && prevX < 450 && prevY < 450) {
                                canvas.getGraphicsContext2D().setStroke(color);
                                canvas.getGraphicsContext2D().strokeLine(prevX, prevY, x, y);
                            }
                        }
                    }

                    prevX = x;
                    prevY = y;
                    prevLineFlag = buf[4];
                } catch (IOException e) {
                    putLog("Client", "Disconnected from the server");
                    this.interrupt();
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        putLog("Client", "Can't close socket");
                    }
                }
            }
        }
    }
}

