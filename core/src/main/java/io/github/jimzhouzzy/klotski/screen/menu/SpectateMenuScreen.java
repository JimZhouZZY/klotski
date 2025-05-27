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
 * SpectateMenuScreen.java
 *
 * This class represents the screen where players can choose a user to spectate.
 * It connects to a WebSocket server to retrieve the list of online users.
 * Players can click on a user to start spectating their game.
 *
 * @author JimZhouZZY
 * @version 1.26
 * @since 2025-5-25
 * @see {@link MenuScreen}
 * @see {@link https://github.com/JimZhouZZY/klotski-server}
 *
 * Change log:
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
import io.github.jimzhouzzy.klotski.screen.SpectateScreen;
import io.github.jimzhouzzy.klotski.screen.core.MenuScreen;
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;
import io.github.jimzhouzzy.klotski.ui.component.Dialog;
import io.github.jimzhouzzy.klotski.web.online.GameWebSocketClient;

public class SpectateMenuScreen extends MenuScreen {

    private final GameWebSocketClient webSocketClient;
    private static final Skin newSkin = new Skin(Gdx.files.internal("skins/cloud-form/skin/cloud-form-ui.json"));

    private Table userListTable; // for scrollable user list
    private Timer.Task refreshOnlineUsersTask;

    public SpectateMenuScreen(final Klotski klotski, GameWebSocketClient webSocketClient, ProtoScreen lastScreen) {
        super(klotski, lastScreen);
        this.webSocketClient = webSocketClient;
    }

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
        Label titleLabel = new Label("Choose a Player to Spectate", skin);
        titleLabel.setFontScale(1.5f);
        table.add(titleLabel).padBottom(50).row();

        // Request online users from the server
        // requestOnlineUsers(table);

        startOnlineUsersAutoRefresh(table);

        // ** TEST **
        // Manually populate user list for testing
        // populateUserButtons(table, new String[]{"Alice", "Bob", "Charlie", "Dana","1","2","3","4","5","6","7","8","9"});

        // Add a "Back" button at the bottom
        addBackButton(table);
    }

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

    private void populateUserButtons(Table table, String[] users) {
        table.clear();

        userListTable = new Table(newSkin);

        // Add a title label
        Label titleLabel = new Label("Choose a Player to Spectate", skin);
        titleLabel.setFontScale(1.5f);
        table.add(titleLabel).padBottom(50).row();
        for (String user : users) {
            if (user == klotski.getLoggedInUser()) {
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
                    spectateUser(user);
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

    private void spectateUser(String user) {
        System.out.println("Spectating user: " + user);
        klotski.setScreen(new SpectateScreen(klotski, user, klotski.webSocketClient)); // Navigate to the SpectateScreen
    }

    @Override
    public void hide() {
        super.hide();
        if (refreshOnlineUsersTask != null) {
            refreshOnlineUsersTask.cancel();
            refreshOnlineUsersTask = null;
        }
    }
}
