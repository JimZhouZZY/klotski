/*
 * Copyright (C) 2025 Zhiyu Zhou (jimzhouzzy@gmail.com)
 * This file is part of github.com/jimzhouzzy/Klotski.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * WebServer.java
 * 
 * This class represents a simple local HTTP server that serves an HTML file.
 * It is used for offline mode spectating in the browser.
 * 
 * It should only be initialized in {@link Klotski} class.
 * 
 * @author JimZhouZZY
 * @version 1.9
 * @since 2025-5-25
 * @see {@link Klotski}
 * 
 * Change log:
 * 2025-05-26: Copyright Header
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-04-22: Settings view
 */

package io.github.jimzhouzzy.klotski.web.offline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

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
}
