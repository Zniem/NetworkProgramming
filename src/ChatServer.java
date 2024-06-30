import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    public static Set<PrintWriter> clientWriters = new HashSet<>();
    public static List<String> chatLogs = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Chat server started...");
        ServerSocket serverSocket = new ServerSocket(PORT);
        try {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } finally {
            serverSocket.close();
        }
    }


}
