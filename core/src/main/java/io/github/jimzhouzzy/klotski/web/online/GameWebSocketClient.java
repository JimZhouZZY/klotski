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
 * GameWebSocketClient.java
 * 
 * This class represents a WebSocket client for the online Klotski game.
 * It allows the client to connect to a WebSocket server for online multiplayer mode.
 * It handles sending and receiving messages, as well as reconnection logic.
 * 
 * It should only be initialized in {@link Klotski} class.
 * 
 * @author JimZhouZZY
 * @version 1.15
 * @since 2025-5-25
 * @see {@link Klotski}
 * @see {@link https://github.com/JimZhouZZY/klotski-server}
 *
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: Implement Co-op
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
    public String cooperateUsername = null;

    public String[] onlineUsers = new String[0];

    public GameWebSocketClient(Klotski klotski, URI serverUri) {
        super(serverUri);
        this.klotski = klotski;
    }

    /**
     * Handles the event triggered when a WebSocket connection to the server is successfully opened.
     * This method is invoked by the WebSocket API immediately after a connection is established,
     * allowing the client to perform initial setup or logging. The provided {@link ServerHandshake}
     * parameter contains details about the handshake process, such as HTTP headers or status codes.
     * A typical implementation might log the connection status or initialize resources required
     * for subsequent communication with the server.
     *
     * @param handshakedata The handshake data exchanged during the WebSocket connection setup,
     * including server response headers and other protocol-related information.
     */
    @Override
        public void onOpen(ServerHandshake handshakedata) {
            System.out.println("Connected to server");
        }

    /**
     * Handles incoming messages from the server, processes them, and triggers appropriate actions.
     * This method prints the received message, notifies any registered {@code onMessageListener},
     * and processes messages starting with the "coop:" prefix. For "coop" messages, which follow
     * the format "coop:&lt;source_username&gt;;&lt;target_username&gt;", the method parses the source
     * and target usernames. If the logged-in user matches either the source or target username,
     * the {@code cooperateUsername} field is updated to reflect the cooperating peer. This enables
     * tracking collaboration between users. The method also logs cooperation updates to the console.
     *
     * @param message The raw message received from the server. Expected to be non-null.
     */
    @Override
        public void onMessage(String message) {
            System.out.println("Message from server: " + message);
    
            if (onMessageListener != null) {
                onMessageListener.onMessage(message); // Trigger the callback
            }
            
            if (message.startsWith("coop:")) {
                // message is like "coop:<username_soucrce>;<username_target>"
                // get the source username
                String sourceUsername = message.substring(5, message.indexOf(";")); // Extract the source username
                // get the target username
                String targetUsername = message.substring(message.indexOf(";") + 1); // Extract the target username
                // broadcast the coop message to all users
                if (klotski.getLoggedInUser().equals(sourceUsername)) {
                    // If the logged-in user is the source, set the cooperateUsername
                    this.cooperateUsername = targetUsername;
                    System.out.println("Cooperate with: " + cooperateUsername);
                } else if (klotski.getLoggedInUser().equals(targetUsername)) {
                    // If the logged-in user is the target, set the cooperateUsername
                    this.cooperateUsername = sourceUsername;
                    System.out.println("Cooperate with: " + cooperateUsername);
                }
            }
        }

    /**
     * Handles the closure of the connection by logging the closure reason and conditionally
     * scheduling a reconnection attempt. This method is invoked when the WebSocket connection
     * is closed, either by the remote host or locally. If automatic reconnection is enabled
     * (when {@code isReconnecting} is {@code false}) and the socket is not explicitly marked
     * for closure ({@code closeSocket} is {@code false}), a reconnection will be scheduled.
     *
     * @param code The status code indicating the closure reason (see RFC6455).
     * @param reason A human-readable string explaining the closure details.
     * @param remote {@code true} if the closure was initiated by the remote endpoint,
     * {@code false} if initiated locally.
     */
    @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("Connection closed: " + reason);
            if (!isReconnecting && !closeSocket) {
                scheduleReconnect();
            }
        }

    /**
     * Handles WebSocket errors by logging the error details and scheduling a reconnection attempt if not already in progress.
     * This method prints the error message to the standard error stream, logs the exception's stack trace for debugging,
     * and initiates a reconnection process via {@link #scheduleReconnect()} when a reconnection is not already underway.
     *
     * @param ex The exception that triggered the error, providing details about the encountered issue.
     */
    @Override
        public void onError(Exception ex) {
            System.err.println("WebSocket error: " + ex.getMessage());
            ex.printStackTrace();
            if (!isReconnecting) {
                scheduleReconnect();
            }
        }

    /**
     * Sends the current board state along with the logged-in user's identifier as a formatted message.
     * The message is constructed by concatenating the logged-in user's username, a "boardState" label,
     * and the provided boardState string. This combined message is then dispatched using the {@code send} method.
     *
     * @param boardState A string representation of the current board state to be included in the message.
     */
    public void sendBoardState(String boardState) {
            send(klotski.getLoggedInUser() + ":\n" + "boardState:\n" + boardState);
        }

    /**
     * Schedules an asynchronous reconnection attempt after a predefined delay. This method increments the retry counter
     * and checks if the maximum allowed retries ({@code MAX_RETRIES}) have been exceeded. If exceeded, it logs an error
     * and aborts further attempts. Otherwise, it starts a new thread that waits for {@code RECONNECT_DELAY} milliseconds
     * before invoking {@code createNewClientAndReconnect()} to re-establish the connection. If the waiting thread is
     * interrupted, the interruption is handled, and a message is logged. The reconnection process runs in a separate
     * thread to avoid blocking the caller.
     */
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

    /**
     * Creates a new instance of {@link GameWebSocketClient}, attempts to establish a blocking connection
     * to the WebSocket server using the configured URI, and updates the client reference in the associated
     * {@link Klotski} instance upon success. If the connection attempt fails, an error message is logged
     * and a reconnection attempt is scheduled. This method ensures that the reconnection flag {@code isReconnecting}
     * is reset to {@code false} upon completion, regardless of the connection outcome.
     *
     * <p>Key steps include:
     * <ul>
     * <li>Instantiating a new {@link GameWebSocketClient} with the current {@link Klotski} instance and URI.</li>
     * <li>Attempting a blocking connection to the server, which waits until the connection is established or fails.</li>
     * <li>Updating the active client reference in the {@link Klotski} instance if the connection is successful.</li>
     * <li>Logging a success message upon reconnection or an error message with failure details.</li>
     * <li>Triggering a subsequent reconnection attempt via {@link #scheduleReconnect()} if an exception occurs.</li>
     * <li>Guaranteeing the {@code isReconnecting} flag is reset in the {@code finally} block to allow future reconnection attempts.</li>
     * </ul>
     */
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
        /**
         * Processes the provided message when it is received. This method is typically invoked as a callback
         * to handle incoming messages from a connected source, such as a message queue, network socket,
         * or event-driven system. The exact behavior depends on the implementation, but common actions
         * include parsing the message content, triggering application-specific logic, or forwarding the
         * message to other components. Implementations should ensure thread safety if this method is called
         * concurrently by multiple threads. If the message is malformed or cannot be processed, implementations
         * may log an error, throw a runtime exception, or handle the failure according to the system's requirements.
         *
         * @param message The message received for processing. Must not be {@code null}, though implementations
         * may handle {@code null} values gracefully depending on context (e.g., logging a warning).
         */
        void onMessage(String message);
    }

    private OnMessageListener onMessageListener;

    /**
     * Sets the {@link OnMessageListener} instance that will receive message events.
     * The provided listener will be notified when a message is received, allowing
     * custom handling or propagation of the event. Passing {@code null} removes any
     * previously registered listener. Only one listener can be registered at a time;
     * subsequent calls will overwrite the existing listener.
     *
     * @param listener The listener implementation to register for message events,
     * or {@code null} to unregister the current listener.
     */
    public void setOnMessageListener(OnMessageListener listener) {
            this.onMessageListener = listener;
        }

    /**
     * Receives a message and forwards it to the registered {@link OnMessageListener}, if available.
     * This method acts as a conduit for delivering incoming messages to the listener's callback,
     * enabling asynchronous handling of messages. If no listener is registered (i.e.,
     * {@code onMessageListener} is {@code null}), the message is silently ignored.
     *
     * @param message The message to be processed. This value is passed directly to the listener's
     * {@link OnMessageListener#onMessage(String)} method and should not be {@code null}
     * if the listener expects valid message content.
     */
    public void receiveMessage(String message) {
            if (onMessageListener != null) {
                onMessageListener.onMessage(message);
            }
        }

    /**
     * Checks whether the current connection is active by verifying if it is open.
     * This method returns the status of the connection by delegating to the {@link #isOpen()} method,
     * indicating whether communication or interaction with the connected resource is currently possible.
     *
     * @return {@code true} if the connection is active and open, {@code false} if the connection is closed
     * or no longer available for interaction.
     */
    public boolean isConnected() {
            return this.isOpen();
        }
}
