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
 * CooperateMenuScreen.java
 *
 * This class represents the screen where players can choose a user to cooperate.
 * It connects to a WebSocket server to retrieve the list of online users.
 * Players can click on a user to start cooperating with other players.
 *
 * @author JimZhouZZY
 * @version 1.29
 * @since 2025-5-25
 * @see {@link MenuScreen}
 * @see {@link https://github.com/JimZhouZZY/klotski-server}
 *
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: filter null user
 * 2025-05-27: use equals() to judge strings
 * 2025-05-27: modify font
 * 2025-05-27: Implement Co-op
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: HD-font & UX improvement
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: Remove duplicated network check
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: fix code style
 * 2025-05-24: Make the sepctsteScreen more pretty.
 * 2025-05-24: fix: invalid call to Dialog
 * 2025-05-24: fix: SpectateChoiceScreen doesnt has background
 * 2025-05-24: refactor Dialog
 * 2025-05-24: Highlight the number of the selected number (#14)
 * 2025-05-24: Refactor screens to enheritage ProtoScreen
 * 2025-05-24: Add H,J,K,L for changing the selected  block.
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-20: Merge branch v1.0.5 into main (#7)
 * 2025-05-08: update soundfx
 * 2025-05-07: formal login & prepare in-game spectate
 */

package io.github.jimzhouzzy.klotski.screen.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.screen.CooperateScreen;
import io.github.jimzhouzzy.klotski.screen.core.MenuScreen;
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;
import io.github.jimzhouzzy.klotski.ui.component.Dialog;
import io.github.jimzhouzzy.klotski.web.online.GameWebSocketClient;

public class CooperateMenuScreen extends MenuScreen {

    private final GameWebSocketClient webSocketClient;
    private static final Skin newSkin = new Skin(Gdx.files.internal("skins/cloud-form/skin/cloud-form-ui.json"));

    private Table userListTable; // for scrollable user list
    private Timer.Task refreshOnlineUsersTask;

    public CooperateMenuScreen(final Klotski klotski, GameWebSocketClient webSocketClient, ProtoScreen lastScreen) {
        super(klotski, lastScreen);
        this.webSocketClient = webSocketClient;
    }

    /**
     * Overrides the parent method to set up the UI components for the player cooperation selection screen.
     * Creates a layout table that fills the parent stage and applies10% of the screen height as padding to the top and bottom.
     * Adds a title label with increased font scale and bottom padding to the layout table.
     * Initiates automatic refreshing of the online users list and integrates it into the layout.
     * Adds a "Back" button at the bottom of the table to allow navigation back to the previous screen.
     * All components are added to the stage for rendering and interaction.
     */
    @Override
        protected void create() {
            // Create a table for layout
            Table table = new Table();
            table.setFillParent(true);
    
            // Add 10% screen height padding to the top and bottom
            float screenHeight = Gdx.graphics.getHeight();
            table.padTop(screenHeight * 0.1f).padBottom(screenHeight * 0.1f);
    
            stage.addActor(table);
    
            // Add a title label
            Label titleLabel = new Label("Choose a Player to Cooperate", skin);
            titleLabel.setFontScale(1.5f);
            table.add(titleLabel).padBottom(50).row();
    
            // Request online users from the server
            // requestOnlineUsers(table);
    
            startOnlineUsersAutoRefresh(table);
    
            // Add a "Back" button at the bottom
            addBackButton(table);
        }

    /**
     * Starts an automatic refresh process for online users data in the specified table. This method
     * cancels any existing scheduled refresh task before creating a new one to avoid duplicate updates.
     * The refresh is performed periodically every1 second, with the first update occurring1 second
     * after invocation. The provided table is passed to the {@link #requestOnlineUsers(Table)} method
     * during each refresh cycle to ensure the displayed data remains current.
     *
     * @param table The table component whose online users data will be refreshed automatically.
     */
    private void startOnlineUsersAutoRefresh(Table table) {
            if (refreshOnlineUsersTask != null) {
                refreshOnlineUsersTask.cancel();
            }
            refreshOnlineUsersTask = Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    requestOnlineUsers(table);
                }
            }, 1, 1); // 1s interval
        }

    /**
     * Requests the list of currently online users from the server and updates the provided UI table with the results.
     * If the WebSocket client is not initialized, an error dialog is displayed to inform the user. Upon receiving
     * the list of online users from the server, the method dynamically populates the table with user-specific buttons
     * and adds a "Back" button at the bottom. The UI updates are executed on the main rendering thread to ensure
     * thread safety. The method sends a "GetOnlineUsers" request to the server via the WebSocket client to trigger
     * the data retrieval process.
     *
     * @param table The UI table component to be populated with online user buttons and the "Back" button.
     * Must not be {@code null}.
     */
    private void requestOnlineUsers(Table table) {
            // Check if webSocketClient is null
            if (webSocketClient == null) {
                Dialog.showErrorDialog(klotski, skin, stage, "Unable to connect to the server. Please try again later.");
                return;
            }
    
            // Set a callback to handle the server's response
            webSocketClient.setOnMessageListener(message -> {
                if (message.startsWith("Online users: ")) {
                    String[] users = message.substring("Online users: ".length()).split(", ");
                    Gdx.app.postRunnable(() -> {
                        populateUserButtons(table, users); // Populate user buttons
                        addBackButton(table); // Add the "Back" button at the bottom
                    });
                }
            });
    
            // Send "GetOnlineUsers" request to the server
            webSocketClient.send("GetOnlineUsers");
        }

    /**
     * Populates the specified table with user selection buttons, excluding the currently logged-in user,
     * empty usernames, and the "null" placeholder. Each valid user is represented as a clickable button
     * within a scrollable vertical list. Buttons trigger cooperation requests via {@link #cooperateUser(String)}
     * when clicked. The table includes a title label and a scroll pane with fixed dimensions (800x400 pixels).
     * Buttons are styled with increased font size and spacing, and vertical scrolling is enabled for overflow.
     *
     * @param table The target table to populate with UI components, which is cleared before population.
     * @param users The array of usernames to process, filtering out invalid entries before creating buttons.
     */
    private void populateUserButtons(Table table, String[] users) {
            table.clear();
    
            userListTable = new Table(newSkin);
    
            // Add a title label
            Label titleLabel = new Label("Choose a Player to Cooperate", skin);
            titleLabel.setFontScale(1.5f);
            table.add(titleLabel).padBottom(50).row();
            for (String user : users) {
                if (user.equals(klotski.getLoggedInUser())
                        || user.equals("null") // Skip "null" user
                        || user.isEmpty()) { // Skip empty usernames
                    // Skip yourself
                    continue;
                }
                System.out.println("Adding button for user: " + user);
                TextButton userButton = new TextButton(user, newSkin);
                userButton.getLabel().setFontScale(1.5f); // Increase font size
                userButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        klotski.playClickSound();
                        cooperateUser(user);
                    }
                });
                userListTable.add(userButton).width(300).height(50).padBottom(20).row();
            }
    
            ScrollPane scrollPane = new ScrollPane(userListTable, newSkin);
            scrollPane.setFadeScrollBars(false);
            scrollPane.setScrollingDisabled(true, false); // enable vertical scroll only
            scrollPane.setScrollbarsOnTop(true);
    
            table.add(scrollPane).height(400).width(800).padBottom(30).row();
        }

    /**
     * Adds a "Back" button to the specified {@link Table} with configured styling and behavior.
     * The button is positioned at the bottom of the table with a width of300 units, height of50 units,
     * and a top padding of20 units. When clicked, it plays a click sound effect, triggers an animation
     * to adjust the focal length of the dynamic board (from10000.0f to1.0f), and invokes the
     * {@link #handleBack()} method to handle navigation logic. The button uses the provided skin for
     * visual styling and expands vertically within the table to maintain its bottom placement.
     */
    private void addBackButton(Table table) {
            TextButton backButton = new TextButton("Back", skin);
            backButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();;
                    klotski.dynamicBoard.triggerAnimateFocalLength(10000.0f, 1.0f);
                    handleBack();
                }
            });
            table.add(backButton).width(300).height(50).padTop(20).expandY().bottom(); // Ensure it's at the bottom
        }

    /**
     * Transitions the application to the {@link CooperateScreen} for the specified user. This method
     * initializes and displays a new cooperation screen, passing the necessary context, user identifier,
     * and WebSocket client to facilitate real-time collaboration. It also logs the initiation of the
     * cooperation process by printing a message to the console.
     *
     * @param user the username or identifier of the user with whom cooperation is being initiated.
     */
    private void cooperateUser(String user) {
            System.out.println("Cooperating user: " + user);
            klotski.setScreen(new CooperateScreen(klotski, user, klotski.webSocketClient)); // Navigate to the CooperateScreen
        }

    /** * {@inheritDoc}
     * <p>
     * This implementation extends the parent class's {@code hide()} method by additionally
     * canceling the active {@code refreshOnlineUsersTask} if it exists. After cancellation,
     * the task reference is set to {@code null} to prevent reuse, ensuring that background
     * processes related to refreshing online users are terminated when this component is hidden.
     * </p> */
    @Override
        public void hide() {
            super.hide();
            if (refreshOnlineUsersTask != null) {
                refreshOnlineUsersTask.cancel();
                refreshOnlineUsersTask = null;
            }
        }
}
