# HTTP Server Project Documentation

**Project Start**: December 11, 2024  
**Languages**: Java  
**GitHub**: [HTTP Server Repository](https://github.com/chenastn/codecrafters-http-server-java)  
**Source**: ["Build Your Own HTTP Server" Challenge](https://app.codecrafters.io/courses/http-server/overview)  

A lightweight HTTP server implementation in Java supporting basic HTTP/1.1 protocol features. Run `./your_program.sh` to run the program. The entry point for the HTTP server implementation is in `src/main/java/Main.java`.

---
## Technical Specifications
- **Language**: Java 23.0.1
- **Build Tool**: Maven
- **Key Dependencies**: None (pure Java implementation)
- **Target Features**: HTTP/1.1 protocol support, GET/POST handling, static file serving, basic routing

## Implementation Roadmap
1. Basic socket connection
2. HTTP request parsing
3. Response handling
4. Static file server
5. Routing implementation
---
## Development Log
### December 11, 2024
- Project initialization
- Basic project structure setup
- Implemented basic TCP server that listens on port `4221`
    - [TCP](https://www.cloudflare.com/en-ca/learning/ddos/glossary/tcp-ip/) is the underlying protocol used by HTTP servers. This implies that HTTP is an application layer protocol that runs on top of TCP. [This video gives a foundation for a basic TCP server program.](https://youtu.be/f9gUFy-9uCM?si=JI2Albx0Zzk6mlmR)
    - `serverSocket.setReuseAddress(true);` is used to allow the server to quickly restart without running into "Address already in use" errors. This is helpful when the server is frequently restarted. This modifies the `SO_REUSEADDR` socket option.
    - `Socket clientSocket = serverSocket.accept();` waits for a client to connect. Once a connection is established, it creates a `Socket` object representing the client connection. This is a blocking operation and the program will not move forward until the client socket connection is established.
    - The current implementation handles one request at a time, sequentially. This makes our approach a [single-threaded](https://dev.to/michinoins/single-threaded-vs-multi-threaded-servers-an-experiment-with-nodejs-and-java-3183) approach
- Note that on macOS, you should run `./your_server.sh` in one terminal session, and `nc -vz 127.0.0.1 4221` in another (`-v` gives more verbose output, and `-z` just scans for network listening daemons, without sending any data to them).
- Implemented a basic `HTTP/1.1 200 OK\r\n\r\n` response (content of the request is omitted, since parsing of the request is ignored for now).
- HTTP Response Structure:
    - An HTTP Response is made up of three parts (each separated by a [CRLF](https://developer.mozilla.org/en-US/docs/Glossary/CRLF))
        1. Status line
        2. Zero or more headers (each ending with a CRLF)
        3. Response body (optional)
    - The `HTTP/1.1 200 OK\r\n\r\n` response uses HTTP version 1.1, has a status code of 200, an "OK" reason phrase, no headers, and no body. An example breakdown of the `HTTP/1.1 200 OK\r\n\r\n` response is shown below.
      ```text
      // Status line
      HTTP/1.1  // HTTP version
      200       // Status code
      OK        // Optional reason phrase
      \r\n      // CRLF that marks the end of the status line
      
      // Headers (empty)
      \r\n      // CRLF that marks the end of the headers
      
      // Response body (empty)
      ```
- HTTP Response Implementation Details:
    1. Created a `Socket` object called `clientSocket` that will handle the client connection. This `clientSocket` will represent the connection to the client.
    2. After the connection is accepted, we send the HTTP Response, `HTTP/1.1 200 OK\r\n\r\n`, to the output stream. To send the response, we take the `clientSocket` and use the `getOutputStream()` method. The `getOutputStream()` method will get the output stream associated to our socket (an output stream is used to send data to the client). We chain the `getOutputStream()` method with the `write()` method to write a specified byte array to the output stream. We then turn the parameterized string HTTP response into a byte array using the `getBytes()` method. The line of code as a whole is shown below.
       ```java
       clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
       ```
- More information on HTTP responses and websites I found helpful:
    - [This section of a Rust book on how to write an HTTP response.](https://doc.rust-lang.org/book/ch20-01-single-threaded.html#writing-a-response)
    - [HTTP Version 1.1 Specification](https://datatracker.ietf.org/doc/html/rfc9112#name-message)
    - [What is TCP?](https://www.cloudflare.com/en-ca/learning/ddos/glossary/tcp-ip/)
    - [MDN Web Docs on HTTP Responses](https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages#http_responses)
    - [What is CRLF?](https://developer.mozilla.org/en-US/docs/Glossary/CRLF))
    - [Java - getOutputStream() Method](https://www.tutorialspoint.com/java/java-socket-getoutputstream.htm)
    - [Arpit Bhiyani's Video on TCP Servers](https://www.youtube.com/watch?v=f9gUFy-9uCM)
    - [Single-Threaded vs. Multi-Threaded Servers](https://dev.to/michinoins/single-threaded-vs-multi-threaded-servers-an-experiment-with-nodejs-and-java-3183)
    - [Another section of a Rust book on how to build a single-threaded web-server](https://doc.rust-lang.org/book/ch20-01-single-threaded.html)
### December 13, 2024
- Wrapped the client socket connection logic in an infinite `while` loop. This allows the server to handle multiple requests sequentially. Within the `while` loop, we also close the client socket after all logic is completed using `clientSocket.close()`.
- Implemented basic URL extraction from an HTTP request. This will allow for a `404 Not Found` response.
- HTTP Request Structure:
    - An HTTP Request, similar to an HTTP response, is made up of three parts (each separated by a [CRLF](https://developer.mozilla.org/en-US/docs/Glossary/CRLF))
        1. Request line
        2. Zero or more headers (each ending with a CRLF)
        3. Request body (optional)
    - A request target has [four possible formats](https://datatracker.ietf.org/doc/html/rfc9112#section-3.2). The "request target" specifies the URL path for this request. An example breakdown of an `HTTP/1.1` request with the URL path as `/index.html` is shown below.
      ```text
      // Request line
      GET                          // HTTP method
      /index.html                  // Request target
      HTTP/1.1                     // HTTP version
      \r\n                         // CRLF that marks the end of the request line
      
      // Headers
      Host: localhost:4221\r\n     // Header that specifies the server's host and port
      User-Agent: curl/7.64.1\r\n  // Header that describes the client's user agent
      Accept: */*\r\n              // Header that specifies which media types the client can accept
      \r\n                         // CRLF that marks the end of the headers
      
      // Request body (empty)
      ```
- URL Extraction Implementation Details:
    1. Create an empty `ArrayList` object to hold our valid URL paths. An `ArrayList` is used since they allow for random access, and have an $O(1)$ constant time complexity when using `contains()` to check for valid paths. Also note that an `ArrayList` is thread-safe in handling the single-threaded approach to our TCP server. After instantiating our `ArrayList`, set some valid path as follows. Here I use the default root path. The `ArrayList` approach allows us to dynamically add other paths we deem valid in our TCP server.
       ```java
       final List<String> VALID_PATHS = new ArrayList<>();  
       VALID_PATHS.add("/");
       ```
    2. We will use the `getInputStream()` method on the client socket to read incoming requests the client socket is making to the server. The `getInputStream()` method will obtain the byte stream data from the client socket. Then, the `InputStreamReader` will allow for us to convert the byte streams that are communicated from the client socket into character strings that the `BufferedReader` will store, using the `BufferedReader` here will allow for the `readLine()` method. Finally, using the `readLine()` method, we store the character string into a `request` variable. Note that the `readLine()` method will read until it encounters a newline character or the end of the stream. This makes `readLine()` a good choice for reading HTTP request lines.
       ```java
       BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  
       String request = in.readLine();
       ```
    3. Recall that a request target has [four possible formats](https://datatracker.ietf.org/doc/html/rfc9112#section-3.2). The implementation is designed to handle the request target as a URL path. This is known as `origin-form`. After the server accepts the client connection, we will `split()` the request components and store the URL path.
       ```java
       String path = "";  
       if (request != null) {  
           String[] requestParts = request.split(" ");  
           if (requestParts.length > 1) {  
               path = requestParts[1];  
           }  
       }
       ```
    4. We check if our request path is among the valid paths we have stored. If it is, we will send `HTTP/1.1 200 OK\r\n\r\n`, and if not, we will send `HTTP/1.1 404 Not Found\r\n\r\n`.
       ```java
       OutputStream out = clientSocket.getOutputStream();  
       if (VALID_PATHS.contains(path)) {  
           out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());  
       } else {  
           out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());  
       }
       ```
    - Note that there are several important imports for this approach:
        - `BufferedReader` and `InputStreamReader` are imported to read data from the client's input stream. This helps in processing the incoming HTTP request.
        - `OutputStream` is imported to send data back to the client, allowing the server to respond to requests.
        - `ArrayList` and `List` are imported to create and manage a list of valid paths.
    - Also note that similarly to the previous entry, on macOS, you should run `./your_server.sh` in one terminal session, and in another terminal session, some `curl` to send custom requests and debug. For example,`curl -i GET http://localhost:4221/index.html`, where `-i` gives a more verbose HTTP response.
- More information on HTTP requests and websites I found helpful:
    - [Very Interesting Website that Breaks Down Shell](https://explainshell.com/explain?cmd=curl)
    - [MDN Web Docs on HTTP Requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages#http_requests)
    - [The Four Request Target Formats](https://datatracker.ietf.org/doc/html/rfc9112#section-3.2)
    - [What is CRLF?](https://developer.mozilla.org/en-US/docs/Glossary/CRLF)
    - [Java - Components of a URL](https://www.tutorialspoint.com/java-program-to-get-components-of-a-url)
    - [This Stack Overflow Forum on Getting the Path of a URL](https://stackoverflow.com/questions/5564998/how-to-get-the-path-of-a-url)
### December 16, 2024
- Implemented a basic response body within an HTTP response.
- Implemented the `/echo/{str}` endpoint, which accepts a string and returns it in the response body.
- HTTP Response Structure & Response Body:
    - A response body is used to return content to the client. This content may be an entire web page, a file, a string, or anything else that can be represented with bytes. An example breakdown of the `200 OK` response is shown below.
      ```text
      // Status line
      HTTP/1.1 200 OK
      \r\n                          // CRLF that marks the end of the status line
      
      // Headers
      Content-Type: text/plain\r\n  // Header that specifies the format of the response body
      Content-Length: 3\r\n         // Header that specifies the size of the response body, in bytes
      \r\n                          // CRLF that marks the end of the headers
      
      // Response body
      abc                           // The string from the request
      ```
    - Note that the two headers are required for the client to be able to parse the response body. Note that each header ends in a CRLF, and the entire header section also ends in a CRLF.
- `/echo/{str}` Endpoint Implementation Details:
    1. Entirety of program has been refactored to follow encapsulation principles (decided to do this after four hours and a crash out, LOL). The `main` method has been simplified with two helper methods known as `handleRequest()` and `sendResponse()`. The use of `finally` will ensure that the client socket connection is always closed. The `main` method is shown as below.
       ```java
       public static void main(String[] args) {  
           try {  
               ServerSocket serverSocket = new ServerSocket(4221);  
               serverSocket.setReuseAddress(true);  
               while(true) {  
                   Socket clientSocket = serverSocket.accept();  
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
       ```
    2. The `handleRequest()` method reads incoming requests the client socket is making to the server. We will then parse the request and invoke `sendRequest()` depending on the contents of the path. Note that we take into account possible errors and exceptions by checking for `null` and checking the amount of request arguments. We also have scrapped the `ArrayList` approach to handling request paths due to the `startsWith()` method; this is more concise. The `handleRequest()` method is shown as below.
       ```java
       private static void handleRequest(Socket clientSocket) throws IOException {  
           BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  
           String request = in.readLine();  
         
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
       ```
    3. The `sendRequest()` method will send a response to the client socket's output stream depending on the request body.
       ```java
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
       ```
    - Again, on macOS, you should run `./your_server.sh` in one terminal session, and in another terminal session, some `curl` to send custom requests and debug. For example,`curl -i 127.0.0.1:4221/echo/abc`, where `-i` gives a more verbose HTTP response.
- Websites I found helpful:
    - [IBM's Information on HTTP Responses](https://www.ibm.com/docs/en/cics-ts/6.x?topic=protocol-http-responses)
    - [Very Interesting Website that Breaks Down Shell (again, because it was so useful)](https://explainshell.com/explain?cmd=curl)
    - [Substring method used in *handleRequest()*](https://www.w3schools.com/java/ref_string_substring.asp)
    - [*getBytes()* method used in *sendResponse()*](https://www.geeksforgeeks.org/string-getbyte-method-in-java/)
    - [*startsWith()* method used in *handleRequest()*](https://www.geeksforgeeks.org/string-startswith-method-in-java-with-examples/)
### December 17, 2024
- Implemented the `/user-agent` endpoint, which reads the `User-Agent` request header and returns it in the response body. The [User-Agent](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent) header is a characteristic string that lets servers and network peers identify the application, operating system, vendor, and/or version of the requesting [user agent](https://developer.mozilla.org/en-US/docs/Glossary/User_agent).
- The `User-Agent` Header:
    - The [`User-Agent`](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent) header describes the client's user agent.
    - Here's an example of a `/user-agent` request:
      ```text
      // Request line
      GET
      /user-agent
      HTTP/1.1
      \r\n
      
      // Headers
      Host: localhost:4221\r\n
      User-Agent: foobar/1.2.3\r\n
      Accept: */*\r\n
      \r\n
      
      // Request body (empty)
      ```
      Here is the expected response:
      ```text
      // Status line
      HTTP/1.1 200 OK
      \r\n
      
      // Headers
      Content-Type: text/plain\r\n
      Content-Length: 12\r\n
      \r\n
      
      // Response body
      foobar/1.2.3 // The value of `User-Agent`
      ```
- `User-Agent` Endpoint Implementation Details:
    1. The `getUserAgent()` method will continuously iterate through headers until the `User-Agent` header is reached. The method then returns the contents of the `User-Agent` header.
       ```java
       private static String getUserAgent(BufferedReader in) throws IOException {  
           String header;  
         
           while (true) {  
               header = in.readLine();  
         
               if (header != null && !header.isEmpty()) {  
                   if (header.startsWith("User-Agent:")) {  
                       return header.substring(11).trim();  
                   }  
               }  
           }  
       }
       ```
    2. We update the conditions of our possible responses.
       ```java
       if (path.startsWith("/echo")) {  
           String echoContent = path.substring(6);
           sendResponse(clientSocket, echoContent);  
       } else if (path.startsWith("/user-agent")) {  
           sendResponse(clientSocket, getUserAgent(in));  
       } else if (path.equals("/")) {  
           sendResponse(clientSocket, path);  
       } else {  
           sendResponse(clientSocket, null);  
       }
       ```
    - Note that all of the `getUserAgent()` method can be simplified using the `HttpServletRequest` library. Within this library, we can access different headers using the `getHeader()` method.
      ```java
      import javax.servlet.http.HttpServletRequest;
      
      public void doGet(HttpServletRequest request, HttpServletResponse response) {
      
          // Get the value of the "User-Agent" header
          String userAgent = request.getHeader("User-Agent"); 
      
          // Check if the header exists
          if (userAgent != null) {
              // Do something with the header value 
              System.out.println("User-Agent: " + userAgent); 
          }
      }
      ```
- Websites I found helpful:
    - [The *User-Agent* Header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent)
    - [More information on the *User-Agent* header](https://www.geeksforgeeks.org/http-headers-user-agent/)
    - [What is a user agent?](https://developer.mozilla.org/en-US/docs/Glossary/User_agent)
    - [*substring()* method used in *getUserAgent()*](https://www.geeksforgeeks.org/substring-in-java/)
    - [*trim()* method used in *getUserAgent()*](https://www.geeksforgeeks.org/java-string-trim-method-example/)
    - [The *getHeader()* method within the *HttpServletRequest* library](https://stackoverflow.com/questions/43908224/how-to-catch-the-request-header-in-java-using-httpservletrequest)
    - [More examples of the *HttpServletRequest* library](https://mkyong.com/java/how-to-get-http-request-header-in-java/)
### December 19, 2024
- Added support for concurrent connections (multithreading).
- Multithreading Implementation Details:
    1. Utilize the `ClientHandler` and `Thread` objects. This allows us to refactor our `main` as shown below. Note that using `ClientHandler` includes implementing its `Runnable` methods.
       ```java
       public static void main(String[] args) {  
           try {  
               ServerSocket serverSocket = new ServerSocket(4221);  
               serverSocket.setReuseAddress(true);  
               
               while(true) {  
                   Socket clientSocket = serverSocket.accept();  
                   ClientHandler clientHandler = new ClientHandler(clientSocket);  
                   new Thread(clientHandler).start();  
               }  
           } catch (IOException e) {  
               System.out.println("IOException: " + e.getMessage());  
           }  
       }
       ```
    2. Implementing `Runnable` within `ClientHandler` involves overriding `run()`. Within run, we can invoke our original `handleRequest()` method and eventually `close()` the socket.
       ```java
       class ClientHandler implements Runnable {  
           private final Socket clientSocket;  
         
           public ClientHandler(Socket socket) {  
               this.clientSocket = socket;  
           }  
         
           public void run() {  
               try {  
                   Main.handleRequest(clientSocket);  
               } catch (IOException e) {  
                   System.out.println("Error handling client: " + e.getMessage());  
               } finally {  
                   try {  
                       clientSocket.close();  
                   } catch (IOException e) {  
                       System.out.println("Error closing socket: " + e.getMessage());  
                   }  
               }  
           }  
       }
       ```
    - Note that seeing strange results when using a server testing tool like `oha` likely means to make sure you implement `keep-alive`. It is the default in `HTTP/1.1`, and if not implemented, you'll probably close the connection while the server testing tool still uses it for the next request. This won't present itself as a problem when testing with `curl`, because that's a one-connection-one-request scenario.
- Websites I found helpful:
    - [What is a Multithreaded Server?](https://www.geeksforgeeks.org/multithreaded-servers-in-java/)
    - [Example of a multithreaded server using executors](https://stackoverflow.com/questions/26986783/way-to-stop-java-util-concurrent-executor-task)
    - [Server Testing CLI: **oha**](https://github.com/hatoo/oha)
    - [Turning Our Single-Threaded Server into a Multithreaded Server in Rust](https://doc.rust-lang.org/stable/book/ch20-02-multithreaded.html#turning-our-single-threaded-server-into-a-multithreaded-server)
### December 20, 2024
- Implemented the `/files/{filename}` endpoint, which returns a requested file to the client.
- Note that when running the server, use `./your_program.sh --directory /tmp/`. The `--directory` flag specifies the directory where the files are stored, as an absolute path.
- `/files/{filename}` Endpoint Implementation Details:
    1. We declare a new static variable called `directory`. This will store the path specified by the `--directory` flag. We choose to make the `directory` static so that it is accessible throughout the class. This gives methods within the class the ability to use the specified directory path when needed.
       ```java
       private static String directory;
       ```
    2. We use a `for` loop to parse command-line arguments and find the `--directory` flag. The loop will iterate over the `String[] args` array and assign the argument after the `--directory` flag to be our directory path. This allows for the program to dynamically set the directory where files are stored, based on the input provided when a request is executed.
       ```java
       for (int i = 0; i < args.length; i++) {
         if (args[i].equals("--directory")) {
           directory = args[i + 1];
           break;
         }
       }
       ```
    3. Change the `sendResponse()` method to allow for an additional `String contentType` parameter. This involves updating each `sendResponse()` call to include either `text/plain` or `application/octet-stream` depending on the endpoint requested.
    4. The `getFileContent()` method will read content using a `BufferedReader`. This buffered reader will read from a `FileReader` pointing toward our file `path`. We then use a `StringBuilder` to return the content in the file.
       ```java
       private static String getFileContent(String path) throws IOException {  
           try (BufferedReader reader = new BufferedReader(new FileReader(getFilePath(path)))) {  
               StringBuilder content = new StringBuilder();  
               String line;  
               while ((line = reader.readLine()) != null) {  
                   content.append(line).append(System.lineSeparator());  
               }  
               return content.toString().trim();  
           } catch (FileNotFoundException e) {  
               return null;  
           }  
       }
       ```
    5. The `getFilePath()` method will use the `directory` and the given file name to return a file path.
       ```java
       private static String getFilePath(String requestPath) {  
           String fileName = requestPath.substring(7);  
           return directory + "/" + fileName;  
       }
       ```
- Websites I found helpful:
    - [File Handling in Java](https://www.geeksforgeeks.org/file-handling-in-java/)
    - [How To Read a File Line-By-Line in Java](https://www.digitalocean.com/community/tutorials/java-read-file-line-by-line)
    - [*FileReader* class used in *getFileContent()*](https://www.geeksforgeeks.org/java-filereader-class-read-method-with-examples/)
    - [*BufferedReader* class used in *getFileContent()*](https://www.javatpoint.com/how-to-read-file-line-by-line-in-java)
    - [Information on the *tmp* directory](https://superuser.com/questions/332610/where-is-the-temporary-directory-in-linux/332616)
    - [Working with a Simple Java Web Server](https://inside.java/2021/12/06/working-with-the-simple-web-server/)
### December 22, 2024
- Added support for the `POST` method of the `/files/{filename}` endpoint, which accepts text from the client and creates a new file with that text. The server will create a new file in the `files` directory.
- An example of a `POST /files/{filename}` request:
  ```text
  // Request line
  POST /files/number HTTP/1.1
  \r\n
  
  // Headers
  Host: localhost:4221\r\n
  User-Agent: curl/7.64.1\r\n
  Accept: */*\r\n
  Content-Type: application/octet-stream  // Header that specifies the format of the request body
  Content-Length: 5\r\n                   // Header that specifies the size of the request body, in bytes
  \r\n
  
  // Request Body
  12345
  ```
    - Note that this request is a result of
      ```bash
      curl -v --data "12345" -H "Content-Type: application/octet-stream" http://localhost:4221/files/file_123
      ```
    - The server must return a `201` response:
      ```bash
      HTTP/1.1 201 Created\r\n\r\n
      ```
    - We can test locally using requests such as
      ```bash
      curl -vvv -d "hello world" localhost:4221/files/readme.txt
      ```
- `POST` Method Implementation:
    - Note that [[CS 1331|Georgia Tech's CS 1331]] "File IO" topic was very helpful in developing the logic of this implementation.
    1. Refactored our control flow when invoking `sendResponse()` using a `switch` construct. The use of a switch construct will allow for simple future implementations of different endpoints.
       ```java
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
       ```
       Note the new methods within this `switch` construct like `sendGETResponse()`, `sendPOSTResponse()`, `getAcceptType()`, `setFileContent()`, `getFileContent()`, and `getPostData()`.
    2. The `sendResponse()` method has been refactored into two possible `GET` and `POST` request methods. The `sendGETResponse()` method will retain the logic of our previous `sendResponse()` method.
       ```java
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
       ```
       The `sendPOSTResponse()` method will introduce a `201 Created` response.
       ```java
       private static void sendPOSTResponse(Socket clientSocket, String body) throws IOException {  
           OutputStream out = clientSocket.getOutputStream();  
           if (body != null && !body.isEmpty()) {  
               out.write("HTTP/1.1 201 Created\r\n\r\n".getBytes());  
           } else {  
               out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());  
           }  
       }
       ```
    3. The `getAcceptType()` method will return a specified accept type depending on the request type.
       ```java
       private static String getAcceptType(String path) {  
           if (path.startsWith("/echo") || path.startsWith("/user-agent") || path.equals("/")) {  
               return "text/plain";  
           } else if (path.startsWith("/files")) {  
               return "application/octet-stream";  
           }  
           return "text/plain";  
       }
       ```
    4. The `setFileContent()` method will `write` content to a file given a specified file path.
       ```java
       private static void setFileContent(String filePath, String content) throws IOException {  
           try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {  
               writer.write(content);  
           }  
       }
       ```
    5. The `getFileContent()` method will create a new `StringBuilder` object that will store the contents of a file given its specified path using a `BufferedReader` object. We then return the contents of the file.
       ```java
       private static String getFileContent(String path) throws IOException {  
           try (BufferedReader reader = new BufferedReader(new FileReader(getFilePath(path)))) {  
               StringBuilder content = new StringBuilder();  
               String line;  
               while ((line = reader.readLine()) != null) {  
                   content.append(line).append(System.lineSeparator());  
               }  
               return content.toString().trim();  
           } catch (FileNotFoundException e) {  
               return null;  
           }  
       }
       ```
    6. The `getPostData()` method will return a request's specified contents. We can then write this contents to a specified file.
       ```java
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
       ```
- Websites I found helpful:
    - [Creating files and writing to them in Java](https://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it)
    - [Writing to files in Java](https://www.geeksforgeeks.org/java-program-to-write-into-a-file/)
    - [Interesting article on the Java "NIO" library](https://www.heise.de/en/background/Secure-Coding-Best-practices-for-using-Java-NIO-against-path-traversal-9996787.html)