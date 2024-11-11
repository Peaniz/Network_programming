package withui.bai3;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatClient extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort = 7000;

    public ChatClient() {
        setTitle("Chat Client");
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
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            chatArea.append("Connected to server at localhost:" + serverPort + "\n");
            chatArea.append("Client port: " + socket.getLocalPort() + "\n\n");
            setTitle("Chat Client - Port: " + socket.getLocalPort());
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

        startListening();
        setVisible(true);
    }

    private void sendMessage() {
        try {
            String msg = inputField.getText();
            if (msg.trim().isEmpty()) return;
            
            String fullMsg = "Client: " + msg;
            byte[] sendData = fullMsg.getBytes();
            
            DatagramPacket sendPacket = new DatagramPacket(
                sendData,
                sendData.length,
                serverAddress,
                serverPort
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
            new ChatClient();
        });
    }
}