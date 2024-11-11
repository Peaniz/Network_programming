package withui.bai2;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

public class MathServer extends JFrame {
    public final static int DEF_PORT = 2021;
    private JTextArea textArea;

    public MathServer() {
        setTitle("Math Server");
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

    // Method to check if a character is an operator
    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    // Method to determine the precedence of operators
    private static int precedence(char op) {
        if (op == '+' || op == '-') {
            return 1;
        } else if (op == '*' || op == '/') {
            return 2;
        }
        return 0;
    }

    // Method to apply an operation on two operands
    private static double applyOperator(double b, double a, char op) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero is not allowed.");
                }
                return a / b;
            default: throw new IllegalArgumentException("Invalid operator: " + op);
        }
    }

    // Method to evaluate a mathematical expression
    private static double evaluateExpression(String expression) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            // Ignore whitespace
            if (c == ' ') continue;

            // If the character is a digit, extract the full number
            if (Character.isDigit(c)) {
                StringBuilder num = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }
                i--; // Adjust index back after the loop
                values.push(Double.parseDouble(num.toString()));
            }
            // If the character is an open parenthesis, push it to the operator stack
            else if (c == '(') {
                operators.push(c);
            }
            // If the character is a closing parenthesis, process enclosed operators
            else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    values.push(applyOperator(values.pop(), values.pop(), operators.pop()));
                }
                operators.pop(); // Remove the opening parenthesis
            }
            // If the character is an operator, process based on precedence
            else if (isOperator(c)) {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    values.push(applyOperator(values.pop(), values.pop(), operators.pop()));
                }
                operators.push(c);
            }
        }

        // Process remaining operators in the stack
        while (!operators.isEmpty()) {
            values.push(applyOperator(values.pop(), values.pop(), operators.pop()));
        }

        // Return the final result rounded to 2 decimal places
        return Math.round(values.pop() * 100.0) / 100.0;
    }

    public void startServer() {
        try (DatagramSocket serverSocket = new DatagramSocket(DEF_PORT)) {
            log("Server is waiting for connection on port " + DEF_PORT);
            byte[] receiveBuffer = new byte[1024];

            while (true) {
                // Prepare packet for receiving data
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                // Log client connection info
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                log("Received request from client at " + clientAddress.getHostAddress() + ":" + clientPort);

                // Get the expression from the packet
                String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                log("Received expression from client: " + clientMessage);

                try {
                    // Evaluate the expression
                    double result = evaluateExpression(clientMessage);
                    String response = "Result: " + result;

                    // Send the result back to the client
                    byte[] sendData = response.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        clientAddress,
                        clientPort
                    );
                    serverSocket.send(sendPacket);
                } catch (Exception e) {
                    // Handle any evaluation or input errors
                    String errorMsg = "Error: " + e.getMessage();
                    byte[] sendData = errorMsg.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        clientAddress,
                        clientPort
                    );
                    serverSocket.send(sendPacket);
                }
            }
        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        MathServer server = new MathServer();
        server.startServer();
    }
}
