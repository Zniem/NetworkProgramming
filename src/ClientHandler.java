import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            synchronized (ChatServer.clientWriters) {
                ChatServer.clientWriters.add(out);
            }

            // Send chat logs to the new client
            synchronized (ChatServer.chatLogs) {
                for (String log : ChatServer.chatLogs) {
                    out.println(log);
                }
            }

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message);
                // Example regex replacement for demonstration
                Pattern pattern = Pattern.compile("(f|s)+u+c+k+", Pattern.CASE_INSENSITIVE);
                Matcher f_matcher = pattern.matcher(message);
                if (f_matcher.find()) {
                    message = f_matcher.replaceAll("duck");
                }

                synchronized (ChatServer.chatLogs) {
                    ChatServer.chatLogs.add(message);
                }
                synchronized (ChatServer.clientWriters) {
                    for (PrintWriter writer : ChatServer.clientWriters) {
                        writer.println(message);
                    }
                }
            }
        } catch (IOException e) {
            // Client disconnected or encountered an error
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (ChatServer.clientWriters) {
                ChatServer.clientWriters.remove(out);
            }
        }
    }
}
