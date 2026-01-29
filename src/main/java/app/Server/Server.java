package app.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static List<Integer> ports = new ArrayList<>();
    public static void main(String[] args) {
        ports.add(1234);
        int port = 1234;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("working on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("connected");
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            System.out.println();
        }
    }
    public static void startNewServer(int port){
        ports.add(port);
        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("working on port " + port);
            while (true) {
                Socket NewclientSocket = serverSocket.accept();
                System.out.println("connected");
                ClientHandler NewclientHandler = new ClientHandler(NewclientSocket);
                new Thread(NewclientHandler).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
//TODO diff port - diff chat(create chats)