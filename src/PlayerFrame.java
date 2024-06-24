import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PlayerFrame extends JFrame implements KeyListener {

    private int width, height;
    private Container contentPane;
    private List<PlayerSprite> players;
    private PlayerSprite me;
    private DrawingComponent dc;
    private Timer animationTimer;
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private Socket socket;
    private int playerId;
    private ReadFromServer rfsRunnable;
    private WriteToServer wtsRunnable;
    private int maxPlayers;

    public PlayerFrame(int w, int h, int maxPlayers) {
        width = w;
        height = h;
        this.maxPlayers = maxPlayers;
        players = new ArrayList<>();
        up = false;
        down = false;
        left = false;
        right = false;
        addKeyListener(this);
    }

    public void setUpGUI() {
        contentPane = this.getContentPane();
        this.setTitle("Player #" + playerId);
        contentPane.setPreferredSize(new Dimension(width, height));
        createSprites();
        dc = new DrawingComponent();
        contentPane.add(dc);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);

        setUpAnimationTimer();

        contentPane.setFocusable(true);
    }

    private void createSprites() {
        for (int i = 0; i < maxPlayers; i++) {
            if (i + 1 == playerId) {
                me = new PlayerSprite(100 + i * 100, 400, 50, Color.BLUE);
                players.add(me);
            } else {
                players.add(new PlayerSprite(100 + i * 100, 400, 50, Color.RED));
            }
        }
    }

    private void setUpAnimationTimer() {
        int interval = 10;
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double speed = 5;
                if (up) {
                    me.moveV(-speed);
                }
                if (down) {
                    me.moveV(speed);
                }
                if (left) {
                    me.moveH(-speed);
                }
                if (right) {
                    me.moveH(speed);
                }
                dc.repaint();
            }
        };
        animationTimer = new Timer(interval, al);
        animationTimer.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) {
            up = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_A) {
            left = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_D) {
            right = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            down = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W) {
            up = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_A) {
            left = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_D) {
            right = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            down = false;
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 45371);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            playerId = in.readInt();
            System.out.println("You are player #" + playerId);
            if (playerId == 1) {
                System.out.println("waiting for other players to connect.......");
            }
            rfsRunnable = new ReadFromServer(in);
            wtsRunnable = new WriteToServer(out);
            rfsRunnable.waitForStartMsg();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class DrawingComponent extends JComponent {
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            for (PlayerSprite player : players) {
                player.drawSprite(g2d);
            }
        }
    }

    private class ReadFromServer implements Runnable {
        private DataInputStream dataIn;

        public ReadFromServer(DataInputStream in) {
            this.dataIn = in;
            System.out.println("RFS RUNNABLE CREATED");
        }

        @Override
        public void run() {
            try {
                while (true) {
                    for (int i = 0; i < maxPlayers; i++) {
                        double x = dataIn.readDouble();
                        double y = dataIn.readDouble();
                        if (i + 1 != playerId) {
                            players.get(i).setX(x);
                            players.get(i).setY(y);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void waitForStartMsg() {
            try {
                String startMsg = dataIn.readUTF();
                System.out.println("Message from server: " + startMsg);
                Thread readThread = new Thread(rfsRunnable);
                Thread writeThread = new Thread(wtsRunnable);
                readThread.start();
                writeThread.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class WriteToServer implements Runnable {
        private DataOutputStream dataOut;

        public WriteToServer(DataOutputStream out) {
            this.dataOut = out;
            System.out.println("WTS RUNNABLE CREATED");
        }

        @Override
        public void run() {
            try {
                while (true) {
                    dataOut.writeDouble(me.getX());
                    dataOut.writeDouble(me.getY());
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
    }

    public static void main(String[] args) {
        PlayerFrame pf = new PlayerFrame(640, 480, 3);
        pf.connectToServer();
        pf.setUpGUI();
    }
}
