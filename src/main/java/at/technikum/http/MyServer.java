package at.technikum.http;

import at.technikum.application.router.Router;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {

    private final Router router;

    public MyServer() {
        this.router = new Router();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(10001)) {
            do {
                Socket socket = serverSocket.accept();
                new Thread(new RequestHandler(socket, router)).start();
            } while (serverSocket.isBound());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}