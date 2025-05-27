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
 * Klotski.java
 * 
 * Main class for the Klotski game, responsible for initializing the game,
 * handling basic user interactions, and managing game resources.
 * 
 * This class extends the LibGDX Game class and serves as the entry point
 * for the application. It sets up the game environment, including the
 * graphics, audio, and user interface. It also manages the game state,
 * including the current screen and user settings.
 * 
 * @author JimZhouZZY
 * @version 1.47
 * @since 2025-5-25
 * @see {@link libgdx.Game}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: modify font
 * 2025-05-27: fix white line
 * 2025-05-27: Show error dialog when load-save failed
 * 2025-05-27: debug auto-login
 * 2025-05-27: make GameScreen seperate
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: refactor util code to ColorHelper and RandomHelper
 * 2025-05-25: remove deprecated loadColors method
 * 2025-05-25: Show dialog instead of error when changing MSAA settings
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: fix: invalid call to Dialog
 * 2025-05-24: refactor Dialog
 * 2025-05-24: Refactor screens to enheritage ProtoScreen
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-21: bug fix (#10)
 * 2025-05-21: code clean up
 * 2025-05-20: Merge branch v1.0.7 into main (#8)
 * 2025-05-08: update soundfx
 * 2025-05-07: formal login & prepare in-game spectate
 * 2025-04-30: optimize local storage
 * 2025-04-29: web inspection
 * 2025-04-29: offline mode & optimize save-load
 * 2025-04-24: fix resize changed base tile size
 * 2025-04-24: MSAA & Settings
 * 2025-04-23: better main screen
 * 2025-04-22: soundtrace.ogg
 * 2025-04-22: better dark mode
 * 2025-04-22: Settings view
 * 2025-04-16: Login & Levels
 * 2025-04-16: Login & Game Mode & Save-Load
 * 2025-04-16: Timer & Moves
 * 2025-04-15: refactor & basic undo redo
 * 2025-04-14: Auto solve & unresizable & exit
 * 2025-04-13: feat: restart hint and congratulations
 * 2025-04-09: drag and collide
 * 2025-04-08: libgdx basic GUI
 * 2025-04-08: init libgdx
 */

package io.github.jimzhouzzy.klotski;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.jimzhouzzy.klotski.screen.GameScreen;
import io.github.jimzhouzzy.klotski.screen.AboutScreen;
import io.github.jimzhouzzy.klotski.screen.LoginScreen;
import io.github.jimzhouzzy.klotski.screen.MainScreen;
import io.github.jimzhouzzy.klotski.screen.SettingsScreen;
import io.github.jimzhouzzy.klotski.screen.menu.SpectateMenuScreen;
import io.github.jimzhouzzy.klotski.ui.DynamicBoard;
import io.github.jimzhouzzy.klotski.ui.component.Dialog;
import io.github.jimzhouzzy.klotski.ui.component.KlotskiTheme;
import io.github.jimzhouzzy.klotski.util.ConfigPathHelper;
import io.github.jimzhouzzy.klotski.util.RandomHelper;
import io.github.jimzhouzzy.klotski.web.offline.GameWebSocketServer;
import io.github.jimzhouzzy.klotski.web.offline.WebServer;
import io.github.jimzhouzzy.klotski.web.online.GameWebSocketClient;

public class Klotski extends Game {
    private GameWebSocketServer webSocketServer;
    public GameWebSocketClient webSocketClient;

    private final String LOGIN_STATUS_FILE = ConfigPathHelper.getConfigFilePath("Klotski", "login_status.dat");
    public SpriteBatch batch;
    public BitmapFont font;
    public FitViewport viewport;
    public GameScreen gameScreen; // for ref only, should not be used directly
    public MainScreen mainScreen;
    public SpectateMenuScreen spectateChoiceScreen;
    public DynamicBoard dynamicBoard;
    public WebServer webServer;
    public SettingsScreen settingsScreen;
    public AboutScreen helpScreen;
    public Music backgroundMusic;
    public Music backgroundMusicLight;
    public Music backgroundMusicDark;
    public LoginScreen loginScreen;
    public RandomHelper randomHelper;
    private boolean arrowControlsEnabled = true;

    public KlotskiTheme klotskiTheme;
    private String loggedInUser; // Field to store the logged-in user's name
    private boolean antialiasingEnabled = true; // Default to enabled
    private boolean vsyncEnabled = true; // Default to enabled
    private Lwjgl3ApplicationConfiguration lwjgl3Config;
    private Skin skin;
    private boolean musicEnabled;
    private boolean isOfflineMode;
    private String token;

    // Should be final
    private Sound alertSound;
    private Sound clickSound;
    private Sound blockCollideSound;

    private Color glClearColor;

    /**
     * Initializes the game environment by setting up core components, resources, and services. This includes:
     * - Creating a random helper instance
     * - Setting background color and theme music (light theme by default)
     * - Loading UI skin and configuring font/viewport
     * - Establishing WebSocket connections (both client and server) unless in offline mode
     * - Starting local web server on port8013
     * - Creating game screens (Settings, Login, Main) and setting initial screen
     * - Initializing WebSocket server on port8014
     * - Configuring custom cursor graphics
     * - Preloading sound effects for UI interactions and game events
     * - Handling login status loading and connection timeouts
     * - Managing resource disposal for cursor pixmaps
     * The method follows a specific initialization order where music loading precedes configuration loading,
     * and settings screen creation precedes login status checks.
     */
    public void create() {
            // Initialize random helper
            randomHelper = new RandomHelper();
    
            // Placeholder defaults to light theme color
            this.glClearColor = new Color(0.68f, 0.85f, 0.9f, 1);
    
            // Load the music file
            // MUST before load configurations
            backgroundMusicLight = Gdx.audio.newMusic(Gdx.files.internal("assets/sound_fx/light_theme.mp3"));
            backgroundMusicLight.setLooping(true);
            backgroundMusicLight.setVolume(1f);
    
            backgroundMusicDark = Gdx.audio.newMusic(Gdx.files.internal("assets/sound_fx/dark_theme.mp3"));
            backgroundMusicDark.setLooping(true);
            backgroundMusicDark.setVolume(1f);
    
            backgroundMusic = backgroundMusicLight; // Default to light theme, place holder
    
            // Load the skin for UI components
            skin = new Skin(Gdx.files.internal("skins/comic/skin/comic-ui.json"));
    
            batch = new SpriteBatch();
            // use libGDX's default font
            font = new BitmapFont();
            viewport = new FitViewport(8, 5);
    
            // font has 15pt, but we need to scale it to our viewport by ratio of viewport
            // height to screen height
            font.setUseIntegerPositions(false);
            font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());
    
            // Start online websocket client if not in offline mode
            if (!isOfflineMode()) {
                try {
                    // Connect to the WebSocket server
                    URI serverUri = new URI("ws://42.194.132.147:8002");
                    webSocketClient = new GameWebSocketClient(this, serverUri);
                    System.out.println("Attempting to connect to WebSocket server: " + serverUri);
    
                    // Attempt to connect with a timeout
                    boolean connected = webSocketClient.connectBlocking(5, TimeUnit.SECONDS);
    
                    if (connected) {
                        System.out.println("WebSocket connection established successfully.");
                    } else {
                        System.err.println("Failed to connect to WebSocket server, timed out.");
                    }
                } catch (Exception e) {
                    System.err.println("Error initializing WebSocket client: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Offline mode enabled. Skipping WebSocket client initialization.");
            }
    
            // Cresate dynamic board before screens
            this.dynamicBoard = new DynamicBoard(this, null);
    
            // After the user loading, settings screen must come first to load settings
            this.settingsScreen = new SettingsScreen(this);
            loadLoginStatus(); // Call this before loginScreen and after settingsScreen
            this.loginScreen = new LoginScreen(this);
            this.mainScreen = new MainScreen(this);
            this.setScreen(mainScreen);
    
            // Start local web socket server
            webSocketServer = new GameWebSocketServer(this, 8014);
            webSocketServer.start();
    
            // Start html web server
            try {
                webServer = new WebServer(8013);
            } catch (IOException e) {
                e.printStackTrace();
            }
    
            // Set custom cursor
            Pixmap originalPixmap = new Pixmap(Gdx.files.internal("assets/image/cursor.png"));
    
            Pixmap resizedPixmap = new Pixmap(32, 32, originalPixmap.getFormat());
            resizedPixmap.drawPixmap(originalPixmap,
                    0, 0, originalPixmap.getWidth(), originalPixmap.getHeight(),
                    0, 0, resizedPixmap.getWidth(), resizedPixmap.getHeight());
    
            int xHotspot = 7, yHotspot = 1;
            Cursor cursor = Gdx.graphics.newCursor(resizedPixmap, xHotspot, yHotspot);
            resizedPixmap.dispose();
            originalPixmap.dispose();
            Gdx.graphics.setCursor(cursor);
    
            // Preload sound effects, should be treated like final
            // this WAV version is not working, idky
            alertSound = Gdx.audio.newSound(Gdx.files.internal("assets/sound_fx/ui_alert.ogg"));
            clickSound = Gdx.audio.newSound(Gdx.files.internal("assets/sound_fx/ui_click.ogg"));
            blockCollideSound = Gdx.audio.newSound(Gdx.files.internal("assets/sound_fx/block_collide.ogg"));
        }

    /**
     * Retrieves the username of the currently logged-in user. If no user is logged in,
     * the method returns the default value "Guest".
     *
     * @return A string representing the logged-in user's name, or "Guest" if no user is logged in.
     */
    public String getLoggedInUser() {
            if (loggedInUser == null) {
                return "Guest"; // If no user is logged in, return "Guest"
            }
            return loggedInUser; // Return the logged-in user's name
        }

    /**
     * Sets the logged-in user's credentials and persists the login status. This method updates the
     * current user's username and associated authentication token, then triggers a save operation
     * to ensure the login state is retained across sessions or application restarts.
     *
     * @param username The username of the logged-in user. This value should correspond to an
     * authenticated user's identifier and cannot be {@code null} or empty.
     * @param token The authentication token associated with the logged-in user. This token is
     * typically used for subsequent authorized operations and must be valid
     * to maintain session integrity.
     */
    public void setLoggedInUser(String username, String token) {
            this.loggedInUser = username; // Set the logged-in user's name
            this.token = token; // Set the token
            saveLoginStatus(); // Save the login status whenever it changes
        }

    /**
     * Sets the logged-in user's username and persists the login status.
     *
     * This method updates the currently logged-in user to the specified username
     * and immediately triggers a save operation to retain the updated login state.
     * It should be called whenever the user's authentication status changes to ensure
     * consistent tracking of the active user session.
     *
     * @param username the name of the user to set as the logged-in user;
     * may be {@code null} to clear the logged-in user.
     */
    public void setLoggedInUser(String username) {
            this.loggedInUser = username; // Set the logged-in user's name
            saveLoginStatus(); // Save the login status whenever it changes
        }

    /**
     * Loads the login status from a predefined file and attempts to validate the stored credentials.
     * If the login status file does not exist, the method exits early. In offline mode, the previously
     * logged-in user is restored without server validation. When online, the stored token is sent to
     * a remote server for validation. If the token is valid, the user is automatically logged in,
     * and a WebSocket client connection is initiated. Invalid tokens, server errors, or missing
     * credentials reset the logged-in user to "Guest". Exceptions during file operations or network
     * communication are caught and logged to standard error.
     *
     * <p>Key steps:
     * <ul>
     * <li>Checks for the existence of the login status file at {@code LOGIN_STATUS_FILE}.</li>
     * <li>Reads the stored username and token from the file if available.</li>
     * <li>Defaults to "Guest" if no valid username is found or token validation fails.</li>
     * <li>In offline mode, restores the previous user immediately and skips server communication.</li>
     * <li>Validates the token via a POST request to {@code http://42.194.132.147:8001/login} when online.</li>
     * <li>Updates the user session and WebSocket connection on successful token validation.</li>
     * <li>Handles network errors, invalid responses, and file read/write exceptions gracefully.</li>
     * </ul>
     */
    private void loadLoginStatus() {
            File file = new File(LOGIN_STATUS_FILE);
            if (!file.exists()) {
                return; // No login status file exists yet
            }
    
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String prevLoggedInUser = reader.readLine(); // Read the logged-in username
                String prevToken = reader.readLine(); // Read the token
    
                if (prevLoggedInUser == null || prevLoggedInUser.isEmpty()) {
                    prevLoggedInUser = "Guest"; // Default to "Guest" if no user is logged in
                    return;
                }
    
                if (this.isOfflineMode) {
                    System.out.println("Offline mode is enabled. Using previous login status.");
                    System.out.println("Offline mode is enabled. Using previous login status.");
                    System.out.println("Offline mode is enabled. Using previous login status.");
                    System.out.println("Offline mode is enabled. Using previous login status.");
                    System.out.println("Offline mode is enabled. Using previous login status.");
                    // If in offline mode, we do not need to load the login status
                    this.setLoggedInUser(prevLoggedInUser);
                    return;
                }
    
                if (prevToken == null || prevToken.isEmpty()) {
                    // We prevously logged in at offline mode
                    this.setLoggedInUser("Guest");
                }
    
                URL url = new URL("http://42.194.132.147:8001/login");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
    
                // Request body
                String requestBody = "token=" + prevToken;
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(requestBody.getBytes());
                    os.flush();
                }
    
                // Wait for response
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String response = in.readLine();
                        if (response.startsWith("success:")) {
                            System.out.println("Token is valid. User logged in automatically.");
                            String token = response.split(":")[1];
    
                            this.setLoggedInUser(prevLoggedInUser, token);
    
                            // Start WebSocket client
                            webSocketClient.send("login:" + prevLoggedInUser);
                            System.out.println("WebSocket client started for user: " + prevLoggedInUser);
                        } else {
                            System.out.println("Token is invalid or expired. Please log in again.");
                        }
                    }
                } else {
                    System.out.println("Failed to connect to the server. Response code: " + responseCode);
                }
            } catch (IOException e) {
                System.err.println("Failed to load login status: " + e.getMessage());
            }
        }

    /**
     * Saves the current login status by writing the logged-in user's username and authentication token
     * to a persistent file. The username is written on the first line and the token on the second line.
     * If no user is logged in (loggedInUser is {@code null}), an empty string is written for the username.
     * Similarly, an empty string is written for the token if it is {@code null}. The file is overwritten
     * each time this method is called. If an I/O error occurs during writing, an error message is printed
     * to the standard error stream, but no exception is propagated.
     *
     * @implSpec The file is created or truncated in the default encoding format. Data is flushed immediately
     * after writing to ensure persistence. The file path is determined by the constant {@code LOGIN_STATUS_FILE}.
     *
     * @throws SecurityException if a security manager exists and denies write access to the file.
     */
    public void saveLoginStatus() {
            File file = new File(LOGIN_STATUS_FILE);
    
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(loggedInUser != null ? loggedInUser : ""); // Save the username or an empty string
                writer.newLine(); // Write a newline character
                writer.write(token != null ? token : ""); // Save the token or an empty string
                writer.flush(); // Ensure all data is written to the file
            } catch (IOException e) {
                System.err.println("Failed to save login status: " + e.getMessage());
            }
        }

    /**
     * Clears the current logged-in user status and removes the login status file if it exists.
     * This method resets the {@code loggedInUser} to {@code null} to indicate no active user session
     * and deletes the persistent login status file stored at {@link #LOGIN_STATUS_FILE} to ensure
     * no residual login data remains. If the file does not exist, no action is taken for the file deletion step.
     * This is typically called during user logout or session termination to maintain security and data consistency.
     */
    public void clearLoginStatus() {
            loggedInUser = null;
            File file = new File(LOGIN_STATUS_FILE);
            if (file.exists()) {
                file.delete(); // Delete the login status file
            }
        }

    /**
     * Renders the component by invoking the superclass's render method. This delegates
     * the rendering logic to the parent class implementation, ensuring any rendering
     * behavior defined in the superclass is executed. This method is typically called
     * as part of the component's lifecycle to display or update its visual representation.
     * Overriding this method allows customization while preserving the superclass's
     * core rendering functionality.
     */
    public void render() {
            super.render();
        }

    /**
     * Disposes of all managed resources and gracefully shuts down the application. This includes:
     * <ul>
     * <li>Disposing LibGDX resources such as the rendering batch and font.</li>
     * <li>Stopping and closing the WebSocket server (if active), ensuring graceful termination and socket closure.</li>
     * <li>Closing the WebSocket client (if active), marking the socket for closure and waiting for termination.</li>
     * <li>Shutting down the web server (if running) and releasing its resources.</li>
     * <li>Disposing of background music resources and dereferencing them for garbage collection.</li>
     * <li>Exiting the LibGDX application framework and terminating the JVM via {@code System.exit(0)}.</li>
     * </ul>
     * Handles exceptions during resource cleanup by printing stack traces. Logs a confirmation message
     * upon completion. Ensures all resources are dereferenced to facilitate garbage collection.
     */
    public void dispose() {
            // Dispose of LibGDX resources
            batch.dispose();
            font.dispose();
    
            // Stop and close the WebSocket server
            if (webSocketServer != null) {
                try {
                    webSocketServer.stop(); // Gracefully stop the server
                } catch (Exception e) {
                    e.printStackTrace();
                }
                webSocketServer.close(); // Ensure the server is fully closed
                webSocketServer = null; // Dereference for garbage collection
            }
    
            // Stop and close the WebSocket client
            if (webSocketClient != null) {
                try {
                    webSocketClient.closeSocket = true; // Ensure the client socket is marked for closure
                    webSocketClient.closeBlocking(); // Wait for the client to close
                } catch (Exception e) {
                    e.printStackTrace();
                }
                webSocketClient = null; // Dereference for garbage collection
            }
    
            // Stop and close the web server
            if (webServer != null) {
                try {
                    webServer.close(); // Gracefully stop the web server
                } catch (Exception e) {
                    e.printStackTrace();
                }
                webServer = null; // Dereference for garbage collection
            }
    
            // Dispose of background music
            if (backgroundMusic != null) {
                backgroundMusic.dispose();
                backgroundMusic = null; // Dereference for garbage collection
            }
    
            Gdx.app.exit();
            System.out.println("Klotski disposed");
            System.exit(0);
        }

    /**
     * Retrieves the current instance of the {@link GameWebSocketServer} associated with this object.
     * This method provides access to the WebSocket server instance, which is responsible for managing
     * real-time communication between clients and the game server via WebSocket connections. The returned
     * instance can be used to interact with active sessions, broadcast messages, or configure server settings.
     *
     * @return the active {@link GameWebSocketServer} instance, or {@code null} if the server has not been
     * initialized or started.
     */
    public GameWebSocketServer getWebSocketServer() {
            return webSocketServer;
        }

    /**
     * Retrieves the GameWebSocketClient instance associated with this object. This method provides
     * access to the web socket client used for real-time communication with the game server, allowing
     * interaction with connection events, message handling, and other web socket operations.
     *
     * @return the current GameWebSocketClient instance, or {@code null} if no web socket client has
     * been initialized or set.
     */
    public GameWebSocketClient getWebSocketClient() {
            return webSocketClient;
        }

    /**
     * Sets the OpenGL clear color based on the current KlotskiTheme. When the theme is LIGHT, the clear color
     * is set to a light blue color with RGBA values (0.68,0.85,0.9,1). For all other themes (e.g., DARK),
     * the clear color is set to a dark gray with RGBA values (0.25,0.25,0.25,1). This method updates both
     * the internal {@code glClearColor} instance variable and the active OpenGL state via {@code Gdx.gl.glClearColor}.
     */
    public void setGlClearColor() {
            if (klotskiTheme == KlotskiTheme.LIGHT) {
                this.glClearColor.set(0.68f, 0.85f, 0.9f, 1);
                Gdx.gl.glClearColor(0.68f, 0.85f, 0.9f, 1);
            }
            else {
                this.glClearColor.set(0.25f, 0.25f, 0.25f, 1);
                Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1);
            }
        }

    /**
     * Returns the current clear color used by the OpenGL context. This color is applied
     * when clearing the color buffer during rendering, typically set via {@link android.opengl.GLES20#glClearColor}.
     * The returned {@link Color} object contains the RGBA components defining the clear color,
     * which defaults to a platform-specific value unless explicitly set. This value determines
     * the background color of the rendering surface before drawing a new frame.
     *
     * @return The current clear color as a {@link Color} object, representing the RGBA values
     * used to clear the framebuffer. Modifying the returned object will not affect
     * the internal clear color unless it is explicitly set again via {@link #setGlClearColor(Color)}.
     */
    public Color getGlClearColor() {
            return glClearColor;
        }

    /**
     * Returns the background color based on the current Klotski theme setting. When the theme is set to
     * {@link KlotskiTheme#LIGHT}, the returned color is a light blue with RGB values (0.68,0.85,0.9) and full opacity.
     * When the theme is set to a non-light value (typically {@link KlotskiTheme#DARK}), the returned color is a dark gray
     * with RGB values (0.25,0.25,0.25) and full opacity. The alpha component for both colors is set to1 (fully opaque).
     *
     * @return A {@link Color} object representing the background color corresponding to the active theme.
     */
    public Color getBackgroundColor() {
            if (klotskiTheme == KlotskiTheme.LIGHT)
                return new Color(0.68f, 0.85f, 0.9f, 1);
            else
                return new Color(0.25f, 0.25f, 0.25f, 1);
        }

    /**
     * Returns an array of Color objects representing the main screen's color palette based on the current theme.
     * When the theme is set to LIGHT, the returned colors include RED, BLUE, GREEN, YELLOW, ORANGE, MAGENTA, CYAN, PINK, and GRAY.
     * For other themes, the same colors are returned but with their RGB values scaled by0.5 to produce darker shades.
     * The adjustment applies to all color components (red, green, blue) uniformly in non-LIGHT themes.
     *
     * @return an array of Color objects configured for the current KlotskiTheme.
     */
    public Color[] getMainScreenColorList() {
            if (this.klotskiTheme == KlotskiTheme.LIGHT) {
                Color[] colorList = {
                        Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                        Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK,
                        Color.GRAY
                };
                return colorList;
            } else {
                Color[] colorList = {
                        Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                        Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK,
                        Color.GRAY
                };
                for (int i = 0; i < colorList.length; i++) {
                    colorList[i].r = 0.5f * colorList[i].r;
                    colorList[i].g = 0.5f * colorList[i].g;
                    colorList[i].b = 0.5f * colorList[i].b;
                }
                return colorList;
            }
        }

    /**
     * Returns an array of predefined {@code Color} objects representing the available light colors
     * for the main screen. The colors are returned in the following order: red, blue, green, yellow,
     * orange, magenta, cyan, pink, and gray. This list is used to populate or configure the lighting
     * elements displayed on the main screen interface.
     */
    public Color[] getMainScreenLightColorList() {
            Color[] colorList = {
                    Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                    Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK,
                    Color.GRAY
            };
            return colorList;
        }

    /**
     * Returns the current state of the antialiasing setting. Antialiasing is a rendering technique used
     * to smooth out jagged edges and improve visual quality in graphical elements. This method provides
     * a simple check to determine whether antialiasing is enabled for the associated component or context.
     *
     * @return {@code true} if antialiasing is currently enabled, {@code false} if it is disabled.
     */
    public boolean isAntialiasingEnabled() {
            return antialiasingEnabled;
        }

    /**
     * Enables or disables antialiasing for the specified stage by updating the internal configuration.
     * This method sets the antialiasing state and triggers a configuration update for the rendering
     * context associated with the provided stage. Note that the current implementation may not
     * immediately apply visual changes due to technical limitations (as indicated by internal comments).
     *
     * @param enabled {@code true} to enable antialiasing, {@code false} to disable it.
     * @param stage The {@link Stage} instance whose rendering configuration should be updated
     * to reflect the new antialiasing state.
     */
    public void setAntialiasingEnabled(boolean enabled, Stage stage) {
            // NO EFFECT NOW!
            this.antialiasingEnabled = enabled;
            updateLwjgl3Config(stage);
        }

    /**
     * Returns whether vertical synchronization (VSync) is currently enabled. VSync is a graphics feature
     * that synchronizes the application's frame rate with the display's refresh rate to prevent screen tearing.
     * This method provides the current status of the VSync setting as a boolean value.
     *
     * @return {@code true} if VSync is enabled, indicating that frame rendering is synchronized with the display's
     * refresh rate; {@code false} if VSync is disabled, allowing the application to render frames as quickly
     * as possible without synchronization.
     */
    public boolean isVsyncEnabled() {
            return vsyncEnabled;
        }

    /**
     * Enables or disables vertical synchronization (V-Sync) for the specified stage. When enabled, V-Sync
     * synchronizes the application's frame rate with the display's refresh rate to prevent screen tearing.
     * This method updates the internal V-Sync state and triggers an immediate configuration update
     * for the LWJGL3 backend associated with the provided stage.
     *
     * @param enabled {@code true} to enable V-Sync, {@code false} to disable it.
     * @param stage The {@link Stage} instance for which the V-Sync configuration will be applied.
     * The stage's LWJGL3 configuration is refreshed after modifying the V-Sync state.
     * @implNote The method currently has no visible effect, as indicated by the code comment. The state
     * is stored internally, but functional V-Sync control might require additional implementation.
     */
    public void setVsyncEnabled(boolean enabled, Stage stage) {
            // NO EFFECT NOW!
            this.vsyncEnabled = enabled;
            updateLwjgl3Config(stage);
        }

    /**
     * Sets the LWJGL3 application configuration to the specified {@link Lwjgl3ApplicationConfiguration} object.
     * This configuration is used to initialize and customize various settings for the LWJGL3 application window
     * and context, such as window title, dimensions, OpenGL version, and other platform-specific parameters.
     *
     * @param config The {@link Lwjgl3ApplicationConfiguration} instance containing the desired configuration settings.
     * This object defines parameters that control the application's window, graphics, and input handling.
     */
    public void setLwjgl3Config(Lwjgl3ApplicationConfiguration config) {
            this.lwjgl3Config = config;
        }

    /**
     * Updates the LWJGL3 application configuration based on current settings. This method checks and applies
     * changes to VSync and antialiasing (MSAA) configurations. If any configuration change requires an application
     * restart to take effect, a dialog is displayed to notify the user. Changes to VSync or antialiasing samples
     * are only applied if they differ from the current configuration values. Note that the application is not
     * automatically restarted; the user is prompted to restart manually.
     *
     * @param stage The UI stage used to display the restart notification dialog. Must not be null.
     * @see #isVsyncEnabled() Checks if VSync is currently enabled.
     * @see #isAntialiasingEnabled() Checks if antialiasing is currently enabled.
     * @see #getVsyncEnabled(Lwjgl3ApplicationConfiguration) Retrieves the current VSync configuration.
     * @see #getBackBufferConfig(Lwjgl3ApplicationConfiguration) Retrieves the current back buffer configuration.
     */
    public void updateLwjgl3Config(Stage stage) {
            //// DO NOT USE NOW!
            if (lwjgl3Config != null) {
                boolean needRestart = false;
                // Update VSync
                boolean newVsync = isVsyncEnabled();
                if (newVsync != getVsyncEnabled(lwjgl3Config)){
                    System.out.println("VSync enabled: " + newVsync);
                    lwjgl3Config.useVsync(newVsync);
                    needRestart = true;
                }
    
                // Update Antialiasing
                int newSamples = isAntialiasingEnabled() ? 4 : 0; // 4x MSAA if enabled, 0 otherwise
                if (newSamples != getBackBufferConfig(lwjgl3Config)[6]) {
                    System.out.println("Antialiasing samples: " + newSamples);
                    lwjgl3Config.setBackBufferConfig(8, 8, 8, 8, 16, 8, newSamples);
                    needRestart = true;
                }
    
                if (needRestart) {
                    // restartApplication();
                    Dialog.showDialog(this, skin, stage, "Notification", "This change needs restarting the game to take effect");
                }
            }
        }

    /**
     * Retrieves the vertical synchronization (vSync) enabled status from the provided {@link Lwjgl3ApplicationConfiguration} instance
     * using reflection to access the private {@code vSyncEnabled} field of the {@link Lwjgl3WindowConfiguration} class. This method
     * temporarily overrides the field's accessibility to read its boolean value. If the field cannot be accessed due to
     * a {@link NoSuchFieldException} or {@link IllegalAccessException}, the exception is logged to the standard error stream,
     * and the method returns {@code false} as a default value.
     *
     * @param config The configuration instance from which to retrieve the vSync status. Must be non-null.
     * @return {@code true} if vertical synchronization is enabled according to the configuration's {@code vSyncEnabled} field,
     * or {@code false} if an error occurs during reflection access.
     */
    public boolean getVsyncEnabled(Lwjgl3ApplicationConfiguration config) {
            try {
                Field vSyncField = Lwjgl3WindowConfiguration.class.getDeclaredField("vSyncEnabled");
                vSyncField.setAccessible(true); // Make the field accessible
                return vSyncField.getBoolean(config);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return false; // Default to false if an error occurs
            }
        }

    /**
     * Retrieves the back buffer configuration settings from the provided {@link Lwjgl3ApplicationConfiguration} instance.
     * This method accesses private fields of the configuration object using reflection to obtain the red, green, blue,
     * and alpha (RGBA) color channel bit depths, depth buffer bit depth, stencil buffer bit depth, and multisample
     * anti-aliasing (MSAA) sample count. The values are returned as an integer array in the following order:
     * <ul>
     * <li>Index0: Red channel bit depth (r)</li>
     * <li>Index1: Green channel bit depth (g)</li>
     * <li>Index2: Blue channel bit depth (b)</li>
     * <li>Index3: Alpha channel bit depth (a)</li>
     * <li>Index4: Depth buffer bit depth (depth)</li>
     * <li>Index5: Stencil buffer bit depth (stencil)</li>
     * <li>Index6: MSAA sample count (samples)</li>
     * </ul>
     * If an error occurs while accessing the fields via reflection (e.g., due to field name changes or access restrictions),
     * this method prints the exception stack trace and returns {@code null}.
     *
     * @param config The {@link Lwjgl3ApplicationConfiguration} instance from which to extract back buffer settings.
     * @return An integer array containing the back buffer configuration values in the order [r, g, b, a, depth, stencil, samples],
     * or {@code null} if reflection fails to access any required field.
     */
    public int[] getBackBufferConfig(Lwjgl3ApplicationConfiguration config) {
            try {
                // Access the private fields using reflection
                Field rField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("r");
                Field gField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("g");
                Field bField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("b");
                Field aField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("a");
                Field depthField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("depth");
                Field stencilField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("stencil");
                Field samplesField = Lwjgl3ApplicationConfiguration.class.getDeclaredField("samples");
    
                // Make the fields accessible
                rField.setAccessible(true);
                gField.setAccessible(true);
                bField.setAccessible(true);
                aField.setAccessible(true);
                depthField.setAccessible(true);
                stencilField.setAccessible(true);
                samplesField.setAccessible(true);
    
                // Retrieve the values of the fields
                int r = rField.getInt(config);
                int g = gField.getInt(config);
                int b = bField.getInt(config);
                int a = aField.getInt(config);
                int depth = depthField.getInt(config);
                int stencil = stencilField.getInt(config);
                int samples = samplesField.getInt(config);
    
                // Return the values as an array
                return new int[]{r, g, b, a, depth, stencil, samples};
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return null; // Return null if an error occurs
            }
        }

    /**
     * Restarts the application by disposing the current instance and initializing a new one with the existing
     * configuration. This method saves the current {@link Lwjgl3ApplicationConfiguration}, terminates the running
     * application via {@link Gdx#app}, then creates a new {@link Klotski} instance, applies the saved configuration,
     * and launches a new {@link Lwjgl3Application}. Note that this method is marked as experimental and may not
     * function correctly in all contexts. Avoid using it until further testing and stabilization are completed.
     */
    public void restartApplication() {
            //// DO NOT USE NOW!
    
            // Save the current configuration
            Lwjgl3ApplicationConfiguration config = lwjgl3Config;
    
            // Dispose of the current application
            Gdx.app.exit();
    
            // Restart the application with the updated configuration
            Klotski newKlotski = new Klotski();
            newKlotski.setLwjgl3Config(config);
            new Lwjgl3Application(newKlotski, config);
        }

    /**
     * Plays the alert sound configured for this instance. This method attempts to trigger the playback
     * of the sound resource referenced by {@code alertSound}. If the sound cannot be played due to
     * an exception (e.g., missing resource, invalid format, or playback failure), the exception is
     * caught and logged to the standard error stream. The method handles errors gracefully without
     * propagating them, ensuring uninterrupted execution of the calling code. Intended for notifying
     * users audibly when specific events or alerts occur.
     */
    public void playAlertSound() {
            try {
                alertSound.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    /**
     * Plays a click sound effect by invoking the playback functionality of the associated sound resource.
     * This method attempts to trigger the sound immediately when called. If an error occurs during playback
     * (e.g., missing resource, audio initialization failure), the exception is caught and its stack trace
     * is printed to the standard error stream for debugging purposes. The method ensures that unexpected
     * exceptions do not propagate upward, preventing application crashes related to sound playback.
     */
    public void playClickSound() {
            try {
                clickSound.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    /**
     * Plays the block collision sound effect when called. This method attempts to trigger the audio
     * associated with block collisions by invoking the underlying sound player. If an exception occurs
     * during playback (e.g., audio file errors, resource loading issues), the exception is caught
     * and its stack trace is printed to standard error. This ensures the application continues running
     * gracefully even if sound playback fails.
     */
    public void playBlockCollideSound() {
            try {
                blockCollideSound.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    /**
     * Sets whether background music is enabled. If the background music instance is not {@code null},
     * this method will either start playing the music (if {@code enabled} is {@code true}) or stop it
     * (if {@code enabled} is {@code false}), and update the internal state reflecting the music enabled status.
     * If the background music is {@code null}, this method has no effect.
     *
     * @param enabled {@code true} to enable and play the background music, {@code false} to disable and stop it.
     */
    public void setMusicEnabled(boolean enabled) {
            if (backgroundMusic != null) {
                if (enabled) {
                    backgroundMusic.play();
                } else {
                    backgroundMusic.stop();
                }
                this.musicEnabled = enabled;
            }
        }

    /**
     * Checks whether the music playback feature is currently enabled. This method returns the current
     * status of the music setting, indicating whether background music or audio effects are permitted
     * to play based on user preferences or application configuration.
     *
     * @return {@code true} if music is enabled and allowed to play, {@code false} if music playback
     * is disabled or muted by user/system settings.
     */
    public boolean isMusicEnabled() {
            return musicEnabled;
        }

    /**
     * Sets the offline mode status for this instance. When enabled, the application will operate without
     * requiring a network connection, using locally cached data or resources where available. When disabled,
     * the application will attempt to connect to remote services or resources as needed. This method directly
     * updates the internal state controlling network-dependent behavior.
     *
     * @param offlineMode {@code true} to enable offline mode, {@code false} to disable it and resume normal
     * network-dependent operations.
     */
    public void setOfflineMode(boolean offlineMode) {
            this.isOfflineMode = offlineMode;
        }

    /**
     * Indicates whether the application is currently running in offline mode. This method returns
     * the current status of offline mode, which determines if the application should avoid
     * attempting network requests or rely solely on locally cached data.
     *
     * @return {@code true} if the application is configured to operate in offline mode,
     * {@code false} if it is connected to network resources and operating normally.
     */
    public boolean isOfflineMode() {
            return isOfflineMode;
        }

    /**
     * Sets the GameWebSocketClient instance for this object. This method assigns the provided
     * WebSocket client to the internal instance variable, enabling communication with the game's
     * WebSocket server. The client is used to send and receive real-time updates during gameplay.
     *
     * @param client The GameWebSocketClient instance to associate with this object. Must not be null
     * and should be properly initialized to handle WebSocket interactions.
     */
    public void setGameWebSocketClient(GameWebSocketClient client) {
            this.webSocketClient = client;
        }

    // THIS SHOULD BE USELESS
    /**
     * Sets the background music to the specified {@link Music} object. This method updates the
     * current background music track to the provided {@code music} parameter. If the provided
     * {@code music} is {@code null}, this will clear the current background music. Note that
     * calling this method does not automatically start playback of the new music; additional
     * logic may be required to manage playback. Ensure the {@link Music} object is properly
     * initialized and loaded before invoking this method to avoid potential resource errors.
     *
     * @param music The {@link Music} object to be used as the new background music.
     * Can be {@code null} to stop or remove the current background music.
     */
    public void setBackgroundMusic(Music music) {
            this.backgroundMusic = music;
        }

    /** * Retrieves the background music associated with this instance.
     * The returned {@link Music} object represents the current background audio track
     * configured for playback. This method provides access to the stored music resource,
     * which may be {@code null} if no background music has been set. The returned instance
     * is the same as the one managed internally, so modifications to the object (if mutable)
     * may affect the application's background music state.
     *
     * @return the {@link Music} instance used as background music, or {@code null} if none exists. */
    public Music getBackgroundMusic() {
            return backgroundMusic;
        }

    /**
     * Returns whether arrow-based controls are enabled for navigation or interaction within the system.
     * This setting determines if directional input (e.g., via keyboard arrow keys) is actively processed
     * to adjust focus, movement, or other UI/application behavior.
     *
     * @return {@code true} if arrow controls are currently active and accepting input, {@code false} if
     * arrow-based navigation is disabled and input events are ignored.
     */
    public boolean isArrowControlsEnabled() { return arrowControlsEnabled; }

    /**
     * Sets whether the arrow controls are enabled or disabled. When enabled, the arrow controls allow
     * user interaction for navigation or input purposes. Disabling this prevents the arrow controls
     * from being used until re-enabled.
     *
     * @param enabled {@code true} to enable the arrow controls, {@code false} to disable them.
     */
    public void setArrowControlsEnabled(boolean enabled) { this.arrowControlsEnabled = enabled; }

    /**
     * Stops the background music playback if it is currently active. This method checks if the
     * backgroundMusic instance is initialized (not null) before invoking its stop() method to
     * prevent null pointer exceptions. If the background music is playing, it will cease playback
     * and release associated resources. If no background music is loaded or already stopped,
     * this method has no effect. Safe to call even when background music is not actively playing.
     */
    public void stopBackgroundMusic() {
            if (backgroundMusic != null) {
                backgroundMusic.stop();
            }
        }

    /**
     * Starts playback of the background music if it has been initialized. This method checks whether
     * the background music resource is available (non-null) and, if so, invokes its play() method
     * to begin playback. If the background music has not been initialized or is null, this method
     * performs no action and exits silently.
     */
    public void startBackgroundMusic() {
            if (backgroundMusic != null) {
                backgroundMusic.play();
            }
        }

    /** * Sets the current game screen to the specified {@link GameScreen} instance.
     * This method updates the internal reference to the active game screen, allowing
     * the application to control which screen is currently being displayed or interacted with.
     *
     * @param gameScreen The {@link GameScreen} instance to set as the active screen.
     * This parameter must not be {@code null} to ensure proper functionality. */
    public void setGameScreen(GameScreen gameScreen) {
            this.gameScreen = gameScreen;
        }
}
