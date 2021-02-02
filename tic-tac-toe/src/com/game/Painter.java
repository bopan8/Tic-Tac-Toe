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

public class Painter extends JPanel implements MouseListener {

    protected final int  WIDTH = 506;
    protected final int HEIGHT = 527;

    public DataOutputStream dos;
    public DataInputStream dis;
    private Checks checks;
    //rendering all images we need
    protected BufferedImage board;
    protected BufferedImage redX;
    protected BufferedImage blueX;
    protected BufferedImage redCircle;
    protected BufferedImage blueCircle;

    protected String[] spaces = new String[9];

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

    protected Font font = new Font("Verdana", Font.BOLD, 32);
    protected Font smallerfont =new Font("Verdana", Font.BOLD, 20);
    protected Font largerfont =new Font("Verdana", Font.BOLD, 50);

    protected String waitingString = "Waiting for another player.";
    protected String unableToCommunicateWithOpponentString = "Unable to communicate with opponent!";
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

    public Painter() {
        checks = new Checks();
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
                        this.errors +=1;
                        e1.printStackTrace();
                    }

                    System.out.println("Data was sent!");
                    won = checks.checkForWin(circle, spaces,  wins, won);
                    tie = checks.checkForTie(won, enemyWon, tie,spaces);

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
    void loadImages(){

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
    protected void render(Graphics g){

        g.drawImage(this.board, 0, 0, null);

        if (unableCommunicationWithOponent){
            g.setColor(Color.red);
            g.setFont(smallerfont);
            Graphics2D g2 = (Graphics2D) g;
            //smoothing text
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            //center the message using its length and pixelisation
            int stringWidth = g2.getFontMetrics().stringWidth(unableToCommunicateWithOpponentString);
            //drawing the string perfectly in the center
            g.drawString(unableToCommunicateWithOpponentString, WIDTH/2 - stringWidth/2, HEIGHT/2);
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
                g.drawLine(checks.getFistSpot() % 3 * lengthOfSpace + 10 * checks.getFistSpot() % 3 + lengthOfSpace / 2,
                        (int) (checks.getFistSpot() / 3) * lengthOfSpace + 10 * (int) (checks.getFistSpot() / 3) + lengthOfSpace / 2,
                        checks.getSecondSpot() % 3 * lengthOfSpace + 10 * checks.getSecondSpot() % 3 + lengthOfSpace / 2,
                        (int) (checks.getSecondSpot() / 3) * lengthOfSpace + 10 * (int) (checks.getSecondSpot() / 3) + lengthOfSpace / 2);
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


        }else {
            g.setColor(Color.RED);
            g.setFont(font);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int stringWidth = g2.getFontMetrics().stringWidth(waitingString);
            g.drawString(waitingString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
//
        }
    }
    void tick(){

        if(errors >= 10) unableCommunicationWithOponent = true;

        if (!yourTurn && !unableCommunicationWithOponent){
            try{
                int space = dis.readInt();

                if (circle) spaces[space] = "X";
                else spaces[space] = "O";
                enemyWon = checks.checkForEnemyWin(circle, spaces,  wins,enemyWon);
                tie = checks.checkForTie(won, enemyWon, tie,spaces);
                yourTurn = true;
            }catch (IOException e){
                e.printStackTrace();
                errors++;
            }
        }
    }
    public DataInputStream getDis() {
        return dis;
    }

    public void setDis(DataInputStream dis) {
        this.dis = dis;
    }

  //protected void checkForWin(){
  //    for (int i = 0 ; i < wins.length; i++){
  //        if (circle){
  //            if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O"){
  //                firstSpot = wins[i][0];
  //                secondSpot = wins[i][2];
  //                won = true;
  //            }
  //        } else{
  //            if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X"){
  //                firstSpot = wins[i][0];
  //                secondSpot = wins[i][2];
  //                won = true;
  //            }
  //        }
  //    }
  //}

  //protected void checkForEnemyWin(){
  //    for (int i = 0 ; i < wins.length; i++){
  //        if (circle){
  //            if (spaces[wins[i][0]] == "X" && spaces[wins[i][1]] == "X" && spaces[wins[i][2]] == "X"){
  //                firstSpot = wins[i][0];
  //                secondSpot = wins[i][2];
  //                enemyWon = true;
  //            }
  //        } else{
  //            if (spaces[wins[i][0]] == "O" && spaces[wins[i][1]] == "O" && spaces[wins[i][2]] == "O"){
  //                firstSpot = wins[i][0];
  //                secondSpot = wins[i][2];
  //                enemyWon = true;
  //            }
  //        }
  //    }
  //}

  //protected void checkForTie(){
  //    //if every spot is filled
  //    if (!won && !enemyWon){
  //        for (int i = 0; i< spaces.length; i++){
  //            if (spaces[i] == null) {
  //                return;
  //            }
  //        }
  //        tie = true;
  //    }

  //}
}
