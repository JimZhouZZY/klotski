package io.github.jimzhouzzy.klotski;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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