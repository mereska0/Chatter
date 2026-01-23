package app.Server;

import app.Gui.Chat;
import app.Gui.StartPage;
import javafx.application.Application;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

public class Client {
    private static PrintWriter out;
    private static Chat chatInstance;

    public static void main(String[] args) throws InterruptedException {
        Thread guiThread = new Thread(() -> {
            Application.launch(StartPage.class, args);
        });
        guiThread.start();

        Thread.sleep(1000);

        Thread serverThread = new Thread(() -> {
            try {
                System.out.println("connecting...");
                Socket socket = new Socket("localhost", 1234);
                OutputStream outputStream = socket.getOutputStream();
                out = new PrintWriter(outputStream, true);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                System.out.println("Connected successfully");

                while (!StartPage.isNicknameSet()) {
                    Thread.sleep(1000);
                }

                String senderName = StartPage.getName();
                out.println(senderName);
                out.flush();

                new Thread(() -> {
                    try {
                        String serverMessage;
                        while ((serverMessage = in.readLine()) != null) {
                            final String msg = serverMessage;
                            System.out.println("got: " + msg);

                            Platform.runLater(() -> {
                                processServerMessage(msg);
                            });
                        }
                    } catch (IOException e) {
                        System.out.println("server disconnected");
                        Platform.runLater(() -> {
                            if (chatInstance != null) {
                                chatInstance.appendMessage("SYSTEM", "Server disconnected");
                            }
                        });
                    }
                }).start();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    System.err.println("ERROR: " + e.getMessage());
                });
            }
        });
        serverThread.start();
    }

    private static void processServerMessage(String message) {
        if (message.contains("CMD_CLOSE_WINDOW")) {
            Platform.runLater(() -> {
                if (chatInstance != null) {
                    chatInstance.close();
                }
            });
            return;
        }

        if (message.contains("/setname")) {
            String username = message.substring("/setname ".length());
            StartPage.setName(username);
            return;
        }
        String sender = "";
        String messageText = message;

        if (message.startsWith("[") && message.contains("]: ")) {
            int bracketEnd = message.indexOf("]: ");
            sender = message.substring(1, bracketEnd);
            messageText = message.substring(bracketEnd + 3);
        }

        if (chatInstance != null) {
            chatInstance.appendMessage(sender, messageText);
        }
    }
    public static void setChatInstance(Chat chat) {
        chatInstance = chat;
    }

    public static PrintWriter getServerWriter() {
        return out;
    }
}