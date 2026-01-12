package server;

import gui.TextInput;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Server {
    private static final Object lock = new Object();
    static boolean isDone = false;
    private static PrintWriter out;

    public static void main(String[] args) {
        Thread gui = new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Chatter (Server)");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(900, 600);
                frame.setLayout(new BorderLayout());
                TextInput textInput = new TextInput();
                frame.add(new JScrollPane(TextInput.textArea));

                TextInput.setOnSendListener(e -> {
                    String message = e.getActionCommand();
                    String username = TextInput.getUserName();
                    System.out.println("SERVER [" + username + "]: " + message);

                    if (out != null) {
                        out.println("[" + username + "]: " + message);
                    }
                });

                frame.setVisible(true);
                synchronized(lock) {
                    isDone = true;
                    lock.notifyAll();
                }
            });
        });

        Thread main = new Thread(() -> {
            synchronized(lock) {
                while (!isDone) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                int port = 1234;
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Сервер запущен на порту " + port);

                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключен!");

                OutputStream outputStream = clientSocket.getOutputStream();
                out = new PrintWriter(outputStream, true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                String username = TextInput.getUserName();
                new Thread(() -> {
                    try {
                        String clientMessage;
                        while ((clientMessage = in.readLine()) != null) {
                            final String msg = clientMessage;
                            System.out.println("Получено от клиента: " + msg);
                            SwingUtilities.invokeLater(() -> {
                                String senderName = "Client";
                                String messageText = msg;

                                if (msg.startsWith("[") && msg.contains("]: ")) {
                                    int endIndex = msg.indexOf("]: ");
                                    senderName = msg.substring(1, endIndex);
                                    messageText = msg.substring(endIndex + 3);
                                }

                                TextInput.appendResponse(senderName, messageText);
                            });
                        }
                    } catch (IOException e) {
                        System.out.println("Клиент отключился");
                        SwingUtilities.invokeLater(() -> {
                            TextInput.appendResponse("System", "Клиент отключился");
                        });
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        gui.start();
        main.start();
    }
}