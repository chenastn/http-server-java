import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("accepted new connection"); // debugging
                try {
                    handleRequest(clientSocket);
                } finally {
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String request = in.readLine();
        System.out.println("request: " + request); // debugging

        if (request == null) {
            sendResponse(clientSocket, null);
            return;
        }

        String[] requestParts = request.split(" ");
        if (requestParts.length < 2) {
            sendResponse(clientSocket, null);
            return;
        }

        String path = requestParts[1];
        if (path.startsWith("/echo/")) {
            String echoContent = path.substring(6);
            sendResponse(clientSocket, echoContent);
        } else if (path.equals("/")) {
            sendResponse(clientSocket, path);
        } else {
            sendResponse(clientSocket, null);
        }
    }

    private static void sendResponse(Socket clientSocket, String body) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        if (body != null && body.equals("/")) {
            out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        } else if (body != null && !body.isEmpty()) {
            out.write(("HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "Content-Length: " + body.length()
                    + "\r\n\r\n"
                    + body).getBytes());
        } else {
            out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        }
    }
}