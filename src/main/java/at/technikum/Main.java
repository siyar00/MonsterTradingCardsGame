package at.technikum;

import at.technikum.http.MyServer;

public class Main {
    public static void main(String[] args){
        MyServer server = new MyServer();
        server.start();
    }
}
