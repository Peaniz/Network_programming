package withui.bai1;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Client extends JFrame {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private JTextField inputField;
    private JTextArea outputArea;

    public Client() {
        try {
            String portStr = JOptionPane.showInputDialog(this, "Enter port number:", "Port Input", JOptionPane.QUESTION_MESSAGE);
            serverPort = Integer.parseInt(portStr);
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            
            // Add connection info to output area
            outputArea = new JTextArea();
            outputArea.setEditable(false);
            outputArea.append("Connected to server at localhost:" + serverPort + "\n");
            outputArea.append("Client port: " + socket.getLocalPort() + "\n\n");
            
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            System.exit(1);
        }

        setTitle("Client - Port: " + socket.getLocalPort());  // Add port to window title
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        inputField = new JTextField();
        
        add(inputField, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String userInput = inputField.getText();
                    
                    // Send data to server
                    byte[] sendData = userInput.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(
                        sendData, 
                        sendData.length, 
                        serverAddress, 
                        serverPort
                    );
                    socket.send(sendPacket);

                    // Receive responses from server
                    byte[] receiveData = new byte[1024];
                    for (int i = 0; i < 5; i++) {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(receivePacket);
                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        outputArea.append(response + "\n");
                    }

                    inputField.setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client().setVisible(true);
            }
        });
    }
}