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
 * @version 1.14
 * @since 2025-5-25
 * @see {@link Klotski}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
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

    /**
     * Invoked when a new WebSocket connection is successfully established. The connection is added to an
     * internal collection of active connections for tracking and management. The method also logs the
     * remote socket address of the newly connected client.
     *
     * @param conn The WebSocket instance representing the newly established connection.
     * @param handshake The handshake details exchanged during the WebSocket protocol opening handshake,
     * including headers and other protocol-specific metadata.
     * @Override
     */
    @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            connections.add(conn);
            System.out.println("New connection: " + conn.getRemoteSocketAddress());
        }

    /**
     * Handles the closing of a WebSocket connection by removing it from the active connections list
     * and logging the closure event. This method is invoked when the connection is closed, either by
     * the remote peer or locally. The connection details, status code, and closure reason are provided
     * for informational purposes.
     *
     * @param conn The WebSocket connection that was closed.
     * @param code The status code indicating the reason for closure (e.g., normal, error).
     * @param reason A string explaining the closure in more detail.
     * @param remote {@code true} if the closure was initiated by the remote peer, {@code false} otherwise.
     */
    @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            connections.remove(conn);
            System.out.println("Connection closed: " + conn.getRemoteSocketAddress());
        }

    /**
     * Handles an incoming text message received through a WebSocket connection.
     * This method is automatically invoked when a message is received from the remote
     * endpoint associated with the specified WebSocket connection. The message content
     * is logged to the standard output stream for debugging or monitoring purposes.
     *
     * @param conn The WebSocket connection through which the message was received.
     * @param message The text message payload sent by the remote endpoint.
     * Guaranteed to be non-null and valid UTF-8 encoded data.
     * @implSpec This implementation prints the received message to {@code System.out}
     * with a prefix indicating a received message. Override this method
     * to provide custom message-handling logic.
     * @override
     */
    @Override
        public void onMessage(WebSocket conn, String message) {
            System.out.println("Message received: " + message);
        }

    @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

    /**
     * Called when the WebSocket server has successfully started. This method
     * outputs a message to the standard console indicating the server is running
     * and specifies the port number it is listening on, retrieved via {@link #getPort()}.
     * <p>
     * This method overrides the parent class's implementation to provide a startup
     * notification specific to the WebSocket server's initialization process.
     */
    @Override
        public void onStart() {
            System.out.println("WebSocket server started on port " + getPort());
        }

    /**
     * Broadcasts the specified game state to all currently connected WebSocket clients.
     * This method synchronizes on the connections collection to ensure thread-safe iteration
     * and prevent concurrent modification while sending data. Each active connection in the
     * connections collection receives the game state string via its WebSocket interface.
     *
     * @param gameState The serialized game state data to send to all connected clients,
     * typically representing the current state of the game as a string
     * (e.g., JSON or other formatted data).
     */
    public void broadcastGameState(String gameState) {
            synchronized (connections) {
                for (WebSocket conn : connections) {
                    conn.send(gameState);
                }
            }
        }

    /**
     * Closes all active WebSocket connections, clears the connections set, and stops the WebSocket server.
     * This method synchronizes on the internal connections collection to safely iterate and close each
     * WebSocket connection with a normal closure status code (1000) and a message indicating server shutdown.
     * After closing all connections, the server is stopped, and a confirmation message is printed upon success.
     * If an exception occurs during the process, the error is caught, logged to the standard error stream,
     * and the stack trace is printed for debugging purposes. This ensures graceful termination of resources.
     */
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
