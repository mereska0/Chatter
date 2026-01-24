package app.Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static final ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            this.username = reader.readLine();
            if (username == null || username.trim().isEmpty()) {
                username = "Anonymous";
            } else {
                username = username.trim();
            }

            clientHandlers.add(this);
            broadcastToAll("[SERVER] " + username + " connected!");
            writer.println("[SERVER] Online: " + getOnlineUsers());

            System.out.println(username + " connected");
        } catch (IOException e) {
            closeAll();
            System.out.println("CONNECTION ERROR");
        }
    }

    private String getOnlineUsers() {
        synchronized (clientHandlers) {
            StringBuilder users = new StringBuilder();
            for (ClientHandler ch : clientHandlers) {
                users.append(ch.username).append(", ");
            }
            if (users.length() > 0) {
                users.setLength(users.length() - 2);
            }
            return users.toString();
        }
    }

    public void broadcastToAll(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch != this) {
                    ch.sendMessage(message);
                }
            }
        }
    }

    public void sendMessageToClient(String targetUsername, String message) {
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch.username.equalsIgnoreCase(targetUsername)) {
                    ch.sendMessage("[Private from " + username + "]: " + message);
                    return;
                }
            }
            writer.println("[SERVER] user '" + targetUsername + "' not found");
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    private void removeClientHandler() {
        synchronized (clientHandlers) {
            clientHandlers.remove(this);
            broadcastToAll("[SERVER] " + username + " left");
        }
        System.out.println(username + " disconnected");
    }

    private void closeAll() {
        removeClientHandler();
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String message;
        try {
            while (socket.isConnected() && !socket.isClosed()) {
                message = reader.readLine();
                if (message == null) {
                    break;
                }

                System.out.println("server from " + username + ": '" + message + "'");

                if (message.equalsIgnoreCase("/exit")) {
                    writer.println("CMD_CLOSE_WINDOW");
                    writer.flush();
                    System.out.println(username + " left");
                    break;
                }
                else if (message.startsWith("/setname ")) {
                    String newUsername = message.substring(9).trim();
                    if (newUsername.isEmpty()) {
                        writer.println("[SERVER] blank name");
                        continue;
                    }

                    boolean nameTaken = false;
                    synchronized (clientHandlers) {
                        for (ClientHandler ch : clientHandlers) {
                            if (ch != this && ch.username.equalsIgnoreCase(newUsername)) {
                                nameTaken = true;
                                break;
                            }
                        }
                    }

                    if (nameTaken) {
                        writer.println("[SERVER] name '" + newUsername + "' is already used");
                    } else {
                        String oldUsername = this.username;
                        this.username = newUsername;
                        broadcastToAll("[SERVER] " + oldUsername + " changed to " + newUsername);
                        writer.println("/setname " + newUsername);
                    }
                }
                else if (message.startsWith("/private ")) {
                    String[] parts = message.split(" ", 3);
                    if (parts.length == 3) {
                        String targetUser = parts[1];
                        String privateMessage = parts[2];

                        if (targetUser.equalsIgnoreCase(this.username)) {
                            writer.println("[SERVER] you can't send private message to yourself");
                        } else {
                            sendMessageToClient(targetUser, privateMessage);
                            writer.println("[Private to " + targetUser + "]: " + privateMessage);
                        }
                    } else {
                        writer.println("[SERVER] error");
                    }
                }
                else if (message.equalsIgnoreCase("/users") || message.equalsIgnoreCase("/who")) {
                    writer.println("[SERVER] Online: " + getOnlineUsers());
                }
                else if (message.equalsIgnoreCase("/help")) {
                    writer.println("[SERVER] COMMANDS:");
                    writer.println("[SERVER] /help - command usage");
                    writer.println("[SERVER] /users - all users online");
                    writer.println("[SERVER] /setname <name> - change username");
                    writer.println("[SERVER] /private <name> <text> - private message to user");
                    writer.println("[SERVER] /exit - leave the chat");
                } else {
                    String formattedMessage = "[" + username + "]: " + message;
                    broadcastToAll(formattedMessage);
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR " + username + ": " + e.getMessage());
        } finally {
            closeAll();
        }
    }
}