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
 * SettingsScreen.java
 * 
 * This class represents the settings screen in the Klotski game.
 * It loads the settings from a JSON file when Klotski is launched.
 * It allows users to configure various settings such as graphics, audio, and gameplay options.
 * 
 * @author JimZhouZZY
 * @version 1.37
 * @since 2025-5-25
 * @see {@link Klotski#create()}
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: modify font
 * 2025-05-27: Show error dialog when load-save failed
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: remove deprecated loadColors method
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: Refactor screens to enheritage ProtoScreen
 * 2025-05-24: fix: bad init process of HelpScreen
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-22: Fix the HelpScreen's BUG! (#11)
 * 2025-05-21: bug fix (#10)
 * 2025-05-21: code clean up
 * 2025-05-21: Changed some bug like: 1. When I reopenned the darkMode, two kind of music played together. 2. Fix the failed Mode exchange.
 * 2025-05-21: Thz (#9)
 * 2025-05-21: bug fix: settings doesn't load normally
 * 2025-05-21: bug fix: audio settings (not completed)
 * 2025-05-20: Merge branch v1.0.7 into main (#8)
 * 2025-05-08: update soundfx
 * 2025-05-06: fix file load problem in windows OS
 * 2025-04-30: optimize local storage
 * 2025-04-29: offline mode & optimize save-load
 * 2025-04-25: fix: load config didn't correctly (un)check enable music
 * 2025-04-25: Revert 'fix:resize'
 * 2025-04-24: fix resize changed base tile size
 * 2025-04-24: MSAA & Settings
 * 2025-04-23: better main screen
 * 2025-04-22: better dark mode
 * 2025-04-22: Settings view
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
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;
import io.github.jimzhouzzy.klotski.ui.component.KlotskiTheme;
import io.github.jimzhouzzy.klotski.util.ConfigPathHelper;

public class SettingsScreen extends ProtoScreen {

    private boolean isDarkMode;
    private static final String SETTINGS_FILE = ConfigPathHelper.getConfigFilePath("Klotski", "settings.json");

    public SettingsScreen(final Klotski klotski) {
        super(klotski);
        this.isDarkMode = false;
        loadSettings(); // Load settings from file
        create(); // MUST be after loadSettings()
    }

    /**
     * Initializes and configures the settings screen UI, including various user-configurable options.
     * Creates a table-based layout with a title label and checkboxes for different settings:
     * - Toggle between light/dark mode (affects theme and background music)
     * - Enable/disable antialiasing for graphics quality
     * - Enable/disable vertical sync for rendering synchronization
     * - Enable/disable background music
     * - Show/hide arrow key controls for gameplay
     * - Enable/disable offline mode for network functionality
     * Includes a back button that saves current settings and returns to the main screen.
     * All setting changes trigger immediate application updates, play feedback sounds, log state changes,
     * and persist settings through the save mechanism. Maintains references to the main game instance (klotski)
     * for state management and theme configuration.
     */
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

    /**
     * Saves the current application and user settings to a JSON configuration file. The method serializes
     * settings including UI preferences (dark mode), rendering options (antialiasing, VSync), audio settings
     * (music enablement), gameplay modes (offline mode), control schemes (arrow controls), and a placeholder
     * username ("default" regardless of logged-in status) into a structured map. The data is written to a file
     * specified by the {@code SETTINGS_FILE} constant. A log entry is generated upon successful save, indicating
     * the username associated with the settings (always "default" in this implementation).
     */
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

    /**
     * Creates and returns a map containing the default application settings. The map includes
     * predefined key-value pairs representing initial configuration values such as display preferences,
     * user settings, and system flags. The default values are:
     * <ul>
     * <li>{@code "isDarkMode"} - {@code false} (light mode)</li>
     * <li>{@code "username"} - {@code "Guest"}</li>
     * <li>{@code "antialiasingEnabled"} - {@code true}</li>
     * <li>{@code "vsyncEnabled"} - {@code true}</li>
     * <li>{@code "musicEnabled"} - {@code true}</li>
     * <li>{@code "offlineMode"} - {@code false}</li>
     * <li>{@code "arrowControlsEnabled"} - {@code true}</li>
     * </ul>
     *
     * @return A {@code Map<String, Object>} containing the default settings for the application.
     */
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

    /**
     * Loads application settings from a JSON file located at {@code SETTINGS_FILE}. If the file does not exist,
     * is invalid, or encounters an error during parsing, default settings are applied instead. Validates loaded
     * settings using {@link #isSettingsValid(Map)} and falls back to defaults if validation fails. Applies loaded
     * or default settings to configure application behavior, including dark mode, antialiasing, VSync, music,
     * offline mode, and control preferences. Updates the UI theme (light/dark) based on the {@code isDarkMode} setting,
     * sets corresponding background music, and adjusts rendering properties via {@link Klotski} instance methods.
     * Logs warnings and errors via {@link Application#log(String, String)} for missing files, validation failures,
     * or parsing exceptions. If no user-specific settings are found for the current user (determined by
     * {@link Klotski#getLoggedInUser()}), applies and persists default settings via {@link #saveSettings()}.
     * Stops any currently playing background music before applying new theme-specific music settings.
     */
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

    /**
     * Validates whether the provided settings map contains all required keys with the correct value types.
     * The required keys and their expected types are:
     * - "isDarkMode" (Boolean)
     * - "antialiasingEnabled" (Boolean)
     * - "vsyncEnabled" (Boolean)
     * - "musicEnabled" (Boolean)
     * - "offlineMode" (Boolean)
     * - "arrowControlsEnabled" (Boolean)
     * - "username" (String)
     * Returns {@code true} if all keys are present and their values match the expected types. Returns {@code false} if any
     * required key is missing, a value has an incorrect type, or an exception occurs during validation. Errors during
     * validation are logged via {@code Gdx.app.log} with the "Settings" category.
     */
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

    /**
     * Renders the game elements and updates the UI stage each frame. This method is part of the game loop
     * and is called continuously to refresh the display. It first clears the screen using the background
     * color configured in the Klotski instance. The dynamic components of the game board (e.g., moving blocks,
     * sprites, or interactive elements) are rendered next. Finally, the UI stage is updated to handle input
     * events and animations, and then drawn to display UI components like buttons, labels, or overlays.
     *
     * @param delta The time in seconds since the last render call, used for frame-rate-independent updates.
     */
    @Override
        public void render(float delta) {
            klotski.setGlClearColor();
            Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
    
            klotski.dynamicBoard.render(delta); // Render the dynamic board
    
            // Render the stage
            stage.act(delta);
            stage.draw();
        }

    /**
     * Resizes the stage's viewport to the specified dimensions and centers the camera.
     * This method updates the viewport's width and height, applies the changes, and
     * repositions the camera to the center of the new viewport configuration. It is
     * typically called when the application window or screen resolution changes.
     *
     * @param width the new width of the viewport in pixels
     * @param height the new height of the viewport in pixels
     */
    @Override
        public void resize(int width, int height) {
            stage.getViewport().update(width, height, true);
        }

    /**
     * Disposes of resources associated with this object to free up memory and prevent memory leaks.
     * This method is called when this object is no longer needed and ensures proper cleanup by:
     * <ul>
     * <li>Disposing the {@code Stage} instance, closing the window and releasing its native resources.</li>
     * <li>Disposing the {@code Skin} instance, releasing any graphical assets or textures it manages.</li>
     * </ul>
     * After calling this method, the disposed objects should no longer be used. This override ensures
     * compliance with the framework's resource management lifecycle.
     */
    @Override
        public void dispose() {
            stage.dispose();
            skin.dispose();
        }

    /**
     * Disables input processing by setting the global input processor to {@code null}. This method
     * effectively removes any active input handlers, preventing touch, keyboard, or other input events
     * from being processed by the application until another input processor is explicitly set.
     */
    @Override
        public void hide() {
            Gdx.input.setInputProcessor(null);
        }

    /**
     * Sets the stage as the primary input processor for handling user input events. This method is called
     * when the screen becomes visible, ensuring that all touch, keyboard, and other input events are
     * routed to the stage. By delegating input processing to the stage, UI components (such as buttons,
     * labels, and other actors within the stage) can respond appropriately to user interactions.
     */
    @Override
        public void show() {
            Gdx.input.setInputProcessor(stage);
        }

    /**
     * Pauses the current process or operation associated with this instance. When invoked, this method
     * temporarily halts ongoing activities or state changes, allowing them to be resumed later via a
     * corresponding {@link #resume()} method. If the process is already paused, invoking this method
     * has no effect. Implementations should ensure that internal state is preserved during the pause,
     * and any necessary resources remain allocated to permit seamless resumption. This method is
     * typically used to manage lifecycle states, such as pausing a thread, media playback, or timed
     * events.
     */
    @Override
        public void pause() {
        }

    /**
     * Resumes the execution of the current instance, typically after a prior {@link #pause()} or equivalent operation.
     * This method transitions the state of the object from a paused or suspended state back to an active state, allowing
     * previously halted operations to continue. If the instance was not in a paused state, calling this method has no effect.
     * Implementations should ensure thread safety and handle any required resource reinitialization or state synchronization
     * to guarantee consistent behavior upon resumption. Overrides must adhere to the concurrency and lifecycle contracts
     * defined by the parent class or interface.
     */
    @Override
        public void resume() {
        }

}
