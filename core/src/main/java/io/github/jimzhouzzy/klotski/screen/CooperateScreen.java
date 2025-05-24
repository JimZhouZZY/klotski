/**
 * CooperateScreen.java
 * 
 * This class represents the cooperative game screen in the Klotski game.
 * It allows two players to play together in a cooperative mode.
 * The screen connects to a WebSocket server to synchronize game state between players.
 * It is enherited from the {@link GameScreen} class.
 * 
 * @author JimZhouZZY
 * @version 1.6
 * @since 2025-5-25
 * @see {@link GameScreen}
 * @see {@link https://github.com/JimZhouZZY/klotski-server}
 * 
 * Change log:
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: Refactor screens to enheritage ProtoScreen
 */

package io.github.jimzhouzzy.klotski.screen;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.web.online.GameWebSocketClient;

public class CooperateScreen extends GameScreen {

    private String username;
    private GameWebSocketClient webSocketClient;

    public CooperateScreen(final Klotski klotski, String username, GameWebSocketClient webSocketClient) {
        super(klotski);
        this.username = username;
        this.webSocketClient = webSocketClient;
        connectWebSocket();
    }


    private void connectWebSocket() {
        System.out.println("SpectateScreen is trying to connect the websocket");
        if (webSocketClient == null || !webSocketClient.isConnected()) {
            System.err.println("WebSocket client is not connected.");
            return;
        }

        webSocketClient.setOnMessageListener(message -> {
            System.out.println("Spec Message from server: " + message);
            if (message.startsWith("Board state updated:") && message.contains(username + ":")) {
                String state = message
                        .replace("Board state updated:", "")
                        .replace(username + ":", "")
                        .trim();

                // Parse the board state
                System.out.println("Received and trimmed board state: " + state);
                game.fromString(state);
                updateBlocksFromGame(game);
            }
        });
    }
}
