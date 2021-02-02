package com.game;

import javax.swing.*;
import java.awt.*;
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
    public DataOutputStream dos;
    public DataInputStream dis;

    protected ServerSocket serverSocket;

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



        painter = new Painter();
        painter.loadImages();
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

            if (!painter.circle && !painter.accepted) {
                listenForServerRequest();

            }
            painter.setDis(dis);
            painter.dos = dos;
            painter.tick();
            painter.repaint();
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
            painter.accepted = true;
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
                painter.accepted = true;
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
        painter.yourTurn = true;
        painter.circle = false;
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        TicTacToe ticTacToe = new TicTacToe();
    }
}
