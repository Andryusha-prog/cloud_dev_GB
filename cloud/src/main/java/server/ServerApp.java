package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8080);
        System.out.println("Server start!");

        while (true){
            Socket socket = server.accept();
            System.out.println("Client accepted!");
            try{
                new Thread(new Handler(socket)).start();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
