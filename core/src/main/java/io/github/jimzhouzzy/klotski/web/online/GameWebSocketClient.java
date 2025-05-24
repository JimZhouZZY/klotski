/**
 * GameWebSocketClient.java
 * 
 * This class represents a WebSocket client for the online Klotski game.
 * It allows the client to connect to a WebSocket server for online multiplayer mode.
 * It handles sending and receiving messages, as well as reconnection logic.
 * 
 * It should only be initialized in {@link Klotski} class.
 * 
 * @author JimZhouZZY
 * @version 1.10
 * @since 2025-5-25
 * @see {@link Klotski}
 * @see {@link https://github.com/JimZhouZZY/klotski-server}
 *
 * Change log:
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-20: Merge branch v1.0.5 into main (#7)
 * 2025-05-07: formal login & prepare in-game spectate
 * 2025-04-29: web inspection
 */

package io.github.jimzhouzzy.klotski.web.online;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import io.github.jimzhouzzy.klotski.Klotski;

public class GameWebSocketClient extends WebSocketClient {
    private static final int MAX_RETRIES = 999999; // Maximum number of reconnection attempts
    private static final int RECONNECT_DELAY = 3000; // Delay between reconnection attempts (in milliseconds)

    private int retryCount = 0; // Current retry attempt
    private boolean isReconnecting = false; // Flag to prevent overlapping reconnections
    public boolean closeSocket = false; // Flag to indicate if the socket should be closed
    private Klotski klotski;

    public String[] onlineUsers = new String[0];

    public GameWebSocketClient(Klotski klotski, URI serverUri) {
        super(serverUri);
        this.klotski = klotski;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Message from server: " + message);

        if (onMessageListener != null) {
            onMessageListener.onMessage(message); // Trigger the callback
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + reason);
        if (!isReconnecting && !closeSocket) {
            scheduleReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
        if (!isReconnecting) {
            scheduleReconnect();
        }
    }

    public void sendBoardState(String boardState) {
        send(klotski.getLoggedInUser() + ":\n" + "boardState:\n" + boardState);
    }

    private void scheduleReconnect() {
        isReconnecting = true;
        retryCount++;

        if (retryCount > MAX_RETRIES) {
            System.err.println("Max reconnection attempts reached. Giving up.");
            return;
        }

        System.out.println("Attempting to reconnect... (Attempt " + retryCount + " of " + MAX_RETRIES + ")");
        new Thread(() -> {
            try {
                Thread.sleep(RECONNECT_DELAY); // Wait before reconnecting
                createNewClientAndReconnect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Reconnection interrupted.");
            }
        }).start();
    }

    private void createNewClientAndReconnect() {
        try {
            // Create a new instance of GameWebSocketClient
            GameWebSocketClient newClient = new GameWebSocketClient(klotski, this.getURI());
            newClient.connectBlocking(); // Attempt to connect
            klotski.setGameWebSocketClient(newClient); // Update the reference in Klotski
            System.out.println("Reconnected to WebSocket server.");
        } catch (Exception e) {
            System.err.println("Error during reconnection: " + e.getMessage());
            scheduleReconnect(); // Retry if reconnection fails
        } finally {
            isReconnecting = false;
        }
    }

    public interface OnMessageListener {
        void onMessage(String message);
    }

    private OnMessageListener onMessageListener;

    public void setOnMessageListener(OnMessageListener listener) {
        this.onMessageListener = listener;
    }

    public void receiveMessage(String message) {
        if (onMessageListener != null) {
            onMessageListener.onMessage(message);
        }
    }

    public boolean isConnected() {
        return this.isOpen();
    }
}
