package server;

import gui.TextInput;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Client {
    private static final Object lock = new Object();
    static boolean isDone = false;
    private static PrintWriter out;

    public static void main(String[] args) {
        Thread gui = new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Chatter");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(900, 600);
                frame.setLayout(new BorderLayout());
                TextInput textInput = new TextInput();
                frame.add(new JScrollPane(TextInput.textArea));

                TextInput.setOnSendListener(e -> {
                    String message = e.getActionCommand();
                    String username = TextInput.getUserName();
                    System.out.println("CLIENT [" + username + "]: " + message);

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
                System.out.println("Подключаюсь к серверу...");
                Socket socket = new Socket("type your ip...", 1234);
                OutputStream outputStream = socket.getOutputStream();
                out = new PrintWriter(outputStream, true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                System.out.println("Подключено к серверу");

                String response = in.readLine();
                if (response != null) {
                    System.out.println("Получено от сервера: " + response);

                    SwingUtilities.invokeLater(() -> {
                        String senderName = "Server";
                        String messageText = response;

                        if (response.startsWith("[") && response.contains("]: ")) {
                            int endIndex = response.indexOf("]: ");
                            senderName = response.substring(1, endIndex);
                            messageText = response.substring(endIndex + 3);
                        }

                        TextInput.appendResponse(senderName, messageText);
                    });
                }

                new Thread(() -> {
                    try {
                        String serverMessage;
                        while ((serverMessage = in.readLine()) != null) {
                            final String msg = serverMessage;
                            System.out.println("Получено от сервера: " + msg);

                            SwingUtilities.invokeLater(() -> {
                                String senderName = "Server";
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
                        System.out.println("Сервер отключился");
                        SwingUtilities.invokeLater(() -> {
                            TextInput.appendResponse("System", "Сервер отключился");
                        });
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Не удалось подключиться к серверу: " + e.getMessage(),
                            "Ошибка подключения",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        });

        gui.start();
        main.start();
    }
}