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
                try {
                    Socket socket = serverSocket.accept();
                    RequestHandler requestHandler = new RequestHandler(socket, router);
                    new Thread(requestHandler).start();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}