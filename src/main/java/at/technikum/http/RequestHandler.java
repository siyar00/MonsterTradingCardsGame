package at.technikum.http;

import at.technikum.application.router.Route;
import at.technikum.application.router.RouteIdentifier;
import at.technikum.application.router.Router;
import at.technikum.http.exceptions.BadRequestException;
import at.technikum.http.exceptions.Except;

import java.io.*;
import java.net.Socket;

public class RequestHandler implements Runnable {

    private final Socket socket;
    private final Router router;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public RequestHandler(Socket socket, Router router) {
        this.socket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final RequestContext requestContext = new RequestContext().parseRequest(bufferedReader);
            //Can be deleted afterwards
            System.out.println("Thread: " + Thread.currentThread().getName());
            System.out.println(requestContext);

            final Route route = router.findRoute(new RouteIdentifier(requestContext.getPath(), requestContext.getHttpVerb()));
            Response response;
            try {
                if(route == null) throw new BadRequestException("Endpoint not available");
                response = route.process(requestContext);
            }catch (Except e) {
                response = new Response(e.getHttpStatus(), e.getMessage());
            } catch (IllegalStateException e) {
                response = new Response(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }

            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(String.valueOf(response));
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeClient();
        }
    }

    private void closeClient() {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
