import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    private ServerSocket serverSocket;
    private int numPlayers;
    private int maxPlayers;

    private List<Socket> playerSockets;
    private List<ReadFromClient> readRunnables;
    private List<WriteToClient> writeRunnables;
    private List<Double> playerX;
    private List<Double> playerY;

    public GameServer() {
        System.out.println("=====GAME SERVER======");
        numPlayers = 0;
        maxPlayers = 2; // Set maximum number of players here

        playerSockets = new ArrayList<>();
        readRunnables = new ArrayList<>();
        writeRunnables = new ArrayList<>();
        playerX = new ArrayList<>();
        playerY = new ArrayList<>();

        // Initialize player coordinates
        for (int i = 0; i < maxPlayers; i++) {
            playerX.add(100.0 + i * 100); // Just an example initialization
            playerY.add(400.0);
        }

        try {
            serverSocket = new ServerSocket(45371);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("waiting for connections");

            while (numPlayers < maxPlayers) {
                Socket s = serverSocket.accept();

                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                numPlayers++;
                out.writeInt(numPlayers);
                System.out.println("Player #" + numPlayers + " has connected");

                ReadFromClient rfc = new ReadFromClient(numPlayers, in);
                WriteToClient wtc = new WriteToClient(numPlayers, out);

                playerSockets.add(s);
                readRunnables.add(rfc);
                writeRunnables.add(wtc);

                Thread readThread = new Thread(rfc);
                readThread.start();

                if (numPlayers == maxPlayers) {
                    for (WriteToClient writeRunnable : writeRunnables) {
                        writeRunnable.sendStartMsg();
                        Thread writeThread = new Thread(writeRunnable);
                        writeThread.start();
                    }
                }
            }
            System.out.println("No longer accepting connections");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ReadFromClient implements Runnable {
        private int playerId;
        private DataInputStream dataIn;

        public ReadFromClient(int pId, DataInputStream in) {
            playerId = pId;
            dataIn = in;
            System.out.println("RFC " + playerId + " Runnable created");
        }

        @Override
        public void run() {
            try {
                while (true) {
                    playerX.set(playerId - 1, dataIn.readDouble());
                    playerY.set(playerId - 1, dataIn.readDouble());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class WriteToClient implements Runnable {
        private int playerId;
        private DataOutputStream dataOut;

        public WriteToClient(int pId, DataOutputStream out) {
            playerId = pId;
            dataOut = out;
            System.out.println("WTC " + playerId + " Runnable created");
        }

        @Override
        public void run() {
            try {
                while (true) {
                    for (int i = 0; i < numPlayers; i++) {
                        if (i != playerId - 1) {
                            if (playerX.get(i) > 300.0)
                                playerX.set(i,300.0);
                            else if (playerX.get(i) < 0)
                                playerX.set(i,0.0);
                            if (playerY.get(i) > 300.0)
                                playerX.set(i,300.0);
                            else if (playerY.get(i) < 0)
                                playerY.set(i,0.0);
                            dataOut.writeDouble(playerX.get(i));
                            dataOut.writeDouble(playerY.get(i));
                        }
                    }
                    dataOut.flush();
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void sendStartMsg() {
            try {
                dataOut.writeUTF("We have " + maxPlayers + " players go!");
                dataOut.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.acceptConnections();
    }
}
