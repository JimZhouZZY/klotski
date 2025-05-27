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
 * CooperateScreen.java
 * 
 * This class represents the cooperative game screen in the Klotski game.
 * It allows two players to play together in a cooperative mode.
 * The screen connects to a WebSocket server to synchronize game state between players.
 * It is enherited from the {@link GameScreen} class.
 * 
 * @author JimZhouZZY
 * @version 1.11
 * @since 2025-5-25
 * @see {@link GameScreen}
 * @see {@link https://github.com/JimZhouZZY/klotski-server}
 * 
 * Change log:
 * 2025-05-27: add try .. catch ... to avoid crashing
 * 2025-05-27: Implement Co-op
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
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
        webSocketClient.send("coop:" + klotski.getLoggedInUser() + ";" + username);
    }


    private void connectWebSocket() {
        System.out.println("CooperateScreen is trying to connect the websocket");
        if (webSocketClient == null || !webSocketClient.isConnected()) {
            System.err.println("WebSocket client is not connected.");
            return;
        }

        webSocketClient.setOnMessageListener(message -> {
            System.out.println("Spec Message from server: " + message);
            if (message.startsWith("Board state updated:") 
                    && (message.contains(username + ":")) 
                        || message.contains(webSocketClient.cooperateUsername + ":")
                ) {
                String state = message
                        .replace("Board state updated:", "")
                        .replace(username + ":", "")
                        .trim();

                // Parse the board state
                System.out.println("Received and trimmed board state: " + state);
                try {
                    game.fromString(state);
                    updateBlocksFromGame(game);
                } catch (Exception e) {
                    System.err.println("Failed to update board state: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
