package withui.bai2;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MathClient extends JFrame {
    private JTextField inputField;
    private JTextArea outputArea;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    public MathClient() {
        // Set up the GUI
        setTitle("Math Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        inputField = new JTextField();
        outputArea = new JTextArea();
        outputArea.setEditable(false);

        add(inputField, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // Connect to the server
        try {
            String portStr = JOptionPane.showInputDialog(this, "Enter port number:", "Port Input", JOptionPane.QUESTION_MESSAGE);
            serverPort = Integer.parseInt(portStr);
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            
            // Add connection info to output area
            outputArea.append("Connected to server at localhost:" + serverPort + "\n");
            outputArea.append("Client port: " + socket.getLocalPort() + "\n\n");
            
            setTitle("Math Client - Port: " + socket.getLocalPort());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendExpression();
            }
        });

        // Start a thread to listen for server responses
        new Thread(new Runnable() {
            public void run() {
                listenForResponses();
            }
        }).start();
    }

    private void sendExpression() {
        try {
            String expression = inputField.getText();
            byte[] sendData = expression.getBytes();
            
            DatagramPacket sendPacket = new DatagramPacket(
                sendData,
                sendData.length,
                serverAddress,
                serverPort
            );
            socket.send(sendPacket);
            inputField.setText("");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void listenForResponses() {
        try {
            byte[] receiveData = new byte[1024];
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String serverResponse = new String(receivePacket.getData(), 0, receivePacket.getLength());
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("Server response: " + serverResponse + "\n");
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MathClient().setVisible(true);
            }
        });
    }
}
