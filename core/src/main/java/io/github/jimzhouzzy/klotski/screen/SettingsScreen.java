/**
 * SettingsScreen.java
 * 
 * This class represents the settings screen in the Klotski game.
 * It loads the settings from a JSON file when Klotski is launched.
 * It allows users to configure various settings such as graphics, audio, and gameplay options.
 * 
 * @author JimZhouZZY
 * @version 1.24
 * @since 2025-5-25
 * @see {@link Klotski#create()}
 * 
 * Change log:
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 */

package io.github.jimzhouzzy.klotski.screen;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.ui.KlotskiTheme;
import io.github.jimzhouzzy.klotski.util.ConfigPathHelper;

public class SettingsScreen extends ProtoScreen {

    private boolean isDarkMode;
    private static final ConfigPathHelper configPathHelper = new ConfigPathHelper();
    private static final String SETTINGS_FILE = configPathHelper.getConfigFilePath("Klotski", "settings.json");

    public SettingsScreen(final Klotski klotski) {
        super(klotski);
        this.isDarkMode = false;
        loadSettings(); // Load settings from file
        create(); // MUST be after loadSettings()
    }

    public void create() {
        // Create a table for layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Add a title label
        Label titleLabel = new Label("Settings", skin, "title");
        titleLabel.setFontScale(1.5f);
        table.add(titleLabel).padBottom(50).row();

        // Add a checkbox for light/dark mode
        CheckBox darkModeCheckBox = new CheckBox("Dark Mode", skin);
        darkModeCheckBox.setChecked(isDarkMode);
        darkModeCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                if (darkModeCheckBox.isChecked()) {
                    isDarkMode = true;
                    klotski.klotskiTheme = KlotskiTheme.DARK;
                    klotski.updateMainScreenColors();

                    // Switch to dark-themed music
                    klotski.stopBackgroundMusic();
                    Music darkMusic = klotski.backgroundMusicDark;
                    darkMusic.setLooping(true);
                    klotski.setBackgroundMusic(darkMusic);
                    if (klotski.isMusicEnabled()) {
                        darkMusic.play();
                    }

                    Gdx.app.log("Settings", "Dark mode enabled");
                } else {
                    isDarkMode = false;
                    klotski.klotskiTheme = KlotskiTheme.LIGHT;
                    klotski.updateMainScreenColors();

                    // Switch to the light-themed music
                    klotski.stopBackgroundMusic();
                    Music lightMusic = klotski.backgroundMusicLight;
                    lightMusic.setLooping(true);
                    klotski.setBackgroundMusic(lightMusic);
                    if (klotski.isMusicEnabled()) {
                        lightMusic.play();
                    }

                    Gdx.app.log("Settings", "Light mode enabled");
                }
                klotski.setGlClearColor();
                saveSettings();
            }

        });
        table.add(darkModeCheckBox).padBottom(20).row();

        // Add a checkbox for Antialiasing
        CheckBox antialiasingCheckBox = new CheckBox("Graphics - Antialiasing", skin);
        antialiasingCheckBox.setChecked(klotski.isAntialiasingEnabled());
        antialiasingCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                klotski.setAntialiasingEnabled(antialiasingCheckBox.isChecked(), stage);
                Gdx.app.log("Settings", "Antialiasing " + (antialiasingCheckBox.isChecked() ? "enabled" : "disabled"));
                saveSettings();
            }
        });
        table.add(antialiasingCheckBox).padBottom(20).row();

        // Add a checkbox for Vertical Sync
        CheckBox vsyncCheckBox = new CheckBox("Graphics - Vertical Sync", skin);
        vsyncCheckBox.setChecked(klotski.isVsyncEnabled());
        vsyncCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                klotski.setVsyncEnabled(vsyncCheckBox.isChecked(), stage);
                // Is this line necessary?
                Gdx.graphics.setVSync(vsyncCheckBox.isChecked());
                Gdx.app.log("Settings", "Vertical Sync " + (vsyncCheckBox.isChecked() ? "enabled" : "disabled"));
                saveSettings();
            }
        });
        table.add(vsyncCheckBox).padBottom(20).row();

        // Add a checkbox for music
        CheckBox musicCheckBox = new CheckBox("Audio - Music", skin);
        musicCheckBox.setChecked(klotski.isMusicEnabled());
        musicCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                if (musicCheckBox.isChecked()) {
                    klotski.setMusicEnabled(true);
                } else {
                    klotski.setMusicEnabled(false);
                }
                Gdx.app.log("Settings", "Music " + (musicCheckBox.isChecked() ? "enabled" : "disabled"));
                saveSettings();
            }
        });
        table.add(musicCheckBox).padBottom(20).row();

        // Add a checkbox for arrow key UI
        CheckBox arrowControlCheckBox = new CheckBox("Gameplay - Show Arrow Controls", skin);
        arrowControlCheckBox.setChecked(klotski.isArrowControlsEnabled());
        arrowControlCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();
                klotski.setArrowControlsEnabled(arrowControlCheckBox.isChecked());
                Gdx.app.log("Settings", "Arrow Controls " + (arrowControlCheckBox.isChecked() ? "enabled" : "disabled"));
                saveSettings();
            }
        });
        table.add(arrowControlCheckBox).padBottom(20).row();

        // Add a checkbox for online mode
        CheckBox offlineModeCheckBox = new CheckBox("Network - Offline Mode", skin);
        offlineModeCheckBox.setChecked(klotski.isOfflineMode());
        offlineModeCheckBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                klotski.clearLoginStatus();
                if (offlineModeCheckBox.isChecked()) {
                    klotski.setOfflineMode(true);
                } else {
                    klotski.setOfflineMode(false);
                }
                Gdx.app.log("Settings", "Music " + (musicCheckBox.isChecked() ? "enabled" : "disabled"));
                saveSettings();
            }
        });
        table.add(offlineModeCheckBox).padBottom(20).row();

        // Add a "Back" button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                klotski.playClickSound();;
                klotski.setScreen(new MainScreen(klotski)); // Navigate back to the main screen
                saveSettings();
            }
        });
        table.add(backButton).width(200).height(50);
    }

    private void saveSettings() {
        // Save settings to a JSON file
        Map<String, Object> settings = new HashMap<>();
        settings.put("isDarkMode", isDarkMode);
        settings.put("antialiasingEnabled", klotski.isAntialiasingEnabled());
        settings.put("vsyncEnabled", klotski.isVsyncEnabled());
        settings.put("musicEnabled", klotski.isMusicEnabled());
        settings.put("offlineMode", klotski.isOfflineMode());
        settings.put("arrowControlsEnabled", klotski.isArrowControlsEnabled());

        String username = klotski.getLoggedInUser();
        if (username == null || username.isEmpty()) {
            username = "Guest";
        }
        // Do not log the username for now
        username = "default";
        settings.put("username", username);

        Json json = new Json();
        FileHandle file = Gdx.files.absolute(SETTINGS_FILE);
        file.writeString(json.prettyPrint(settings), false);

        Gdx.app.log("Settings", "Settings saved for user: " + username);
    }

    private Map<String, Object> getDefaultSettings() {
        Map<String, Object> defaultSettings = new HashMap<>();
        defaultSettings.put("isDarkMode", false); // Default to light mode
        defaultSettings.put("username", "Guest"); // Default username
        defaultSettings.put("antialiasingEnabled", true);
        defaultSettings.put("vsyncEnabled", true);
        defaultSettings.put("musicEnabled", true);
        defaultSettings.put("offlineMode", false);
        defaultSettings.put("arrowControlsEnabled", true);
        return defaultSettings;
    }

    private void loadSettings() {
        // Load settings from a JSON file
        FileHandle file = Gdx.files.absolute(SETTINGS_FILE);
        Map<String, Object> settings;

        if (!file.exists()) {
            Gdx.app.log("Settings", "No settings file found. Using default settings.");
            settings = getDefaultSettings();
        } else {
            try {
                Json json = new Json();
                settings = json.fromJson(HashMap.class, file.readString());

                // Validate the settings file
                if (!isSettingsValid(settings)) {
                    Gdx.app.log("Settings", "Settings file is invalid. Using default settings.");
                    settings = getDefaultSettings();
                }
            } catch (Exception e) {
                Gdx.app.log("Settings",
                        "Failed to load settings file. Using default settings. Error: " + e.getMessage());
                settings = getDefaultSettings();
            }
        }

        // Apply settings
        String username = klotski.getLoggedInUser();
        // Do not specifically log username for now
        username = "default";
        if (username != null && username.equals(settings.getOrDefault("username", "Guest"))) {
            isDarkMode = (boolean) settings.getOrDefault("isDarkMode", getDefaultSettings().get("isDarkMode"));
            klotski.setAntialiasingEnabled((boolean) settings.getOrDefault("antialiasingEnabled", true), stage);
            klotski.setVsyncEnabled((boolean) settings.getOrDefault("vsyncEnabled", true), stage);
            klotski.setMusicEnabled((boolean) settings.getOrDefault("musicEnabled", true));
            klotski.setOfflineMode((boolean) settings.getOrDefault("offlineMode", false));
            klotski.setArrowControlsEnabled((boolean) settings.getOrDefault("arrowControlsEnabled", true));
            Gdx.app.log("Settings", "Settings loaded for user: " + username);
        } else {
            Gdx.app.log("Settings", "No settings found for user: " + username + ". Using default settings.");
            settings = getDefaultSettings();
            isDarkMode = (boolean) settings.get("isDarkMode");
            klotski.setAntialiasingEnabled((boolean) settings.get("antialiasingEnabled"), stage);
            klotski.setVsyncEnabled((boolean) settings.get("vsyncEnabled"), stage);
            klotski.setMusicEnabled((boolean) settings.getOrDefault("musicEnabled", true));
            klotski.setOfflineMode((boolean) settings.getOrDefault("offlineMode", false));
            klotski.setArrowControlsEnabled((boolean) settings.getOrDefault("arrowControlsEnabled", true));
            saveSettings();
        }

        klotski.stopBackgroundMusic();

        // Set Klotski.klotskiTheme based on isDarkMode
        if (isDarkMode) {
		    Music darkMusic = klotski.backgroundMusicDark;
		    darkMusic.setLooping(true);
		    klotski.setBackgroundMusic(darkMusic);
            if (klotski.isMusicEnabled()) {
		        darkMusic.play();
            }
            klotski.klotskiTheme = KlotskiTheme.DARK;
        } else {
		    Music lightMusic = klotski.backgroundMusicLight;
            klotski.setBackgroundMusic(lightMusic);
		    lightMusic.setLooping(true);
            if (klotski.isMusicEnabled()) {
		        lightMusic.play();
            }
		    klotski.klotskiTheme = KlotskiTheme.LIGHT;
        }

        klotski.setGlClearColor();
    }

    private boolean isSettingsValid(Map<String, Object> settings) {
        try {
            // Check if all required keys are present and of the correct type
            if (!settings.containsKey("isDarkMode") || !(settings.get("isDarkMode") instanceof Boolean)) {
                return false;
            }
            if (!settings.containsKey("antialiasingEnabled")
                    || !(settings.get("antialiasingEnabled") instanceof Boolean)) {
                return false;
            }
            if (!settings.containsKey("vsyncEnabled") || !(settings.get("vsyncEnabled") instanceof Boolean)) {
                return false;
            }
            if (!settings.containsKey("musicEnabled") || !(settings.get("vsyncEnabled") instanceof Boolean)) {
                return false;
            }
            if (!settings.containsKey("offlineMode") || !(settings.get("vsyncEnabled") instanceof Boolean)) {
                return false;
            }
            if (!settings.containsKey("arrowControlsEnabled") || !(settings.get("arrowControlsEnabled") instanceof Boolean)) {
                return false;
            }
            if (!settings.containsKey("username") || !(settings.get("username") instanceof String)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            Gdx.app.log("Settings", "Error validating settings: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void render(float delta) {
        klotski.setGlClearColor();
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        klotski.dynamicBoard.render(delta); // Render the dynamic board

        // Render the stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

}
