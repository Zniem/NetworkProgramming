import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.*;
import java.net.*;

public class ChatClient extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static JTextArea messageArea;
    private static JTextField textField;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Socket socket;

    private PrintWriter out;
    private BufferedReader in;

    private TextArea messageArea;
    private TextArea inputArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat Client");

        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);

        inputArea = new TextArea();
        inputArea.setPrefRowCount(3);
        inputArea.setWrapText(true);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());

        inputArea.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER:
                    if (e.isShiftDown()) {
                        inputArea.appendText("\n");
                    } else {
                        sendMessage();
                        e.consume();
                    }
                    break;
                default:
                    break;
            }
        });

        HBox inputBox = new HBox(10, inputArea, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));
        inputBox.setPrefHeight(70); // Combined height for input area and button

        VBox root = new VBox(10, messageArea, inputBox);
        root.setPadding(new Insets(10));
        VBox.setVgrow(messageArea, Priority.ALWAYS);
        VBox.setVgrow(inputBox, Priority.NEVER);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        startConnection();
    }

    private void startConnection() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        final String msg = message;
                        Platform.runLater(() -> messageArea.appendText(msg + "\n"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        // Start the connection attempt thread
        new Thread(() -> {
            while (true) {
                try {
                    connectToServer();
                    listenForMessages();
                } catch (IOException e) {
                    messageArea.append("Failed to connect. Retrying in 5 seconds...\n");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }

    private static void connectToServer() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        messageArea.append("Connected to the server.\n");
    }

    private static void listenForMessages() throws IOException {
        String message;
        while ((message = in.readLine()) != null) {
            messageArea.append(message + "\n");
        }
        throw new IOException("Connection lost.");
    }

    private void sendMessage() {
        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            inputArea.clear();
        }
    }
}
