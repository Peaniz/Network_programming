package withui.bai3;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatServer extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort;
    private static final int SERVER_PORT = 7000;

    public ChatServer() {
        setTitle("Chat Server");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        add(panel, BorderLayout.SOUTH);

        try {
            socket = new DatagramSocket(SERVER_PORT);
            System.out.println("Server is started on port " + SERVER_PORT);
            chatArea.append("Server is waiting for connection on port " + SERVER_PORT + "\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
        startListening();
    }

    private void sendMessage() {
        if (clientAddress == null || clientPort == 0) {
            chatArea.append("No client connected yet.\n");
            return;
        }
        
        try {
            String msg = inputField.getText();
            if (msg.trim().isEmpty()) return;
            
            String fullMsg = "Server: " + msg;
            byte[] sendData = fullMsg.getBytes();
            
            DatagramPacket sendPacket = new DatagramPacket(
                sendData,
                sendData.length,
                clientAddress,
                clientPort
            );
            socket.send(sendPacket);
            
            chatArea.append(fullMsg + "\n");
            inputField.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListening() {
        new Thread(() -> {
            byte[] receiveData = new byte[1024];
            while (true) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    
                    // Store client info for sending responses
                    clientAddress = receivePacket.getAddress();
                    clientPort = receivePacket.getPort();
                    
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append(message + "\n");
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatServer();
        });
    }
}