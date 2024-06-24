import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private ServerSocket serverSocket;
    private int numPlayers;
    private int maxPlayers;

    private Socket p1Socket;
    private Socket p2Socket;
    private ReadFromClient p1ReadRunnable;
    private ReadFromClient p2ReadRunnable;
    private WriteToClient p1WriteRunnable;
    private WriteToClient p2WriteRunnable;
    private double p1x, p1y, p2x, p2y;

    public GameServer() {
        System.out.println("=====GAME SERVER======");
        numPlayers = 0;
        maxPlayers = 2;

        p1x = 100;
        p1y = 400;
        p2x = 490;
        p2y = 400;

        try {
            serverSocket = new ServerSocket(45371);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptConections(){
        try {
            System.out.println("waiting for conections");

            while (numPlayers< maxPlayers){
                Socket s = serverSocket.accept();

                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                numPlayers++;
                out.writeInt(numPlayers);
                System.out.println("Player #" + numPlayers + " has connected");

                ReadFromClient rfc = new ReadFromClient(numPlayers, in);
                WriteToClient wtc = new WriteToClient(numPlayers, out);

                if (numPlayers == 1){
                    p1Socket = s;
                    p1ReadRunnable = rfc;
                    p1WriteRunnable = wtc;
                }else {
                    p2Socket = s;
                    p2ReadRunnable = rfc;
                    p2WriteRunnable = wtc;
                    p1WriteRunnable.sendStartMsg();
                    p2WriteRunnable.sendStartMsg();
                    Thread readThread1 = new Thread(p1ReadRunnable);
                    Thread readThread2 = new Thread(p2ReadRunnable);
                    readThread1.start();
                    readThread2.start();
                    Thread writeThread1 = new Thread(p1WriteRunnable);
                    Thread writeThread2 = new Thread(p1WriteRunnable);
                    writeThread1.start();
                    writeThread2.start();
                }

            }
            System.out.println("No longer accepting conections");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private class ReadFromClient implements Runnable{
        private int playerId;
        private DataInputStream dataIn;

        public ReadFromClient(int pId, DataInputStream in){
            playerId = pId;
            dataIn = in;
            System.out.println("RFC " + playerId + " Runnable created");
        }

        @Override
        public void run() {
            try{
                while (true){
                    if (playerId == 1){
                        p1x = dataIn.readDouble();
                        p1y = dataIn.readDouble();
                    }else {
                        p2x = dataIn.readDouble();
                        p2y = dataIn.readDouble();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private class WriteToClient implements Runnable{
        private int playerId;
        private DataOutputStream dataOut;

        public WriteToClient(int pId, DataOutputStream out){
            playerId = pId;
            dataOut = out;
            System.out.println("WTC " + playerId + " Runnable created");
        }

        @Override
        public void run() {
            try {
                while (true){
                    if (playerId == 1){
                        dataOut.writeDouble(p2x);
                        dataOut.writeDouble(p2y);
                        dataOut.flush();
                    }else {
                        dataOut.writeDouble(p1x);
                        dataOut.writeDouble(p1y);
                        dataOut.flush();
                    }
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
        public void sendStartMsg(){
            try {
                dataOut.writeUTF("We have 2 players go!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.acceptConections();
    }
}
