package com.game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TicTacToe implements Runnable {

    protected String ip = "localhost";
    protected  int port= 22222;
    protected Scanner scanner = new Scanner(System.in);
    protected JFrame frame;
    protected final int  WIDTH = 506;
    protected final int HEIGHT = 527;
    protected Thread thread;
    protected int count = 0;

    protected Painter painter;
    protected Socket socket;
    protected DataOutputStream dos;
    protected DataInputStream dis;

    protected ServerSocket serverSocket;

    //rendering all images we need
    protected BufferedImage board;
    protected BufferedImage redX;
    protected BufferedImage blueX;
    protected BufferedImage redCircle;
    protected BufferedImage blueCircle;

    protected   String[] spaces = new String[9];

    protected boolean yourTurn = false;
    protected boolean circle = true;
    protected boolean accepted = false;
    protected boolean unableCommunicationWithOponent = false;
    protected boolean won = false;
    protected boolean enemyWon = false;
    protected boolean tie = false;

    //length of squares
    protected int lengthOfSpace = 160;
    //if many errors accur unableCommunicationWithoponen will set to true and throw an error
    protected int errors = 0;
    //start and end for rendering the final message
    protected int firstSpot = -1;
    protected int secondSpot = -1;

    protected Font font = new Font("Verdana", Font.BOLD, 32);
    protected Font smallerfont =new Font("Verdana", Font.BOLD, 20);
    protected Font largerfont =new Font("Verdana", Font.BOLD, 50);

    protected String waitingString = "Waiting for another player.";
    protected String unnableToCommunicateWithOpponentString = "Unnable to communicate with opponent!";
    protected String wonString = "You win!";
    protected String enemyWonString = "Enemy wins!";
    protected String tieString = "TIE";

    protected int[][] wins = new int[][] { { 0, 1, 2 },
            { 3, 4, 5 },
            { 6, 7, 8 },
            { 0, 3, 6 },
            { 1, 4, 7 },
            { 2, 5, 8 },
            { 0, 4, 8 },
            { 2, 4, 6 } };

    public TicTacToe(){
        System.out.println("Please enter an IP : ");
        ip = scanner.nextLine();
        System.out.println("Please enter a port : ");
        port = scanner.nextInt();
        //port check
        while(port <1 || port > 65535)
        {
            System.out.println("Invalid port! Please, try again: ");
            port = scanner.nextInt();
        }

        loadImages();

        painter = new Painter();
        painter.setPreferredSize(new Dimension(WIDTH,HEIGHT));

        if (!connect()) initializeServer();

        frame = new JFrame();
        frame.setTitle("Tic-Tac-Toe");
        frame.setContentPane(painter);
        frame.setSize(WIDTH,HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);

        thread = new Thread(this, "Tic-Tac-Toe");
        thread.start();

    }

    @Override
    public void run() {
        while (true){
            tick();
            painter.repaint();

            if (!circle && !accepted) {
                listenForServerRequest();
            }
        }

    }

    protected void render(Graphics g){

        g.drawImage(this.board, 0, 0, null);

        if (unableCommunicationWithOponent){
            g.setColor(Color.red);
            g.setFont(smallerfont);
            Graphics2D g2 = (Graphics2D) g;
            //smoothing text
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            //center the message using its length and pixelisation
            int stringWidth = g2.getFontMetrics().stringWidth(unnableToCommunicateWithOpponentString);
            //drawing the string perfectly in the center
            g.drawString(unnableToCommunicateWithOpponentString, WIDTH/2 - stringWidth/2, HEIGHT/2);
            return;
        }

        if (accepted){
            for (int i = 0; i <spaces.length; i++){
                if (spaces[i] != null){
                    if (spaces[i].equals("X")){
                        if (circle){
                            //rendering it in the correct box on the screen
                            //10 is length of the borders
                            //lengthOfSpace is pixels per box
                            //by x %
                            //by y / and round down (thats why we use (int))
                            g.drawImage(redX, (i % 3) * lengthOfSpace + 10 * (i % 3),
                                    (int) (i/3) * lengthOfSpace + 10 * (int) (i / 3), null);
                        }
                        else{
                            g.drawImage(blueX, (i % 3) * lengthOfSpace + 10 * (i % 3),
                                    (int) (i/3) * lengthOfSpace + 10 * (int) (i / 3), null);
                        }
                    }

                    if (spaces[i].equals("O")){
                        if (circle){
                            g.drawImage(blueCircle, (i % 3) * lengthOfSpace + 10 * (i % 3),
                                    (int) (i/3) * lengthOfSpace + 10 * (int) (i / 3), null);
                        }
                        else{
                            g.drawImage(redCircle, (i % 3) * lengthOfSpace + 10 * (i % 3),
                                    (int) (i/3) * lengthOfSpace + 10 * (int) (i / 3), null);
                        }
                    }
                }
            }

            if (won || enemyWon){
                Graphics2D g2 = (Graphics2D) g;
                //width of the line for winning
                g2.setStroke(new BasicStroke(10));
                g.setColor(Color.BLACK);

                //x and y spots for every end
                g.drawLine(firstSpot % 3 * lengthOfSpace + 10 * firstSpot % 3 + lengthOfSpace / 2,
                        (int) (firstSpot / 3) * lengthOfSpace + 10 * (int) (firstSpot / 3) + lengthOfSpace / 2,
                        secondSpot % 3 * lengthOfSpace + 10 * secondSpot % 3 + lengthOfSpace / 2,
                        (int) (secondSpot / 3) * lengthOfSpace + 10 * (int) (secondSpot / 3) + lengthOfSpace / 2);
                g.setColor(Color.red);
                g.setFont(largerfont);

                if (won){
                    int stringWidth = g2.getFontMetrics().stringWidth(wonString);
                    g.drawString(wonString, WIDTH/2 - stringWidth/2, HEIGHT/2);
                }
                else if (enemyWon){
                    int stringWidth = g2.getFontMetrics().stringWidth(enemyWonString);
                    g.drawString(enemyWonString, WIDTH/2 - stringWidth/2, HEIGHT/2);
                }
            }

            if (tie) {
                Graphics2D g2 = (Graphics2D) g;
                g.setColor(Color.BLACK);
                g.setFont(largerfont);
                int stringWidth = g2.getFontMetrics().stringWidth(tieString);
                g.drawString(tieString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
            }


        }else{
            g.setColor(Color.RED);
            g.setFont(font);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int stringWidth = g2.getFontMetrics().stringWidth(waitingString);
            g.drawString(waitingString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);

        }
    }

    private void tick(){

        if(errors >= 10) unableCommunicationWithOponent = true;

        if (!yourTurn && !unableCommunicationWithOponent){
            try{
                int space = dis.readInt();

                if (circle) spaces[space] = "X";
                else spaces[space] = "O";
                checkForEnemyWin();
                checkForTie();
                yourTurn = true;
            }catch (IOException e){
                e.printStackTrace();
                errors++;
            }
        }
    }

    private  void checkForWin(){
        for (int i = 0 ; i < wins.length; i++){
            if (circle){
                if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O"){
                    firstSpot = wins[i][0];
                    secondSpot = wins[i][2];
                    won = true;
                }
            } else{
                if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X"){
                    firstSpot = wins[i][0];
                    secondSpot = wins[i][2];
                    won = true;
                }
            }
        }
    }

    private void checkForEnemyWin(){
        for (int i = 0 ; i < wins.length; i++){
            if (circle){
                if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X"){
                    firstSpot = wins[i][0];
                    secondSpot = wins[i][2];
                    enemyWon = true;
                }
            } else{
                if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O"){
                    firstSpot = wins[i][0];
                    secondSpot = wins[i][2];
                    enemyWon = true;
                }
            }
        }
    }

    private void checkForTie(){
        //if every spot is filled
        if (!won && !enemyWon){
            for (int i = 0; i< spaces.length; i++){
                if (spaces[i] == null) {
                    return;
                }
            }
            tie = true;
        }

    }

    private  void listenForServerRequest(){
        Socket socket = null;
        try{
            //accept method wiil block the game until we have another player to play with
            //or until we receive server socket connection to begin playing
            socket = serverSocket.accept();
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            accepted = true;
            System.out.println("Client requested joining. Joining acepted!");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private  boolean connect(){
        try{
            if (count <= 2)
            {
                socket = new Socket(ip, port);
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
                accepted = true;
                count++;
            }

        }catch (IOException e){
            System.out.println("Unable to connect to the address: " + ip + ":" + port + " | Starting a server");
            return false;
        }
        System.out.println("Successfully connected to the server.");
        return true;
    }

    private void initializeServer(){
        try{
            serverSocket = new ServerSocket(port, 8 , InetAddress.getByName(ip));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        yourTurn = true;
        circle = false;
    }

    private void loadImages(){

        try{
            board = ImageIO.read(getClass().getResourceAsStream("/board.png"));
            redX = ImageIO.read(getClass().getResourceAsStream("/redX.png"));
            redCircle = ImageIO.read(getClass().getResourceAsStream("/redO.png"));
            blueX = ImageIO.read(getClass().getResourceAsStream("/blueX.png"));
            blueCircle = ImageIO.read(getClass().getResourceAsStream("/blueO.png"));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        TicTacToe ticTacToe = new TicTacToe();
    }

    private class Painter extends JPanel implements MouseListener {

        private  static final long serialversionUID = 1L;

        public Painter() {
            setFocusable(true);
            requestFocus();
            setBackground(Color.getHSBColor(25, 25, 100));
            addMouseListener(this);
        }

        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            render(g);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (accepted) {
                if (yourTurn && !unableCommunicationWithOponent && !won && !enemyWon) {
                    int x = e.getX() / lengthOfSpace;
                    int y = e.getY() / lengthOfSpace;
                    y *= 3;
                    int position = x + y;

                    if (spaces[position] == null) {
                        if (!circle) spaces[position] = "X";
                        else spaces[position] = "O";
                        yourTurn = false;
                        repaint();
                        Toolkit.getDefaultToolkit().sync();

                        try {
                            dos.writeInt(position);
                            dos.flush();
                        } catch (IOException e1) {
                            errors++;
                            e1.printStackTrace();
                        }

                        System.out.println("Data was sent!");
                        checkForWin();
                        checkForTie();

                    }
                }
            }

        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

}
