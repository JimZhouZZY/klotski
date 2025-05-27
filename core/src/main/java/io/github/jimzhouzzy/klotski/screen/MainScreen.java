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
 * MainScreen.java
 *
 * This class represents the main menu screen of the Klotski game.
 *
 * @author JimZhouZZY
 * @version 1.41
 * @since 2025-5-25
 *
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement blocked pieces
 * 2025-05-27: modify font
 * 2025-05-27: fix white line
 * 2025-05-27: make GameScreen seperate
 * 2025-05-26: Update changelog
 * 2025-05-26: add comment
 * 2025-05-26: Copyright Header
 * 2025-05-26: HD-font & UX improvement
 * 2025-05-26: refactor screens & add Kltozki game
 * 2025-05-25: refactor util code to ColorHelper and RandomHelper
 * 2025-05-25: remove deprecated loadColors method
 * 2025-05-25: Refactor all the change logs
 * 2025-05-25: Organize import (doc)
 * 2025-05-25: Organize import
 * 2025-05-25: generate change log
 * 2025-05-25: Update documentary
 * 2025-05-24: refactor Dialog
 * 2025-05-24: Refactor screens to enheritage ProtoScreen
 * 2025-05-23: Refactor project structure (#12)
 * 2025-05-23: Refactor project structure
 * 2025-05-22: Fix the HelpScreen's BUG! (#11)
 * 2025-05-20: Merge branch v1.0.7 into main (#8)
 * 2025-05-08: update soundfx
 * 2025-04-29: focal length animation
 * 2025-04-29: offline mode & optimize save-load
 * 2025-04-27: fix:resize dynamic board
 * 2025-04-25: Revert 'fix:resize'
 * 2025-04-24: fix resize changed base tile size
 * 2025-04-24: MSAA & Settings
 * 2025-04-24: misty & click effect
 * 2025-04-23: better main screen
 * 2025-04-23: better main menu
 * 2025-04-22: soundtrace.ogg
 * 2025-04-22: better color blocks in main screen
 * 2025-04-22: better dark mode
 * 2025-04-22: Settings view
 * 2025-04-22: better main screen dynamic background
 * 2025-04-21: background
 * 2025-04-21: resizable
 * 2025-04-16: Login & Game Mode & Save-Load
 */

package io.github.jimzhouzzy.klotski.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.jimzhouzzy.klotski.Klotski;
import io.github.jimzhouzzy.klotski.screen.core.ProtoScreen;
import io.github.jimzhouzzy.klotski.screen.menu.GameModeMenuScreen;

public class MainScreen extends ProtoScreen {
    private float baseTileSize;
    private Label greetingLabel; // Attribute for greeting label
    private ShapeRenderer shapeRenderer;
    private float offsetY;
    private Color currentColor;
    private float colorChangeSpeed = 0.001f; // Speed of color change
    public Color[] colorList; // Predefined list of colors
    public Map<String, Double> zPositionCache;
    public Map<String, Double> zPositionTempCache;
    public Map<String, Double> yRotationCache;
    public Map<String, Boolean> triggerYRotation; // Flag to trigger Y rotation
    private boolean triggerYRotationAnimation;
    private int yRotationAnimationStartingRow;
    private float yRotationAnimationStartingOffsetY;
    private List<Color> targetColors; // List of target colors
    private int currentColorIndex = 0; // Index of the current base color
    private float interpolationFactor = 0f;
    private float interpolationSpeedMultiplier = 1f; // Speed of color interpolation

    public MainScreen(final Klotski klotski) {
        super(klotski);

        yRotationCache = new HashMap<>();
        zPositionCache = new HashMap<>();
        zPositionTempCache = new HashMap<>();
        triggerYRotation = new HashMap<>();

        triggerYRotationAnimation = false;
        baseTileSize = 50f;
        offsetY = 0f;

        create();
    }

    /**
     * Creates and initializes the main menu screen layout for the Klotski game. This method constructs
     * a table-based UI containing a title label, a dynamic greeting label, and multiple navigation buttons.
     * The title is styled with an enlarged font and white color. The greeting text is retrieved via
     * {@code getGreetingText()} and displayed using narration styling. Buttons for "Play," "Login,"
     * "Settings," "Help," and "Exit" are added with uniform dimensions (200x50) and padding. Each button
     * triggers a click sound and navigates to a corresponding screen: Play opens the {@code GameModeMenuScreen},
     * Login opens the {@code LoginScreen}, Settings opens the {@code SettingsScreen}, Help opens the
     * {@code AboutScreen}, and Exit terminates the application. All UI elements use styles from the
     * provided {@code skin} resource.
     */
    public void create() {
            // Create a table for layout
            Table table = new Table();
            table.setFillParent(true);
            stage.addActor(table);
    
            // Add a title label
            Label.LabelStyle titleStyle = skin.get("title", Label.LabelStyle.class);
            Label titleLabel = new Label("Klotski Game", titleStyle);
            titleLabel.setFontScale(1.5f); // Make the title larger
            titleLabel.setColor(Color.WHITE); // Set the title color to white
            table.add(titleLabel).padBottom(50).row();
    
            // Add a greeting label
            Label.LabelStyle narrationStyle = skin.get("narration", Label.LabelStyle.class);
            greetingLabel = new Label(getGreetingText(), narrationStyle);
            greetingLabel.setFontScale(1f);
            greetingLabel.setColor(Color.WHITE);
            table.add(greetingLabel).padBottom(30).row();
    
            // Add a "Play" button
            TextButton playButton = new TextButton("Play", skin);
            playButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();;
                    klotski.setScreen(new GameModeMenuScreen(klotski, klotski.mainScreen)); // Navigate to the GameModeScreen
                }
            });
            table.add(playButton).width(200).height(50).padBottom(20).row();
    
            // Add a "Login" button
            TextButton loginButton = new TextButton("Login", skin);
            loginButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();;
                    klotski.setScreen(klotski.loginScreen); // Navigate to the LoginScreen
                }
            });
            table.add(loginButton).width(200).height(50).padBottom(20).row();
    
            // Add a "Settings" button
            TextButton settingsButton = new TextButton("Settings", skin);
            settingsButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();;
                    klotski.setScreen(klotski.settingsScreen); // Navigate to the SettingsScreen
                }
            });
            table.add(settingsButton).width(200).height(50).padBottom(20).row();
            //Add a "Help" button
            TextButton helpButton = new TextButton("Help", skin);
            helpButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();
                    klotski.setScreen(new AboutScreen(klotski));
                }
            });
            table.add(helpButton).width(200).height(50).padBottom(20).row();
    
            // Add an "Exit" button
            TextButton exitButton = new TextButton("Exit", skin);
            exitButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    klotski.playClickSound();;
                    Gdx.app.exit(); // Exit the application
                }
            });
            table.add(exitButton).width(200).height(50);
        }

    // Method to get greeting text
    /**
     * Generates a greeting string that welcomes the currently logged-in user or a guest.
     * This method checks the logged-in user via the {@code klotski} instance. If a user is
     * currently logged in (i.e., the username is not {@code null}), the returned string
     * will be "Welcome, [username]!", where [username] is the name of the logged-in user.
     * If no user is logged in, the greeting defaults to "Welcome, Guest!".
     *
     * @return A greeting message personalized with the logged-in user's username if available;
     * otherwise, returns a generic guest greeting.
     */
    private String getGreetingText() {
            String username = klotski.getLoggedInUser();
            return username != null ? "Welcome, " + username + "!" : "Welcome, Guest!";
        }

    // Method to update greeting label with current username
    /**
     * Updates the text of the greeting label to the current greeting text.
     * This method checks if the greeting label is initialized (non-null) and, if so,
     * sets its text content by invoking {@link #getGreetingText()}. If the label
     * is not initialized, the method performs no action.
     */
    public void updateGreetingLabel() {
            if (greetingLabel != null) {
                greetingLabel.setText(getGreetingText());
            }
        }

    /**
     * Generates a new color similar to the provided base color, with controlled randomization and theme-based adjustments.
     * The new color is created by applying random offsets to the RGB components of the base color, scaled by the specified variability.
     * The green component is intentionally reduced by10% to shift the color spectrum. The alpha value of the base color is preserved.
     *
     * <p>The method ensures RGB values remain within the valid range [0,1] by clamping values after applying offsets. An additional
     * offset parameter shifts the clamped values further, allowing fine-tuned brightness adjustments.
     *
     * <p>If the generated color's luminance does not meet the theme-specific threshold (e.g., minimum0.3 luminance for light themes,
     * maximum0.8 luminance for dark themes), the method recursively adjusts the variability and offset parameters to produce
     * a compliant color. Recursion terminates when variability falls below1% of the provided limit, returning the original base color.
     *
     * @param baseColor The starting color from which the new color is derived.
     * @param variability Controls the range of random offsets applied to RGB values (higher values increase variation).
     * @param offset A fixed value added to each RGB component after clamping, influencing overall brightness.
     * @param limit Threshold for terminating recursion, expressed as a multiplier for variability reduction checks.
     * @return A new color similar to the base color, adjusted for theme compliance and variability constraints.
     */
    public Color generateSimilarColor(Color baseColor, float variability, float offset, float limit) {
            // Generate small random offsets for RGB values
            float redOffset = (klotski.randomHelper.nextFloat() - 0.5f) * variability;
            float greenOffset = (klotski.randomHelper.nextFloat() - 0.5f) * variability;
            float blueOffset = (klotski.randomHelper.nextFloat() - 0.5f) * variability;
    
            // Clamp the values to ensure they remain between 0 and 1
            float newRed = Math.min(Math.max(baseColor.r + redOffset, 0) + offset, 1);
            float newGreen = 0.9f * Math.min(Math.max(baseColor.g + greenOffset, 0) + offset, 1); // Eliminate green
            float newBlue = Math.min(Math.max(baseColor.b + blueOffset, 0) + offset, 1);
    
            // Create the new color
            Color newColor = new Color(newRed, newGreen, newBlue, baseColor.a); // Preserve the alpha value
    
            // Adjust luminance if necessary
            float luminance = calculateLuminance(newColor);
            if (klotski.klotskiTheme == klotski.klotskiTheme.LIGHT && luminance < 0.3f) {
                if (variability > 0.01f * limit) {
                    return generateSimilarColor(baseColor, 0.5f * variability, 0.2f, 1.0f);
                } else {
                    return baseColor;
                }
            } else if (klotski.klotskiTheme != klotski.klotskiTheme.LIGHT && luminance > 0.8f) {
                if (variability > 0.01f * limit) {
                    return generateSimilarColor(baseColor, 0.5f * variability, -0.2f, 1.0f);
                } else {
                    return baseColor;
                }
            }
    
            return newColor;
        }

    /**
     * Generates a smoothly transitioning color by interpolating between predefined target colors.
     * The method initializes a list of target colors if not already populated and cycles through them,
     * blending from the current color to the next using a smoothstep interpolation for eased transitions.
     * The interpolation speed is randomized each time a new target color is selected, within defined limits.
     * Colors transition periodically based on the interpolation factor, which is updated using a combination
     * of fixed and randomized speed multipliers. The resulting color is computed as a linear interpolation
     * of RGB channels between the current and next target color, adjusted by the smoothed interpolation factor.
     *
     * @param delta The time delta used for frame-based interpolation calculations (unused in current implementation).
     * @return The interpolated {@link Color} representing the current smooth transition state.
     */
    public Color generateSmoothChangingColor(float delta) {
            if (targetColors == null) {
                targetColors = new ArrayList<>();
                targetColors.add(new Color(204 / 255f, 204 / 255f, 255 / 255f, 1)); // rgb(204, 204, 255)
                targetColors.add(new Color(255 / 255f, 204 / 255f, 153 / 255f, 1)); // rgb(255, 204, 153)
                targetColors.add(new Color(255 / 255f, 153 / 255f, 255 / 255f, 1)); // rgb(255, 153, 255)
                targetColors.add(new Color(153 / 255f, 255 / 255f, 204 / 255f, 1)); // rgb(153, 255, 204)
                targetColors.add(new Color(51 / 255f, 153 / 255f, 255 / 255f, 1)); // rgb(51, 153, 255)
            }
    
            Color currentBaseColor = targetColors.get(currentColorIndex);
            Color nextBaseColor = targetColors.get((currentColorIndex + 1) % targetColors.size());
    
            interpolationFactor += 0.3 * colorChangeSpeed * interpolationSpeedMultiplier;
            if (interpolationFactor > 1f) {
                interpolationFactor = 0f;
                currentColorIndex = (currentColorIndex + 1) % targetColors.size();
                currentBaseColor = nextBaseColor;
                nextBaseColor = targetColors.get(klotski.randomHelper.nextInt(targetColors.size()));
                if (currentBaseColor == nextBaseColor) {
                    nextBaseColor = targetColors.get((currentColorIndex + 1) % targetColors.size());
                }
                // Randomize the interpolation speed
                interpolationSpeedMultiplier = klotski.randomHelper.nextFloat() * 0.5f + 0.5f; // Randomize the speed
                if (interpolationSpeedMultiplier > 1.5f) {
                    interpolationSpeedMultiplier = 1.5f; // Limit the maximum speed
                }
                if (interpolationSpeedMultiplier < 0.8f) {
                    interpolationSpeedMultiplier = 0.8f; // Limit the minimum speed
                }
            }
            float t = interpolationFactor;
            t = t * t * (3 - 2 * t); // smoothstep(t)
    
            float red   = currentBaseColor.r + t * (nextBaseColor.r - currentBaseColor.r);
            float green = currentBaseColor.g + t * (nextBaseColor.g - currentBaseColor.g);
            float blue  = currentBaseColor.b + t * (nextBaseColor.b - currentBaseColor.b);
    
            currentColor = new Color(red, green, blue, 1.0f);
            return currentColor;
        }

    /**
     * Calculates the relative luminance of a given color using the standard formula for sRGB color space.
     * Relative luminance represents the brightness of a color normalized to a reference range, with0.0
     * representing the darkest black and1.0 representing the brightest white. This method applies the
     * weighted sum of the color's RGB components based on their perceived contribution to brightness:
     * the red component is weighted by0.2126, green by0.7152, and blue by0.0722. The result is commonly
     * used to evaluate contrast ratios for accessibility standards.
     *
     * @param color The {@link Color} object from which to compute luminance. Must not be null.
     * @return A float value between0.0 (inclusive) and1.0 (inclusive) representing the relative luminance
     * of the input color. Higher values indicate greater perceived brightness.
     */
    private float calculateLuminance(Color color) {
            // Use the standard formula for relative luminance
            return 0.2126f * color.r + 0.7152f * color.g + 0.0722f * color.b;
        }

    /**
     * Draws a3D-style top rectangle with dynamic border effects based on perspective and rotation. The method
     * first renders a filled quadrilateral using two triangles formed by the provided corner points (tl, tr, bl, br).
     * It then adds a highlighted border along the top-left edges and a shadow border along the bottom-right edges,
     * with line widths modulated by the rotation angle and vertical position to simulate depth. The highlight is
     * a lighter variant of the base color, while the shadow is darker. The line thickness of the borders dynamically
     * adjusts based on the screen-relative vertical position (y) and rotation angle (yRotateAngle) to enhance
     * the3D illusion.
     *
     * @param tl The top-left corner position of the rectangle.
     * @param tr The top-right corner position of the rectangle.
     * @param bl The bottom-left corner position of the rectangle.
     * @param br The bottom-right corner position of the rectangle.
     * @param y The vertical position of the rectangle, used to calculate perspective scaling for line width.
     * @param tileColor The base color of the rectangle; highlights and shadows are derived from this color.
     * @param yRotateAngle The rotation angle around the Y-axis (in radians) affecting line width modulation for borders.
     */
    public void drawTopRectangle(Vector2 tl, Vector2 tr, Vector2 bl, Vector2 br, float y, Color tileColor, float yRotateAngle) {
            float screenHeight = Gdx.graphics.getHeight();
    
            // Draw the tile
            Gdx.gl.glLineWidth(1f); // Set line width
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(tileColor);
            shapeRenderer.triangle(tl.x, tl.y, tr.x, tr.y, br.x, br.y);
            shapeRenderer.triangle(br.x, br.y, bl.x, bl.y, tl.x, tl.y);
            shapeRenderer.end();
    
            // Draw the top-left highlight border
    
            Gdx.gl.glLineWidth(Math.max(1f, 6f * (float) Math.abs(Math.cos(yRotateAngle) * (1 - y / screenHeight)))); // Set line width
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(tileColor.cpy().mul(1.2f)); // Lighter color for highlight
            shapeRenderer.line(tl.x, tl.y, tr.x, tr.y); // Top edge
            shapeRenderer.line(tl.x, tl.y, bl.x, bl.y); // Left edge
            shapeRenderer.end();
    
            // Draw the bottom-right shadow border
            Gdx.gl.glLineWidth(Math.max(1f, 12f * (float) Math.abs(Math.cos(yRotateAngle) * (1 - y / screenHeight)))); // Set line width
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(tileColor.cpy().mul(0.6f)); // Darker color for shadow
            shapeRenderer.line(bl.x, bl.y, br.x, br.y); // Bottom edge
            shapeRenderer.line(tr.x, tr.y, br.x, br.y); // Right edge
            shapeRenderer.end();
    
            Gdx.gl.glLineWidth(1f); // Set back line width
        }

    /**
     * Triggers the flip animation by initializing the starting parameters for the y-axis rotation animation.
     * This method sets the initial offset and row values based on the current vertical offset (offsetY) and the base tile size.
     * It enables the rotation animation by setting the trigger flag (triggerYRotationAnimation) to true and logs the starting row
     * of the animation for debugging purposes. If the animation trigger condition is met, the starting values are updated,
     * and the animation sequence is activated.
     */
    public void triggerFlip() {
            if (!triggerYRotationAnimation || true) {
                yRotationAnimationStartingOffsetY = offsetY;
                yRotationAnimationStartingRow = (int) Math.floor(yRotationAnimationStartingOffsetY / baseTileSize);
                triggerYRotationAnimation = true;
                System.out.println("Triggering flip animation, with starting row: " + yRotationAnimationStartingRow);
            }
        }

    /**
     * Triggers a y-axis rotation (flip) animation starting from the specified row. This method
     * initializes the animation's starting vertical offset based on the given row and the base tile size,
     * sets the starting row for the animation, enables the animation trigger flag, and logs the
     * starting row of the animation. If the animation trigger condition is met, the flip sequence
     * is prepared and activated.
     *
     * @param row The row index from which the y-axis rotation animation will start.
     */
    public void triggerFlip(int row) {
            if (!triggerYRotationAnimation || true) {
                yRotationAnimationStartingOffsetY = row * baseTileSize;
                yRotationAnimationStartingRow = row;
                triggerYRotationAnimation = true;
                System.out.println("Triggering flip animation, with starting row: " + yRotationAnimationStartingRow);
            }
        }

    /**
     * Updates and displays the current state of the UI components, configures input handling, and links the game board to the UI stage.
     * This method ensures the greeting label reflects the latest username, sets the input processor to the stage to handle user interactions
     * (e.g., button clicks, touch events), and initializes the game board's connection to the stage for dynamic UI updates.
     * Overrides the parent implementation to provide custom setup logic required for the Klotski game's UI and input management.
     */
    @Override
        public void show() {
            updateGreetingLabel(); // in case the username changed
            Gdx.input.setInputProcessor(stage);
            klotski.dynamicBoard.setStage(stage);
        }
}
