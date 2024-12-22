import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("accepted new connection"); // debugging

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    public static void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String request = in.readLine();
        System.out.println("request: " + request); // debugging

        if (request == null) {
            sendResponse(clientSocket, null, null);
            return;
        }

        String[] requestParts = request.split(" ");
        if (requestParts.length < 2) {
            sendResponse(clientSocket, null, null);
            return;
        }

        String path = requestParts[1];
        System.out.println("path: " + path); // debugging
        if (path.startsWith("/echo")) {
            sendResponse(clientSocket, "text/plain", path.substring(6));
        } else if (path.startsWith("/user-agent")) {
            sendResponse(clientSocket, "text/plain", getUserAgent(in));
        } else if (path.startsWith("/files")) {
            sendResponse(clientSocket, "application/octet-stream", getFileContent(path));
        } else if (path.equals("/")) {
            sendResponse(clientSocket, "text/plain", path);
        } else {
            sendResponse(clientSocket, "text/plain", null);
        }
    }

    private static void sendResponse(Socket clientSocket, String contentType, String body) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        if (body != null && body.equals("/")) {
            out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        } else if (body != null && !body.isEmpty()) {
            out.write(("HTTP/1.1 200 OK\r\n"
                    + "Content-Type: " + contentType
                    + "\r\n"
                    + "Content-Length: " + body.getBytes().length
                    + "\r\n\r\n"
                    + body).getBytes());
        } else {
            out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        }
    }

    private static String getUserAgent(BufferedReader in) throws IOException {
        String header;

        while (true) {
            header = in.readLine();

            if (header != null && !header.isEmpty()) {
                System.out.println("header = " + header); // debugging
                if (header.startsWith("User-Agent:")) {
                    System.out.println("userAgent: " + header.substring(11).trim()); // debugging
                    return header.substring(11).trim();
                }
            }
        }
    }

    private static String getFileContent(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(getFilePath(path)))) {
            System.out.println("getFilePath(path): " + getFilePath(path)); // debugging
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            System.out.println("content = " + content.toString().trim()); // debugging
            return content.toString().trim();
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private static String getFilePath(String requestPath) {
        String fileName = requestPath.substring(7);
        System.out.println("file path: " + "/files/" + fileName);
        return "/files/" + fileName;
    }
}