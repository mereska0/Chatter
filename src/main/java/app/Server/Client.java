package app.Server;

import app.Gui.Chat;
import app.Gui.StartPage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class Client {
    private static PrintWriter out;
    private static Chat chat;
    private static Socket socket;
    private static Thread mainThread;
    private static BufferedReader mainIn;
    public static void main(String[] args) throws InterruptedException {
        Thread guiThread = new Thread(() -> {
            Application.launch(StartPage.class, args);
        });
        guiThread.start();

        Thread.sleep(1000);

        Thread serverThread = new Thread(() -> {
            try {
                System.out.println("connecting...");
                socket = new Socket("localhost", 1234);
                OutputStream outputStream = socket.getOutputStream();
                out = new PrintWriter(outputStream, true);

                mainIn = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                System.out.println("Connected successfully");

                while (!StartPage.isNicknameSet()) {
                    Thread.sleep(1000);
                }

                String senderName = StartPage.getName();
                out.println(senderName);
                out.flush();

                mainThread = new Thread(() -> {
                    try {
                        String serverMessage;
                        while ((serverMessage = mainIn.readLine()) != null) {
                            final String msg = serverMessage;
                            System.out.println("got: " + msg);

                            Platform.runLater(() -> {
                                try {
                                    processServerMessage(msg);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } catch (IOException e) {
                        System.out.println("server disconnected");
                        Platform.runLater(() -> {
                            if (chat != null) {
                                chat.appendMessage("SYSTEM", "Server disconnected");
                            }
                        });
                    }
                });
                mainThread.start();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    System.err.println("ERROR: " + e.getMessage());
                });
            }
        });
        serverThread.start();
    }

    private static void processServerMessage(String message) throws IOException {
        if (message.contains("CMD_CLOSE_WINDOW")) {
            Platform.runLater(() -> {
                if (chat != null) {
                    chat.close();
                }
            });
            return;
        }

        if (message.contains("/setname")) {
            String username = message.substring("/setname ".length());
            StartPage.setName(username);
            return;
        }

        if (message.contains("/create ") || message.contains("/connect")) {
            int port = 0;
            if (message.contains("/create ")){port =Math.abs(message.substring(8).hashCode() % 10000);}
            if (message.contains("/connect ")){port =Math.abs(message.substring(9).hashCode() % 10000);}
            mainThread.interrupt();
            socket.close();
            int finalPort = port;
            Platform.runLater(() -> {
                try {
                    if (chat != null) {
                        chat.close();
                    }
                    Stage chatStage = new Stage();
                    Chat newChat = new Chat(finalPort + "");
                    newChat.start(chatStage);
                    chat = newChat;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            try {
                mainIn.close();
                Socket NewSocket = new Socket("localhost", port);
                OutputStream outputStream = NewSocket.getOutputStream();
                PrintWriter Newout = new PrintWriter(outputStream, true);

                BufferedReader NewIn = new BufferedReader(
                        new InputStreamReader(NewSocket.getInputStream()));
                System.out.println("Connected to port " + port);

                new Thread(() -> {
                    try {
                        String serverMessage;
                        while ((serverMessage = NewIn.readLine()) != null) {
                            final String msg = serverMessage;
                            Platform.runLater(() -> {
                                try {
                                    processServerMessage(msg);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } catch (IOException e) {
                        System.out.println("Disconnected from port " + finalPort);
                    }
                }).start();

                Newout.println(StartPage.getName());
                Newout.flush();

            } catch (IOException e) {
                System.err.println("Failed to connect to port " + port + ": " + e.getMessage());
            }
            return;
        }
        String sender = "";
        String messageText = message;

        if (message.startsWith("[") && message.contains("]: ")) {
            int bracketEnd = message.indexOf("]: ");
            sender = message.substring(1, bracketEnd);
            messageText = message.substring(bracketEnd + 3);
        }

        if (chat != null) {
            chat.appendMessage(sender, messageText);
        }
    }

    public static void setChatInstance(Chat chat) {
        Client.chat = chat;
    }

    public static PrintWriter getServerWriter() {
        return out;
    }
}