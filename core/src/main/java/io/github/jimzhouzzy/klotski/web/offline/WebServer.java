package io.github.jimzhouzzy.klotski.web.offline;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;

public class WebServer {
    HttpServer server;

    public WebServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", exchange -> {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("web/index.html");

            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }

                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.length());

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.toString().getBytes());
                    }
                }
            } else {
                String errorMessage = "404 - File Not Found";
                exchange.sendResponseHeaders(404, errorMessage.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errorMessage.getBytes());
                }
            }
        });

        server.start();
        System.out.println("HTTP server started on http://127.0.0.1:" + port);
    }

    public void close() {
        server.stop(0);
    }

    public static void main(String[] args) {
        try {
            int port = 8013;  // You can change this port as needed
            new WebServer(port);  // Create an instance of WebServer to start the server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
