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
 * GameWebSocketServer.java
 * 
 * This class represents a local WebSocket server for the Klotski game.
 * It allows multiple clients to connect and communicate with the local server.
 * It is used for offline multiplayer mode.
 * 
 * It should only be initialized in {@link Klotski} class.
 * 
 * @author JimZhouZZY
 * @version 1.11
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
 * 2025-04-29: web inspection
 * 2025-04-22: Settings view
 * 2025-04-16: Login & Levels
 */

package io.github.jimzhouzzy.klotski.web.offline;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import io.github.jimzhouzzy.klotski.Klotski;

public class GameWebSocketServer extends WebSocketServer {
    private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());
    private Klotski klotski;

    public GameWebSocketServer(Klotski klotski, int port) {
        super(new InetSocketAddress(port));
        this.klotski = klotski;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message received: " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port " + getPort());
    }

    public void broadcastGameState(String gameState) {
        synchronized (connections) {
            for (WebSocket conn : connections) {
                conn.send(gameState);
            }
        }
    }

    public void close() {
        try {
            // Close all active WebSocket connections
            synchronized (connections) {
                for (WebSocket conn : connections) {
                    conn.close(1000, "Server shutting down"); // Close with normal closure code
                }
                connections.clear(); // Clear the connections set
            }

            // Stop the WebSocket server
            stop();
            System.out.println("WebSocket server stopped.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while closing WebSocket server: " + e.getMessage());
        }
    }
}
