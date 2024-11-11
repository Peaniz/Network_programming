package withui.bai1;

import javax.swing.*;
import java.awt.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Server extends JFrame {
    private static final int DEF_PORT = 2021;
    private JTextArea textArea;

    public Server() {
        setTitle("Server");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        setVisible(true);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    }

    private static String toUpperCase(String var0) {
        StringBuilder var1 = new StringBuilder();
        char[] var2 = var0.toCharArray();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            char var5 = var2[var4];
            if (var5 >= 'a' && var5 <= 'z') {
                var5 = (char) (var5 - 32);
            }

            var1.append(var5);
        }

        return var1.toString();
    }

    private static String toLowerCase(String var0) {
        StringBuilder var1 = new StringBuilder();
        char[] var2 = var0.toCharArray();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            char var5 = var2[var4];
            if (var5 >= 'A' && var5 <= 'Z') {
                var5 = (char) (var5 + 32);
            }

            var1.append(var5);
        }

        return var1.toString();
    }

    private static String reverseString(String var0) {
        StringBuilder var1 = new StringBuilder();

        for (int var2 = var0.length() - 1; var2 >= 0; --var2) {
            var1.append(var0.charAt(var2));
        }

        return var1.toString();
    }

    private static String alternateCase(String var0) {
        StringBuilder var1 = new StringBuilder();

        for (int var2 = 0; var2 < var0.length(); ++var2) {
            char var3 = var0.charAt(var2);
            if (var2 % 2 == 0) {
                if (var3 >= 'a' && var3 <= 'z') {
                    var3 = (char) (var3 - 32);
                }
            } else if (var3 >= 'A' && var3 <= 'Z') {
                var3 = (char) (var3 + 32);
            }

            var1.append(var3);
        }

        return var1.toString();
    }

    private static String countWordsAndVowels(String var0) {
        int var1 = 0;
        int var2 = 0;
        boolean var3 = false;

        for (int var4 = 0; var4 < var0.length(); ++var4) {
            char var5 = var0.charAt(var4);
            if (var5 != ' ' && !var3) {
                ++var1;
                var3 = true;
            } else if (var5 == ' ') {
                var3 = false;
            }

            if (isVowel(var5)) {
                ++var2;
            }
        }

        return "Word count: " + var1 + ", Vowel count: " + var2;
    }

    private static boolean isVowel(char var0) {
        var0 = Character.toLowerCase(var0);
        return var0 == 'a' || var0 == 'e' || var0 == 'i' || var0 == 'o' || var0 == 'u';
    }

    public static void main(String[] var0) {
        Server server = new Server();
        server.startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try (DatagramSocket serverSocket = new DatagramSocket(DEF_PORT)) {
                log("Server is waiting for connection on port " + DEF_PORT);
                byte[] receiveBuffer = new byte[1024];

                while (true) {
                    // Prepare packet for receiving data
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    serverSocket.receive(receivePacket);

                    // Convert received data to string
                    String received = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    log("Received from client: " + received);

                    // Process the string
                    String reversed = reverseString(received);
                    String uppercased = toUpperCase(received);
                    String lowercased = toLowerCase(received);
                    String alternated = alternateCase(received);
                    String wordAndVowelCount = countWordsAndVowels(received);

                    // Prepare response messages
                    String[] responses = {
                        "Reversed: " + reversed,
                        "Uppercase: " + uppercased,
                        "Lowercase: " + lowercased,
                        "Alternating case: " + alternated,
                        wordAndVowelCount
                    };

                    // Send each response back to the client
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();
                    
                    for (String response : responses) {
                        byte[] sendBuffer = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(
                            sendBuffer, 
                            sendBuffer.length, 
                            clientAddress, 
                            clientPort
                        );
                        serverSocket.send(sendPacket);
                    }
                }
            } catch (Exception e) {
                log("Error: " + e.getMessage());
            }
        }).start();
    }
}
