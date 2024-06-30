import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static JTextArea messageArea;
    private static JTextField textField;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Socket socket;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chat Client");
        messageArea = new JTextArea(20, 50);
        textField = new JTextField(50);
        messageArea.setEditable(false);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (out != null) {
                    out.println(textField.getText());
                    textField.setText("");
                }
            }
        });

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
}
