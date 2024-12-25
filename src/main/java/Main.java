import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Main {
    private static String directory;
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--directory")) {
                    directory = args[i + 1];
                    break;
                }
            }

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
            sendGETResponse(clientSocket, null, null);
            return;
        }

        String[] requestParts = request.split(" ");
        if (requestParts.length < 2) {
            sendGETResponse(clientSocket, null, null);
            return;
        }

        String requestType = requestParts[0];
        String path = requestParts[1];
        System.out.println("request type: " + requestType); // debugging
        System.out.println("path: " + path); // debugging

        switch (requestType) {
            case "GET":
                if (path.startsWith("/echo")) {
                    sendGETResponse(clientSocket, getAcceptType(path), path.substring(6));
                } else if (path.startsWith("/user-agent")) {
                    sendGETResponse(clientSocket, getAcceptType(path), getUserAgent(in));
                } else if (path.startsWith("/files")) {
                    sendGETResponse(clientSocket, getAcceptType(path), getFileContent(path));
                } else if (path.equals("/")) {
                    sendGETResponse(clientSocket, getAcceptType(path), path);
                } else {
                    sendGETResponse(clientSocket, getAcceptType(path), null);
                }
                break;
            case "POST":
                if (path.startsWith("/files")) {
                    String filePath = getFilePath(path);
                    String postData = getPostData(in);
                    try {
                        setFileContent(filePath, postData);
                        sendPOSTResponse(clientSocket, "201 Created");
                    } catch (IOException e) {
                        sendPOSTResponse(clientSocket, "404 Not Found");
                    }
                }
                break;
            default:
                sendGETResponse(clientSocket, getAcceptType(path), null);
                break;
        }
    }

    private static void sendGETResponse(Socket clientSocket, String contentType, String body) throws IOException {
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

    private static void sendPOSTResponse(Socket clientSocket, String body) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        if (body != null && !body.isEmpty()) {
            out.write("HTTP/1.1 201 Created\r\n\r\n".getBytes());
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

    private static String getAcceptType(String path) throws IOException {
        if (path.startsWith("/echo") || path.startsWith("/user-agent") || path.equals("/")) {
            return "text/plain";
        } else if (path.startsWith("/files")) {
            return "application/octet-stream";
        }
        return "text/plain";
    }

    private static String getPostData(BufferedReader in) throws IOException {
        String line;
        int contentLength = 0;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Content-Length: ")) {
                contentLength = Integer.parseInt(line.substring(16));
            }
        }

        char[] body = new char[contentLength];
        in.read(body, 0, contentLength);
        return new String(body);
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

    private static void setFileContent(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    private static String getFilePath(String requestPath) {
        String fileName = requestPath.substring(7);
        return directory + "/" + fileName;
    }

//    private static boolean createFile(String pathString) {
//        try {
//            String filePath = getFilePath(pathString);
//            Path path = Paths.get(filePath);
//            Files.createFile(path);
//            return true;
//        } catch (IOException e) {
//            System.err.println("Error creating file: " + e.getMessage());
//            return false;
//        }
//    }
}