package app.Server;

public class RoomHandler implements Runnable{
    private ClientHandler ch;
    private String port;
    public RoomHandler(String port, ClientHandler ch){
        this.ch = ch;
        this.port = port;
    }
    @Override
    public void run() {
        //code will be down here...
    }
}
