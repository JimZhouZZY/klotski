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
import io.github.jimzhouzzy.klotski.screen.HelpScreen;
import io.github.jimzhouzzy.klotski.screen.MainScreen;
import io.github.jimzhouzzy.klotski.screen.SettingsScreen;
import io.github.jimzhouzzy.klotski.screen.SpectateChoiceScreen;
import io.github.jimzhouzzy.klotski.ui.Dialog;
import io.github.jimzhouzzy.klotski.ui.DynamicBoard;
import io.github.jimzhouzzy.klotski.ui.KlotskiTheme;
import io.github.jimzhouzzy.klotski.util.ConfigPathHelper;
import io.github.jimzhouzzy.klotski.web.offline.GameWebSocketServer;
import io.github.jimzhouzzy.klotski.web.offline.WebServer;
import io.github.jimzhouzzy.klotski.web.online.GameWebSocketClient;

public class Klotski extends Game {
    private GameWebSocketServer webSocketServer;
    public GameWebSocketClient webSocketClient;

    private final ConfigPathHelper configPathHelper = new ConfigPathHelper();
    private final String LOGIN_STATUS_FILE = configPathHelper.getConfigFilePath("Klotski", "login_status.dat");
    public SpriteBatch batch;
    public BitmapFont font;
    public FitViewport viewport;
    public GameScreen gameScreen;
    public MainScreen mainScreen;
    public SpectateChoiceScreen spectateChoiceScreen;
    public DynamicBoard dynamicBoard;
    public WebServer webServer;
    public SettingsScreen settingsScreen;
    public HelpScreen helpScreen;
    public Music backgroundMusic;
    public Music backgroundMusicLight;
    public Music backgroundMusicDark;
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

    public void create() {
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

        // Load the last logged-in user
        loadLoginStatus();

        // Cresate dynamic board before screens
        this.dynamicBoard = new DynamicBoard(this, null);

        // After the user loading, settings screen must come first to load settings
        this.settingsScreen = new SettingsScreen(this);
        this.mainScreen = new MainScreen(this);
        this.gameScreen = new GameScreen(this);
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

    public String getLoggedInUser() {
        if (loggedInUser == null) {
            return "Guest"; // If no user is logged in, return "Guest"
        }
        return loggedInUser; // Return the logged-in user's name
    }

    public void setLoggedInUser(String username, String token) {
        this.loggedInUser = username; // Set the logged-in user's name
        this.token = token; // Set the token
        saveLoginStatus(); // Save the login status whenever it changes
    }

    public void setLoggedInUser(String username) {
        this.loggedInUser = username; // Set the logged-in user's name
        saveLoginStatus(); // Save the login status whenever it changes
    }

    private void loadLoginStatus() {
        File file = new File(LOGIN_STATUS_FILE);
        if (!file.exists()) {
            return; // No login status file exists yet
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String prevLoggedInUser = reader.readLine(); // Read the logged-in username
            String prevToken = reader.readLine(); // Read the token
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

    public void clearLoginStatus() {
        loggedInUser = null;
        File file = new File(LOGIN_STATUS_FILE);
        if (file.exists()) {
            file.delete(); // Delete the login status file
        }
    }

    public void render() {
        super.render();
    }

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

    public GameWebSocketServer getWebSocketServer() {
        return webSocketServer;
    }

    public GameWebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

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

    public Color getGlClearColor() {
        return glClearColor;
    }

    public Color getBackgroundColor() {
        if (klotskiTheme == KlotskiTheme.LIGHT)
            return new Color(0.68f, 0.85f, 0.9f, 1);
        else
            return new Color(0.25f, 0.25f, 0.25f, 1);
    }

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

    public Color[] getMainScreenLightColorList() {
        Color[] colorList = {
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK,
                Color.GRAY
        };
        return colorList;
    }

    public void updateMainScreenColors() {
        mainScreen.loadColors();
    }

    public boolean isAntialiasingEnabled() {
        return antialiasingEnabled;
    }

    public void setAntialiasingEnabled(boolean enabled, Stage stage) {
        // NO EFFECT NOW!
        this.antialiasingEnabled = enabled;
        updateLwjgl3Config(stage);
    }

    public boolean isVsyncEnabled() {
        return vsyncEnabled;
    }

    public void setVsyncEnabled(boolean enabled, Stage stage) {
        // NO EFFECT NOW!
        this.vsyncEnabled = enabled;
        updateLwjgl3Config(stage);
    }

    public void setLwjgl3Config(Lwjgl3ApplicationConfiguration config) {
        this.lwjgl3Config = config;
    }

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
                Dialog.showErrorDialog(this, skin, stage, "Some changes needs restarting the game to take effect", stage);
            }
        }
    }

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

    public void playAlertSound() {
        try {
            alertSound.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playClickSound() {
        try {
            clickSound.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playBlockCollideSound() {
        try {
            blockCollideSound.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.isOfflineMode = offlineMode;
    }

    public boolean isOfflineMode() {
        return isOfflineMode;
    }

    public void setGameWebSocketClient(GameWebSocketClient client) {
        this.webSocketClient = client;
    }

    // THIS SHOULD BE USELESS
    public void setBackgroundMusic(Music music) {
        this.backgroundMusic = music;
    }

    public Music getBackgroundMusic() {
        return backgroundMusic;
    }

    public boolean isArrowControlsEnabled() { return arrowControlsEnabled; }

    public void setArrowControlsEnabled(boolean enabled) { this.arrowControlsEnabled = enabled; }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public void startBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.play();
        }
    }
}
