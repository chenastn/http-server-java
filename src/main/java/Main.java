import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final List<String> VALID_PATHS = new ArrayList<>();
        VALID_PATHS.add("/");


        try {
            ServerSocket serverSocket = new ServerSocket(4221);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            while(true) {
                Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
                System.out.println("accepted new connection");

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String request = in.readLine();

                String path = "";
                if (request != null) {
                    String[] query = request.split(" ");
                    if (query.length > 1) {
                        path = query[1];
                    }
                }

                OutputStream out = clientSocket.getOutputStream();
                if (VALID_PATHS.contains(path)) {
                    out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
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
