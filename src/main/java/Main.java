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
        final List<String> VALID_PATHS = new ArrayList<>();
        VALID_PATHS.add("/");
        VALID_PATHS.add("/echo/abc");

        try {
            ServerSocket serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("accepted new connection");

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String request = in.readLine();
                System.out.println("request: " + request); // debugging

                String path = "";
                String responseString = "";
                if (request != null) {
                    String[] requestParts = request.split(" ");
                    String[] intermediateResponseParts = request.split(" ");
                    String[] responseParts = intermediateResponseParts[1].split("/");

                    if (requestParts.length > 1) {
                        path = requestParts[1];
                    }
                    if (responseParts.length > 1) {
                        responseString = responseParts[2];
                    }
                }
                System.out.println("path: " + path); // debugging
                System.out.println("string portion: " + responseString); // debugging

                OutputStream out = clientSocket.getOutputStream();
                if (VALID_PATHS.contains(path)) {
                    out.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: 3\r\n\r\n" + responseString).getBytes());
                } else {
                    out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}